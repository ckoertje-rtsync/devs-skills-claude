# Pattern: termination, terminals, and leaf aggregation

**Purpose.** Capture nodes that represent final outcomes (not to be expanded further), then aggregate across them to produce the answer the user asked for (volume, hit probability, PMF, classification histogram, …).

## The `terminated` flag and the `terminals` set

`ParaDevsNode.terminated` (`ParaDevsNode.java:15`) is a `boolean` on every node. `ParaDevsTree.terminals` (`ParaDevsTree.java:20`) is a `HashSet` that collects them.

A node becomes terminal when a domain predicate evaluates true. Typical predicates:

| Domain | Terminal predicate |
|--------|--------------------|
| Volume | `partialSumSq >= 1.0` (on the sphere boundary / outside) |
| AirBattle (Engagement) | `level == Math.ceil(engaged)` — engagement has completed |
| RPO | phase transition reaches `RESPONSE` or a timeout; set `n.terminated = true` and move to `gt.terminals` |
| Safety | episode budget exhausted or all cues observed |

## The move-to-terminals idiom

In `advanceLeafFront`, branch children into two streams:

```java
for (Object c : n.children.values()) {
    NodeForX child = (NodeForX) c;
    if (isTerminal(child)) {
        child.terminated = true;
        gt.terminals.add(child);
    } else if (useEquivalence) {
        NodeForX equiv = equivalentTo(child, newLeaves);
        if (equiv == null) newLeaves.add(child);
        else equiv.pathProb += child.pathProb;
    } else {
        newLeaves.add(child);
    }
}
```

Terminals do **not** participate in equivalence merging at the frontier — they leave the frontier entirely. If you want to merge equivalent terminals (often the right answer for PMF aggregation), do it inside `gt.terminals`:

```java
NodeForX equivTerm = equivalentTo(child, gt.terminals);
if (equivTerm != null) equivTerm.pathProb += child.pathProb;
else gt.terminals.add(child);
```

RunSafetyExpansion takes a simpler path: at the very end of `expandEpisode` it does `gt.terminals.addAll(gt.leaves);` (`RunSafetyExpansion.java:87`), treating every final leaf as a terminal without a mid-expansion predicate. Use this when the termination depth is fixed (e.g., after expanding every cue).

## Aggregation over terminals

Two common shapes:

### Scalar aggregate (e.g., volume, hit probability)

```java
public static double computeVolume(ParaDevsTree bt) {
    double hitProb = 0.0;
    for (Object o : bt.leaves) {
        NodeForX n = (NodeForX) o;
        if (n.partialSumSq < 1) hitProb += n.pathProb;
    }
    return Math.pow(2.0, numDimensions) * hitProb;
}
```
(`ParaDEVSTreeForVolume.java:132`)

### Grouping by outcome class (PMF over classifications)

```java
public static HashMap<String, HashSet<NodeForX>> splitByClassification(HashSet terminals) {
    HashMap<String, HashSet<NodeForX>> groups = new HashMap<>();
    for (Object o : terminals) {
        NodeForX n = (NodeForX) o;
        String cls = n.classificationResult;
        groups.computeIfAbsent(cls, k -> new HashSet<>()).add(n);
    }
    return groups;
}
```
(`RunRPO.java:367` — also `splitByResponse` at `:382`, `splitByTermination` at `:397`, `splitByEngage` in `RunEngagement.java:471`)

Summing `pathProb` across each group yields the probability of that outcome class.

### Convert to PMF entries (Safety)

```java
public static List<LeafPMFEntry> terminalsToLeaves(ParaDevsTree gt) {
    List<LeafPMFEntry> result = new ArrayList<>();
    for (Object o : gt.terminals) {
        NodeForX n = (NodeForX) o;
        result.add(new LeafPMFEntry(n.stateKey, n.pathProb, n.evidence));
    }
    return result;
}
```
(`RunSafetyExpansion.java:185`)

## Probability invariant

After terminals are captured:

```
sum(leaf.pathProb for leaf in bt.leaves) + sum(term.pathProb for term in bt.terminals) ≈ 1.0
```

…modulo floating-point drift and (for Volume) intentional early-elimination that drops non-contributing branches. Asserting this in tests is a cheap sanity check — any drift means a `setBranch` forgot the `pathProb * prob` multiplication or an equivalence merge double-counted.

## When to skip `gt.terminals` entirely

If your only output is an aggregate over surviving leaves at the final depth (Volume's case), you do not need `gt.terminals`. `bt.leaves` at loop exit *is* the terminal set, and `computeVolume` reads directly from it. Don't add `gt.terminals` just because it's available.
