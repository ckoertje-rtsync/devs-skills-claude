# SES/MB Source Notes: Pitfalls and Inconsistencies

Known defects in the sources behind this skill: archive and DOI record errors, conflicting titles and author lists, a draft manuscript with internal contradictions, claims that look plausible but have no source, guide oddities, and the limits of what could be read. They are recorded so that answers and new documents do not repeat them, and so that nobody "corrects" a source silently. When one of them matters to an answer, say so. Current as of 2026-10-06.

Provenance tags: [Z] Zeigler or coauthors, read directly; [T] third-party literature; [G] MS4 Me User's Guide (page); [F] field observation from this repo's practice; [I] inference no source states.

## 1. Archive and DOI record problems

| Problem | Detail | Do this |
|---|---|---|
| WSC'85 archive mislink | The INFORMS-SIM WSC'85 index links Rozenblit & Zeigler, "Concepts for knowledge-based system design environments," to `1985_0047.pdf`, which actually holds Venkatraman & Wilson, "Using Path Control Variates in Activity Network Simulation" (pp. 217–222). The SES paper is `1985_0048.pdf` (cover page checked). The archived copy has only pp. 223–228, so its reference list ([1], [17], [18]) is missing. | Link [1985_0048.pdf](https://informs-sim.org/wsc85papers/1985_0048.pdf). Do not guess what [1] and [18] are beyond "Belogus" and "Zeigler", which the definition names. |
| Two DOIs for one paper | Rozenblit & Zeigler 1993 (WSC) is deposited as 10.1109/wsc.1993.718106 and as 10.1145/256563.256742. Rozenblit, Hu, Kim & Zeigler 1990 (JORS, KBDSE) resolves as 10.1038/sj/jors/0410602 and via JSTOR as 10.2307/2583032. | Either works; prefer the first, and do not treat them as two papers. |
| Duplicate deposit | Zeigler 1987, *SIMULATION* 49(5), has a duplicate Crossref deposit 10.1177/003754978904900506 with identical metadata. | Cite 10.1177/003754978704900506. |
| Online-first vs issue dates | Kim, Seo & Zeigler: Crossref issued 2009-11-12, issue vol. 86 (2010). MetaSES: online 29 Dec 2022, issue *Information* 14(1), 2023. TMS 3rd ed.: commonly dated 2018, Crossref dates the chapters 2019. | State which date you use; "2022/2023" for MetaSES is acceptable. |
| Toolbox README errors | The SES_Tbx_Matlab README misprints the SNE 24(2) DOI as `...tn.102241` (correct: 10.11128/sne.24.tn.10241), and cites the IFAC-PapersOnLine 48(1) paper as ARGESIM Report 44, pp. 369–370. | Use the Crossref records in `references.md`. |

## 2. Index and venue errors

- **OpenAlex mislabels IJMSSC.** OpenAlex labels DOIs `10.1142/S17939623…` as *Advances in Complex Systems*. Crossref says they belong to the *International Journal of Modeling, Simulation, and Scientific Computing* (IJMSSC). This affects Zeigler, Seo & D. Kim 2013 (IJMSSC 4(3):1340006) and Zeigler 2022, "DEVS and MBSE: A review" (IJMSSC 13(2):2230001) [T]. Use Crossref.
- **Zeigler & Hammonds 2007 publisher.** One research note marks the publisher UNVERIFIED (CiNii record only); two others verified Elsevier/Academic Press from the publisher page and an ISBN catalog record. Treat it as verified: Elsevier/Academic Press, ISBN 978-0-12-372515-8, 448 pp.
- **The guide cites Zeigler & Hammonds with a slightly garbled title** ("Modeling Simulation-Based Data Engineering", p. 121, footnote 11) [G]. Use the publisher's title.

## 3. Conflicting titles and author lists

| Work | Conflict | Status |
|---|---|---|
| SpringSim 2020 Mod4Sim paper, [DOI 10.22360/springsim.2020.mod4sim.001](https://doi.org/10.22360/springsim.2020.mod4sim.001) | Crossref title: "Automated Generation of Simulation Models Using Automated, Reactive Pruning of System Entity Structures", with no authors. The SESToPy README: "Automated, Reactive Pruning of System Entity Structures for Simulation Engineering" by Folkerts, Pawletta, Deatcu, Zeigler. The ACM landing page was blocked. | Cite by DOI and Crossref title; mark the authors UNVERIFIED. |
| Python eSES/MB framework paper (Folkerts, Pawletta, Deatcu, Hartmann 2019) | Crossref has *SNE* 29(4):207–215 and a same-titled IIPhDW 2019 paper (pp. 121–126). The SESToPy README cites ASIM Workshop 2019, ARGESIM Report 57, pp. 69–75. | All may exist. Cite the SNE journal version. |
| "Design Patterns for Variability Modeling using SES Ontology", Mod4Sim 2018 | Crossref gives title and venue only; authors come from the SESTools README. | Authors UNVERIFIED. |
| Belogus 1985 dissertation | The Mathematics Genealogy record gives "Methodology and Software Tools for Modeling and Simulating Multifaceted Systems, With Applications to Microprocessor Digital Control and Aerospace Systems". Zeigler-group reference lists give "Multifacetted Modelling and Simulation: A Software Engineering Implementation". | Use the genealogy-record title; flag the other as UNVERIFIED. |
| Cheon, Kim & Zeigler 2008 | **Two different papers.** "DEVS Model Composition by System Entity Structure" (IEEE IRI 2008, Crossref-verified DOI) and "System Entity Structure for XML Meta Data Modeling; Application to the US Climate Normals" (SEDE 2008, OpenAlex only, no DOI). The SES-as-schema / PES-as-instance quote comes from the SEDE paper. | Do not merge them. The deep-research report's "2008 SEDE paper (an OpenAlex record only, no DOI)" means the SEDE paper. |
| Folkerts & Pawletta 2017 German workshop paper | Title page read; the venue (AUTSYM 2017) is inferred from the URL. | Venue UNVERIFIED. |

## 4. The EFSUT draft manuscript

The local file `docs/efsut/Three Complementary Uses of the EFSUT Methodology.docx` is an unpublished manuscript by Zeigler marked "Submitted to: Digital Engineering and Digital Twin (DEDT)" [Z]. Its pitfalls:

- **The main text never mentions the SES.** It speaks of model families aligned with an experimental-frame partial order and an "applicability relation".
- **An appended second draft does use the SES** ("EFSUT-Driven DEVS Modeling for Processor–Device Digital Twins"): "A pruned Entity Structure (SES) formalizes these relationships, yielding ten Entity–Function/Model (EF/Model) pairings", and "SES pruning ensures minimality... Pruned SES ensures no over-modeling."
- **That appended draft expands EFSUT as "Entities, Functions, Structure, Usage, Time"**, which contradicts the main paper and the published version, *Systems* 14(6):691 (2026), where EFSUT is Experimental Frame–System Under Test.
- It also calls a *pruned* structure an "SES" and offers no proof or evaluation for the minimality claim.
- The published EFSUT paper's abstract does not mention the SES; its full text was not readable.

Do this: cite the SES-pairing idea only as an unpublished draft idea, never as a result, and never repeat the "Entities, Functions, ..." expansion. If an EFSUT question depends on the SES, say that the published paper's SES content is unknown.

## 5. Claims not to make

Each of these looks plausible, and none has a source in the material behind this skill:

| Do not claim | Why |
|---|---|
| DEVSJAVA or DEVS-C++ had SES or model-base features | No primary source found. The DEVS-C++ HICSS-29 abstract mentions neither the SES nor the model base. The SES lineage runs ESP → ESP-4 → DEVS-Scheme/ESP-Scheme → FRASES/MODSYN → SESBuilder → MS4 Me. |
| "The SES is more expressive than XML Schema" | The Zeigler group describes a **mapping** (SES to schema or DTD, PES to instance documents), not an expressiveness ranking. SES structures are "compact relative to equivalent Schema" (eUDEVS 2009) is the strongest statement found. |
| Zeigler describes the SES as an "AND/OR tree" or a generative grammar | No Zeigler text examined uses AND/OR-tree language. The 1993 paper calls the SES "generative", but the grammar analogy is an inference. Label both [I]. |
| "Select-if" rules or "synchronization of specializations" are Zeigler terms | Found in no Zeigler source; the phrases came from a research prompt. Selection constraints attached to specializations (Rozenblit & Huang 1987) and rule-based pruning are documented. |
| A "double arrow" drawing convention | No source. The 1980s figures draw aspects as a single line, specializations as a double vertical line, and multiple aspects as a triple vertical line; only the triple line is named in the text ([Z] Zeigler 1986, p. 136). |
| The 1984 book is the first appearance of the SES | Entity-structuring software existed by about 1980 (cited reference UNVERIFIED), and the first open formal definition (1985) is credited to Belogus and Zeigler. Call 1984 the canonical book statement. |
| TMS (2000 or 2018) has an SES chapter | No chapter title names the SES; its content was not read. |
| The MS4 Me guide states the SES axioms | It does not mention them anywhere (full-text search). The axioms come from the WSC papers and Traoré et al. 2019. |
| The SES is an OWL ontology | Zeigler and the Wismar group use "ontology" in a non-OWL sense. The only verified OWL link is Djitog & Kah 2020, which maps SES entities to OWL classes. An arXiv "SES ontology" paper is about socio-ecological systems. |
| A `pyses` package, SES support in VLE, or working selection constraints in SESToPy | None found; SESToPy's README says "selections constraints in pruning ignored". |
| A published SES application to DoD mission engineering | None found in the open literature. |
| What `inv-transform` does | The DEVS-Scheme function is named (1988) but its semantics are not described in any text read. |

## 6. Inaccessible sources and what that means for confidence

**Not read in full:** Zeigler 1984 (*Multifacetted Modelling and Discrete Event Simulation*), Zeigler 1990 (*Object-Oriented Simulation with Hierarchical, Modular Models*), Zeigler & Hammonds 2007, the Guide chapters (2013/2017), TMS 2nd and 3rd editions, the 1991 TOMACS paper, Kim et al. 1990 (IEEE SMC), the KBDSE JORS paper, Kim 1988 (abstract only), the Karmokar et al. 2019 standard-representation paper, and most MDPI papers (MetaSES, SES-X, IoCT, MBSE with/out Simulation, EFSUT) beyond their abstracts. Korean- and Chinese-language SES literature was only partly reachable.

**What could be read:** the open WSC papers of 1985, 1986, 1987, 1988, 1989, and 1993; the Traoré et al. 2019 accepted manuscript; the MS4 Me User's Guide; and several SNE papers, tool READMEs, and AIAA papers.

**Consequences:**

- **High confidence** in the definitional core (labeled tree with attached variable types; entity, aspect, specialization, multi-aspect; pruning; the six axioms as stated in 2019 and named in 1993). It is stable across every source read, 1985–2019.
- **Unknown**: Zeigler's own wording and page numbers for the axioms in the 1984, 1990, 2007, and Guide books; the set-theoretic characterization credited to Zeigler & Hammonds 2007; the exact SES-to-XML-Schema mapping rules (Zeigler & Hammonds Ch. 3); what the 1991 TOMACS paper says about model-base management beyond its conference abstract.
- **Do not cite page numbers in the books.** Book claims here rest on chapter titles and publisher tables of contents.
- **Application outcomes** for MDPI papers are abstract-level; do not add details the abstract does not state.

## 7. MS4 Me User's Guide oddities

- **Undated.** No year on the cover; the PDF metadata creation date is 2021-10-19. PDF page numbers equal printed page numbers [G].
- **Space before `!`.** Guide examples usually print `write class files !`; the canonical form used by `ses-designer` has no space [G].
- **`short_Boy` vs `shortish`.** p. 124 couples `short_Boy`, but the DatingGame SES on p. 123 lists the options `shortish, medium, or tall`; the copy would be `shortish_Boy` [G].
- **Missing PES files.** `DatingGame.pes` is not printed; the World example's PES is given only as fragments (pp. 121–122). Any full PES for them is assembled, not quoted [G].
- **`inherit from` is explained on p. 27**; p. 26 is a screenshot of the pruning view only [G].
- **Unexplained directive.** `mergeAll from X.ses!` appears in examples (pp. 27, 129) with no explanation [G].
- **Undocumented aspect choice.** Pruning must pick one aspect of a multi-aspect entity (p. 168), but no PES text directive for it is documented [G].
- **"Similarities"** (a truck is like a car) are listed as an SES construct (p. 173) with no syntax given [G].
- **Trailing-`s` names.** The guide's examples use plural collective names (`Processors`, `People`, `Boys`, `Enclaves`), and it states no naming rule. This repo has seen MS4 Me fail intermittently on such names [F]. Both facts stand; the cautious `...Group` naming costs nothing.

## 8. Superseded content in the source copies

- `../source/report.md`, section "This repo's ses-designer references diverge from the sources in thirteen places", describes the `ses-designer` skill **before** commit `fa91953`, which fixed those discrepancies (axioms, coupling scope, PES grammar, examples). Do not report them as current defects.
- The research notes in `../source/notes/` refer to "the report writer" and "other researchers"; those were roles in the original research task, not sources.
