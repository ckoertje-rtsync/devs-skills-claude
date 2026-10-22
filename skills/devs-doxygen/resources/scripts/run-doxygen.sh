#!/usr/bin/env bash
# Run Doxygen on a generated Doxyfile and check the result.
#
# Usage: run-doxygen.sh <Doxyfile> [--expect-page <page-id>]...
#
# Checks (exit 0 iff all pass):
#   1. doxygen exits 0
#   2. <OUTPUT_DIRECTORY>/<HTML_OUTPUT>/index.html exists
#   3. every --expect-page <id> rendered as <id>.html (\page ids; \mainpage is index.html)
#   4. every \dot block in the .dox files next to the Doxyfile produced a
#      dot_inline_dotgraph_N image (only checked when HAVE_DOT = YES)
#   5. no warning in the warning log refers to a .dox file or to dot / Graphviz
# Warnings about the user's own source files are listed but do not fail the run.
#
# Used by the devs-doxygen skill's VALIDATE stage. Safe to call standalone.
set -u

usage() {
    echo "usage: $(basename "$0") <Doxyfile> [--expect-page <id>]..." >&2
    exit 2
}

[[ $# -ge 1 ]] || usage
doxyfile="$1"; shift
[[ -f "$doxyfile" ]] || { echo "error: $doxyfile not found" >&2; exit 2; }

expect_pages=()
while [[ $# -gt 0 ]]; do
    case "$1" in
        --expect-page) [[ $# -ge 2 ]] || usage; expect_pages+=("$2"); shift 2 ;;
        *) usage ;;
    esac
done

command -v doxygen >/dev/null 2>&1 || { echo "error: doxygen not on PATH" >&2; exit 2; }

dir="$(cd "$(dirname "$doxyfile")" && pwd)"
cfg="$(basename "$doxyfile")"
cd "$dir" || exit 2

# getcfg KEY DEFAULT — last assignment of KEY in the Doxyfile, quotes stripped.
getcfg() {
    local v
    v="$(grep -E "^[[:space:]]*$1[[:space:]]*=" "$cfg" | tail -n1 \
        | sed -E 's/^[^=]*=[[:space:]]*//; s/[[:space:]]+$//; s/^"//; s/"$//')"
    echo "${v:-$2}"
}

outdir="$(getcfg OUTPUT_DIRECTORY .)"
htmldir="$(getcfg HTML_OUTPUT html)"
warnlog="$(getcfg WARN_LOGFILE "")"
have_dot="$(getcfg HAVE_DOT NO)"
html="$outdir/$htmldir"

rc=0

# 1. run doxygen
if doxygen "$cfg" > doxygen-run.log 2>&1; then
    echo "ok    doxygen exit 0"
else
    echo "FAIL  doxygen exit $? (see $dir/doxygen-run.log)"
    rc=1
fi

# 2. index.html
if [[ -f "$html/index.html" ]]; then
    echo "ok    $html/index.html"
else
    echo "FAIL  $html/index.html missing"
    rc=1
fi

# 3. expected pages
for id in "${expect_pages[@]:-}"; do
    [[ -n "$id" ]] || continue
    if [[ -f "$html/$id.html" ]]; then
        echo "ok    page $id -> $html/$id.html"
    else
        echo "FAIL  page $id not rendered (expected $html/$id.html)"
        rc=1
    fi
done

# 4. inline dot graphs
shopt -s nullglob
dox_files=(*.dox)
shopt -u nullglob
dot_expected=0
if [[ ${#dox_files[@]} -gt 0 ]]; then
    dot_expected="$(grep -o -E '[\\@]dot\b' "${dox_files[@]}" | wc -l | tr -d ' ')"
fi
if [[ "${have_dot^^}" == "YES" ]]; then
    dot_rendered="$(ls "$html"/dot_inline_dotgraph_* 2>/dev/null | grep -c -E '\.(svg|png)$' || true)"
    if [[ "$dot_rendered" -ge "$dot_expected" ]]; then
        echo "ok    dot graphs: $dot_rendered rendered, $dot_expected expected"
    else
        echo "FAIL  dot graphs: $dot_rendered rendered, $dot_expected expected"
        rc=1
    fi
else
    echo "skip  dot graphs: HAVE_DOT is not YES ($dot_expected \\dot blocks will show as text)"
fi

# 4b. inline msc (message sequence) charts — mscgen is built into Doxygen
msc_expected=0
if [[ ${#dox_files[@]} -gt 0 ]]; then
    msc_expected="$(grep -o -E '[\\@]msc\b' "${dox_files[@]}" | wc -l | tr -d ' ')"
fi
if [[ "$msc_expected" -gt 0 ]]; then
    msc_rendered="$(ls "$html"/msc_inline_mscgraph_* 2>/dev/null | grep -c -E '\.(svg|png)$' || true)"
    if [[ "$msc_rendered" -ge "$msc_expected" ]]; then
        echo "ok    msc charts: $msc_rendered rendered, $msc_expected expected"
    else
        echo "FAIL  msc charts: $msc_rendered rendered, $msc_expected expected"
        rc=1
    fi
fi

# 5. warnings
if [[ -n "$warnlog" && -f "$warnlog" ]]; then
    warnings="$(cat "$warnlog")"
else
    warnings="$(grep -i -E 'warning:|error:' doxygen-run.log || true)"
fi
dox_warn="$(printf '%s\n' "$warnings" | grep -E '\.dox:[0-9]+:' | sort -u || true)"
dot_warn="$(printf '%s\n' "$warnings" | grep -v -E '\.dox:[0-9]+:' | grep -i -E '\bdot\b|graphviz' | sort -u || true)"
other_warn="$(printf '%s\n' "$warnings" | grep -v -E '\.dox:[0-9]+:' | grep -v -i -E '\bdot\b|graphviz' | grep -v '^[[:space:]]*$' | sort -u || true)"

if [[ -n "$dox_warn" ]]; then
    echo "FAIL  warnings in generated .dox files:"
    printf '%s\n' "$dox_warn" | sed 's/^/    /'
    rc=1
else
    echo "ok    no warnings in .dox files"
fi
if [[ -n "$dot_warn" ]]; then
    echo "FAIL  dot / Graphviz warnings:"
    printf '%s\n' "$dot_warn" | sed 's/^/    /'
    rc=1
fi
if [[ -n "$other_warn" ]]; then
    n="$(printf '%s\n' "$other_warn" | wc -l | tr -d ' ')"
    echo "note  $n warning(s) about source files (informational):"
    printf '%s\n' "$other_warn" | head -n 10 | sed 's/^/    /'
    [[ "$n" -gt 10 ]] && echo "    ... ($((n - 10)) more in ${warnlog:-doxygen-run.log})"
fi

echo ""
if [[ $rc -eq 0 ]]; then
    echo "doxygen validation: PASS  (open $dir/$html/index.html)"
else
    echo "doxygen validation: FAIL"
fi
exit "$rc"
