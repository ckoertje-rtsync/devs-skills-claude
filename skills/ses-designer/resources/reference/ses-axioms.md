# SES Axioms (Structural Rules MS4 Me Enforces)

MS4 Me rejects SES files that violate these five axioms. The `ses-designer` skill enforces them at DISCOVER time (through the prompting order and the cross-reference validator), so the user never sees a parse error for a structural reason. See MS4 Me User's Guide pp. 164–169, 173–177.

## 1. Uniformity

Two entities with the same name in the same position must have identical internal structure (same aspects, same specializations, same variables). In practice, the skill enforces this by treating entity *names* as unique keys in the Model Context — re-declaring an entity's children or variables overwrites rather than shadows.

## 2. Strict hierarchy

The entity structure is a tree. An entity cannot be its own ancestor. The validator's cross-reference pass builds the parent→child graph from aspects + multi-aspects + specializations and rejects cycles.

## 3. Alternating mode

Aspects and specializations alternate down the tree: an entity may have aspects *or* specializations as its immediate children, not both on the same level of the same entity. Practically:

- An aspect's children are entities (never aspects).
- A specialization's options are entities (never more specializations on the same entity at the same level).
- Each new entity may start its own aspect(s) or its own specialization(s) — but pick one.

The skill's DISCOVER order (root → aspects → children → *per-child decide: decompose via aspect, replicate via multi-aspect, or specialize*) naturally honors this.

## 4. Valid brothers

Sibling entities within a single aspect must be distinct (by name). The validator rejects duplicates inside an `is made of` list.

## 5. Attached variables

Variables attach to entities and are inherited along the parent→child path. A variable declared on an ancestor is readable by descendants; no explicit re-declaration is required to reference it. When the same variable name is declared on both ancestor and descendant, the descendant's declaration locally overrides the ancestor's range.

---

## Coupling scope (a sixth practical rule)

Couplings are only legal between:

- A parent entity and one of its children in the same aspect.
- Two sibling entities under the same aspect of the same parent.
- Two entities that are replicas under the same multi-aspect (via `all`/`each` qualifiers).

Cross-aspect couplings (between entities whose lowest common ancestor has them in *different* aspects) are illegal. The validator flags this as an xref-pass error.

---

## Practical implications for the skill

| Rule | How DISCOVER enforces it | How the validator catches violations |
|---|---|---|
| Uniformity | One Model Context entry per entity name; updates merge. | Duplicate `can be ... in` for the same label with divergent options → error. |
| Strict hierarchy | Recursive top-down prompting; never asks "is this entity a parent of its ancestor?" | Cycle detection during cross-reference pass. |
| Alternating mode | Per-entity prompt: *"decompose (aspect)", "replicate (multi-aspect)", "specialize", or "leaf atomic"*. | Statement-order scan: specialization lines for an entity must not interleave with aspect lines on the same entity. |
| Valid brothers | `AskUserQuestion` de-dupes sibling lists before committing. | Duplicate child in an `is made of` list → error with line number. |
| Attached variables | Variables recorded on the declaring entity; lookup walks up the tree. | Range referencing undeclared `{ENTITY}'s {VAR}` → error. |
| Coupling scope | `AskUserQuestion` constrains sender/receiver choices to legal siblings under a selected aspect. | LCA check: every coupling's endpoints must share an aspect or multi-aspect. |

Reading order when unsure: this file → `coupling-scope.md` pattern → `ses-grammar.md` for exact syntax.
