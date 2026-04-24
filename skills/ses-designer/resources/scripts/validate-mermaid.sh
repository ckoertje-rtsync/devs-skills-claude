#!/usr/bin/env bash
# Extract every ```mermaid ... ``` fenced block from each given Markdown file
# and validate it by feeding it through mmdc (@mermaid-js/mermaid-cli).
#
# Usage: validate-mermaid.sh <file.md> [<file.md> ...]
# Exit 0 iff every block in every file rendered successfully.
#
# Used by the closure-devs-atomic skill's VALIDATE stage. Safe to call
# standalone on any Markdown file that embeds Mermaid diagrams.

set -u

if [[ $# -lt 1 ]]; then
    echo "usage: $(basename "$0") <file.md> [<file.md> ...]" >&2
    exit 2
fi

tmpdir="$(mktemp -d)"
trap 'rm -rf "$tmpdir"' EXIT

overall_rc=0
total_blocks=0
failed_blocks=0

for md in "$@"; do
    if [[ ! -f "$md" ]]; then
        echo "skip  $md (not a file)" >&2
        continue
    fi

    # Split into numbered mermaid blocks.
    awk '
        /^```mermaid[[:space:]]*$/ { in_block=1; block_idx++; fname=sprintf("%s/block-%02d-%s.mmd", tmpdir, block_idx, base); next }
        /^```[[:space:]]*$/ && in_block { in_block=0; next }
        in_block { print > fname }
    ' tmpdir="$tmpdir" base="$(basename "$md" .md)" "$md"

    # Iterate over blocks produced for this file.
    shopt -s nullglob
    blocks=("$tmpdir"/block-*-"$(basename "$md" .md)".mmd)
    shopt -u nullglob

    if [[ ${#blocks[@]} -eq 0 ]]; then
        echo "ok    $md (no mermaid blocks)"
        continue
    fi

    for block in "${blocks[@]}"; do
        total_blocks=$((total_blocks + 1))
        out="${block%.mmd}.svg"
        log="${block%.mmd}.log"
        if npx --yes --package=@mermaid-js/mermaid-cli@latest mmdc \
              -i "$block" -o "$out" >"$log" 2>&1; then
            echo "ok    $md :: $(basename "$block")"
        else
            failed_blocks=$((failed_blocks + 1))
            overall_rc=1
            echo "FAIL  $md :: $(basename "$block")"
            echo "----- block content -----"
            sed 's/^/    /' "$block"
            echo "----- mmdc output -----"
            sed 's/^/    /' "$log"
            echo "-------------------------"
        fi
    done
done

echo ""
echo "mermaid validation: $total_blocks blocks, $failed_blocks failed"
exit "$overall_rc"
