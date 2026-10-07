# Lessons and Working Memory

This file is the skill's working memory. It records what this repo learned while researching SES/MB and fixing the `ses-designer` skill: practitioner rules for writing MS4 Me SES/PES, research-method lessons for citing the SES literature, guidance for building or extending SES-related skills, and open opportunities. Read it before planning new SES work. Current as of 2026-10-06.

Provenance tags: [Z] Zeigler or coauthors, read directly; [T] third-party literature; [G] MS4 Me User's Guide (page); [F] field observation from this repo's practice; [I] inference no source states.

## 1. Practitioner rules for MS4 Me SES/PES

| Rule | Why | Source |
|---|---|---|
| Name a multi-aspect's collective parent with a suffix (`UAVGroup`, `ProcessorPool`, `TargetSet`), not a trailing-`s` plural (`UAVs`) | MS4 Me's parser intermittently mishandles trailing-`s` plurals; the guide is silent and uses plurals itself | [F]; [G pp. 120–129] |
| Emit the canonical forms: no comma before `and`/`or`, no articles before names, no space before `!` | The guide's stated rule is no comma [G p. 174]. The articles and the space are legal in MS4 Me but rejected by `ses-designer`'s validators, which keep one form | [G pp. 174–176]; [F] |
| Follow every `select OPT from LABEL for E!` with `inherit from OPT!` | Without it "the selected models have empty behaviors" | [G pp. 27, 158] |
| Select on copies after restructuring (`id1_Vehicle`), never on the unit | Copies inherit the unit's sub-SES and are pruned independently | [G p. 122] |
| Write one `restructure multiaspects using ...!` line; with several labels, list them bottom-up joined by `then` | The guide requires bottom-up order for different labels | [G p. 126] |
| Reuse the label `index` for every multi-aspect | "Permissible, and desirable" | [G p. 126] |
| Resolve every specialization in the PES | Unmade choices are random at run time, which breaks reproducibility (e.g. for EFSUT studies) | [G pp. 122, 168]; [I] |
| Avoid more than one aspect per entity when the PES must be reproducible; model the alternatives as a specialization whose options each carry one aspect | No PES text directive for aspect choice is documented, so the choice falls to random selection | [G p. 168]; [I] |
| Never `set multiplicity` on a label used for `each`/`each` matching | The guide's DatingGame works only "provided you don't use height as the basis for identifying multi-entity copies" | [G p. 124] |
| Couple a multi-aspect unit in the aspect that contains its parent, never under the multi-aspect's own perspective | Restructuring hoists copies into the grandparent's aspect and removes the multi-aspect | [G pp. 121, 176] |
| An entity may carry aspects *and* specializations | Alternating mode constrains layers along a path, not the mix under one entity | [Z] (Traoré et al. 2019; Rozenblit & Zeigler 1985); [G pp. 121–122, 125, 129] |

## 2. Research-method lessons

1. **Guide pages.** The MS4 Me guide's printed page equals its PDF page. Extract text per page (`pdftotext -layout`, split on form feeds) and cite the printed number [F].
2. **Best open primary source for 1985–1993.** The INFORMS-SIM Winter Simulation Conference archive (`informs-sim.org/wscNNpapers/`) holds scanned full texts of the key Zeigler-group SES papers (1985, 1986, 1987, 1988, 1989, 1993) [F]. The core books cannot be read in full text online:
   - Zeigler 1984 and 1990: HathiTrust search-only.
   - Zeigler & Hammonds 2007, the *Guide* 2013/2017, and *Theory of Modeling and Simulation* 2000/2018: paywalled.

   Claims about them rest on tables of contents and secondary statements [F].
3. **Archive mislink.** The WSC'85 index links Rozenblit & Zeigler, "Concepts for knowledge-based system design environments," to `1985_0047.pdf`, which holds a different paper. The correct file is `1985_0048.pdf` (pp. 223–228 only; the reference list is missing) [F].
4. **Metadata errors.** OpenAlex misattributed two Zeigler papers to *Advances in Complex Systems*; Crossref gives *IJMSSC*. Prefer Crossref, and confirm on the publisher page or PDF title page [F].
5. **Title pages first.** Open the PDF title page before citing. Never infer authors, venue, or year from a filename or a reference list. Distinguish single-author origin papers from multi-author tutorials and extensions [F].
6. **Keep sources apart.** Separate what Zeigler and coauthors state from what later authors attribute to them. Examples:
   - The precise six-axiom wording comes from Traoré et al. 2019, which credits Zeigler & Sarjoughian 2017. Zeigler's own wording in the books was not read [Z/F].
   - "Select-if" rules, "synchronization of specializations," a "double arrow" drawing convention, and "AND/OR tree" language appear in no Zeigler text examined. Do not attribute them [F].
7. **Write UNVERIFIED when a record cannot be checked.** Do not drop the claim silently and do not upgrade it [F].
8. **Unpublished material is a draft.** The local EFSUT manuscript's appended section prunes an SES into EF/Model pairings and expands EFSUT inconsistently. Treat it as draft thinking, not a result [F].

## 3. Guidance for building SES-related skills

