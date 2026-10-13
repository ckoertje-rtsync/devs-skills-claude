#!/usr/bin/env bash
# Validate an MS4 Me PES file.
#
# Checks:
#   - First line is `set name of model as NAME!`.
#   - Second line is `pruned from NAME.ses!`.
#   - Every subsequent non-blank, non-comment line matches one of the PES
#     directives (`restructure`, `set multiplicity`, `select`, `inherit from`,
#     `mergeAll from`, `don't select`, `write class files`); see
#     reference/pes-grammar.md.
#   - Last non-blank line is `write class files!`.
#   - Every `set multiplicity of LABEL as [N] for ENTITY!` is preceded (somewhere
#     earlier) by a `restructure multiaspects using ...!` that names LABEL
#     (alone or in an `A then B` list).
#   - Every `inherit from OPT!` is preceded by a `select OPT from ... for ...!`.
#
# Usage: validate-pes.sh <file.pes>

set -u

if [[ $# -lt 1 ]]; then
    echo "usage: $(basename "$0") <file.pes>" >&2
    exit 2
fi

file="$1"
if [[ ! -f "$file" ]]; then
    echo "error: $file is not a readable file" >&2
    exit 2
fi

ID='[A-Za-z_][A-Za-z0-9_-]*'

RE_SET_NAME="^set name of model as ${ID}!$"
RE_PRUNED_FROM="^pruned from ${ID}\.ses!$"
RE_RESTRUCTURE="^restructure multiaspects using ${ID}( then ${ID})*!$"
RE_MULTIPLICITY="^set multiplicity of ${ID} as \[[0-9]+\] for ${ID}!$"
RE_SELECT="^select ${ID} from ${ID} for ${ID}!$"
RE_SELECT_OLD="^select ${ID} for ${ID}!$"
RE_INHERIT="^inherit from ${ID}!$"
RE_MERGEALL="^mergeAll from ${ID}\.ses!$"
RE_DONT_SELECT="^don't select ${ID} under ${ID}!$"
RE_WRITE="^write class files!$"

errors=()
declared_restructure_labels=()
selected_options=()

# Collect non-blank, non-comment lines with their original line numbers.
lines=()
lnums=()
line_no=0
while IFS= read -r raw || [[ -n "$raw" ]]; do
    line_no=$((line_no + 1))
    line="${raw%$'\r'}"
    trimmed="${line#"${line%%[![:space:]]*}"}"
    [[ -z "$trimmed" ]] && continue
    [[ "$trimmed" == \#* || "$trimmed" == //* ]] && continue
    # The guide prints a space before "!" (`write class files !`); accept it.
    if [[ "$line" =~ ^(.*[^[:space:]])[[:space:]]+!$ ]]; then line="${BASH_REMATCH[1]}!"; fi
    lines+=("$line")
    lnums+=("$line_no")
done < "$file"

n=${#lines[@]}
if [[ $n -lt 3 ]]; then
    errors+=("file has fewer than 3 directives (need at least set name, pruned from, write class files)")
fi

# First line must be set-name.
if [[ $n -ge 1 && ! "${lines[0]}" =~ $RE_SET_NAME ]]; then
    errors+=("L${lnums[0]}: first directive must be 'set name of model as ...!' — got: ${lines[0]}")
fi

# Second line must be pruned-from.
if [[ $n -ge 2 && ! "${lines[1]}" =~ $RE_PRUNED_FROM ]]; then
    errors+=("L${lnums[1]}: second directive must be 'pruned from NAME.ses!' — got: ${lines[1]}")
fi

# Last line must be write-class-files.
if [[ $n -ge 1 ]]; then
    last_idx=$((n - 1))
    if [[ ! "${lines[$last_idx]}" =~ $RE_WRITE ]]; then
        errors+=("L${lnums[$last_idx]}: last directive must be 'write class files!' — got: ${lines[$last_idx]}")
    fi
fi

# Middle directives.
for (( i = 0; i < n; i++ )); do
    line="${lines[$i]}"
    ln="${lnums[$i]}"
    if [[ $i -eq 0 || $i -eq 1 || $i -eq $((n - 1)) ]]; then
        # Boundary directives — already checked.
        continue
    fi

    if [[ "$line" =~ $RE_RESTRUCTURE ]]; then
        # extract labels ("using A!" or "using A then B!")
        rest="${line%!}"
        rest="${rest#restructure multiaspects using }"
        for label in ${rest// then / }; do
            declared_restructure_labels+=("$label")
        done
        continue
    fi

    if [[ "$line" =~ $RE_MULTIPLICITY ]]; then
        # extract label
        rest="${line%!}"
        after_of="${rest#set multiplicity of }"
        label="${after_of%% as *}"
        # Check that label was declared by an earlier restructure.
        found=0
        for l in "${declared_restructure_labels[@]}"; do
            if [[ "$l" == "$label" ]]; then found=1; break; fi
        done
        if [[ $found -eq 0 ]]; then
            errors+=("L${ln}: 'set multiplicity of ${label} ...!' without a prior 'restructure multiaspects using ${label}!'")
        fi
        continue
    fi

    if [[ "$line" =~ $RE_SELECT ]]; then
        rest="${line#select }"
        selected_options+=("${rest%% from *}")
        continue
    fi

    if [[ "$line" =~ $RE_SELECT_OLD ]]; then
        errors+=("L${ln}: 'select X for E!' is missing the specialization label; write 'select X from LABEL for E!' (guide pp. 122, 157)")
        continue
    fi

    if [[ "$line" =~ $RE_INHERIT ]]; then
        rest="${line%!}"
        opt="${rest#inherit from }"
        found=0
        for o in "${selected_options[@]}"; do
            if [[ "$o" == "$opt" ]]; then found=1; break; fi
        done
        if [[ $found -eq 0 ]]; then
            errors+=("L${ln}: 'inherit from ${opt}!' without a prior 'select ${opt} from ... for ...!'")
        fi
        continue
    fi

    if [[ "$line" =~ $RE_MERGEALL || "$line" =~ $RE_DONT_SELECT ]]; then
        continue
    fi

    if [[ "$line" =~ $RE_WRITE ]]; then
        errors+=("L${ln}: 'write class files!' must appear only as the last directive")
        continue
    fi

    if [[ "$line" =~ $RE_SET_NAME ]]; then
        errors+=("L${ln}: 'set name of model as ...!' must appear only as the first directive")
        continue
    fi

    if [[ "$line" =~ $RE_PRUNED_FROM ]]; then
        errors+=("L${ln}: 'pruned from ...!' must appear only as the second directive")
        continue
    fi

    errors+=("L${ln}: no PES directive matched — ${line}")
done

if [[ ${#errors[@]} -gt 0 ]]; then
    echo "PES validation errors (${#errors[@]}):"
    for e in "${errors[@]}"; do echo "  - $e"; done
    exit 1
fi

echo "ok    $file ($n directives)"
exit 0
