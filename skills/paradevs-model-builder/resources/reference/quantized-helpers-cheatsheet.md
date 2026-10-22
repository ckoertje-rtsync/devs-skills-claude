# Quantized-evidence helpers cheatsheet

Evidence-integrated drivers (`templates/paradevs-tree-driver-evidence.java.tmpl`) carry their own quantization helpers as static members — no external library. Theoretical basis: `docs/paradevs/EvidenceAppendix.pdf` §§A, B, C, E, F.

The helpers reproduce the semantics of the `Quantizer`, `QuantizedOperatorTable` and `ThresholdClassifier` classes in the Closure-DEVS paradevs plugin, which the Safety / RPO reference models use.

## Configuration constants

| Member | Purpose |
|--------|---------|
| `static final int G` | Graininess (number of integer levels). Embedded: 5–10; simulation: 100+. Do not use `G = 1`. |
| `static final DoubleBinaryOperator U` | Continuous combining operator `U : [0,1]² → [0,1]`. Canonical risk-assessment choice is the probabilistic sum `(v1, v2) -> v1 + v2 - v1 * v2`; use `Math::max` for MAX. |
| `static final int[][] T` | `buildTable(G, U)` — the `G×G` homomorphic lookup table, built once at class load. |
| `static final int[] THRESHOLDS` | G-domain cutpoints, strictly ascending. |
| `static final String[] ACTION_LABELS` | Length `THRESHOLDS.length + 1`; `ACTION_LABELS[0]` is the default label. |

A `static { checkClassifier(THRESHOLDS, ACTION_LABELS); }` block fails class load on a length mismatch or unsorted thresholds.

## Helper methods

| Signature | Purpose |
|-----------|---------|
| `static int quantize(double x, int G)` | `Q_G(x) = min(G−1, ⌊G·x⌋)`, with `x` clamped into `[0,1]`. Call on raw evidence before `setBranchEvidence`. |
| `static double representative(int k, int G)` | Bin-center `r_G(k) = (k + 0.5) / G`. Used by `buildTable` to evaluate `U` at canonical points. Not interchangeable with the linear `k/(G−1)` used for threshold porting. |
| `static int[][] buildTable(int G, DoubleBinaryOperator U)` | `T[i][j] = Q_G(U(r_G(i), r_G(j)))`. |
| `static String labelFor(int k)` | Highest-exceeded-threshold rule: `i* = max{ i \| THRESHOLDS[i] ≤ k }`, returns `ACTION_LABELS[i* + 1]`, or `ACTION_LABELS[0]` when no threshold is exceeded. |
| `static void checkClassifier(int[] thresholds, String[] labels)` | The class-load validation above. |

On the node, the combine step is a plain array lookup: `n.evidenceLevel = T[this.evidenceLevel][evidenceIn];`.

## Porting thresholds across graininess

Not emitted by default. When a threshold vector authored at `G_from` must run at `G_to`, map each level through the canonical value `v = k/(G_from−1)`:

```java
static int convertLevel(int k, int G_from, int G_to) {
    if (G_from == 1 || G_to == 1) return 0;
    long kPrime = Math.round((double) k / (G_from - 1) * (G_to - 1));
    return (int) Math.max(0, Math.min(G_to - 1, kPrime));
}
```

Monotonic, so threshold ordering is preserved.

## End-to-end pipeline (one line each)

```java
int     level       = quantize(rawEvidence, G);        // ingress
int     accumulated = T[prevLevel][level];             // combine
String  action      = labelFor(accumulated);           // classify
```

## Cross-references

- Integration pattern for tree-expansion drivers: `resources/patterns/quantized-evidence-integration.md`
- Histogram equivalence (symmetry-merging): `resources/patterns/equivalence-merging.md`
- Theoretical justification: `docs/paradevs/EvidenceAppendix.pdf` §§A–F
