# SES/MB Glossary

Terms used in System Entity Structure / Model Base (SES/MB) work, grouped by topic. Each entry has a short meaning and its source. Fuller treatment: `concepts.md` (structure and pruning), `axioms.md`, `sesmb-workflow.md`, `tools.md`, `ms4me-facts.md`. Current as of 2026-10-06.

Provenance tags: [Z] Zeigler or coauthors, read directly; [T] third-party literature; [G] MS4 Me User's Guide (page); [F] field observation from this repo's practice; [I] inference no source states. Short citations below are resolved in `references.md`. WSC papers are open on the INFORMS-SIM archive: [1985](https://informs-sim.org/wsc85papers/1985_0048.pdf), [1986](https://informs-sim.org/wsc86papers/1986_0019.pdf), [1987](https://informs-sim.org/wsc87papers/1987_0086.pdf), [1988](https://www.informs-sim.org/wsc88papers/1988_0033.pdf), [1989](https://www.informs-sim.org/wsc89papers/1989_0012.pdf), [1993](https://informs-sim.org/wsc93papers/1993_0084.pdf).

## 1. Structure

| Term | Meaning | Source |
|---|---|---|
| **System entity structure (SES)** | "a labeled tree with attached variable types"; one SES represents a family of hierarchical, modular models | [Z] Rozenblit & Zeigler 1985, p. 225; Zeigler 1986, p. 135 |
| **Entity** | A conceptual component "for which models may reside in a library (model base)"; carries attribute slots. Called "entity," not "model," "since the entity may have several possible models to represent it" | [Z] Rozenblit & Zeigler 1993, p. 606; Zeigler 1986, p. 135 |
| **Aspect** | A labeled decomposition of an entity into child entities. Called "aspect" "since there may be several possible decompositions for a given entity" | [Z] Zeigler 1986, p. 135; Rozenblit & Zeigler 1993, p. 606 |
| **Specialization** | A labeled classification of an entity into mutually exclusive variants; pruning picks one | [Z] Rozenblit & Zeigler 1993, p. 606; Rozenblit & Huang 1987, p. 604 |
| **Specialized entity** | A variant under a specialization; inherits the general entity's properties | [Z] Rozenblit & Huang 1987, p. 604 |
| **Multiple aspect** | 1980s term: the decomposition of a multiple entity into copies of one entity, drawn as three vertical lines | [Z] Zeigler 1986, p. 135; Rozenblit & Zeigler 1985, p. 225 |
| **Multiple entity** | 1980s term: the entity whose multiple aspect yields a variable number of copies; carries a count variable such as `FPS.NUMBER` | [Z] Zeigler 1986, p. 136 |
| **Multi-aspect** | Later term for the multiple aspect: "a special case of an aspect except that it has only one child called its multi-entity – which can generate a specified number of copies" | [G] pp. 120–121, citing Zeigler & Hammonds 2007; [Z] Traoré et al. 2019 |
| **Multi-entity** | The single child of a multi-aspect, the unit that is copied | [G] pp. 120–121 |
| **Attached variable** | A variable type attached to an item. "a variable I.V may be used to describe the item occurrence I"; drawn with a `~` prefix | [Z] Rozenblit & Zeigler 1985, p. 225; Rozenblit & Huang 1987, p. 607 |
| **Coupling constraint / coupling specification** | Attached to an aspect. Restricts how that decomposition's components "can be joined together." Has external input, external output and internal parts | [Z] Rozenblit & Huang 1987, p. 604; Zeigler 1986, p. 130 |
| **Selection constraint** | Attached to a specialization. Restricts "the way in which its subentities may replace it" | [Z] Rozenblit & Huang 1987, p. 604 |
| **Minimal scoping aspect** | The lowest aspect that scopes every entity a constraint mentions; the constraint attaches there | [Z] Rozenblit & Zeigler 1985, p. 225 |
| **Composition tree** | The tree of components and couplings of one hierarchical, modular model. The SES generalizes it to a family of models | [Z] Zeigler 1986, p. 135 |
| **Conceptual network** | 1987 name for the declarative domain representation the SES provides | [Z] Rozenblit & Huang 1987, p. 605 |

## 2. Pruning and its results

| Term | Meaning | Source |
|---|---|---|
| **Pruning** | Top-down selection over the SES: "selecting a single aspect for each entity and zero or more entities for each aspect," after removing specializations; also choosing one variant per specialization and a count per multi-aspect | [Z] Rozenblit & Zeigler 1985, p. 225; Zeigler 1986, p. 136; [I] as a choice function |
| **Constraint-driven pruning** | Pruning automated by production rules over selection and synthesis rule sets | [Z] Rozenblit & Huang 1987, pp. 604–607; Rozenblit & Huang 1991 |
| **Selection rule** | "each selection rule stands for a choice of an entity in a specialization"; associated with entities | [Z] Rozenblit & Huang 1987, pp. 604–607 |
| **Synthesis rule** | Applied after selection rules to "ensure proper configuration of the selected entities"; attached to aspects | [Z] Rozenblit & Huang 1987, pp. 604–607 |
| **Generic experimental (observation) frame** | Design objectives expressed as standard variable types. Pruning against it decides which aspects are kept, how deep pruning goes, and which variables are descriptive | [Z] Rozenblit & Zeigler 1985, pp. 223, 226 |
| **Decomposition tree** | 1985 name for a pruning result: "a skeleton for a hierarchical model construction" | [Z] Rozenblit & Zeigler 1985, pp. 226–227 |
| **Model composition tree** | 1993 name for a pruning result: "a tree whose leaf nodes are system specifications" | [Z] Rozenblit & Zeigler 1993, pp. 606–607 |
| **Pruned entity structure (PES)** | Modern name for a pruning result. "A PES is a pruned SES"; it "specifies one of those configurations that will be used to run a simulation" | [G] pp. 17, 168; [Z] Cheon, Kim & Zeigler 2008 (PARTIALLY VERIFIED) |
| **Partial PES** | A PES with choices left open. MS4 Me fills "any unmade choices" at random when it runs | [G] p. 168 |
| **Context-sensitive pruning** | Pruning whose choices depend on context elsewhere in the structure; introduced for large model bases | [Z] Zeigler, Luh & Kim 1990, 1991 |
| **Pragmatic frame** | In SES-as-ontology work, a consumer's information need that drives pruning and extraction | [Z] Lee & Zeigler 2010 |

## 3. Model base and workflow

| Term | Meaning | Source |
|---|---|---|
| **Model base (MB)** | The behavioral store of modular, port-based models: atomic and coupled models, plus experimental-frame components. Called the "behavioral knowledge base" | [Z] Kim 1988; Zeigler 1986, pp. 133–135; Zeigler, Hu & Rozenblit 1989, p. 85 |
| **Entity structure base (ENBASE)** | The structural store holding SES and PES files. Called the "structural knowledge base" | [Z] Kim 1988; Rozenblit, Kim & Zeigler 1988, Fig. 3, p. 228 |
| **MBASE** | DEVS-Scheme's model-base store of model-definition and state-variable files | [Z] Rozenblit, Kim & Zeigler 1988, Fig. 3, p. 228 |
| **SES/MB framework** | The originators' own name for the paired SES and model base | [Z] Zeigler, Luh & Kim 1990 |
| **Partitioned entity structure base** | An entity structure base split into parts to keep large bases coherent | [Z] Zeigler, Luh & Kim 1990, 1991 |
| **transform** | The operation that searches the model base for the components named in a pruned structure and couples them bottom-up. It stops at any entity that already has a model, looking "first in working memory, then in model definition files, and finally, provided that the entity is a leaf, in pruned-entity structure files" | [Z] Rozenblit, Kim & Zeigler 1988, pp. 226, 228 |
| **inv-transform** | Named alongside `transform` in DEVS-Scheme; its semantics are not described in the accessible text | [Z] Rozenblit, Kim & Zeigler 1988, Fig. 3, p. 228 |
| **retrieve / store** | DEVS-Scheme operations that load and save models in the model base (with `save-state`) | [Z] Rozenblit, Kim & Zeigler 1988, Fig. 3, p. 228 |
| **make-entstr, add-item, prune, save-entstr, load-entstr** | ESP-Scheme operations on entity structures | [Z] Rozenblit, Kim & Zeigler 1988, Fig. 3, p. 228 |
| **Closure under coupling** | A coupled DEVS model is itself a DEVS model with "the same interface properties as the components," so hierarchical synthesis from a PES yields valid models | [Z] Zeigler, Hu & Rozenblit 1989, pp. 84–85 |
| **Multifacetted modelling** | "an approach to simulation modelling that recognizes the irreducible complexity of reality, while affirming that useful partial models can be constructed in the service of limited decision making objectives," supported by "an organized base of models" | [Z] Zeigler & Ören 1986, WSC, pp. 708–712; Zeigler 1984 (book) |
| **Experimental frame (EF)** | A specification of the conditions under which a model is observed. In the MB it is realized "as models of special kinds: generators, transducers, acceptors" | [Z] Zeigler, Hu & Rozenblit 1989, p. 85 |
| **Frame base** | A repository of basic frame components from which experimental frames are generated | [Z] Rozenblit, Kim & Zeigler 1988 |
| **EFSUT** | Experimental Frame / System Under Test methodology (Zeigler 2026). The published abstract does not mention the SES; SES pruning appears only in an unpublished draft section | [Z] Zeigler 2026, *Systems* 14(6):691; [F] local draft in `docs/efsut/` |

## 4. Implementations and lineage

| Term | Meaning | Source |
|---|---|---|
| **ESP / ESP-4** | Entity Structuring Program; ESP-4 was "a PASCAL system available for VAX under VMS" (entity structurer and pruner). ESP dates to about 1980 (that reference is UNVERIFIED) | [Z] Zeigler 1986, p. 137; Rozenblit & Huang 1987, p. 605 |
| **ESP-Scheme** | Scheme realization of the SES that "acts as a model base management system in DEVS-Scheme": structuring, pruning, constraint-driven pruning | [Z] Kim, Lee, Christensen & Zeigler 1990 |
| **DEVS-Scheme** | Object-oriented DEVS environment in Scheme. Its four layers are Lisp objects, system specification, SES/MB, and systems design | [Z] Zeigler 1987; Zeigler, Hu & Rozenblit 1989 |
| **MODSYN** | Rule-based pruning shell written in Turbo Prolog | [Z] Rozenblit, Kim & Zeigler 1988, Fig. 1, p. 227 |
| **FRASES** | Frame-and-rule SES design environment used in DEVS-Scheme's systems-design layer: goal-driven pruning, automatic frame generation, multi-criteria ranking | [Z] Zeigler, Hu & Rozenblit 1989, pp. 86–89 |
| **SESBuilder** | Java/XML tool with "natural language input for an SES definition and XML instance generation of its PES" | [Z] Cheon, Kim & Zeigler 2008, IRI |
| **MS4 Me** | MS4 Systems' modeling environment with controlled-English `.ses`/`.pes` files and DEVS natural-language (`.dnl`) behavior | [G] pp. 160, 164–178; see `ms4me-facts.md` |
| **Model Store** | MS4 Systems' cloud store for sharing model suites | [Z] Zeigler, Seo & Kim 2013 |
| **SES merging** | Combining SESs to build suites of models | [Z] Zeigler, Seo & Kim 2013 |
| **RASES** | Relational Algebraic SES: the SES as relations, with SES operations in relational algebra | [Z] Park, Lee & Kim 1994; 1996 journal version |
| **CoSMoS** | Arizona State University's visual model-family tool; replaced SESM and generates DEVS-Suite code | [T] Sarjoughian & Elamvazhuthi 2009 |

## 5. Third-party extensions

| Term | Meaning | Source |
|---|---|---|
| **eSES / eSES/MB** | Hochschule Wismar's extended SES, based on Zeigler & Hammonds (2007), adding the elements below and an experiment-control loop | [T] Pawletta, Schmidt, Durak & Zeigler 2018, SNE 28(1); Pawletta et al. 2016 |
| **SES variable** | A global eSES input variable whose values steer pruning | [T] SNE 28(1); Folkerts et al. 2017, SNE 27(4) |
| **Semantic condition** | A condition restricting the values of SES variables | [T] SNE 27(4); SNE 28(1) |
| **SES function (SESFunction)** | Procedural code evaluated during pruning that computes couplings, attributes or iterators. It extends "the declarative specification defined by the baseline SES by procedural knowledge descriptions" | [T] SNE 28(1); Pawletta et al. 2016 |
| **Aspect rule** | eSES rule choosing among sibling aspects or multi-aspects on one level | [T] SNE 27(4) |
| **numRep** | eSES multi-aspect attribute giving the number of replications | [T] SNE 27(4); SNE 33(1) |
| **NONE element** | eSES option for optional parts: "if the NONE branch is selected, the entity is not included at all" | [T] SNE 27(4), p. 170 |
| **FPES** | Flattened PES; eSES reduces a PES's hierarchy before model building | [T] SNE 27(4); SNE 28(1), p. 17 |
| **SESTools (SESEditor / PESEditor)** | DLR / TU Clausthal Java tools. The SES is an XML instance of a fixed `ses.xsd` meta-schema with XPath constraints and no coupling element | [T] Karmokar et al. 2019; SESTools repository |
| **ODME** | Operational-design-domain toolkit, "a successor to the SES tools": SES domain model, pruning into scenarios, execution, feedback | [T] Naja, Gupta, Durak & Hartmann 2024, SNE 34(1) |
| **MetaSES** | Zeigler's "abstraction of the SES ... that supports the construction of complex systems of systems with multiple components belonging to specified classes," linking the SES to the hierarchy of system specifications | [Z] Zeigler 2023, *Information* 14(1):22 |
| **SoSES/FB** | System of Systems Entity Structure / Federate Base, a federation-oriented extension for HLA/RTI | [T] B. S. Kim, Choi & T. G. Kim 2013 |
| **SES-X** | SES/MB combined with the X language (not SysML), with "two levels of model pruning" | [T] Xie et al. 2023, *Information* 14(1):23 |

## 6. MS4 Me terms

| Term | Meaning | Source |
|---|---|---|
| **Perspective** | The aspect label in MS4 Me text: `from the X perspective, Y is made of ...!` | [G] p. 174 |
| **Restructuring** | `restructure multiaspects using LABEL!`: expands multi-aspects into copies under the grandparent's aspect and removes the multi-aspect. Different labels are listed bottom-up with `then` | [G] pp. 121, 126 |
| **Index specialization** | A specialization on the multi-entity, such as `Person can be id in index!`, whose option names the copies. Reusing `index` for every multi-aspect is "permissible, and desirable" | [G] pp. 121, 126 |
| **Copy name** | The generated name of a copy: option + index + `_` + unit (`id0_Person`), or option + `_` + unit when the label has several options (`tall_Boy`) | [G] pp. 121, 124 |
| **set multiplicity** | `set multiplicity of LABEL as [N] for UNIT!`: number of copies; nested multi-aspects share one count | [G] pp. 121, 127 |
| **select ... from ... for** | `select OPT from LABEL for ENTITY!`: resolves a specialization; on copies after restructuring | [G] pp. 122, 157–158 |
| **inherit from** | `inherit from OPT!`: without it "the selected models have empty behaviors" | [G] pp. 27, 158 |
| **mergeAll** | `mergeAll from X.ses!` appears after `pruned from` in two guide examples; the guide does not explain it | [G] pp. 27, 129 |
| **don't select** | `don't select OPT under ENTITY!`: excludes an option for every copy | [G] p. 129 |
| **write class files** | Final PES directive that generates the Java classes | [G] pp. 128, 158 |
| **each / all** | Coupling quantifiers over multi-aspect copies. `each`–`each` matches copies by pruned-name prefix; all-to-all adds no self-couplings | [G] pp. 123–124, 176–177 |

## 7. Axioms

| Term | Meaning | Source |
|---|---|---|
| **Uniformity** | "any two nodes with the same labels have isomorphic subtrees" | [Z] Traoré et al. 2019 |
| **Strict hierarchy** | "prohibits a label from appearing more than once down any path of the tree"; relaxed for recursion in 1993 | [Z] Traoré et al. 2019; Rozenblit & Zeigler 1993, p. 607 |
| **Alternating mode** | "if a node is an Entity, then the successor is either Aspect or Specialization, and vice versa" | [Z] Traoré et al. 2019; Rozenblit & Zeigler 1985, p. 225 |
| **Valid brothers / valid siblings** | "forbids having two brothers with the same label" | [Z] Traoré et al. 2019; Rozenblit & Zeigler 1993, p. 606 |
| **Attached variables** | "variable types attached to the same item shall have distinct names" | [Z] Traoré et al. 2019 |
| **Inheritance** | "Specialization inherits all variables and Aspects from the parent Entity to the children Entities" | [Z] Traoré et al. 2019; Rozenblit & Huang 1987, p. 604 |

## 8. Not to confuse

| Term | Note |
|---|---|
| **ParaDEVS** | Paratemporal DEVS, a tree-expansion method for fast stochastic simulation. Unrelated to the SES; the ParaDEVS papers do not discuss it [Z] (Zeigler 2024, *Systems* 12(3):80). Distinct from Parallel DEVS. |
| **AND/OR tree** | A common informal reading of the SES (aspect = AND, specialization = OR). No examined Zeigler text uses the phrase [I]. |
| **"Select-if" rule, "synchronization of specializations"** | Phrases not found in any examined Zeigler-group text. Use "selection rule" and "selection constraint" instead [Z] (Rozenblit & Huang 1987). |
