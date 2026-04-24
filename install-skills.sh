#!/usr/bin/env bash
# Install devs-skills-claude skills as symlinks (project-local or global)
set -euo pipefail

usage() {
    echo "Usage: $(basename "$0") [--global|-g]"
    echo ""
    echo "  (default)    Install to ./.claude/  (project-local, for testing)"
    echo "  --global -g  Install to ~/.claude/  (available in all projects)"
    exit "${1:-0}"
}

GLOBAL=0
for arg in "$@"; do
    case "$arg" in
        --global|-g) GLOBAL=1 ;;
        --help|-h)   usage 0 ;;
        *)           echo "Unknown option: $arg"; usage 1 ;;
    esac
done

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SKILLS_SRC="$SCRIPT_DIR/skills"

if [[ "$GLOBAL" == "1" ]]; then
    SKILLS_DST="$HOME/.claude/skills"
    echo "Installing globally to ~/.claude/"
else
    SKILLS_DST="$SCRIPT_DIR/.claude/skills"
    echo "Installing locally to ./.claude/"
fi

mkdir -p "$SKILLS_DST"

installed=0
skipped=0

for skill_dir in "$SKILLS_SRC"/*/; do
    [ -d "$skill_dir" ] || continue
    skill_name="$(basename "$skill_dir")"
    target="$SKILLS_DST/$skill_name"

    # --- symlink skill ---
    if [ -L "$target" ]; then
        existing="$(readlink "$target")"
        if [ "$existing" = "$skill_dir" ] || [ "$existing" = "${skill_dir%/}" ]; then
            echo "  skip  $skill_name (already installed)"
            skipped=$((skipped + 1))
        else
            echo "  update $skill_name (relink from $existing)"
            rm "$target"
            ln -s "$skill_dir" "$target"
            installed=$((installed + 1))
        fi
    elif [ -e "$target" ]; then
        echo "  WARN  $skill_name: $target exists and is not a symlink, skipping"
        skipped=$((skipped + 1))
    else
        ln -s "$skill_dir" "$target"
        echo "  link  $skill_name -> $skill_dir"
        installed=$((installed + 1))
    fi
done

echo ""
if [[ "$GLOBAL" == "1" ]]; then
    echo "Done: $installed installed, $skipped skipped (global: ~/.claude/)"
else
    echo "Done: $installed installed, $skipped skipped (local: ./.claude/)"
fi
