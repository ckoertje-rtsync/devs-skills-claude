#!/usr/bin/env bash
# Validate MS4 Me .dnl files with the ms4me CLI from the devs-vscode-extensions repo,
# optionally generate the Java and compile it against the Closure-DEVS jar.
#
# Usage: check-dnl.sh [--generate DIR] [--compile] <file.dnl> ...
#
#   (no flags)       ms4me check FILE...   -> exit 0 iff no errors
#   --generate DIR   also ms4me generate each file into DIR (Model.java + one .java per declared class)
#   --compile        with --generate: javac the generated files against the Closure-DEVS jar
#
# Environment:
#   MS4ME_EXT_DIR     checkout of devs-vscode-extensions (default: ~/DEVS/devs-vscode-extensions)
#   CLOSURE_DEVS_JAR  jar for --compile (default: ~/.m2/repository/com/ms4systems/Closure-DEVS/1.0.0/Closure-DEVS-1.0.0.jar)
set -u

EXT_DIR="${MS4ME_EXT_DIR:-$HOME/DEVS/devs-vscode-extensions}"
CLI="$EXT_DIR/packages/cli/bin/cli.js"
JAR="${CLOSURE_DEVS_JAR:-$HOME/.m2/repository/com/ms4systems/Closure-DEVS/1.0.0/Closure-DEVS-1.0.0.jar}"

gen_dir=""
compile=0
files=()
while [[ $# -gt 0 ]]; do
    case "$1" in
        --generate) gen_dir="$2"; shift 2 ;;
        --compile)  compile=1; shift ;;
        -h|--help)  sed -n '2,15p' "$0"; exit 0 ;;
        *)          files+=("$1"); shift ;;
    esac
done

if [[ ${#files[@]} -eq 0 ]]; then
    echo "usage: $(basename "$0") [--generate DIR] [--compile] <file.dnl> ..." >&2
    exit 2
fi
if [[ ! -f "$CLI" ]]; then
    echo "error: ms4me CLI not found at $CLI; set MS4ME_EXT_DIR to the devs-vscode-extensions checkout" >&2
    exit 2
fi
if [[ ! -f "$EXT_DIR/packages/cli/out/main.js" || ! -d "$EXT_DIR/packages/language/out" ]]; then
    echo "error: the extension is not built; run 'npm install && npm run build' in $EXT_DIR" >&2
    exit 2
fi
for f in "${files[@]}"; do
    [[ -f "$f" ]] || { echo "error: $f is not a readable file" >&2; exit 2; }
done

node "$CLI" check "${files[@]}"
status=$?

if [[ -n "$gen_dir" && $status -ne 0 ]]; then
    echo "skipping --generate: fix the errors above first" >&2
elif [[ -n "$gen_dir" ]]; then
    mkdir -p "$gen_dir"
    for f in "${files[@]}"; do
        node "$CLI" generate "$f" --out "$gen_dir" || status=1
    done
    if [[ $compile -eq 1 ]]; then
        if [[ ! -f "$JAR" ]]; then
            echo "error: Closure-DEVS jar not found at $JAR; set CLOSURE_DEVS_JAR" >&2
            exit 2
        fi
        command -v javac >/dev/null || { echo "error: javac not on PATH" >&2; exit 2; }
        javac -cp "$JAR" -d "$gen_dir/classes" "$gen_dir"/*.java && echo "javac: ok" || status=1
    fi
fi
exit $status
