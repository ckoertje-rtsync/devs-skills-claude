# Pattern: episode-aware expansion (full-BFS vs single-layer)

**Purpose.** When the paratemporal expander is run repeatedly across "episodes" (simulation rounds that refine an estimate, or cue-observation rounds that extend a base tree), the expansion strategy changes with the episode index. RunSafetyExpansion codifies a two-rule scheme; other adaptive paratemporal models follow the same shape.

Cite: `RunSafetyExpansion.java:50–89`, `ExpansionUtils.java:77–157`.

## Two rules

| Episode | Rule | Behavior |
|---------|------|----------|
| `episode == 1` | **Rule A — full BFS** | Expand every unobserved cue layer. Produces `k^(#unobserved)` leaves where `k` is the branch alphabet size (3 for Severity). |
| `episode > 1` | **Rule B — single layer** | Expand only the first unobserved cue. Produces `k` leaves (or 1 if all cues observed). |

Implementation `RunSafetyExpansion.java:71–84`:

```java
if (episode == 1) {
    for (String cueId : unobserved) {
        double[] probs = severityProbsByCue.get(cueId);
        AdvanceLeafFront(gt, cueId, probs, ctx);
    }
} else {
    if (!unobserved.isEmpty()) {
        String nextCue = unobserved.get(0);
        double[] probs = severityProbsByCue.get(nextCue);
        AdvanceLeafFront(gt, nextCue, probs, ctx);
    }
}
```

## Why it's useful

- **Episode 1 gives the full prior PMF** over outcomes — a one-shot prediction across every branch combination.
- **Later episodes update incrementally** as new evidence arrives. There's no need to re-expand layers the user has already observed; only the *next* unobserved cue matters.
- The same data structure (`TreeForSafety` + `ParaDevsNodeForSafety`) is used for both modes — only the expansion loop changes.

## Helpers in `ExpansionUtils`

| Signature | File:line | Use |
|-----------|-----------|-----|
| `unobservedCues(Map<String, CueAssignment> path)` | `ExpansionUtils.java:54` | Returns cue IDs that are still `unobserved()` in the root path. |
| `expandNode(...)` | `ExpansionUtils.java:77` | Expands one parent into children on one cue. |
| `expandFullBFS(...)` | `ExpansionUtils.java:104` | Full nested expansion — Rule A. |
| `expandSingleLayer(...)` | `ExpansionUtils.java:142` | Expand only the first unobserved cue — Rule B. |

For skills generating *new* paratemporal drivers, the skill should emit `expandFullBFS` / `expandSingleLayer` helpers **only** when the user opts into episode-aware mode at Stage 2 APPROVE. Non-adaptive models (Volume, RPO) don't need them.

## When *not* to use episode-aware expansion

- **Single-shot analyses.** Volume's `makeTreeToDepth(numDimensions)` runs once and returns a full tree; splitting it into episodes adds complexity without benefit.
- **Fixed-depth phase models (RPO).** Each phase is expanded exactly once; there is no "next episode" concept.
- **Monte-Carlo replacement only.** If the user's goal is just "replace MC sampling with branching," Rule A alone suffices — they don't need Rule B.

## Signal for the skill's Stage 1 DISCOVER

Flag the model as episode-aware when the description mentions any of:

- "rounds," "episodes," "replanning," "adaptive expansion"
- "prior PMF" + later "posterior update"
- A running counter on the expander that affects expansion strategy
- A notion of "already observed" / "newly arrived" state variables that shrink the expansion scope

Otherwise default to single-shot full BFS (Volume-style).

## Probability preservation across episodes

Rule B expands only one cue per episode, so after `N` episodes with `N` newly-observed cues, the tree has the same combinatorial structure as Rule A would have produced in one shot — provided the observation order is fixed. This is important for comparing episode-aware posteriors against an oracle full-BFS run during testing.

## Context object

Episode-aware expansion usually rides on an `ExpansionContext` carrier that bundles:

- episode counter
- probability model reference
- evidence scorer (optional)
- any per-episode mutable state (e.g., batch of new cue results)

RunSafetyExpansion uses `ExpansionContext` + `SeverityProbabilityModel`. Skill-generated code should introduce a small context class rather than threading many parameters through every helper.
