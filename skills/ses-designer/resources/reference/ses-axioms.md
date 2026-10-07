# SES Axioms and Coupling Scope

The six SES axioms come from Zeigler's SES theory, not from MS4 Me. The MS4 Me User's Guide never states them: it documents the SES syntax (pp. 173–178) and pruning behavior (pp. 120–129, 168). The `ses-designer` skill applies the axioms at DISCOVER time. `scripts/validate-ses.sh` checks the ones that can be checked from the `.ses` text; the rest hold by construction in MS4 Me's natural language.

Sources for the axiom definitions:

- Traoré, M. K., Zacharewicz, G., Duboz, R. & Zeigler, B. P. (2019), "Modeling and simulation framework for value-based healthcare systems," *SIMULATION* 95(6):481–497, Sect. 2.1. [DOI 10.1177/0037549718776765](https://doi.org/10.1177/0037549718776765). The quoted definitions below come from this paper, which credits them to Zeigler & Sarjoughian, *Guide to Modeling and Simulation of Systems of Systems*, 2nd ed., Springer, 2017.
- Rozenblit, J. W. & Zeigler, B. P. (1993), "Representing and constructing system specifications using the system entity structure concepts," *Proc. 1993 Winter Simulation Conference*, pp. 604–611. [DOI 10.1109/wsc.1993.718106](https://doi.org/10.1109/wsc.1993.718106). Names the same six axioms and credits them to Zeigler (1984).
- Rozenblit, J. W. & Zeigler, B. P. (1985), "Concepts for knowledge-based system design environments," *Proc. 1985 Winter Simulation Conference*, pp. 223–231. [DOI 10.1145/21850.253157](https://doi.org/10.1145/21850.253157). Gives the alternation property and the CAR example.

## 1. Uniformity

> "Uniformity forces that any two nodes with the same labels have isomorphic subtrees." (Traoré et al. 2019)

The definition does not depend on position. In MS4 Me text an entity name is a global key: every statement about `Engine` applies wherever `Engine` appears, so uniformity holds by construction. The guide shows the same idea for multi-aspects: each copy of a multi-entity inherits its sub-SES (p. 122). The skill keeps one Model Context entry per entity name.

## 2. Strict hierarchy

> "Strict hierarchy prohibits a label from appearing more than once down any path of the tree." (Traoré et al. 2019)

This is stronger than "no cycles": it is about labels, so an entity name may not reappear anywhere below itself. Because entities are keyed by name, a repeated entity label on a path shows up as a cycle in the parent-to-child graph built from aspects, multi-aspects, and specializations. The validator reports any entity that is both above and below another.

The validator applies this to **entity** labels only. The guide recommends reusing one specialization label, such as `index`, for nested multi-aspects (`Enclave can be enclaveid in index!` above `Client can be clientid in index!`, p. 126), and those two labels sit on one path. Rozenblit & Zeigler (1993, p. 607) also note that strict hierarchy has to be relaxed for recursive structures. MS4 Me's SES language has no recursion construct, so the skill keeps the axiom strict for entities.

## 3. Alternating mode

> "Alternating mode states that, if a node is an Entity, then the successor is either Aspect or Specialization, and vice versa." (Traoré et al. 2019)

Entity layers alternate with aspect, multi-aspect, and specialization layers along every path. Aspects and specializations "each alternate with entities, but cannot be hung from each other" (Rozenblit & Zeigler 1985, p. 225).

The axiom does **not** limit an entity to one kind of child. One entity may carry several aspects and several specializations at once:

- Rozenblit & Zeigler's CAR entity has two aspects and a specialization (1985, pp. 225, 228).
- In the guide, `Person` carries both `Person can be id in index!` and `Person can be short, medium, or tall in height!` (pp. 121–122).
- In the guide, `AA` carries both `type` and `index` specializations (p. 129).

In MS4 Me text the axiom holds by construction. Aspect, multi-aspect, and specialization statements always attach to an entity, and their children are always entities.

## 4. Valid brothers

> "Valid brothers forbids having two brothers with the same label." (Traoré et al. 2019)

The 1993 paper calls this axiom *valid siblings*. It applies to the children of every node:

- the entities listed in one aspect,
- the options of one specialization,
- the aspect, multi-aspect, and specialization labels hanging from one entity.

The validator rejects a name listed twice in one `is made of` or `can be` list. It also rejects a label used twice under one entity. That includes declaring the same aspect in two statements: list every child of an aspect in one statement.

## 5. Attached variables

> "Attached variables constraints that variable types attached to the same item shall have distinct names." (Traoré et al. 2019)

The axiom only requires distinct variable names per entity. It says nothing about scoping variables down the tree. The validator rejects a variable name declared twice on one entity, including across separate `has` statements.

## 6. Inheritance

> "Inheritance asserts that Specialization inherits all variables and Aspects from the parent Entity to the children Entities." (Traoré et al. 2019)

A specialization option carries the variables and aspects of its general entity. If `Engine has HoursRun!` and `Engine can be ElectricEngine or GasolineEngine in engineType!`, then `ElectricEngine` and `GasolineEngine` both have `HoursRun`. Specialized entities "inherit properties of an entity to which they are related by the specialization relation" (Rozenblit & Huang, *Proc. 1987 Winter Simulation Conference*, p. 604, [DOI 10.1145/318371.318671](https://doi.org/10.1145/318371.318671)).

This is **not** ancestor-to-descendant scoping: a child in an aspect does not inherit its parent's variables. MS4 Me applies inheritance during pruning, so the validator has nothing to check.

Separately from the axiom, MS4 Me needs `inherit from {OPT}!` in the `.pes` before the generated model takes on the selected option's behavior (guide pp. 27, 158). See `pes-grammar.md`.

---

## Coupling scope (MS4 Me rule)

The guide states the rule on p. 176:

> "Messages can only be passed between a subsystem and its parent system or subsystems within the same aspect of a system (subsystems within the same aspect are known as siblings). The only exception to this rule is that the subsystem of a multi-aspect can send messages to siblings of its parent system."

The exception follows from restructuring. When the `.pes` expands a multi-aspect, the copies of its unit become children of the unit's grandparent, inside the grandparent's aspect, and the multi-aspect itself is removed (p. 121). So a multi-aspect unit couples inside the aspect that contains its parent.

For `from the {A} perspective, {X} sends {M} to {Y}!`, each endpoint must be one of:

1. the parent entity of aspect `{A}`,
2. a child entity of aspect `{A}`,
3. the unit of a multi-aspect whose parent is a child of `{A}` (the exception above).

More rules:

- A generated copy name (`id0_Person`, `tall_Boy`) counts as its multi-aspect unit (pp. 121–124). A specialization option counts as its general entity.
- `all` and `each` are legal only on multi-aspect units.
- `{X} sends {M} to {X}!` is rejected because a DEVS coupled model does not couple a component to itself. `all {X} sends {M} to all {X}!` is legal, and MS4 Me adds no self-couplings for it (p. 123).
- A multi-aspect perspective, such as `multiPerson`, is not a valid coupling aspect. The guide never couples under one, and restructuring removes it.

See `patterns/coupling-scope.md` for worked examples.

---

## Practical implications for the skill

| Rule | How DISCOVER enforces it | What `validate-ses.sh` checks |
|---|---|---|
| Uniformity | One Model Context entry per entity name; updates merge. | Nothing to check: holds by construction. |
| Strict hierarchy | Recursive top-down prompting; never re-introduces an ancestor as a descendant. | Entity label that is both above and below another entity. |
| Alternating mode | Children of aspects and specializations are always entities. An entity may get aspects *and* specializations. | Nothing to check: holds by construction. |
| Valid brothers | `AskUserQuestion` de-dupes sibling lists before committing. | Name listed twice in one list; label used twice under one entity, including an aspect declared in two statements. |
| Attached variables | Variables are recorded per entity; names are de-duped. | Variable name declared twice on one entity. |
| Inheritance | Options are presented with their general entity's variables. The PES emits `inherit from {OPT}!` after each `select`. | Nothing to check: MS4 Me applies it during pruning. |
| Coupling scope | `AskUserQuestion` offers only the legal endpoints of the chosen aspect. | Endpoints outside the named aspect's scope; `all`/`each` on a non-unit; plain self-coupling; coupling under a multi-aspect perspective. |

Reading order when unsure: this file → `patterns/coupling-scope.md` → `ses-grammar.md` for exact syntax.
