---
name: "ses-mb-expert"
description: "Expert knowledge base on Zeigler's System Entity Structure / Model Base (SES/MB) methodology for DEVS modeling and simulation. Holds the formal theory (entities, aspects, specializations, multi-aspects, attached variables and constraints, the six axioms, pruning and the pruned entity structure), the SES/MB prune-and-synthesize workflow (entity structure base, model base, transform, closure under coupling, experimental frames, multifacetted modelling), the lineage from ESP and DEVS-Scheme to MS4 Me, the tool landscape (MS4 Me natural-language SES/PES, XML SES, Wismar extended SES/MB toolboxes, DLR SESTools/ODME, MetaSES), verified MS4 Me behaviors with guide page numbers, applications and Zeigler's recent work, open problems, a verified bibliography, known source pitfalls, and lessons for future skill development. Use when answering questions about SES, PES, pruning, model bases, multifacetted modelling, SES axioms, SES tools, or SES research, when drafting papers, proposals or briefings that discuss SES/MB, or when planning or extending SES-related skills. For generating MS4 Me .ses/.pes files, use ses-designer."
---

# SES/MB Expert

## What This Skill Does

Answers questions about the **System Entity Structure / Model Base (SES/MB)** methodology and supports writing and development work that depends on it. Curated reference files cover each topic, with every claim tagged by source and locator. Full-text copies of the October 2026 deep-research report and its four research notes support verbatim quotes. The skill holds knowledge only: to produce MS4 Me `.ses`/`.pes` files, use `ses-designer`. The skill deliberately holds **no personnel information**.

## Quick Facts

| | |
|---|---|
| **Subject** | Zeigler's System Entity Structure / Model Base (SES/MB) methodology for hierarchical, modular DEVS models |
| **SES** | A labeled tree of entities, aspects (decompositions), specializations (taxonomic variants), and multi-aspects (replication), with attached variables, couplings on aspects, and selection constraints on specializations. One SES describes a *family* of models. |
| **Model base (MB)** | A library of modular, port-based DEVS components (atomic, coupled, experimental-frame). Structure lives in a separate entity structure base. |
| **Workflow** | Specify the SES → **prune** it against objectives to a pruned entity structure (PES) → **transform**: retrieve components from the MB and couple them bottom-up → simulate in an experimental frame → evaluate and iterate |
| **Why synthesis is sound** | DEVS is closed under coupling, and components are modular, so every node of the composition tree is a valid DEVS model |
| **Six axioms** | Uniformity, strict hierarchy, alternating mode, valid brothers (valid siblings), attached variables, inheritance |
| **Origins** | Entity-structuring software by about 1980 (the citing reference is unverified); the book *Multifacetted Modelling and Discrete Event Simulation* (1984); the first open formal definition (WSC 1985) credits Belogus and Zeigler |
| **Tool lineage** | ESP-4 → DEVS-Scheme / ESP-Scheme (1987–1991) → SESBuilder (2008) → MS4 Me natural-language `.ses`/`.pes`. Third-party: Hochschule Wismar extended SES/MB toolboxes (MATLAB, Python), DLR / TU Clausthal SESTools and ODME |
| **Applications** | Clustered in about five organizations; most use SES as a design or configuration space pruned into variants. One study reports a hard benefit: up to 8% time and 23% cost savings |
| **Zeigler's current use** | One model-family tool among several: MetaSES (2023), IoT design (2025). The EFSUT pairing with SES appears only in an unpublished draft |
| **Main open problems** | No standard representation; coupling and multiplicity semantics differ by tool; automatic pruning of nested structures; variant explosion; scarce empirical evidence |
| **Evidence limits** | The core books (1984, 1990, 2007, the SoS Guide, TMS editions) were not readable in full. Formal content rests on open WSC papers (1985–1993) and a 2019 Zeigler-coauthored paper. |

**SES/MB in one sentence:** the SES declares every way a system *could* be built, pruning chooses one, and closure under coupling guarantees that the chosen structure, filled in from the model base, is itself a runnable DEVS model.

## Where to Look

Paths are relative to this skill's base directory. Read only the files the question needs.

| Question is about… | Read |
|--------------------|------|
| What an SES is: node types, attached variables and constraints, drawing conventions, pruning, the PES, ontology/XML/algebra readings, a worked example | `resources/reference/concepts.md` |
| The six axioms, how the list changed (1985, 1993, 2019), relaxations, common misstatements | `resources/reference/axioms.md` |
| The SES/MB workflow: entity structure base vs model base, prune and transform, why synthesis is sound, experimental frames, multifacetted modelling, reuse | `resources/reference/sesmb-workflow.md` |
| History, timeline, implementation lineage, how the framing evolved | `resources/reference/history.md` |
| Tools and representations: MS4 Me, XML SES, Wismar extended SES, SESTools/ODME, CoSMoS, SES-X, MetaSES, where tools disagree | `resources/reference/tools.md` |
| Exact MS4 Me behavior, with User's Guide page numbers | `resources/reference/ms4me-facts.md` |
| Applications, case studies, quantified outcomes, comparisons, Zeigler's recent work | `resources/reference/applications.md` |
| Limitations, critiques, evidence gaps, research opportunities | `resources/reference/open-problems.md` |
| A term or acronym | `resources/reference/glossary.md` |
| Bibliography and verification status of each citation | `resources/reference/references.md` |
| Pitfalls: archive mislinks, metadata errors, claims not to make, guide oddities, inaccessible sources | `resources/reference/source-notes.md` |
| Practitioner rules, research-method lessons, development guidance for SES skills, open opportunities | `resources/reference/lessons.md` |
| Exact wording or a quote with page locator not covered above | `resources/source/report.md`, then `resources/source/notes/*.md` (grep, then read around the hit) |

