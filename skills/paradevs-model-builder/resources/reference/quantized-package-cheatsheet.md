# `com.ms4systems.devs.paradevs.quantized` API cheatsheet

Source: `~/DEVS/paradevs/Closure-DEVS-paradevs-plugin/src/com/ms4systems/devs/paradevs/quantized/`. Theoretical basis: `docs/paradevs/EvidenceAppendix.pdf` §§A, B, C, E, F.

## `Quantizer` (`Quantizer.java`) — utility class

Homomorphic quantizer `Q_G` and bin-center representative `r_G`.

| Signature | Line | Purpose |
|-----------|------|---------|
| `static int quantize(double x, int G)` | 28 | `Q_G(x) = min(G−1, ⌊G·x⌋)`, clamps `x` into `[0,1]`. Throws on `G < 1`. |
| `static double representative(int k, int G)` | 49 | `r_G(k) = (k + 0.5) / G`. Used by `QuantizedOperatorTable` to evaluate the continuous operator at canonical points. Not interchangeable with `GDomainConverter`'s linear `k/(G−1)`. |

Private constructor; never instantiate.

## `QuantizedOperatorTable` (`QuantizedOperatorTable.java`)

Precomputed `T_G[i][j] = Q_G(U(r_G(i), r_G(j)))` for any continuous `U : [0,1]² → [0,1]`.

| Signature | Line | Purpose |
|-----------|------|---------|
| `static QuantizedOperatorTable build(int G, DoubleBinaryOperator U)` | 35 | Build the `G×G` table for arbitrary `U`. |
| `static QuantizedOperatorTable forProbabilisticSum(int G)` | 55 | Factory for the canonical risk-assessment operator `U_red(v1,v2) = v1 + v2 − v1·v2`. |
| `int lookup(int i, int j)` | 64 | O(1) integer table access. Throws `IndexOutOfBoundsException` if either index is outside `{0,…,G−1}`. |
| `int G()` | 69 | Graininess this table was built for. |
| `boolean verifyHomomorphism(DoubleBinaryOperator U, int tol)` | 83 | Asserts `\|T[i][j] − Q_G(U(r_G(i), r_G(j)))\| ≤ tol` over all `(i,j)`. Run once in a sanity check; `tol = 1` is typical. |
| `int[][] snapshot()` | 100 | Defensive copy of the table (for diagnostics / serialization). |

Private constructor; use the factories.

## `GDomainConverter` (`GDomainConverter.java`) — utility class

Maps integer levels between two graininess values while preserving the canonical value `v = k / (G_from − 1)`.

| Signature | Line | Purpose |
|-----------|------|---------|
| `static int convert(int k, int G_from, int G_to)` | 31 | `v = k/(G_from−1); k' = round(v·(G_to−1))`. Monotonic; preserves threshold ordering. Returns `0` if `G_from == 1 \|\| G_to == 1`. |
| `static int[] convertAll(int[] ks, int G_from, int G_to)` | 50 | Vectorized conversion (e.g., whole threshold vectors). |

## `ThresholdClassifier<L>` (`ThresholdClassifier.java`)

Parallel-array classifier mapping an integer level `k` to an application label of type `L` using the highest-exceeded-threshold rule.

Rule: `i* = max{ i | thresholds[i] ≤ k }`, `labelFor(k) = labels[i* + 1]`; default `labels[0]` when no threshold is exceeded.

| Signature | Line | Purpose |
|-----------|------|---------|
| `ThresholdClassifier(int[] thresholds, L[] labels)` | 40 | Constructor. Validates `labels.length == thresholds.length + 1` and that `thresholds` is strictly sorted ascending. Defensive-copies both arrays. |
| `L labelFor(int k)` | 60 | Reverse-scan classifier (branch-predictable, fast early exit). |
| `int indexFor(int k)` | 72 | Index of the highest exceeded threshold, or `-1` if none. Useful for ordinal severity. |
| `int[] thresholds()` | 80 | Defensive copy of thresholds. |
| `L[] labels()` | 85 | Defensive copy of labels. |
| `String toString()` | 90 | Debug format. |

Generic `L` so callers may use `String`, an enum, `Integer`, or any other identifier. The reference RPO domain uses `L = ActionLabel`.

## Import block (for generated evidence-integrated drivers)

```java
import com.ms4systems.devs.paradevs.quantized.Quantizer;
import com.ms4systems.devs.paradevs.quantized.QuantizedOperatorTable;
import com.ms4systems.devs.paradevs.quantized.GDomainConverter;
import com.ms4systems.devs.paradevs.quantized.ThresholdClassifier;
```

## End-to-end pipeline (one line each)

```java
int   G = 10;
QuantizedOperatorTable T = QuantizedOperatorTable.forProbabilisticSum(G);
int   level = Quantizer.quantize(rawEvidence, G);                         // ingress
int   accumulated = T.lookup(prevLevel, level);                           // combine
int[] portedThresholds = GDomainConverter.convertAll(sourceThresholds,    // port
                                                     sourceG, G);
ThresholdClassifier<String> C = new ThresholdClassifier<>(                // classify
        portedThresholds, new String[]{"normal","caution","warning","critical"});
String action = C.labelFor(accumulated);
```

## Cross-references

- Integration pattern for tree-expansion drivers: `resources/patterns/quantized-evidence-integration.md`
- Histogram equivalence (symmetry-merging): `resources/patterns/equivalence-merging.md`
- Theoretical justification: `docs/paradevs/EvidenceAppendix.pdf` §§A–F
