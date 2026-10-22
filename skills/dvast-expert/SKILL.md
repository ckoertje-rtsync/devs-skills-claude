---
name: "dvast-expert"
description: "Expert on DVAST (DEVS-based Virtual As Is Simulation Test Platform), RTSync and ORNL's MDA STTR MDA20-T002 second Phase II for non-real-time simulation of unmodified operational software. Grounded in the accepted Technical Volume (proposal B2S-0089) and the quarterly status reports (QSR #1 to #3). Holds program facts, a sourced glossary, the sequential DEVS simulation protocol, the DVAST kernel module vs KVM decision, the BCM-to-VCM homomorphism, the revised four-task plan (T1 BOA/DVAST demo, T2 verification and documentation, T3 kernel module, T4 HPC support) with schedule, deliverables and dated progress, the original proposal plan with a crosswalk, commercialization, and known source inconsistencies. Use when answering questions about DVAST, TESLA, BOA, MDST, the DVAST kernel module, TSC virtualization, as-is software simulation, or the project's tasks, schedule, deliverables and progress, or when drafting DVAST status reports, design documents or briefings."
---

# DVAST Expert

## What This Skill Does

Answers questions about the **DVAST** project and supports writing about it. Answers are grounded in the accepted second Phase II Technical Volume and, for the post-award plan and progress, in the quarterly status reports (QSR #1–#3). Curated reference files cover each topic, and a full-text copy of the proposal (with figures) supports verbatim quotes. The status reports are Distribution B, so the skill keeps only curated facts from them, with no full text. The skill deliberately holds **no personnel information**.

## Quick Facts

| | |
|---|---|
| **Title** | DEVS-based Virtual As Is Simulation Test Platform (DVAST) |
| **Program** | MDA STTR **MDA20-T002**, *Non-Real-Time Hardware Assisted Computer System Simulation*, **second Phase II**, proposal **B2S-0089** |
| **Team** | RTSync Corp (prime) and Oak Ridge National Laboratory (Research Institution, more than 30% of the work) |
| **Contract** | HQ0860-25-C-E058. Period of performance Sep 29, 2025 – Sep 28, 2027 (plan). |
| **Goal** | Run unmodified ("as is") tactical software on a virtual core whose execution and TSC follow **DEVS simulation time**, replacing the QEMU-based **TESLA** |
| **Current plan (revised after award)** | **T1** Demonstrate BOA/DVAST: TCP/UDP interface, MDST interface, NUMA and GPU hardware models, virtualize BOA and verify it in **MDST**. **T2** Verification and Documentation: regression test suite, API documentation. **T3** Complete the DVAST kernel module: page tables and multicore, exit conditions, MSI and IRQ. **T4** HPC support: modern C++ core, optimization, multi-threading. |
| **As proposed (superseded)** | Task 1: kernel module, TESLA integration and BOA/DVAST in MDST (TRL 8). Task 2: high performance DVAST via **HLT** and parallel DEVS (TRL 4). |
| **Status as of Jun 2026 (QSR #3)** | Complete: TCP/UDP, the NUMA/GPU models, HLT, and a first draft of the API documentation. Kernel module: not started. BOA demonstration: "looking for a new demonstration target". |
| **Claimed benefits** | At least 10x lower maintenance cost, 5x timing precision relative to QEMU TESLA, a virtual TSC that reports simulation time, and simulation of computers up to 2x the host |
| **Duration** | 24 months. Deliverables fall at +3 to +24 months after award. The revised list adds API documentation at +12 and drops three reports. |

**DVAST in one sentence:** a DEVS coordinator gets `(ta, out)` from the other models, sends `(ta, in)` to the as-is simulator, and that simulator runs the guest vCPU until the guest either emits an output or reaches `ta`. This is the *sequential* protocol. The as-is side "does not have to remember the state of its model" as it runs forward.

## Where to Look

Paths are relative to this skill's base directory. Read only the files the question needs.

| Question is about… | Read |
|--------------------|------|
| Contract facts, team organizations, stakeholders, reporting, prior contracts | `resources/reference/program.md` |
| An acronym or term (with provenance tags [G]/[T]/[B]/[S]) | `resources/reference/glossary.md` |
| How DVAST works: protocol, platform, KVM decision, computer-system model, homomorphism, TCP timing, HLT and parallel speedup | `resources/reference/architecture.md` |
| What Phase I and the First Phase II did | `resources/reference/history.md` |
| **Current tasks, subtasks, schedule, deliverables, progress, open issues** (revised plan) | `resources/reference/revised-plan.md` |
| The plan **as proposed**: Table 1 and 2, TRL targets, dependencies | `resources/reference/work-plan.md` |
| Market, forecast, customers, competitors, patents | `resources/reference/commercialization.md` |
| Bibliography | `resources/reference/references.md` |
| Typos, numbering conflicts, missing facts, carried-over text, in the proposal (§A–G) or the status reports (§H) | `resources/reference/source-notes.md` |
| Exact wording, or anything not covered above | `resources/source/tech-volume.md` (use grep, then read around the hit) |
| The original figures | `resources/source/figures/fig1-…fig7-*.png` |

Example of a full-text lookup:
```bash
grep -n -i "time stamp counter\|TSC" resources/source/tech-volume.md
```

## Answering Rules

1. **Cite locators.**
   - For the proposal: section numbers (1)–(10) and (12), Figure 1–7, Table 1–4, and Appendix 1–2. The page breaks in the .docx are unreliable, so do not cite page numbers.
   - For the status reports: the report and its section or slide title, for example "QSR #3, App. C, 'Schedule of major events'".
2. **Separate sources.** The glossary tags each term as [G] proposal glossary, [T] proposal text, [S] status reports, or [B] general background. Keep proposal claims, post-award reports and background apart. Present [B] content as background.
3. **Report numbers as claims with their basis**, for example "10x lower maintenance cost, per ORNL/TM-2021/1994 as cited in the proposal".
4. **Never fill in missing facts.** The contract value, the formal award date, the Phase I dates and the expansion of SWIC are **not stated** in any source. Calendar dates for the plan quarters and deliverables are **derived** from the period of performance (`revised-plan.md` §4 and §5). Say so, and ask the user if the answer depends on them.
5. **Use the revised schedule IDs** (T1–T4, 1.1–4.3) for current work, and write "proposal T1.1" when you mean a Table 1 ID. The reports' "Goal 1–4" numbering is in a different order, so convert it with `revised-plan.md` §3. Check `source-notes.md` before quoting task numbers, deliverable IDs or commercialization text.
6. **Keep the terminology straight.** Write "ParaDEVS" for the related paratemporal program (never "pDEVS"). "Parallel DEVS" (proposal T2.2, revised 4.3) is a different thing.
7. **Time-dependent items are snapshots.** Progress in `revised-plan.md` §6 stops at QSR #3 (June 2026). The patent planned "by June 2026" is a statement of intent in the proposal. For anything later, ask the user.
8. **No personnel information.** The skill deliberately records no names, roles or contacts. If a question needs them, point the user to the source documents.
9. **Mind the markings.** The status reports are Distribution B, and QSR #2 and #3 carry an export-control warning. Text drafted from `revised-plan.md` inherits those markings. Do not send it to external services.

## Writing DVAST Content

When drafting text (status report sections, design-document prose, briefings):
- Pull current tasks, status and open issues from `revised-plan.md`, the original goals and success criteria from `work-plan.md`, the rationale from `architecture.md`, and the lineage from `history.md`.
- For a new quarterly status report, report progress against the revised schedule IDs. Use the deck's status words, and flag any text carried over from earlier reports (source-notes §H).
- Describe the product as **DVAST / the DVAST kernel module**, not as a "KVM platform". See source-notes §E.
- For diagrams, use Mermaid in UML style: `sequenceDiagram` for the protocol, `flowchart` for the platform, `classDiagram` for DEVS structure, `stateDiagram-v2` for core states. Validate every Mermaid block before delivering it:
  ```bash
  resources/scripts/validate-mermaid.sh <file.md>
  ```
  This needs `node` and `npx`, and it installs `@mermaid-js/mermaid-cli` on first use. In labels, use ASCII only, no `;`, and no `:=`.

## Related Skills

- `closure-devs-atomic`, `closure-devs-coupled` and `devs-builder` build DEVS models, such as the device atomics of the computer-system model.
- `ef-builder` and `generate-ef-sut` build experimental frames, such as the "desired EF" in which the BCM is valid, and support T2's EFSUT and inverse-model regression testing.
- `devs-model-outline` produces DEVS model specifications (atomic and coupled Markdown with Mermaid) of the kind T2's documentation work describes.
- `paradevs-model-builder` builds ParaDEVS models for the sibling program MDA22-T001.

## Portability

The skill needs only stock tools (Read, Grep, Bash). Mermaid validation additionally needs Node. Nothing depends on MCP servers or AgentDB.
