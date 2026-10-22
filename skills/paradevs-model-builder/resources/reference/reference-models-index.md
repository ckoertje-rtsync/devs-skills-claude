# Reference ParaDEVS models — index

Four reference implementations live at `~/DEVS/paradevs/Closure-DEVS-paradevs-plugin/test/Models/java/reference/`. Use them as the templates when generating a new domain; they embody the canonical patterns.

## Paths

```
~/DEVS/paradevs/Closure-DEVS-paradevs-plugin/test/Models/java/reference/
├── paradevs/                 # Volume (simplest canonical)
├── AirBattle/                # Engagement (multi-field state, multiple setBranch overloads)
├── RPO/                      # Phase-based model (5 sequential phases)
└── treeexpander/             # Safety (evidence-scored, episode-aware)
```

## Quick reference

### 1. Volume (`paradevs/`)

**Idea.** Estimate the volume of an n-sphere via granule discretization of each coordinate. No Monte Carlo; the tree enumerates all granule combinations with early-elimination on the sphere constraint.

| File | Lines | Role |
|------|-------|------|
| `ParaDEVSNodeForVolume.java` | 40 | Node with `partialSumSq` accumulator and `setBranchDim(dim, rep, prob)`. |
| `ParaDEVSTreeForVolume.java` | 247 | Driver with `makeTreeToDepth(54)`, `advanceLeafFront(73)`, `expandDimension(98)`, `equivalentTo(113)`, `equivalent(125)` (tolerance-based on `partialSumSq`), `computeVolume(132)`, `distanceDistribution(142)`, `main(207)`. |

**Equivalence flavor:** tolerance (`Math.abs(a - b) < 0.0001`).
**Generate-this-style when:** the model is one continuous accumulator over a fixed depth.

### 2. AirBattle / Engagement (`AirBattle/`)

**Idea.** Model an air-to-air engagement where each salvo branches on (shots, hits, sspk) combinations. Hybrid state: both discrete (engaged phase) and continuous (hit accumulator).

| File | Lines | Role |
|------|-------|------|
| `ParaDEVSNodeForEngagement.java` | 150 | Node with seven `setBranch` overloads (lines 32, 41, 51, 60, 71, 83, 95, 108, 122) for different branch semantics. |
| `RunEngagement.java` | 732 | Driver with three `makeTreeToDepth` overloads (126, 156, 192), two `AdvanceLeafFront` variants (225, 272), max/min specializations (277, 359), `equivalent(465)`, `splitByEngage(471)`, `main(727)`. |

**Equivalence flavor:** hybrid (tolerance on hits + exact on engagement flag).
**Generate-this-style when:** branching is on a combinatorial product of several fields and multiple `setBranch` shapes are natural.

### 3. RPO (`RPO/`)

**Idea.** Five-phase proximity scenario (DETECTION → TRACKING → CLASSIFICATION → PROXIMITY → RESPONSE). Each phase is expanded once; phase-specific branch helpers on the node.

| File | Lines | Role |
|------|-------|------|
| `ParaDEVSNodeForRPO.java` | 332 | Node with `setBranchDetection(97)`, `setBranchTracking(131)`, `setBranchClassification(170)`, `setBranchProximity(201)`, `setBranchResponse(239)`, and a generic `setBranch(274)`. |
| `RunRPO.java` | 698 | Driver with `makeTreeToDepth(52)`, `advanceLeafFront(86)` that dispatches on `phase`, `equivalent(357)`, `splitByClassification(367)`, `splitByResponse(382)`, `splitByTermination(397)`, `main(683)`. |
| `RPOModel.java` | 486 | Probability-computation helpers (separate from node / driver). |

**Equivalence flavor:** hybrid (exact on phase + classification + tolerance on threat probability).
**Generate-this-style when:** the scenario has a fixed phase sequence and each phase has distinct branch semantics.

### 4. Safety (`treeexpander/`)

**Idea.** Playground safety severity expansion with evidence scoring and episode-aware adaptive expansion. Integrates per-branch evidence computation via an `EvidenceScorer`.

| File | Lines | Role |
|------|-------|------|
| `ParaDevsNodeForSafety.java` | 84 | Node with `cueAssignments: Map`, `evidence`, `severity`, `setBranch(59)` deep-copies the assignment map. |
| `RunSafetyExpansion.java` | 213 | Driver with `expandEpisode(50)`, `AdvanceLeafFront(108)`, `equivalentTo(148)`, `equivalent(164)` (map-equals OR tolerance on evidence), `terminalsToLeaves(185)`, `computeNodeEvidence(205)`. |
| `ExpansionUtils.java` | 157 | `unobservedCues(54)`, `expandNode(77)`, `expandFullBFS(104)`, `expandSingleLayer(142)`. |
| `ExpansionContext.java` | 92 | Bundle for episode counter, probability model, scorer. |
| `LeafPMFEntry.java` | 81 | PMF entry for terminal aggregation. |
| `Severity_ParaDEVS.java` | 380 | **Atomic DEVS wrapper** around the expander (out of scope for this skill; shown here for reference). |

**Equivalence flavor:** exact on map (primary) with tolerance-on-evidence fallback.
**Generate-this-style when:** the model has evidence scoring, threshold classification, or episode-adaptive expansion.

## Picking a reference to emulate

The skill's Stage 1 DISCOVER classifies the described model into one of four buckets:

| Signal in description | Closest reference | Template family |
|-----------------------|-------------------|-----------------|
| Single continuous accumulator, fixed depth | Volume | non-evidence, tolerance equivalence |
| Multi-field branching, multiple `setBranch` shapes | Engagement | non-evidence, hybrid equivalence |
| Sequential phases, one per depth level | RPO | non-evidence, hybrid equivalence |
| Evidence accumulator + thresholds + labels | Safety + quantized helpers | **evidence-integrated**, histogram equivalence |
| Adaptive/replanning expansion | Safety | non-evidence (or evidence), episode-aware |

## Naming convention

- Node class: `NodeForX` or `ParaDevsNodeForX` (capitalization varies across references — Volume uses `ParaDEVSNodeForVolume`, Safety uses `ParaDevsNodeForSafety`). The skill defaults to **`ParaDevsNodeFor{Domain}`** (camelCase after `Devs`, matching the Safety reference). The generated node is self-contained; it does not extend the plugin's `ParaDevsNode`.
- Driver class: `TreeFor{Domain}` (like `ParaDEVSTreeForVolume`) or `Run{Domain}` (like `RunRPO`, `RunEngagement`, `RunSafetyExpansion`). The skill defaults to **`TreeFor{Domain}`** because the user explicitly referred to "a Tree class with the driver methods."

## Licensing / attribution

Reference sources are read-only for skill purposes. When generated code emulates a reference pattern, cite the reference file and line in the generated README so maintainers can trace idioms back to the source.
