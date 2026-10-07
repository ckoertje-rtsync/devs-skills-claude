#!/usr/bin/env bash
# Validate an MS4 Me SES file.
#
# Four passes:
#   1. Lexical    — every non-blank, non-comment line matches exactly one of the
#                   six SES construct regexes and ends with "!".
#   2. Reserved   — no identifier collides with a reserved grammar keyword.
#   3. Xref       — every entity referenced in a coupling, specialization, or
#                   variable statement must be declared (as root, aspect child,
#                   multi-aspect unit, or specialization option) somewhere in the
#                   file. Generated copy names such as id0_Person or tall_Boy
#                   (option + optional index + "_" + multi-aspect unit) resolve
#                   to their unit.
#   4. Structural — SES axioms and coupling scope (see reference/ses-axioms.md):
#                   valid brothers, strict hierarchy (entity labels), attached
#                   variables, all/each only on multi-aspect units, no plain
#                   self-coupling, and coupling scope with the multi-aspect
#                   exception (MS4 Me User's Guide p. 176).
#
# Usage: validate-ses.sh <file.ses>
# Exit 0 iff all four passes clean. Error messages carry line numbers.

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

# Lists accept the canonical "A, B and C" and the Oxford-comma variant
# "A, B, and C" that the guide also shows (pp. 127, 175).
RE_ASPECT="^[Ff]rom( the)? ${ID} perspective, ${ID} (is|are) made of ${ID}(, ${ID})*(,? and ${ID})?!$"
RE_MULTIASPECT="^[Ff]rom( the)? ${ID} perspective, ${ID} (is|are) made of more than one ${ID}!$"
RE_SPECIALIZATION="^${ID} can be ${ID}(, ${ID})*(,? or ${ID})? in ${ID}!$"
RE_COUPLING="^[Ff]rom( the)? ${ID} perspective, ${ID} sends ${ID} to ${ID}( as ${ID})?!$"
RE_MULTICOUPLING="^[Ff]rom( the)? ${ID} perspective, (all |each )?${ID} sends ${ID} to (all |each )?${ID}( as ${ID})?!$"
RE_VARIABLES="^${ID} has ${ID}(, ${ID})*(,? and ${ID})?!$"
RE_RANGE_TYPE="^the range of ${ID}'s ${ID} is ${ID}!$"
RE_RANGE_NUM="^the range of ${ID}'s ${ID} is (number|integer|real) with values (\[|\()-?[0-9]+(\.[0-9]+)?, -?[0-9]+(\.[0-9]+)?(\]|\))!$"
RE_RANGE_ENUM="^the range of ${ID}'s ${ID} is (number|integer|real|string) with values ${ID}(, ${ID})*(,? and ${ID})?!$"

# Reserved keywords that must not be used as identifiers.
RESERVED=(from the perspective is are made of more than one can be in or sends to as all each has range with values number integer real string set name model pruned restructure multiaspects using multiplicity for select write class files and)

declared=()    # entities declared via aspect children, multi-aspect units, or specialization options
referenced=() # entities referenced in couplings (sender, receiver), specializations (base entity), and variables

errors_lex=()
errors_res=()
errors_xref=()
errors_struct=()

# Facts for the structural pass, one per line, "|"-separated:
#   A|line|label|parent|child child ...      aspect
#   M|line|label|parent|unit                 multi-aspect
#   S|line|entity|label|option option ...    specialization
#   C|line|aspect|sq|sender|rq|receiver      coupling (sq/rq: all, each, or -)
#   V|line|entity|var var ...                variables
#   R|line|name                              entity reference (xref)
facts=""

add_fact() {
    facts+="$1"$'\n'
}

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
    add_fact "R|${line_no}|${name}"
}

# Split a coupling endpoint into qualifier ("all", "each", or "-") and name.
split_quantifier() {
    local s="$1"
    case "$s" in
        "all "*)  q="all";  name="${s#all }" ;;
        "each "*) q="each"; name="${s#each }" ;;
        *)        q="-";    name="$s" ;;
    esac
}

