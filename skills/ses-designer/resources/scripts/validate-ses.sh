#!/usr/bin/env bash
# Validate an MS4 Me SES file.
#
# Three passes:
#   1. Lexical  — every non-blank, non-comment line matches exactly one of the
#                 six SES construct regexes and ends with "!".
#   2. Reserved — no identifier collides with a reserved grammar keyword.
#   3. Xref     — every entity referenced in a coupling or specialization must
#                 be declared (as root, aspect child, multi-aspect unit, or
#                 specialization option) somewhere in the file.
#
# Usage: validate-ses.sh <file.ses>
# Exit 0 iff all three passes clean. Error messages carry line numbers.

set -u

if [[ $# -lt 1 ]]; then
    echo "usage: $(basename "$0") <file.ses>" >&2
    exit 2
fi

file="$1"
if [[ ! -f "$file" ]]; then
    echo "error: $file is not a readable file" >&2
    exit 2
fi

# --- Regex bank (POSIX-extended, bash =~ dialect) ---------------------------

# Identifier fragment.
ID='[A-Za-z_][A-Za-z0-9_-]*'

RE_ASPECT="^[Ff]rom( the)? ${ID} perspective, ${ID} (is|are) made of ${ID}(, ${ID})*( and ${ID})?!$"
RE_MULTIASPECT="^[Ff]rom( the)? ${ID} perspective, ${ID} (is|are) made of more than one ${ID}!$"
RE_SPECIALIZATION="^${ID} can be ${ID}(, ${ID})*( or ${ID})? in ${ID}!$"
RE_COUPLING="^[Ff]rom( the)? ${ID} perspective, ${ID} sends ${ID} to ${ID}( as ${ID})?!$"
RE_MULTICOUPLING="^[Ff]rom( the)? ${ID} perspective, (all |each )?${ID} sends ${ID} to (all |each )?${ID}( as ${ID})?!$"
RE_VARIABLES="^${ID} has ${ID}(, ${ID})*( and ${ID})?!$"
RE_RANGE_NUM="^the range of ${ID}'s ${ID} is (number|integer|real) with values (\[|\()-?[0-9]+(\.[0-9]+)?, -?[0-9]+(\.[0-9]+)?(\]|\))!$"
RE_RANGE_ENUM="^the range of ${ID}'s ${ID} is (number|integer|real|string) with values ${ID}(, ${ID})*( and ${ID})?!$"

# Reserved keywords that must not be used as identifiers.
RESERVED=(from the perspective is are made of more than one can be in or sends to as all each has range with values number integer real string set name model pruned restructure multiaspects using multiplicity for select write class files and)

declared=()    # entities declared via aspect children, multi-aspect units, or specialization options
referenced=() # entities referenced in couplings (sender, receiver) and specializations (base entity)

errors_lex=()
errors_res=()
errors_xref=()

is_reserved() {
    local tok="$1"
    local kw
    for kw in "${RESERVED[@]}"; do
        [[ "$tok" == "$kw" ]] && return 0
    done
    return 1
}

declare_entity() {
    local name="$1"
    local seen
    for seen in "${declared[@]}"; do
        [[ "$seen" == "$name" ]] && return 0
    done
    declared+=("$name")
}

reference_entity() {
    local name="$1" line_no="$2"
    referenced+=("${name}@${line_no}")
}

# Strip the "all " / "each " qualifier from a coupling endpoint before xref.
strip_quantifier() {
    local s="$1"
    s="${s#all }"
    s="${s#each }"
    echo "$s"
}

# --- Pass 1+2: walk lines, classify, harvest entities -----------------------

line_no=0
while IFS= read -r line || [[ -n "$line" ]]; do
    line_no=$((line_no + 1))
    # Skip blanks and comments.
    trimmed="${line#"${line%%[![:space:]]*}"}"
    [[ -z "$trimmed" ]] && continue
    [[ "$trimmed" == \#* ]] && continue

    # Normalize line — strip trailing carriage return from CRLF files.
    line="${line%$'\r'}"

    # Must end with "!".
    if [[ "$line" != *! ]]; then
        errors_lex+=("L${line_no}: missing trailing '!' — ${line}")
        continue
    fi

    matched=0

    if [[ "$line" =~ $RE_ASPECT ]]; then
        matched=1
        # Parse: "from [the] ASPECT perspective, PARENT (is|are) made of C1, C2, ... (and CN)!"
        rest="${line%!}"
        rest="${rest#*perspective, }"
        parent="${rest%% *}"
        declare_entity "$parent"
        children="${rest#*made of }"
        children="${children//, / }"
        children="${children// and / }"
        for c in $children; do
            declare_entity "$c"
        done

    elif [[ "$line" =~ $RE_MULTIASPECT ]]; then
        matched=1
        rest="${line%!}"
        rest="${rest#*perspective, }"
        parent="${rest%% *}"
        declare_entity "$parent"
        unit="${rest##*more than one }"
        declare_entity "$unit"

    elif [[ "$line" =~ $RE_SPECIALIZATION ]]; then
        matched=1
        # Parse: "ENTITY can be O1, O2, ... (or ON) in LABEL!"
        rest="${line%!}"
        entity="${rest%% *}"
        reference_entity "$entity" "$line_no"
        after_can_be="${rest#* can be }"
        in_label="${after_can_be##* in }"   # unused except for sanity
        opts_part="${after_can_be% in *}"
        opts_part="${opts_part//, / }"
        opts_part="${opts_part// or / }"
        for o in $opts_part; do
            declare_entity "$o"
        done

    elif [[ "$line" =~ $RE_MULTICOUPLING ]]; then
        matched=1
        # Greedier regex first — handles "all X" / "each Y".
        rest="${line%!}"
        rest="${rest#*perspective, }"
        sender="${rest%% sends *}"
        sender="$(strip_quantifier "$sender")"
        after_to="${rest#* to }"
        if [[ "$after_to" == *" as "* ]]; then
            receiver="${after_to% as *}"
        else
            receiver="$after_to"
        fi
        receiver="$(strip_quantifier "$receiver")"
        reference_entity "$sender"   "$line_no"
        reference_entity "$receiver" "$line_no"

    elif [[ "$line" =~ $RE_COUPLING ]]; then
        matched=1
        rest="${line%!}"
        rest="${rest#*perspective, }"
        sender="${rest%% sends *}"
        after_to="${rest#* to }"
        if [[ "$after_to" == *" as "* ]]; then
            receiver="${after_to% as *}"
        else
            receiver="$after_to"
        fi
        reference_entity "$sender"   "$line_no"
        reference_entity "$receiver" "$line_no"

    elif [[ "$line" =~ $RE_VARIABLES ]]; then
        matched=1
        rest="${line%!}"
        entity="${rest%% *}"
        reference_entity "$entity" "$line_no"

    elif [[ "$line" =~ $RE_RANGE_NUM ]] || [[ "$line" =~ $RE_RANGE_ENUM ]]; then
        matched=1
        rest="${line%!}"
        after_of="${rest#the range of }"
        entity="${after_of%%\'s *}"
        reference_entity "$entity" "$line_no"
    fi

    if [[ $matched -eq 0 ]]; then
        errors_lex+=("L${line_no}: no SES construct matched — ${line}")
    fi
done < "$file"

# --- Pass 2: reserved-keyword lint on declared names ------------------------

for name in "${declared[@]}"; do
    if is_reserved "$name"; then
        errors_res+=("identifier '${name}' collides with a reserved SES keyword")
    fi
done

# --- Pass 3: cross-reference --------------------------------------------------

for entry in "${referenced[@]}"; do
    ref="${entry%@*}"
    ln="${entry##*@}"
    found=0
    for d in "${declared[@]}"; do
        if [[ "$d" == "$ref" ]]; then
            found=1
            break
        fi
    done
    if [[ $found -eq 0 ]]; then
        errors_xref+=("L${ln}: entity '${ref}' referenced but never declared (not in any aspect, multi-aspect, or specialization)")
    fi
done

# --- Report ------------------------------------------------------------------

rc=0
if [[ ${#errors_lex[@]} -gt 0 ]]; then
    rc=1
    echo "Lexical errors (${#errors_lex[@]}):"
    for e in "${errors_lex[@]}"; do echo "  - $e"; done
fi
if [[ ${#errors_res[@]} -gt 0 ]]; then
    rc=1
    echo "Reserved-keyword errors (${#errors_res[@]}):"
    for e in "${errors_res[@]}"; do echo "  - $e"; done
fi
if [[ ${#errors_xref[@]} -gt 0 ]]; then
    rc=1
    echo "Cross-reference errors (${#errors_xref[@]}):"
    for e in "${errors_xref[@]}"; do echo "  - $e"; done
fi

if [[ $rc -eq 0 ]]; then
    echo "ok    $file (${#declared[@]} entities declared, ${#referenced[@]} references resolved)"
fi

exit $rc