**How `ses-designer` encodes the theory** (commit `fa91953`):
- `scripts/validate-ses.sh` runs four passes: lexical, reserved-keyword, cross-reference, structural. The structural pass is POSIX awk over "fact" lines emitted by the bash pass [F].
- **Strict hierarchy is checked on entity labels only.** With name-keyed entities, a repeated label on a path equals a cycle in the parent-to-child graph. Specialization labels are exempt because the guide recommends reusing `index` for nested multi-aspects, which puts the same label on one path [F; G p. 126]. Rozenblit & Zeigler 1993 (p. 607) relaxed strict hierarchy for recursion [Z].
- **Uniformity and alternating mode hold by construction** in MS4 Me text. Inheritance is applied by MS4 Me during pruning. None of the three needs a check [F].
- **Copy-name resolution.** A name matching `^<option>[0-9]*_<unit>$`, where `<unit>` is a multi-aspect unit with that specialization option, resolves to the unit. So couplings like `all Person sends Bye to id0_Person!` validate [F; G pp. 121–124].
- **Scope check.** A coupling's endpoints must be the named aspect's parent, its children, or units of multi-aspects hanging from those children. This is the restructuring hoist [F; G pp. 121, 176].
- **Fixtures.** Test fixtures copied from the guide (World, DatingGame, MultiEnclaveNet, SimpleWorkFlow, Try, PhysicsModelPM1, ZooChimpsTest) plus one invalid file per check gave 37 expected outcomes, also under `gawk --posix` [F]. Rebuild a similar suite for any validator change.

**Known validator and knowledge gaps** [F]:
- `validate-pes.sh` does not read the `.ses`, so it cannot tell when a `select` targets a unit instead of its copies.
- Articles before names and a space before `!` are rejected even though MS4 Me accepts them. Normalize guide-style input first.
- No documented text directive chooses among an entity's aspects.
- The semantics of repeating `inherit from X!` when several copies select the same option are unknown. `ses-designer` emits it once per distinct option.
- MS4 Me gives every copy of a nested multi-aspect the same multiplicity [G p. 127].
- The syntax of `mergeAll` and of "Similarities" is not explained [G pp. 129, 173].

**Repo conventions for SES skills** [F]:
- **Portable:** stock tools only. AgentDB, ReasoningBank, and `mcp__claude-flow__*` may appear only as optional notes.
- **Design approval:** a design summary gets explicit user approval before files are generated.
- **Mermaid:** UML-style diagrams instead of ASCII art. ASCII-only labels, no `;`, no `:=`, and every block render-checked with `validate-mermaid.sh` (mmdc).
- **No personnel information:** name research clusters by organization; author names appear only in citations.
- **Terminology:** write "ParaDEVS," never "pDEVS."
- **Output paths:** inside this repo, skill outputs go under `test/<skill>/`. Elsewhere, use the MS4 Me `ses/` folder, Maven/Gradle source folders, or the current directory.
- **Commits:** never commit unless asked in the current message.

## 4. Opportunities for future development

All items here are inferences [I] unless tagged otherwise.

- **Constrained pruning as SAT/CSP.** Selection rules, constraints, and multiplicity bounds could be encoded for a solver to enumerate or sample valid PESs. Practitioners have used metaheuristics over SES-derived spaces instead (Hagendorf, Pawletta & Larek 2013, [doi:10.1177/0037549713500066](https://doi.org/10.1177/0037549713500066)) [T]. Zeigler's 2026 SAT/SMT model-seed synthesis (Alshareef & Zeigler 2026, *Logics*, [doi:10.3390/logics4030007](https://doi.org/10.3390/logics4030007)) does not touch the SES [Z].
- **Pruning-space metrics.** Count PESs as a function of SES size, measure coverage of the pruning space (cf. ODME's ODD scenario-coverage metric, Gupta, Sprockhoff & Durak 2025, [doi:10.2514/6.2025-2326](https://doi.org/10.2514/6.2025-2326) [T]), and label and prioritize large variant sets.
- **LLM proposes, validator enforces.** An LLM drafts the SES from a description while the axiom and scope validators guarantee well-formedness. This mirrors the "model seeds completed by formal synthesis" architecture. No published SES + LLM work was found.
- **SES-driven EF/Model pairing.** Prune an SES with experimental-frame and model aspects to enumerate EF/Model pairings for EFSUT studies (only in an unpublished draft). This would connect `ses-designer` with `ef-builder` and `generate-ef-sut`.
- **MetaSES support.** Class-based abstraction over many-component systems of systems (Zeigler 2023, *Information* 14(1):22, [doi:10.3390/info14010022](https://doi.org/10.3390/info14010022)) [Z]. No tool implements it yet.
- **Context-dependent multiplicity.** Per-copy multiplicity for nested multi-aspects, the limitation MS4 Me itself names [G p. 127]. eSES approaches it with SES variables and iterator functions [T].
- **PES validator cross-check.** Let `validate-pes.sh` read the sibling `.ses` to catch selects on units, unknown labels, and options that do not exist.

## 5. Provenance of this knowledge base

- **Deep research (2026-10-06).** Four parallel research passes (formal theory, SES/MB workflow, tools and extensions, applications and recent work) produced notes in which every citation was checked against a DOI record, publisher page, library catalog, or PDF title page. A writer then produced the report. Both are bundled under `../source/` with personnel details removed.
- **`ses-designer` fix (commit `fa91953`).** Corrected 13 discrepancies against primary sources and the MS4 Me guide (axioms, the coupling-scope exception, PES `select ... from ... for`, `inherit from`, `then`, single-child aspects, the comma rule) and added the structural validator described in §3.
- **Not reached.** Full texts of the core books, Zeigler & Hammonds ch. 3, the 2019 standard-representation paper, Korean- and Chinese-language SES literature, and MDPI full texts (abstracts only). Treat claims about them as limited to what `references.md` records.