Example of a full-text lookup:
```bash
grep -n -i "minimal scoping\|scopes all the entities" resources/source/notes/*.md
```

## Answering Rules

1. **Cite locators.** Give the paper and page (for example "Rozenblit & Zeigler 1985, WSC, p. 225") or the MS4 Me User's Guide page. The guide's printed page numbers equal its PDF page numbers.
2. **Separate sources.** The reference files tag claims [Z] Zeigler or coauthors, read directly; [T] third-party literature; [G] MS4 Me User's Guide; [F] field observation from this repo's practice; [I] inference that no source states. Keep them apart in answers, and present [I] as inference.
3. **Keep three layers distinct:** SES *theory* (axioms, pruning semantics), *MS4 Me behavior* (what the tool accepts and does), and *ses-designer rules* (what this repo's validator enforces). They differ in places; for example, strict hierarchy is checked only on entity labels because MS4 Me reuses specialization labels such as `index` down a path.
4. **Check the bibliography before citing.** Use only entries marked verified in `references.md`, keep UNVERIFIED items flagged, and open the title page of any new source before citing it. Never infer a citation from a filename.
5. **Never fill in missing facts.** Where the core books could not be read, say so instead of guessing what they contain. Check `source-notes.md` for claims not to make (for example, SES features in DEVSJAVA, or "the SES is more expressive than XML Schema").
6. **Keep the terminology straight.** "Multifacetted" is the spelling in the 1984 book title. "Valid brothers" and "valid siblings" name the same axiom. "Multiple aspect" (1980s) and "multi-aspect" (later) are the same construct. Write "ParaDEVS" for paratemporal DEVS, never "pDEVS"; it is unrelated to SES and distinct from Parallel DEVS.
7. **Time-dependent items are snapshots.** Tool maintenance status and "recent work" are current as of 2026-10-06. For later developments, say so and ask the user or verify.
8. **No personnel information.** Name research clusters by organization. Author names appear only as citations. If a question needs more, point the user to the cited source.

## Writing SES Content

- To generate MS4 Me `.ses`/`.pes` files, hand off to `ses-designer`. Its reference files hold the operational grammar and validators. `ms4me-facts.md` here records the guide behaviors those rules rest on.
- For prose (papers, proposals, briefings): take definitions from `concepts.md` and `axioms.md`, the method from `sesmb-workflow.md`, lineage from `history.md`, evidence from `applications.md`, and caveats from `open-problems.md`. Report the 8% / 23% savings figure as a single study's result.
- For diagrams, use Mermaid in UML style: `classDiagram` with stereotypes (`<<entity>>`, `<<aspect>>`, `<<specialization>>`, `<<multi-aspect>>`) for SES trees, `flowchart` for the SES/MB loop, `timeline` for history. Validate every Mermaid block before delivering it:
  ```bash
  resources/scripts/validate-mermaid.sh <file.md>
  ```
  This needs `node` and `npx` and installs `@mermaid-js/mermaid-cli` on first use. In labels, use ASCII only, no `;`, and no `:=`.

## Extending This Knowledge Base

When new sources or findings arrive:

1. Verify the source (title page, DOI record, or publisher page) and add it to `references.md` with how it was verified.
2. Add the finding to the matching reference file with a provenance tag and locator. Put inconsistencies or traps in `source-notes.md`, and practical rules in `lessons.md`.
3. Leave `resources/source/` unchanged; it is a dated snapshot. Update the "Current as of" line in each file you touch.
4. Keep the skill free of personnel information and of hard dependencies on AgentDB or MCP tools.

## Related Skills

- `ses-designer` builds MS4 Me `.ses`/`.pes` files and validates them against the SES grammar, the axioms, and MS4 Me's coupling-scope rule.
- `devs-builder`, `closure-devs-atomic` and `closure-devs-coupled` build the DEVS models that populate a model base.
- `ef-builder` and `generate-ef-sut` build experimental frames, the other half of SES/MB experimentation.
- `devs-model-outline` produces DEVS model specifications that can feed SES design.

## Portability

The skill needs only stock tools (Read, Grep, Bash). Mermaid validation also needs Node. Nothing depends on MCP servers or AgentDB.
