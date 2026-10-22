# Pattern: `makeTreeToDepth(int depth)`

**Purpose.** Outer BFS loop that grows a paratemporal tree level-by-level to a bounded depth. Every reference model exposes it as a `public static` method on the domain-specific driver class.

## Canonical skeleton

```java
public static TreeForX makeTreeToDepth(int maxDepth) {
    TreeForX bt = new TreeForX();          // the driver class is the tree container
    NodeForX root = new NodeForX("root");
    root.pathProb = 1.0;
    root.level = 0;
    // ... domain state init (e.g., root.partialSumSq = 0.0;) ...
    bt.root = root;
    bt.leaves.add(root);
    for (int d = 0; d < maxDepth; d++) {
        advanceLeafFront(bt, d);
    }
    return bt;
}
```

## Invariants

| Step | What must be true |
|------|-------------------|
| Construct `TreeForX` | `bt.leaves`, `bt.interiors`, `bt.terminals` start as empty typed `HashSet`s; `bt.root` is `null` until installed. |
| Initialize root | `root.level = 0`, `root.pathProb = 1.0`. The node's `pathProb` field already defaults to `1.0`; set it explicitly anyway so the invariant is visible (and so converted legacy code, where the plugin base class defaulted it to `0.0`, cannot zero out all descendants). |
| Install root | `bt.root = root` AND `bt.leaves.add(root)`. Omitting the leaf-set add leaves the frontier empty and `advanceLeafFront` is a no-op. |
| BFS loop | Pass the depth index `d` into `advanceLeafFront`; many domains use `d` to select a phase / dimension / cue layer. |

## Variants observed in reference code

| File | Signature | Variation |
|------|-----------|-----------|
| `ParaDEVSTreeForVolume.java:54` | `makeTreeToDepth(int maxDepth)` | Pure depth loop, one dimension per step. |
| `RunEngagement.java:156` | `makeTreeToDepth(int depth)` | Closes over static `numGranules`. |
| `RunEngagement.java:126` | `makeTreeToDepth(int depth, int numGranules)` | Parameterized granules overload. |
| `RunEngagement.java:192` | `makeTreeToDepth(int depth, double pkill)` | Parameterized kill probability overload. |
| `RunRPO.java:52` | `makeTreeToDepth(int maxDepth)` | Depth is phase count (5 phases); each iteration expands one phase. |
| `RunSafetyExpansion.java:50` | `expandEpisode(rootPath, episode, probModel, ctx)` | Episode-indexed variant (no fixed maxDepth) — see `episode-aware-expansion.md`. |

## Early-elimination option

Volume sets `earlyElimination = true` (`ParaDEVSTreeForVolume.java:50`) — during expansion, candidates whose partial state already violates the termination condition (`partialSumSq > 1.0`) are dropped before they enter the new leaf set. Reduces exponential blow-up when merging isn't enough.

## What `makeTreeToDepth` is NOT

- It is **not** a DEVS simulator. It builds a data structure offline; no `Atomic.internalTransition` is invoked.
- It does **not** sample randomly. All branches are enumerated deterministically (see `set-branch-state-snapshot.md`).
- It does **not** schedule events in simulated time. The "depth" parameter is a combinatorial count of expansion rounds, not a time advance.

## Gotchas

- Always build a fresh `TreeForX` per run; the frontier sets are instance fields, so reusing a tree carries over old leaves and terminals.
- Volume's reference implementation leaves `bt.interiors` and `bt.terminals` commented out (line 63–64). Generated drivers always initialize them, so terminal aggregation works without extra setup.
