# Pattern: integrating `com.ms4systems.devs.paradevs.quantized`

**Purpose.** When the paratemporal model represents evidence accumulation — continuous `[0,1]` values combined by a binary operator (probabilistic sum, MAX, …), with thresholds that map the accumulated value to a decision label — do not reinvent the quantization stack. Use the generic package at `com.ms4systems.devs.paradevs.quantized`.

Theoretical basis: `docs/paradevs/EvidenceAppendix.pdf` §§A, B, C, E, F. The package realizes the appendix's framework.

## The four classes

| Class | One-line role |
|-------|---------------|
| `Quantizer` | `Q_G(x) = min(G−1, ⌊G·x⌋)` and bin-center `r_G(k) = (k+0.5)/G`. |
| `QuantizedOperatorTable` | Precomputed `G×G` lookup `T_G[i][j] = Q_G(U(r_G(i), r_G(j)))`; constant-time `lookup(i,j)`. |
| `GDomainConverter` | Maps integer level `k` from `G_from`-domain to `G_to`-domain preserving `v = k/(G_from-1)`. |
| `ThresholdClassifier<L>` | Parallel-array classifier: highest-exceeded-threshold rule, returns label of type `L`. |

See `resources/reference/quantized-package-cheatsheet.md` for full signatures. Citations below use `file_path:line_number` into `~/DEVS/paradevs/Closure-DEVS-paradevs-plugin/src/`.

## When to wire this in

Flag the model as an evidence candidate when Stage 1 DISCOVER sees any of:

- **Continuous evidence accumulator**: each branch contributes a `[0,1]` value that is combined into a running `evidence` field via a binary operator. Canonical: probabilistic sum `v1 + v2 − v1·v2`.
- **Decision thresholds with labels**: a sorted array of cutpoints maps the accumulated evidence to an action category (normal / caution / warning / critical).
- **Fidelity selector**: the user wants to tune graininess `G` at compile time (e.g., embedded G=5 vs. simulation G=100) without rewriting the model.
- **Symmetry / order-independence**: the evidence result depends only on the *multiset* of quantized levels observed along the path, not their order. That is the canonical condition for histogram equivalence.

The Stage-2 APPROVE gate poses: *"this looks like a candidate for `com.ms4systems.devs.paradevs.quantized`; wire it in?"* When answered yes, the skill switches to the `*-evidence` templates.

## Node state fields

An evidence-integrated node carries (on top of `ParaDevsNode`):

```java
public int   G;               // graininess (redundantly cached; matches table.G())
public int   evidenceLevel;   // integer accumulator in [0, G-1]
public int[] histogram;       // optional: histogram[k] = count of level k on path (for merging)
```

`histogram` is only needed if equivalence is histogram-based; otherwise scalar `evidenceLevel` alone is enough.

## Driver state fields (static in the Run class)

```java
static final int G = 10;                             // compile-time graininess
static final QuantizedOperatorTable T =              // built once at class init
        QuantizedOperatorTable.forProbabilisticSum(G);
static final int[]    THRESHOLDS = {3, 6, 8};        // in G-domain
static final String[] ACTIONLABELS = {"normal", "caution", "warning", "critical"};
static final ThresholdClassifier<String> CLASSIFIER =
        new ThresholdClassifier<>(THRESHOLDS, ACTIONLABELS);
```

`ThresholdClassifier` validates `labels.length == thresholds.length + 1` and that thresholds are strictly sorted at construction time (`ThresholdClassifier.java:40–57`) — a misaligned pair fails at class load, not silently at first use.

## setBranch update

The branch update is a constant-time integer operation:

```java
public NodeForX setBranchEvidence(String label, int evidenceIn, double prob) {
    NodeForX n = new NodeForX(name + ":" + label);
    children.put(label, n);
    n.level = level + 1;
    n.prob = prob;
    n.pathProb = pathProb * prob;
    n.G = this.G;
    n.evidenceLevel = T.lookup(this.evidenceLevel, evidenceIn);   // constant-time
    if (this.histogram != null) {                                  // optional
        n.histogram = this.histogram.clone();
        n.histogram[evidenceIn] += 1;
    }
    return n;
}
```

