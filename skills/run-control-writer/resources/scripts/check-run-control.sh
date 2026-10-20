#!/usr/bin/env bash
# Structural checks for a run control (a SimulateModel subclass), optionally cross-checked
# against the project's SES/PES files and compiled.
#
# Usage: check-run-control.sh [--flavor ms4me|closure] [--project DIR] [--compile] <RunX.java>
#
#   (no flags)       structural + flavor checks           -> exit 0 iff no errors
#   --flavor F       API the file targets; default: guessed from the cast in defineModel()
#   --project DIR    MS4 Me project root: the sesfile/pesfile named in the run control must exist
#                    under DIR/src/Models/ses|pes, and every .replace("...") target must occur in the PES
#   --compile        javac: closure flavor against the Closure-DEVS jar; ms4me flavor against
#                    $MS4ME_CLASSPATH (skipped with a notice when unset)
#
# Environment:
#   CLOSURE_DEVS_JAR  (default: ~/.m2/repository/com/ms4systems/Closure-DEVS/1.0.0/Closure-DEVS-1.0.0.jar)
#   MS4ME_CLASSPATH   MS4 Me plugin jars, colon-separated, for compiling the ms4me flavor
set -u

JAR="${CLOSURE_DEVS_JAR:-$HOME/.m2/repository/com/ms4systems/Closure-DEVS/1.0.0/Closure-DEVS-1.0.0.jar}"
flavor=""
project=""
compile=0
file=""
while [[ $# -gt 0 ]]; do
    case "$1" in
        --flavor)  flavor="$2"; shift 2 ;;
        --project) project="$2"; shift 2 ;;
        --compile) compile=1; shift ;;
        -h|--help) sed -n '2,16p' "$0"; exit 0 ;;
        *)         file="$1"; shift ;;
    esac
done
[[ -n "$file" && -f "$file" ]] || { echo "usage: $(basename "$0") [--flavor ms4me|closure] [--project DIR] [--compile] <RunX.java>" >&2; exit 2; }

errors=0; warnings=0
err()  { echo "error:   $*"; errors=$((errors+1)); }
warn() { echo "warning: $*"; warnings=$((warnings+1)); }
has()  { grep -qE "$1" "$file"; }

# ---- flavor ----------------------------------------------------------------------------
if [[ -z "$flavor" ]]; then
    if has 'CoupledModelImpl'; then flavor=ms4me
    elif has '\(Coupled\)' || has '\.execute\([0-9A-Za-z_]+\)'; then flavor=closure
    else flavor=ms4me; fi
    echo "flavor:  $flavor (guessed)"
else
    echo "flavor:  $flavor"
fi
[[ "$flavor" == ms4me || "$flavor" == closure ]] || { echo "error: --flavor must be ms4me or closure" >&2; exit 2; }

# ---- class shape -------------------------------------------------------------------------
has 'class +[A-Za-z_][A-Za-z0-9_]* +extends +SimulateModel' || err "class does not extend SimulateModel"
for m in defineModel setParams getAndAnalyzeResults; do
    has "public +void +$m *\(" || err "missing public void $m()"
done
has 'public +static +void +main *\( *String' || err "missing public static void main(String[] args)"
has '(^|[^A-Za-z_.])model *= *' || err "defineModel() never assigns 'model'"

if has 'appendToFile *\(' && ! has 'writeToFile *\('; then
    err "results are appended but never cleared: call fileHandler.writeToFile(sm.resultsFile, header) in main() before the first execute"
fi
if has 'writeToFile *\(' && has '\.execute(WView)? *\('; then
    first_clear=$(grep -nE 'writeToFile *\(' "$file" | head -1 | cut -d: -f1)
    first_exec=$(grep -nE '\.execute(WView)? *\(' "$file" | head -1 | cut -d: -f1)
    [[ "$first_clear" -lt "$first_exec" ]] || err "writeToFile (clear) appears after the first execute (line $first_exec)"