# Normalize an "A, B, and C" / "A, B and C" list to space-separated names.
list_words() {
    local s="$1" conj="$2"
    s="${s//, ${conj} / }"
    s="${s//, / }"
    s="${s// ${conj} / }"
    echo "$s"
}

# --- Pass 1: walk lines, classify, harvest entities and facts ----------------

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
        label="${rest#* }"
        label="${label#the }"
        label="${label%% perspective,*}"
        rest="${rest#*perspective, }"
        parent="${rest%% *}"
        declare_entity "$parent"
        children="$(list_words "${rest#*made of }" and)"
        for c in $children; do
            declare_entity "$c"
        done
        add_fact "A|${line_no}|${label}|${parent}|${children}"

    elif [[ "$line" =~ $RE_MULTIASPECT ]]; then
        matched=1
        rest="${line%!}"
        label="${rest#* }"
        label="${label#the }"
        label="${label%% perspective,*}"
        rest="${rest#*perspective, }"
        parent="${rest%% *}"
        declare_entity "$parent"
        unit="${rest##*more than one }"
        declare_entity "$unit"
        add_fact "M|${line_no}|${label}|${parent}|${unit}"

    elif [[ "$line" =~ $RE_SPECIALIZATION ]]; then
        matched=1
        # Parse: "ENTITY can be O1, O2, ... (or ON) in LABEL!"
        rest="${line%!}"
        entity="${rest%% *}"
        reference_entity "$entity" "$line_no"
        after_can_be="${rest#* can be }"
        in_label="${after_can_be##* in }"
        opts_part="$(list_words "${after_can_be% in *}" or)"
        for o in $opts_part; do
            declare_entity "$o"
        done
        add_fact "S|${line_no}|${entity}|${in_label}|${opts_part}"

    elif [[ "$line" =~ $RE_MULTICOUPLING ]]; then
        matched=1
        # Covers plain couplings too; RE_COUPLING below is kept as the
        # documented construct-4 shape.
        rest="${line%!}"
        label="${rest#* }"
        label="${label#the }"
        label="${label%% perspective,*}"
        rest="${rest#*perspective, }"
        split_quantifier "${rest%% sends *}"
        sq="$q"; sender="$name"
        after_to="${rest#* to }"
        if [[ "$after_to" == *" as "* ]]; then
            receiver="${after_to% as *}"
        else
            receiver="$after_to"
        fi
        split_quantifier "$receiver"
        rq="$q"; receiver="$name"
        reference_entity "$sender"   "$line_no"
        reference_entity "$receiver" "$line_no"
        add_fact "C|${line_no}|${label}|${sq}|${sender}|${rq}|${receiver}"

    elif [[ "$line" =~ $RE_COUPLING ]]; then
        matched=1
        rest="${line%!}"
        label="${rest#* }"
        label="${label#the }"
        label="${label%% perspective,*}"
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
        add_fact "C|${line_no}|${label}|-|${sender}|-|${receiver}"

    elif [[ "$line" =~ $RE_VARIABLES ]]; then
        matched=1
        rest="${line%!}"
        entity="${rest%% *}"
        reference_entity "$entity" "$line_no"
        add_fact "V|${line_no}|${entity}|$(list_words "${rest#* has }" and)"

    elif [[ "$line" =~ $RE_RANGE_NUM ]] || [[ "$line" =~ $RE_RANGE_ENUM ]] || [[ "$line" =~ $RE_RANGE_TYPE ]]; then
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

# --- Passes 3+4: cross-reference and structural checks (awk) -----------------
# awk prints "X<TAB>message" for xref errors and "S<TAB>message" for
# structural errors. Written for POSIX awk (no gawk extensions).

struct_out="$(printf '%s' "$facts" | awk -F'|' '
BEGIN { q = sprintf("%c", 39) }    # single quote, for messages

function err(kind, ln, msg) { printf "%s\tL%s: %s\n", kind, ln, msg }

# Valid brothers: no two aspect/multi-aspect/specialization nodes with the
# same label under one entity (this also catches a node declared twice).
function add_node(entity, kind, label, ln,   key) {
    key = entity SUBSEP label
    if (key in node_kind) {
        if (node_kind[key] == kind)
            err("S", ln, "valid brothers: " kind " " q label q " of " q entity q " is declared twice (first on L" node_ln[key] "); list every child in one statement")
        else
            err("S", ln, "valid brothers: label " q label q " under " q entity q " names two nodes, a " node_kind[key] " (L" node_ln[key] ") and this " kind)
        return
    }
    node_kind[key] = kind; node_ln[key] = ln
}

function add_edge(from, to) {
    kids[from] = (from in kids) ? kids[from] " " to : to
    has_parent[to] = 1
    node[from] = 1; node[to] = 1
}

# Strict hierarchy: an entity label may appear only once down any path.
# With name-keyed entities that is exactly "the parent-child graph has no
# cycle". Colors: 1 = on the current path, 2 = finished.
function dfs(u,   n, i, ks, v) {
    color[u] = 1
    n = split(kids[u], ks, " ")
    for (i = 1; i <= n; i++) {
        v = ks[i]
        if (color[v] == 1) {
            if (!((u SUBSEP v) in cycle_seen)) {
                cycle_seen[u, v] = 1
                err("S", edge_ln[u, v], "strict hierarchy: " q v q " is both above and below " q u q ", so an entity label repeats on a path")
            }
        } else if (color[v] != 2) {
            dfs(v)
        }
    }
    color[u] = 2
}

# A generated copy name (id0_Person, tall_Boy) is a specialization option of
# a multi-aspect unit, an optional index, "_", and the unit name (guide
# pp. 121, 124). Returns the unit, or "".
function copy_unit(name,   u, n, i, os) {
    for (u in is_unit) {
        n = split(spec_opts[u], os, " ")
        for (i = 1; i <= n; i++)
            if (name ~ ("^" os[i] "[0-9]*_" u "$")) return u
    }
    return ""
}

function exists(name) { return (name in decl) || copy_unit(name) != "" }

# Resolve a coupling endpoint to the entity whose place it takes in the
# tree: a specialization option stands for its general entity, and a copy
# name stands for its multi-aspect unit.
function canon(name,   guard, u) {
    for (guard = 0; guard < 50; guard++) {
        if (name in opt_of) { name = opt_of[name]; continue }
        if (name in decl) return name
        u = copy_unit(name)
        return u
    }
    return name
}

# Coupling scope (guide p. 176): parent and children of the named aspect,
# plus the unit of any multi-aspect hanging from one of those children (the
# "only exception": restructuring moves the copies into this aspect, p. 121).
function in_scope(i, e,   n, j, cs) {
    if (e == asp_par[i]) return 1
    n = split(asp_kids[i], cs, " ")
    for (j = 1; j <= n; j++) {
        if (e == cs[j]) return 1
        if ((cs[j] in unit_of) && unit_of[cs[j]] == e) return 1
    }
    return 0
}

$1 == "A" {
    n = split($5, cs, " "); split("", seen)
    for (i = 1; i <= n; i++) {
        if (cs[i] in seen)
            err("S", $2, "valid brothers: " q cs[i] q " is listed twice in aspect " q $3 q " of " q $4 q)
        seen[cs[i]] = 1
        add_edge($4, cs[i]); edge_ln[$4, cs[i]] = $2
    }
    add_node($4, "aspect", $3, $2)
    decl[$4] = 1; for (i = 1; i <= n; i++) decl[cs[i]] = 1
    na++; asp_ln[na] = $2; asp_lab[na] = $3; asp_par[na] = $4; asp_kids[na] = $5
    next
}
$1 == "M" {
    add_edge($4, $5); edge_ln[$4, $5] = $2
    add_node($4, "multi-aspect", $3, $2)
    unit_of[$4] = $5; is_unit[$5] = 1; multi_lab[$3] = 1
    decl[$4] = 1; decl[$5] = 1
    next
}
$1 == "S" {
    n = split($5, os, " "); split("", seen)
    for (i = 1; i <= n; i++) {
        if (os[i] in seen)
            err("S", $2, "valid brothers: option " q os[i] q " is listed twice in specialization " q $4 q " of " q $3 q)
        seen[os[i]] = 1
        add_edge($3, os[i]); edge_ln[$3, os[i]] = $2
        opt_of[os[i]] = $3; decl[os[i]] = 1
        spec_opts[$3] = ($3 in spec_opts) ? spec_opts[$3] " " os[i] : os[i]
    }
    add_node($3, "specialization", $4, $2)
    next
}
$1 == "C" { nc++; c_ln[nc] = $2; c_lab[nc] = $3; c_sq[nc] = $4; c_s[nc] = $5; c_rq[nc] = $6; c_r[nc] = $7; next }
$1 == "V" {
    n = split($4, vs, " ")
    for (i = 1; i <= n; i++) {
        key = $3 SUBSEP vs[i]
        if (key in var_ln)
            err("S", $2, "attached variables: " q $3 q " already has a variable named " q vs[i] q " (L" var_ln[key] ")")
        else
            var_ln[key] = $2
    }
    next
}
$1 == "R" { nr++; r_ln[nr] = $2; r_name[nr] = $3; next }

END {
    # Xref.
    for (k = 1; k <= nr; k++)
        if (!exists(r_name[k]))
            err("X", r_ln[k], "entity " q r_name[k] q " referenced but never declared (not in any aspect, multi-aspect, or specialization)")

    # Strict hierarchy, starting from roots first so messages point downward.
    for (e in node) if (!(e in has_parent) && color[e] != 2) dfs(e)
    for (e in node) if (color[e] != 2) dfs(e)

    # Couplings.
    for (k = 1; k <= nc; k++) {
        ln = c_ln[k]; s = c_s[k]; r = c_r[k]
        if (c_sq[k] != "-" && !(s in is_unit))
            err("S", ln, q c_sq[k] q " is only legal on a multi-aspect unit; " q s q " is not one")
        if (c_rq[k] != "-" && !(r in is_unit))
            err("S", ln, q c_rq[k] q " is only legal on a multi-aspect unit; " q r q " is not one")
        if (c_sq[k] == "-" && c_rq[k] == "-" && s == r)
            err("S", ln, "self-coupling: " q s q " cannot send to itself")
        cs_ = canon(s); cr_ = canon(r)
        if (cs_ == "" || cr_ == "") continue    # already reported by xref
        found = 0; label_seen = 0
        for (i = 1; i <= na; i++) {
            if (asp_lab[i] != c_lab[k]) continue
            label_seen = 1
            if (in_scope(i, cs_) && in_scope(i, cr_)) { found = 1; break }
        }
        if (found) continue
        if (!label_seen && (c_lab[k] in multi_lab))
            err("S", ln, "coupling scope: " q c_lab[k] q " is a multi-aspect perspective, which restructuring removes (guide p. 121); name the aspect that contains the multi-aspect parent")
        else if (!label_seen)
            err("S", ln, "coupling scope: no aspect is labeled " q c_lab[k] q)
        else
            err("S", ln, "coupling scope: " q s q " and " q r q " are not both in aspect " q c_lab[k] q " (legal: the aspect parent, its children, and units of multi-aspects under those children)")
    }
}')"

while IFS=$'\t' read -r kind msg; do
    case "$kind" in
        X) errors_xref+=("$msg") ;;
        S) errors_struct+=("$msg") ;;
    esac
done <<< "$struct_out"

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
if [[ ${#errors_struct[@]} -gt 0 ]]; then
    rc=1
    echo "Structural errors (${#errors_struct[@]}):"
    for e in "${errors_struct[@]}"; do echo "  - $e"; done
fi

if [[ $rc -eq 0 ]]; then
    echo "ok    $file (${#declared[@]} entities declared, ${#referenced[@]} references resolved)"
fi

exit $rc
