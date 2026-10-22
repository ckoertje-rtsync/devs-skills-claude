# Pattern: log-space probability accumulation (`prob_accumulation = log`)

**Purpose.** Replace the product-mode `pathProb = parent.pathProb * prob` chain with a sum of log-probabilities so that deep trees and rare branches do not underflow `double` to `0.0`. This is an **opt-in** alternative selected in Stage 2; the default remains product mode, which is what every reference model uses.

## When to choose it

Choose `log` when any of these hold:

| Signal | Why product mode fails |
|--------|------------------------|
| Depth × `−log(min branch prob)` approaches 700 (e.g., 300 layers at `prob ≈ 0.1`) | `pathProb` drops below `Double.MIN_VALUE ≈ 4.9e-324` and becomes exactly `0.0`; every descendant is silently lost. |
| Rare-event analysis (branch probs `≤ 1e-6`) over more than ~50 layers | Same underflow, reached sooner. |
| Legacy code being converted already carries log-likelihoods | Converting to products would reintroduce the problem the legacy author avoided. |
| Merged masses are later divided (conditional probabilities, likelihood ratios) | Ratios of underflowed zeros are `NaN`. |

Stay with `product` when the tree is shallow (Volume, RPO, Safety: tens of layers) and all branch probabilities are moderate. Product mode is simpler, matches the references line-for-line, and the `sum(pathProb) ≈ 1.0` invariant is directly readable.

## The substitutions

Product-mode templates are the base. Apply every row below when `prob_accumulation = log`.

### Node class

| Location | Product mode (template default) | Log mode |
|----------|--------------------------------|----------|
| State fields | — | `public double logPathProb;` |
| Constructor | `pathProb = 1.0;` | `pathProb = 1.0;` **and** `logPathProb = 0.0;` |
| `setBranch*` | `n.pathProb = pathProb * prob;` | `n.logPathProb = logPathProb + Math.log(prob);` **then** `n.pathProb = Math.exp(n.logPathProb);` |
| `toString` | `pathProb=%.6f` | add `logPathProb=%.4f` |

`Math.log(0.0)` is `−∞`, so a zero-probability branch produces `logPathProb = −∞` and `pathProb = 0.0` with no special casing. The `Math.exp` mirror keeps the node's `pathProb` field meaningful for debugging and for any reader that only knows product mode; when it underflows to `0.0` the true mass is still in `logPathProb`, which is the field every computation in log mode reads.

```java
public NodeForX setBranchX(String label, /* params */, double prob) {
    NodeForX n = new NodeForX(name + label);
    children.put(label, n);
    n.level = level + 1;
    n.prob = prob;
    n.logPathProb = logPathProb + Math.log(prob);   // log-space product
    n.pathProb = Math.exp(n.logPathProb);           // mirror for display only
    // copy + mutate domain fields …
    return n;
}
```

### Driver class

| Location | Product mode | Log mode |
|----------|--------------|----------|
| Helpers | — | splice `resources/templates/accumulate-log.java.frag` (`LOG_ZERO`, `logAdd`, `logSumPathProb`) |
| `makeTreeToDepth` root init | `root.pathProb = 1.0;` | `root.pathProb = 1.0;` **and** `root.logPathProb = 0.0;` |
| Merge in `advanceLeafFront` | `equiv.pathProb = equiv.pathProb + child.pathProb;` | `equiv.logPathProb = logAdd(equiv.logPathProb, child.logPathProb);` **then** `equiv.pathProb = Math.exp(equiv.logPathProb);` |
| Terminal merge (if used) | `equivTerm.pathProb += child.pathProb;` | same `logAdd` form |
| Scalar aggregate | `hitProb += n.pathProb;` | `logHit = logAdd(logHit, n.logPathProb);` then `Math.exp(logHit)` at the very end |
| PMF grouping | `pmf.merge(k, n.pathProb, Double::sum)` | `logPmf.merge(k, n.logPathProb, Driver::logAdd)` then exponentiate per entry |
| `main` total | `totalProb += n.pathProb;` | `double totalLog = logSumPathProb(bt.leaves);` print `Math.exp(totalLog)` and `totalLog` |

```java
if (useEquivalence) {
    NodeForX equiv = equivalentTo(child, newLeaves);
    if (equiv == null) {
        newLeaves.add(child);
    } else {
        equiv.logPathProb = logAdd(equiv.logPathProb, child.logPathProb);
        equiv.pathProb = Math.exp(equiv.logPathProb);
    }
}
```

`logAdd` is the two-argument log-sum-exp: `max + log1p(exp(min − max))`. Using `Math.log1p` keeps precision when the two masses differ by many orders of magnitude, which is exactly the case merging is meant to handle.

### Equivalence predicate

Unchanged. `equivalent(s, t)` compares domain state, never probability mass, so exact / tolerance / histogram fragments splice in as-is.

### Evidence-integrated variant

Same substitutions. `setBranchEvidence` in `paradevs-node-evidence.java.tmpl` has the identical `n.pathProb = pathProb * prob;` line; `actionPMF` in the driver becomes a log-space merge with a final per-label `Math.exp`.

## Probability invariant in log mode

```
logAdd(logSumPathProb(bt.leaves), logSumPathProb(bt.terminals)) ≈ 0.0
```

i.e. `exp(...) ≈ 1.0`. Assert on the log value; `Math.abs(total) < 1e-9` is a reasonable check. Never assert on `sum(pathProb)` in log mode — the mirrored field can legitimately read `0.0` for leaves that carry real mass.

## Output artifacts

- README: state `Probability accumulation: log-space (logPathProb, logAdd merge)`.
- Tree diagram "Key invariants": replace the `child.pathProb = parent.pathProb * prob` line with `child.logPathProb = parent.logPathProb + log(prob)` and the sum invariant with the `logAdd` form above.
- Mermaid labels: `log(prob)` is ASCII-safe; do not write `ln p` with Unicode or `:=`.

## Gotchas

- **Do not mix modes.** One field must be authoritative. In log mode every add, merge, and aggregate reads `logPathProb`; `pathProb` is written but never read.
- **`Math.log(prob)` with `prob > 1`** is a silent modeling error (positive log-prob). Product mode has the same bug shape (`pathProb > 1`); neither is caught by the compiler. Validate branch PMFs sum to 1 in `LAYER_SETUP` if they are computed rather than tabulated.
- **`logAdd(LOG_ZERO, LOG_ZERO)`** returns `LOG_ZERO`, so aggregating an empty set stays `−∞` and `Math.exp` gives `0.0`. This is correct, but a scalar aggregate printed as `-Infinity` surprises people; print the exponentiated value.
- **Tolerance equivalence on `logPathProb`** is not a thing. Equivalence is over state; if you find yourself wanting to merge on similar mass you have picked the wrong state variables.
