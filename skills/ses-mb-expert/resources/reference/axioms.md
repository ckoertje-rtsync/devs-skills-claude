# SES Axioms: Definitions, History, and Common Misstatements

The axioms that make a System Entity Structure well formed: their precise wording, how the list changed between 1985 and 2019, where the Zeigler group relaxed them, how MS4 Me's natural-language SES realizes each one, and the misreadings to avoid. This file holds the theory. The operational rules and what the validator checks are in the `ses-designer` skill (`resources/reference/ses-axioms.md` and `resources/scripts/validate-ses.sh`). Current as of 2026-10-06.

Provenance tags: [Z] Zeigler or coauthors, read directly; [T] third-party literature; [G] MS4 Me User's Guide (page); [F] field observation from this repo's practice; [I] inference no source states. Rozenblit & Huang (1987), written without Zeigler, is tagged [Z] and named in the citation.

## 1. The six axioms

From 1993 on, the Zeigler group names six axioms. The most precise accessible wording is in a 2019 paper coauthored by Zeigler, which credits the list to the 2017 *Guide to Modeling and Simulation of Systems of Systems* [Z] ([Traoré, Zacharewicz, Duboz & Zeigler 2019, *SIMULATION* 95(6):481–497, Sect. 2.1, accepted manuscript p. 6](http://agritrop.cirad.fr/588108/1/Valued-based-Healthcare-Simulation.pdf); [DOI 10.1177/0037549718776765](https://doi.org/10.1177/0037549718776765)):

> "SES has six axioms (Zeigler & Sarjoughian, 2017): uniformity, strict hierarchy, alternating mode, valid brothers, attached variables and inheritance. Uniformity forces that any two nodes with the same labels have isomorphic subtrees. Strict hierarchy prohibits a label from appearing more than once down any path of the tree. Alternating mode states that, if a node is an Entity, then the successor is either Aspect or Specialization, and vice versa. Valid brothers forbids having two brothers with the same label. Attached variables constraints that variable types attached to the same item shall have distinct names. Inheritance asserts that Specialization inherits all variables and Aspects from the parent Entity to the children Entities. Zeigler & Hammond (2007) provide a formal set-theoretic characterization of the SES that shows how the axioms are satisfied."

| # | Axiom | Precise content | What it forbids |
|---|---|---|---|
| 1 | Uniformity | Two nodes with the same label have isomorphic subtrees, wherever they occur | Two `Engine` nodes anywhere in the tree with different substructure |
| 2 | Strict hierarchy | A label appears at most once on any root-to-leaf path | A node reappearing below itself, i.e. recursion, unless relaxed (§4) |
| 3 | Alternating mode | Entities alternate with aspects/specializations along every path | Entity directly under entity; aspect or specialization directly under an aspect or specialization |
| 4 | Valid brothers (valid siblings) | No two children of one node share a label | Duplicate labels among any node's children |
| 5 | Attached variables | Variable types attached to the same item have distinct names | Two variables of the same name on one item |
| 6 | Inheritance | A specialization passes its parent entity's variables and aspects to each variant | A variant losing the general entity's structure |

**Notes on each.**

- **Uniformity** does not depend on position. It is about labels, not places in the tree [Z] (Traoré et al. 2019).
- **Strict hierarchy** is label-based and stronger than "no cycles." It forbids repeating a label down a path even in a tree that has no cycle [Z] (Traoré et al. 2019).
- **Alternating mode** constrains how layers stack. It does **not** stop one entity from carrying both aspects and specializations. The 1985 text: aspects and specializations "each alternate with entities, but cannot be hung from each other" [Z] ([Rozenblit & Zeigler 1985, WSC, p. 225](https://informs-sim.org/wsc85papers/1985_0048.pdf)). In the same paper, entity CAR has "Physical Decomposition, Service Aspect, and Utility Specialization" (p. 228). In 1993, "An entity may have several *aspects* ... An entity may also have several *specializations*" [Z] ([Rozenblit & Zeigler 1993, WSC, p. 606](https://informs-sim.org/wsc93papers/1993_0084.pdf)). The multi-aspect counts as an aspect-like layer [I].
- **Valid brothers** covers the children of *every* node: the entities in one aspect, the variants of one specialization, and the aspect and specialization labels hanging from one entity [Z] (Traoré et al. 2019). The 1993 paper calls it "valid siblings" (p. 606).
- **Attached variables** is a uniqueness rule only. It says nothing about scoping variables down the tree [Z] (Traoré et al. 2019).
- **Inheritance** runs along specializations: "Called specialized entities, such variants inherit properties of an entity to which they are related by the specialization relation" [Z] ([Rozenblit & Huang 1987, WSC, p. 604](https://informs-sim.org/wsc87papers/1987_0086.pdf)). Also: "A specialization may occur in more than one location; whenever it occurs it carries with it all its attributes and substructures" [Z] (Rozenblit & Zeigler 1985, p. 225). A child in an aspect does not inherit its parent's variables [I, from the definitions].

Zeigler's own verbatim axiom wording in the 1984, 1990, 2007 and 2013/2017 books could not be opened. The 2019 text is a Zeigler-coauthored restatement.

## 2. How the list changed

| Year | Source | Axioms listed | Notes |
|---|---|---|---|
| 1985 | Rozenblit & Zeigler, WSC, p. 225 [Z] | "alternation entity/aspect", "entity/specialization", "strict hierarchy", "inheritance: birth, life", "multiple entity" | Also lists operations: "naming scheme", "generation of distribution/aggregation relations", "transformations to taxonomy free form", "pruning", "attachment of constraints to aspects". "Birth, life" is not explained in the accessible pages. |
| 1993 | Rozenblit & Zeigler, WSC, p. 606 [Z] | "uniformity, strict hierarchy, alternating mode, inheritance, valid siblings, and attached variables (Zeigler 1984)" | First appearance of the six names; credited to the 1984 book. "The axioms furnish a unifying set of rules for developing and manipulating entity structures." |
| 2019 | Traoré et al., Sect. 2.1 [Z] | uniformity, strict hierarchy, alternating mode, valid brothers, attached variables, inheritance | Precise definitions; credited to Zeigler & Sarjoughian (2017) |

Reading of the change [I]:

- The 1985 list folds in "multiple entity" and splits alternation into entity/aspect and entity/specialization.
- By 1993 the list had settled into the six names still used in 2019.
- Because the 1993 paper credits the six to the 1984 book, the book may contain both framings. That cannot be confirmed without the book.

The 1985 alternation property was stated twice. For specializations: "entities and specializations alternate along any path from root to leaves." For aspects: "The alternation property now requires alternation of entities and aspects ... Formally, aspects and specializations are quite alike in their behavior (but not in their interpretation)" [Z] (Rozenblit & Zeigler 1985, p. 225).

## 3. How MS4 Me text realizes each axiom

MS4 Me's natural-language SES makes some axioms hold by construction. The `ses-designer` validator checks the others. The MS4 Me User's Guide itself never names the axioms: a full-text search finds no "axiom," "uniformity," "alternating," "valid brother" or "strict hierarch" [G].

| Axiom | In MS4 Me text | Checked by `ses-designer`'s `validate-ses.sh`? |
|---|---|---|
| Uniformity | Holds by construction. Entity names are global keys, so every statement about `Engine` applies wherever `Engine` appears. Copies of a multi-entity each inherit its sub-SES [G] (p. 122). | No; nothing to check |
| Strict hierarchy | Possible to violate by naming an ancestor as a descendant | Yes, for entity labels. Specialization labels are exempt because the guide recommends reusing `index` down one path [G] (p. 126) [F]. |
| Alternating mode | Holds by construction. Aspect, multi-aspect and specialization statements attach to an entity, and their children are entities. | No; nothing to check |
| Valid brothers | Possible to violate with duplicate names in a list, or with a label used twice under one entity | Yes, including an aspect declared in two statements [F] |
| Attached variables | Possible to violate with a repeated name across `has` statements | Yes [F] |
| Inheritance | Applied by MS4 Me during pruning | No. Separately, MS4 Me needs `inherit from {OPT}!` in the PES before a selected option takes on behavior [G] (pp. 27, 158). That is a code-generation directive, not the axiom. |

The coupling-scope rule ("Messages can only be passed between a subsystem and its parent system or subsystems within the same aspect ... The only exception to this rule is that the subsystem of a multi-aspect can send messages to siblings of its parent system") is an MS4 Me rule [G] (p. 176), not one of the six axioms. Its theoretical ancestor is the minimal-scoping-aspect rule for coupling constraints [Z] (Rozenblit & Zeigler 1985, p. 225; see `concepts.md` §3).

## 4. Relaxations and departures

**Strict hierarchy relaxed for recursion.** To build a recursive canonical SES of system specifications (a set of system specifications nested inside a system specification), "we must relax the strict hierarchy SES axiom as in Cho (1993) in order to facilitate such a process" [Z] (Rozenblit & Zeigler 1993, p. 607). "Cho (1993)" is UNVERIFIED. This is the only documented departure from the six axioms inside the Zeigler group.

**Third-party handling.** The Hochschule Wismar extended SES treats the axioms as constraints to respect. It claims "a complete solution without violation of the strict hierarchy axiom" for that recursion problem, and renames replicas "to comply with the valid brothers axiom" [T] ([Pawletta, Schmidt, Durak & Zeigler 2018, SNE 28(1), pp. 14–16](https://doi.org/10.11128/sne.28.tn.10402); Zeigler is a coauthor, but the extension is Wismar's). The DLR SESTools meta-schema checks strict hierarchy, sibling uniqueness and variable uniqueness through XML Schema 1.1 assertions, but not uniformity [T] ([SESTools ses.xsd](https://github.com/aeronautical-informatics/SESTools)). See `tools.md`.

**MS4 Me expansion.** Restructuring removes the multi-aspect and makes the copies children of the grandparent (`id0_Person`, `id1_Person`, ...) [G] (p. 121). The copies get distinct labels, so valid brothers holds after expansion [I].

## 5. Common misstatements

Before commit `fa91953` (2026-10-06), the `ses-designer` skill's `ses-axioms.md` made each of the errors below [F]. Writers of new skills and documents should avoid them. The left column paraphrases the old file.

| Misstatement | Correct reading | Evidence |
|---|---|---|
| "MS4 Me rejects SES files that violate these axioms" (citing guide pp. 164–177) | The guide never states the axioms. They come from Zeigler's theory. | [G] full-text search; [Z] Traoré et al. 2019 |
| Five axioms | Six; **inheritance** was missing | [Z] Rozenblit & Zeigler 1993, p. 606; Traoré et al. 2019 |
| Alternating mode: an entity has aspects *or* specializations, not both | Layers alternate along paths. One entity may carry both, and several of each. | [Z] 1985 CAR, p. 228; 1993, p. 606; [G] `Person` with `index` and `height`, pp. 121–122 |
| Uniformity: same-name entities "in the same position" match | Position-independent: any two nodes with the same label have isomorphic subtrees | [Z] Traoré et al. 2019 |
| Strict hierarchy: an entity cannot be its own ancestor (a cycle check) | Label-based: no label more than once down any path | [Z] Traoré et al. 2019 |
| Attached variables: ancestor-to-descendant inheritance with local override | Distinct variable names per item; inheritance is a separate axiom and runs along specializations only | [Z] Traoré et al. 2019; Rozenblit & Huang 1987, p. 604 |
| Valid brothers: siblings within one aspect are distinct | Any node's children are distinct, including several aspects or specializations under one entity | [Z] Traoré et al. 2019 |

Other misreadings seen in secondary material:

- **"Select-if" rules and "synchronization of specializations."** No examined Zeigler-group text uses these phrases. The closest Zeigler-group constructs are 1987 selection constraints and rules, and synthesis rules on aspects [Z] (Rozenblit & Huang 1987, pp. 604–607). These phrases may come from later third-party work.
- **The axioms as the definition of the SES.** The SES is a labeled tree with attached variable types. The axioms are well-formedness conditions on that tree [Z] (Rozenblit & Zeigler 1985, p. 225; [Kim, Lee, Christensen & Zeigler 1990, *IEEE SMC* 20(5)](https://doi.org/10.1109/21.59966): "Formally, the SES is a labeled tree with attached variable types that satisfy certain axioms").

## 6. Gaps

- Zeigler's verbatim axiom wording, and its page numbers, in the 1984 book, the 1990 book (chapter "System Entity Structures," pp. 145–168), Zeigler & Hammonds (2007, ch. 2) and the *Guide* (2013/2017, ch. 3) were not accessible.
- The set-theoretic characterization in Zeigler & Hammonds (2007), which Traoré et al. (2019) report, was not seen.
- The meaning of "inheritance: birth, life" in the 1985 list is unexplained in the accessible pages.
- "Cho (1993)" is UNVERIFIED.
