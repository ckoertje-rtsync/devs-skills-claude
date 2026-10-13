#!/usr/bin/env bash
# Run the VS Code extension's ms4me checker on .ses and .pes files (and any .dnl beside them).
# Unlike validate-ses.sh / validate-pes.sh, this is the real parser and it checks across files:
# a PES against the SES it prunes (sibling ses/ folder, same folder, or the workspace) and,
# inside an MS4 Me project, an SES against the .dnl ports and model files.
#
# Usage: check-ms4me.sh [--generate DIR] <file-or-dir> ...
#
#   (no flags)       ms4me check PATH...   -> exit 0 iff no errors
#   --generate DIR   after a clean check, write the coupled Java of every .pes among the paths into DIR
#
# Environment:
#   MS4ME_EXT_DIR    checkout of devs-vscode-extensions (default: ~/DEVS/devs-vscode-extensions)
set -u

EXT_DIR="${MS4ME_EXT_DIR:-$HOME/DEVS/devs-vscode-extensions}"
CLI="$EXT_DIR/packages/cli/bin/cli.js"

gen_dir=""
paths=()
while [[ $# -gt 0 ]]; do
    case "$1" in
        --generate) gen_dir="$2"; shift 2 ;;
        -h|--help)  sed -n '2,14p' "$0"; exit 0 ;;
        *)          paths+=("$1"); shift ;;
    esac
done

if [[ ${#paths[@]} -eq 0 ]]; then
    echo "usage: $(basename "$0") [--generate DIR] <file-or-dir> ..." >&2
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
for p in "${paths[@]}"; do
    [[ -e "$p" ]] || { echo "error: $p does not exist" >&2; exit 2; }
done

node "$CLI" check "${paths[@]}"
status=$?

if [[ -n "$gen_dir" && $status -ne 0 ]]; then
    echo "skipping --generate: fix the errors above first" >&2
elif [[ -n "$gen_dir" ]]; then
    mkdir -p "$gen_dir"
    pes_files=()
    for p in "${paths[@]}"; do
        if [[ -d "$p" ]]; then
            while IFS= read -r f; do pes_files+=("$f"); done < <(find "$p" -name '*.pes' -not -path '*/node_modules/*' -not -path '*/bin/*' | sort)
        elif [[ "$p" == *.pes ]]; then
            pes_files+=("$p")
        fi
    done
    if [[ ${#pes_files[@]} -eq 0 ]]; then
        echo "no .pes file among the paths; nothing to generate" >&2
    fi
    for f in "${pes_files[@]}"; do
        node "$CLI" generate "$f" --out "$gen_dir" || status=1
    done
fi
exit $status
