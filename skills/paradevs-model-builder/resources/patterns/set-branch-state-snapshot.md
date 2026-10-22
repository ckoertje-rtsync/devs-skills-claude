# Pattern: `setBranch*()` — state snapshot on a child node

**Purpose.** Create one child node, install it in the parent's `children` map, and snapshot parent state into the child with exactly the fields that change on this branch mutated. Every reference node class exposes one or more `setBranch*()` overloads — this is where domain semantics enter the tree.

## The shape every `setBranch` must follow

```java
public NodeForX setBranchAction(String label, /* branch-specific params */, double prob) {
    NodeForX n = new NodeForX(name + label);    // 1. Fresh child, name = parent + label
    children.put(label, n);                      // 2. Register in parent's children
    n.level = level + 1;                         // 3. Depth + 1
    n.prob  = prob;                              // 4. Single-branch probability
    n.pathProb = pathProb * prob;                // 5. Cumulative probability (product)
    // 6. Copy parent state that should persist unchanged
    n.fieldA = this.fieldA;
    n.fieldB = this.fieldB;
    // 7. Mutate only the field(s) this branch changes
    n.fieldC = this.fieldC + delta;              // or similar transformation
    return n;                                    // 8. Return the child to the caller
}
```

Steps 1–5 are **invariant across every paratemporal model**. Steps 6–7 are what the domain authors.

## Reference signatures

| File | Signature | What changes |
|------|-----------|--------------|
| `ParaDevsNode.java:35` (plugin base class) | `setBranch(String label, double prob)` | Steps 1–5 only — no domain state. Generated nodes inline these fields instead of extending it. |
| `ParaDEVSNodeForVolume.java:15` | `setBranchDim(int dim, double rep, double prob)` | Adds one coordinate; `n.partialSumSq = this.partialSumSq + rep*rep`. |
| `ParaDEVSNodeForEngagement.java:32–122` | 7 overloads (`setBranch`, `setBranch(s, shots, hits, prob)`, …) | Different fields mutate per overload (shots, hits, sspk, engagement status). |
| `ParaDEVSNodeForRPO.java:97,131,170,201,239,274` | `setBranchDetection`, `setBranchTracking`, `setBranchClassification`, `setBranchProximity`, `setBranchResponse`, `setBranch` | Phase-specific branching — each overload mutates the phase's state variables. |
| `ParaDevsNodeForSafety.java:59` | `setBranch(String label, String cueId, Severity sev, double prob)` | Deep-copies `cueAssignments` map, adds `(cueId → observed(sev))`. |

## Copy-vs-delta rule

For each field on the node, decide one of three things:

1. **Immutable across branch** (e.g., target range in RPO's tracking phase) — direct assignment `n.field = this.field` is fine.
2. **Accumulated** (e.g., `partialSumSq`, `shots`, `pathProb`) — transform: `n.field = this.field + delta` (or `*` / `max` / …).
3. **Mutable reference** (e.g., `Map`, `List`, array) — **must** deep-copy: `n.map = new LinkedHashMap<>(this.map)` (see `ParaDevsNodeForSafety.java:62`). Skipping the copy aliases parent and child and corrupts every sibling in that layer.

## Label naming conventions

The `label` string doubles as the key in `children` AND is appended to the child's `name`:
- Volume uses coordinate representatives: `dim + String.format("%.3f", rep)` (`ParaDEVSNodeForVolume.java:16`)
- Safety uses severity ordinals prefixed by the cue: `":" + sev.name()` (`ParaDevsNodeForSafety.java:61`)
- RPO uses phase tags: `"Detect_" + outcome`

Keep labels ASCII-safe — they flow into Mermaid diagrams and file names via `node.name`.

## pathProb correctness

`pathProb = parentPathProb × branchProb` must hold every time. Two common failure modes:

- Forgetting to multiply (`n.pathProb = prob` instead of `pathProb * prob`). The tree root must be `pathProb = 1.0` (the generated node's field default), never `0.0` — watch for this when converting legacy code, whose plugin base class defaulted `pathProb` to `0.0`.
- Using cumulative `prob` on both sides (`n.pathProb = pathProb + prob`). Additive accumulation is wrong for statistically independent branches.

When the model was approved with `prob_accumulation = log`, step 5 becomes `n.logPathProb = logPathProb + Math.log(prob);` followed by `n.pathProb = Math.exp(n.logPathProb);`. See `log-space-accumulation.md` for the full substitution table.

## Multiple overloads vs. a single setBranch

When the domain branches on different state dimensions at different layers (RPO, Engagement), giving each layer its own `setBranchXxx()` method is cleaner than one mega-signature. Callers dispatch by method name in their layer expanders (e.g., `expandDetectionPhase` calls `setBranchDetection`). See `advance-leaf-front.md` for how layer dispatch lines up.

## What to return

Always return the newly-created child node. The caller (`advanceLeafFront`) will need it to (a) look up equivalents, and (b) hand it to `computeNodeEvidence` or similar post-hoc annotators.
