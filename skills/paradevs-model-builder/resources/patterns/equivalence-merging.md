# Pattern: `equivalent(Node, Node)` + `equivalentTo(leaf, HashSet)`

**Purpose.** Collapse nodes that reach the same observable state so the tree stays polynomial instead of exponential. Equivalence is **domain-specific**; its definition is the single most important modeling decision after choosing the state variables themselves.

## The merge rule (every driver uses it)

Inside `advanceLeafFront`:

```java
NodeForX equiv = equivalentTo(child, newLeaves);
if (equiv == null) {
    newLeaves.add(child);
} else {
    equiv.pathProb = equiv.pathProb + child.pathProb;   // absorb the child's mass
}
```

The merged node accumulates `pathProb` (or, in `prob_accumulation = log` mode, `logPathProb` via `logAdd` — see `log-space-accumulation.md`); the discarded child is dropped on the floor. Over many layers this turns `G^n` enumerations into `C(n+G-1, G-1)` histogram classes — the symmetry-merging effect documented in `EvidenceAppendix.pdf` §C.2 and `F.3`.

## `equivalentTo` — the lookup wrapper

Every driver copies essentially the same helper:

```java
public static NodeForX equivalentTo(NodeForX leaf, HashSet<NodeForX> nodes) {
    for (NodeForX n : nodes) {
        if (equivalent(n, leaf)) return n;
    }
    return null;
}
```

Reference citations: `ParaDEVSTreeForVolume.java:113`, `RunSafetyExpansion.java:148`, equivalent pattern in `RunEngagement.java` and `RunRPO.java`.

This is an O(|newLeaves|) scan per child. For paratemporal models that's fine — the whole point of merging is that `|newLeaves|` stays small. Do not replace it with a HashMap keyed on some hash of node state unless you've profiled and the scan is actually hot; equality checks are subtle (floating-point, map-equals) and a buggy hash silently miscounts probability mass.

## Three flavors of `equivalent`

### (a) Exact state match

```java
public static boolean equivalent(NodeForX s, NodeForX t) {
    return s.level == t.level
        && s.stateField.equals(t.stateField);    // or primitive ==
}
```

Use when state is a **discrete** identifier (enum, `String`, deep-equatable `Map`). The Safety model's primary predicate is exactly this — `a.cueAssignments.equals(b.cueAssignments)` (`RunSafetyExpansion.java:167`).

### (b) Tolerance-based

```java
public static boolean equivalent(NodeForX s, NodeForX t) {
    return s.level == t.level
        && Math.abs(s.contField - t.contField) < EPS;
}
```

Use when state is a **continuous accumulator** prone to floating-point drift. Volume uses `Math.abs(s.partialSumSq - t.partialSumSq) < 0.0001` (`ParaDEVSTreeForVolume.java:127`). Pick `EPS` small enough that distinct bins don't merge but large enough that numerically-equal accumulations do.

Multiple tolerance fields may be combined:
```java
return s.level == t.level
    && Math.abs(s.hits - t.hits) < 1e-6
    && s.engaged == t.engaged;      // hybrid: tolerance AND exact
```

RunEngagement.java:465 uses this hybrid shape.

### (c) Histogram / multiset

When the model accumulates a sequence of quantized levels (evidence pipeline), equivalence is on the **multiset** of levels, not their order:

```java
public static boolean equivalent(NodeForX s, NodeForX t) {
    return s.level == t.level
        && Arrays.equals(s.levelHistogram, t.levelHistogram);
}
```

where `levelHistogram[k]` counts occurrences of quantized level `k` across the path from root. This is the key to the `C(n+G-1, G-1)` compression — see `EvidenceAppendix.pdf` §C.1–C.2. Use in combination with the quantized operator pipeline (`quantized-evidence-integration.md`).

## How to pick the predicate

| Situation | Predicate |
|-----------|-----------|
| State is an enum, a `String`, or a `Map` of discrete values | **exact** |
| State is a `double` accumulator (sum, product, probability, distance) | **tolerance** |
| State is a sequence of quantized evidence levels combined by an associative operator (prob-sum, max, …) | **histogram** |
| State mixes discrete + continuous | Predicate is an `&&` of the relevant flavors (see Engagement). |

The skill's Stage-2 APPROVE gate asks the user which of these three to generate; see `SKILL.md` Stage 2 for the exact menu.

## Level check is load-bearing

Every reference predicate starts with `s.level == t.level`. This is not optional — the BFS layer must agree before any state comparison. Two nodes at different depths can't be equivalent even if their state is identical, because subsequent expansion branches have different semantics by depth.

## Why not `equals()` / `hashCode()`?

You could override `Object.equals` and `Object.hashCode` on the node class and use a `HashSet<Node>` to deduplicate. None of the reference models do this. Reasons:

- The node's `name` field distinguishes *identity* from *equivalence*; collapsing them would break debug output.
- Floating-point equality is ill-suited to `equals`/`hashCode` contracts.
- The O(|newLeaves|) scan is fast enough; there's no speed reason to switch.

## Gotcha: merging only within a single layer

`equivalentTo(child, newLeaves)` only sees leaves from *this* layer — never prior layers. Cross-layer equivalence would be a semantic error (same state at different depths is not the same event). Don't try to be clever.