`evidenceIn` is already quantized (the caller did `Quantizer.quantize(rawValue, G)` before calling `setBranchEvidence`). Keeping the public signature integer-only preserves the "no floating-point inside the expansion loop" guarantee from `EvidenceAppendix.pdf` §F.2.

## Raw-value ingress

At the boundary where raw observations enter the expander:

```java
double raw = sensor.read();
int   level = Quantizer.quantize(raw, G);   // Quantizer.java:28
// …then call setBranchEvidence(label, level, prob)
```

Do the quantization **once**, at ingress. Do not re-quantize inside the table — `T.lookup` takes integer levels on both sides.

## Threshold classification at the output

At terminal aggregation time:

```java
for (Object o : gt.terminals) {
    NodeForX n = (NodeForX) o;
    String action = CLASSIFIER.labelFor(n.evidenceLevel);   // ThresholdClassifier.java:60
    // accumulate PMF[action] += n.pathProb
}
```

`labelFor` is a reverse scan — short-circuits on the first threshold met from the top. `EvidenceAppendix.pdf` §F.5 notes this is cache-friendly and branch-predictable for typical 3–7 cutpoints.

## Porting thresholds across G values

If thresholds were authored for `G_from = 10` but this deployment uses `G_to = 100`:

```java
int[] ported = GDomainConverter.convertAll(THRESHOLDS, 10, 100);   // GDomainConverter.java:50
ThresholdClassifier<String> CLASSIFIER_100 = new ThresholdClassifier<>(ported, ACTIONLABELS);
```

The converter preserves the canonical value `v = k/(G_from-1)` (`GDomainConverter.java:31`) and round-maps into the target domain. Ordering is preserved monotonically; ACTIONLABELS do not need to change.

## Equivalence becomes histogram-based

Because the lookup table is the homomorphic image of a continuous operator, the accumulated `evidenceLevel` depends only on the multiset of inputs, not their order (`EvidenceAppendix.pdf` §C.1 multiset semantics). So the natural equivalence relation is:

```java
public static boolean equivalent(NodeForX s, NodeForX t) {
    return s.level == t.level && Arrays.equals(s.histogram, t.histogram);
}
```

This is the compression mechanism that reduces the `G^n` sequence space to `C(n+G-1, G-1)` distinct histograms (`EvidenceAppendix.pdf` §C.2). If you choose histogram equivalence, include `int[] histogram` in the node's setBranch update (the snippet above).

If histograms are too aggressive (two different histograms happen to produce the same `evidenceLevel`), fall back to `s.evidenceLevel == t.evidenceLevel` (scalar equivalence) — coarser, but cheaper.

## Verification

`QuantizedOperatorTable.verifyHomomorphism(U, tol)` (`QuantizedOperatorTable.java:83`) asserts the table really is the homomorphic image of the continuous operator at tolerance `tol` (usually `1` G-unit). Run it once in `main()` or a test when `G` or `U` changes — cheap sanity check:

```java
assert T.verifyHomomorphism((v1, v2) -> v1 + v2 - v1*v2, 1);
```

## Gotchas

- `Quantizer.representative` uses `(k + 0.5) / G` (bin center), while `GDomainConverter.convert` uses `k / (G_from - 1)` (linear endpoints). They are **not** interchangeable. See `Quantizer.java:37–42` for the rationale — the representative is for operator evaluation; the converter is for preserving ordered thresholds.
- `ThresholdClassifier`'s label array is 1 longer than the threshold array (`labels[0]` is the default). Off-by-one here aborts class load.
- When `G = 1`, `GDomainConverter.convert` returns `0` for any input (`GDomainConverter.java:38`). Don't build models with `G = 1`.
- The integer histogram is bounded by the path length, not by `G`. For very deep trees this is the right choice; for wide shallow trees the histogram may be mostly zeros — in that case a sparse representation is an obvious future optimization, but the references use dense `int[]`.
