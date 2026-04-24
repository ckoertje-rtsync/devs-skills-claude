# Pattern: `advanceLeafFront(ParaDevsTree, …)`

**Purpose.** Replace the leaf frontier with the set of next-level children, merging equivalents as we go. This is the inner loop of paratemporal tree expansion — the direct replacement for "pick one Monte Carlo sample and advance it."

## Canonical skeleton

```java
public static void advanceLeafFront(ParaDevsTree bt, /* layer index or params */) {
    HashSet newLeaves = new HashSet();
    // Optional: per-layer setup (e.g., granule PMF, cue ID, phase dispatcher)
    for (Object o : bt.leaves) {
        NodeForX n = (NodeForX) o;
        // Optional: bt.interiors.add(n); — if you track interiors
        expandOneParent(n, /* layer params */);           // populates n.children
        for (Object c : n.children.values()) {
            NodeForX child = (NodeForX) c;
            if (useEquivalence) {
                NodeForX equiv = equivalentTo(child, newLeaves);
                if (equiv == null) {
                    newLeaves.add(child);
                } else {
                    equiv.pathProb += child.pathProb;     // merge: accumulate
                }
            } else {
                newLeaves.add(child);
            }
        }
    }
    bt.leaves = newLeaves;                                 // frontier replacement
}
```

## The three moves per parent

1. **Expand** — call the domain helper (or inline logic) that creates child nodes via `setBranch*()`. Children are installed into `n.children` as a side effect.
2. **Merge or admit** — for each child, look up an equivalent already in `newLeaves`. If found, `equiv.pathProb += child.pathProb` (the merged node absorbs the probability mass). Otherwise add the child to `newLeaves`.
3. **Swap** — once every parent is processed, replace `bt.leaves` with `newLeaves`. The old leaves have become interiors (tracked explicitly in some drivers, implicitly via the tree shape in others).

## Reference variants

| File | Signature | Layer parameter |
|------|-----------|-----------------|
| `ParaDEVSTreeForVolume.java:73` | `advanceLeafFront(ParaDevsTree bt, int dim)` | coordinate dimension index |
| `RunEngagement.java:225` | `AdvanceLeafFront(ParaDevsTree gt, int[] labels, int numGranules)` | threshold labels + granule count |
| `RunRPO.java:86` | `advanceLeafFront(ParaDevsTree gt, int phase)` | phase ordinal (0=Detection … 4=Response); dispatches to `expandDetectionPhase`, `expandTrackingPhase`, etc. |
| `RunSafetyExpansion.java:108` | `AdvanceLeafFront(ParaDevsTree gt, String cueId, double[] probs, ExpansionContext ctx)` | cue id + per-severity PMF |

Note: casing (`advanceLeafFront` vs `AdvanceLeafFront`) varies across the reference models. Both are valid — pick one and use it consistently within a single skill-generated module. The skill defaults to **camelCase** (`advanceLeafFront`) matching Volume and RPO.

## Terminal handling

When a child represents a state that should not be expanded further (termination predicate), move it to `gt.terminals` instead of `newLeaves`:

```java
if (child.terminated) {
    gt.terminals.add(child);
} else if (useEquivalence) { /* … as above */ }
```

See `termination-and-aggregation.md` for the full terminal-move idiom.

## Empty-parent short-circuit

If `n` has no surviving children (e.g., Volume's `earlyElimination` killed them all), the inner loop silently skips — `n.children` is empty, nothing is added. This is the intended behavior for early-kill pruning.

## Interior tracking

`bt.interiors` is optional. RunSafetyExpansion (`line 115`) does `gt.interiors.add(n)`; Volume (`line 78`) leaves it commented. Track interiors only when you need to walk the full tree later for analysis (e.g., rendering, JSON export); otherwise dropping them saves memory.

## pathProb accumulation rule

When merging, `equiv.pathProb` grows by `child.pathProb`. The discarded child's `prob` (single-branch probability) is lost — only the cumulative `pathProb` matters for downstream aggregation. This is intentional because the merged node represents the union of paths leading to equivalent states.

## Gotchas

- **Do not** mutate `bt.leaves` in place while iterating. The skeleton deliberately accumulates into a separate `newLeaves` set.
- **Do not** re-use the same child object across branches; each child must be a fresh node so the Hashtable key in `n.children` is unique.
- If `useEquivalence = false`, no merging happens — the tree grows by the full branch alphabet every level. Use this only for debugging.
- Some domains' `expandXxx` helpers (RPO's phase expanders) also decide whether to emit a terminal; make sure the terminal path and the new-leaf path are mutually exclusive.
