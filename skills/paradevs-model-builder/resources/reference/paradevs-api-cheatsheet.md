# `com.ms4systems.devs.paradevs` API cheatsheet

Base classes generated models depend on. Source: `~/DEVS/paradevs/Closure-DEVS-paradevs-plugin/src/com/ms4systems/devs/paradevs/`.

## `ParaDevsNode` (`ParaDevsNode.java`)

Base class for every paratemporal tree node. Domain-specific nodes extend this.

### Public fields

| Field | Type | Default | Purpose |
|-------|------|---------|---------|
| `children` | `Hashtable` | `new Hashtable()` | Map from branch label → child node. Keys are typically `String` labels. |
| `key` | `String` | `null` | Optional secondary identifier (rarely used by drivers). |
| `name` | `String` | `null` | Human-readable node name; set in constructor. Children's names are `parent.name + label`. |
| `level` | `int` | `0` | Depth from root. Set in `setBranch` as `level = parent.level + 1`. |
| `prob` | `double` | `1.0` | Single-branch probability `Pr(branch \| parent)`. |
| `pathProb` | `double` | `0.0` | Cumulative probability from root, `parent.pathProb × prob`. **Must** be explicitly set to `1.0` on the root. The base class has no log-space field; skill-generated nodes add `logPathProb` themselves when `prob_accumulation = log` (see `patterns/log-space-accumulation.md`). |
| `terminated` | `boolean` | `false` | Marks the node as a terminal outcome (do not expand further). |
| `path` | `String` | `""` | Optional concatenated branch label trace. |
| `cm` | `Coupled` | `null` | Optional reference to a Closure-DEVS Coupled model — used when later wiring the tree into a DEVS simulator. Leave `null` for offline tree construction. |

### Public methods

| Signature | Line | Purpose |
|-----------|------|---------|
| `ParaDevsNode()` | 21 | No-op constructor. |
| `ParaDevsNode(String nm)` | 23 | Name only. |
| `ParaDevsNode(String nm, Coupled cm)` | 27 | Name + Coupled reference. |
| `ParaDevsNode(String name, int i)` | 32 | Stub (unused in references). |
| `ParaDevsNode setBranch(String label, double prob)` | 35 | Base-class branch — creates child, installs in `children`, sets `level`, `prob`, `pathProb`. Domain nodes override with their own `setBranchXxx` that also snapshots domain state. |
| `void replaceNodeWithEquiv(ParaDevsNode leaf, ParaDevsNode equiv)` | 44 | Swap child entry — used by merge routines (rarely called directly by drivers). |
| `String toString()` | 51 | Default debug format `GeneralNode{name,N,pathProb,P,level,L}`. Override in domain nodes. |

### Convention: extending `ParaDevsNode`

```java
public class NodeForX extends ParaDevsNode {
    // domain fields
    public double partialSumSq;
    public int    phase;
    // …

    public NodeForX(String nm) { name = nm; }

    public NodeForX setBranchX(String label, /* branch params */, double prob) {
        NodeForX n = new NodeForX(name + label);
        children.put(label, n);
        n.level = level + 1;
        n.prob = prob;
        n.pathProb = pathProb * prob;
        // copy + mutate domain fields
        return n;
    }

    @Override public String toString() { /* …with domain fields… */ }
}
```

Reference implementations: `ParaDEVSNodeForVolume.java`, `ParaDevsNodeForSafety.java`, `ParaDEVSNodeForEngagement.java`, `ParaDEVSNodeForRPO.java`.

## `ParaDevsTree` (`ParaDevsTree.java`)

Container for a paratemporal tree.

### Public fields

| Field | Type | Default | Purpose |
|-------|------|---------|---------|
| `root` | `ParaDevsNode` | a placeholder `ParaDevsNode("0", 0)` | Tree root. Drivers **must** overwrite with their domain node. |
| `leaves` | `HashSet` | `new HashSet()` | Current BFS frontier. Replaced every iteration of `advanceLeafFront`. |
| `interiors` | `HashSet` | `new HashSet()` | Optional record of non-frontier, non-terminal nodes. |
| `terminals` | `HashSet` | `new HashSet()` | Outcome nodes that will not be expanded further. |
| `useEquivalence` | `boolean` | `false` | Not consulted by the library itself — driver classes read their own static flag. Present as a field for potential future use. |

### Public methods

| Signature | Line | Purpose |
|-----------|------|---------|
| `ParaDevsTree()` | 29 | Empty tree; installs a `ParaDevsNode` root with `level=0`. |
| `ParaDevsTree(String[] labels, double[] probs)` | 34 | Tree with a first-layer fan-out via the base-class `setBranch`. |
| `static ParaDevsTree makeGeneralTree(String[] labels, double[] probs)` | 43 | Static factory for the two-arg constructor. |
| `int getDepth()` | 47 | Recursive depth of the tree from `root`. |
| `int getDepth(ParaDevsNode n)` | 51 | Depth below a given node. |
| `static void print(ParaDevsTree bt)` | 69 | Debug print, level-by-level. |
| `static void printNextLevel(ParaDevsTree gt, HashSet hs, int d)` | 76 | Helper called by `print`. |
| `String selectFrom(String[] labels, double[] probs)` | 93 | **Sampling** helper (Monte Carlo) — kept for legacy parity; paratemporal drivers do not call this. |
| `HashSet getTerminals()` | 25 | Returns the terminals set. |

### Gotchas

- The default constructor (`ParaDevsTree()`) pre-installs a generic `ParaDevsNode` root. Every driver overwrites `bt.root` with its own domain node; forgetting to do so silently builds a two-type tree that casts will fail on.
- `leaves`, `interiors`, `terminals` are raw `HashSet` (no generic parameter). Cast explicitly when reading.
- `selectFrom(...)` is the sampling API. Paratemporal drivers enumerate branches; do not call `selectFrom` from `advanceLeafFront`.

## Supporting utilities (same package)

| Class | File | Role |
|-------|------|------|
| `DiscretizeDistribution` | `DiscretizeDistribution.java` | `subSetProbsUniform(numGranules)` returns a `HashMap<Double, Double>` mapping representative values to uniform granule probabilities (used by Volume). |
| `ChangeToCTM`, `CTM`, `CTMToDeterministic`, `MapToDeterministic` | (legacy) | Continuous-time Markov helpers; not used by the tree-expansion pattern. |

## `com.ms4systems.devs.paradevs.quantized.*`

See `quantized-package-cheatsheet.md` for the four classes in this sub-package (`Quantizer`, `QuantizedOperatorTable`, `GDomainConverter`, `ThresholdClassifier`).

## Minimum classpath for generated code

Compile a skill-generated Node + Driver against:

```
$CLOSURE_DEVS_JAR : $PARADEVS_SRC
```

where

- `CLOSURE_DEVS_JAR` → `~/.m2/repository/com/ms4systems/Closure-DEVS/1.0.0/Closure-DEVS-1.0.0.jar` (default)
- `PARADEVS_SRC` → `~/DEVS/paradevs/Closure-DEVS-paradevs-plugin/src` (default; alternately `PARADEVS_JAR` if a built JAR is available)

Both env vars are overrideable. The skill's Stage 4 VALIDATE step reads these.