fi
has 'replaceAll *\( *"[^"]*\[' && warn "replaceAll with '[' in the pattern: replaceAll is a regex; use replace (literal) for PES edits"
has 'setResultsFile *\( *"[^"]*/' && warn "setResultsFile already prefixes src/Models/txt/; pass a bare file name"

# ---- flavor consistency ------------------------------------------------------------------
if [[ "$flavor" == closure ]]; then
    has 'CoupledModelImpl' && err "closure flavor: CoupledModelImpl is not in the Closure-DEVS jar; cast to Coupled"
    has '\.execute *\( *\)' && err "closure flavor: execute() without arguments does not exist in the jar; use execute(numIterations)"
    has 'executeWView' && warn "closure flavor: executeWView() does not exist in the jar (no SimViewer)"
    has 'setMaxSimulationTime' && warn "closure flavor: setMaxSimulationTime has no effect under execute(int); the iteration count stops the run"
else
    has '\.execute *\( *[0-9A-Za-z_]+ *\)' && warn "ms4me flavor: execute(n) is the Closure-DEVS signature; MS4 Me uses execute() / executeWView()"
    has 'import +com\.ms4systems\.devs\.core\.model\.impl\.Coupled;' && ! has 'CoupledModelImpl' \
        && warn "ms4me flavor: cast is Coupled; the documentation uses CoupledModelImpl (fall back to Coupled only if that cast fails)"
fi

# ---- project cross-check -----------------------------------------------------------------
if [[ -n "$project" ]]; then
    [[ -d "$project" ]] || { echo "error: --project $project is not a directory" >&2; exit 2; }
    ses=$(grep -oE 'sesfile *= *"[^"]+"' "$file" | head -1 | sed -E 's/.*"([^"]+)"/\1/')
    pes=$(grep -oE 'pesfile *= *"[^"]+"' "$file" | head -1 | sed -E 's/.*"([^"]+)"/\1/')
    [[ -n "$ses" ]] || warn "no literal sesfile = \"...\" found; skipping SES check"
    [[ -n "$pes" ]] || warn "no literal pesfile = \"...\" found; skipping PES check"
    [[ -z "$ses" || -f "$project/src/Models/ses/$ses" ]] || err "SES not found: $project/src/Models/ses/$ses"
    pesPath="$project/src/Models/pes/$pes"
    if [[ -n "$pes" && -f "$pesPath" ]]; then
        while IFS= read -r target; do
            [[ -z "$target" ]] && continue
            grep -qF -- "$target" "$pesPath" || err "replace target not in $pes: \"$target\""
        done < <(grep -oE '\.replace *\( *"[^"]+"' "$file" | sed -E 's/.*"([^"]+)"$/\1/')
    elif [[ -n "$pes" ]]; then
        err "PES not found: $pesPath"
    fi
fi

# ---- compile -------------------------------------------------------------------------------
if [[ $compile -eq 1 ]]; then
    out=$(mktemp -d)
    if [[ "$flavor" == closure ]]; then
        if [[ -f "$JAR" ]]; then
            if javac -d "$out" -cp "$JAR" "$file"; then echo "compile: ok ($(basename "$JAR"))"; else err "javac failed"; fi
        else
            err "Closure-DEVS jar not found at $JAR; set CLOSURE_DEVS_JAR"
        fi
    else
        if [[ -n "${MS4ME_CLASSPATH:-}" ]]; then
            if javac -d "$out" -cp "$MS4ME_CLASSPATH" "$file"; then echo "compile: ok (MS4ME_CLASSPATH)"; else err "javac failed"; fi
        else
            echo "compile: skipped (ms4me flavor; set MS4ME_CLASSPATH to the MS4 Me plugin jars to compile)"
        fi
    fi
    rm -rf "$out"
fi

echo "summary: $errors error(s), $warnings warning(s)"
[[ $errors -eq 0 ]]
