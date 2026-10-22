# Generated node + tree API cheatsheet

Generated code is **self-contained plain Java** — no base class, no external library, no classpath. The node class carries its own tree bookkeeping fields; the driver class doubles as the tree container. This page is the contract both templates implement.

The shape mirrors the `ParaDevsNode` / `ParaDevsTree` pair from the Closure-DEVS paradevs plugin that the reference models extend (see `reference-models-index.md`), minus the parts the tree-expansion idiom never uses.

## Node class `ParaDevsNodeFor{Domain}`

### Bookkeeping fields

| Field | Type | Default | Purpose |
|-------|------|---------|---------|
| `children` | `HashMap<String, ParaDevsNodeFor{Domain}>` | empty, `final` | Branch label → child node. Filled by `setBranch*`. |
| `name` | `String` | set in constructor | Human-readable node name. Children's names are `parent.name + label`. |
| `level` | `int` | `0` | Depth from root. Set in `setBranch*` as `level = parent.level + 1`. |
| `prob` | `double` | `1.0` | Single-branch probability `Pr(branch \| parent)`. |
| `pathProb` | `double` | `1.0` | Cumulative probability from root, `parent.pathProb × prob`. The default is `1.0`, so a root built with the plain constructor is already correct; drivers still set it explicitly for readability. Nodes add `logPathProb` when `prob_accumulation = log` (see `patterns/log-space-accumulation.md`). |
| `terminated` | `boolean` | `false` | Marks the node as a terminal outcome (do not expand further). |

Domain state fields follow the bookkeeping block.

### Methods

| Signature | Purpose |
|-----------|---------|
| `ParaDevsNodeFor{Domain}(String nm)` | Name + domain-state defaults. The evidence variant takes `(String nm, int G)`. |
| `ParaDevsNodeFor{Domain} setBranch{X}(String label, /* branch params */, double prob)` | Creates the child, installs it in `children`, sets `level` / `prob` / `pathProb`, copies and mutates domain state. One overload per branch shape. |
| `String toString()` | Debug format including domain fields. |

### Convention

```java
public class NodeForX {
    public final HashMap<String, NodeForX> children = new HashMap<>();
    public String  name;
    public int     level;
    public double  prob = 1.0;
    public double  pathProb = 1.0;
    public boolean terminated;

    // domain fields
    public double partialSumSq;

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

## Driver class `TreeFor{Domain}` (also the tree container)

### Instance fields

| Field | Type | Default | Purpose |
|-------|------|---------|---------|
| `root` | `ParaDevsNodeFor{Domain}` | `null` | Tree root; set by `makeTreeToDepth`. |
| `leaves` | `HashSet<ParaDevsNodeFor{Domain}>` | empty | Current BFS frontier. Replaced every iteration of `advanceLeafFront`. |
| `interiors` | `HashSet<ParaDevsNodeFor{Domain}>` | empty | Nodes that have been expanded. |
| `terminals` | `HashSet<ParaDevsNodeFor{Domain}>` | empty | Outcome nodes that will not be expanded further. |

All three sets are typed — iterate them directly with no casts.

### Static fields and methods

| Signature | Purpose |
|-----------|---------|
| `static boolean useEquivalence` | Merge equivalent children when `true`. |
| `static int defaultDepth` | Depth used by `main` when no argument is given. |
| `static TreeFor{Domain} makeTreeToDepth(int maxDepth)` | Builds the root and runs `advanceLeafFront` `maxDepth` times. |
| `static void advanceLeafFront(TreeFor{Domain} bt, int layer)` | Expands every leaf one layer, merging equivalents into `newLeaves`, then swaps `bt.leaves`. |
| `static ParaDevsNodeFor{Domain} equivalentTo(leaf, HashSet<…> nodes)` | Linear scan for an equivalent node, or `null`. |
| `static boolean equivalent(s, t)` | The approved equivalence predicate (exact / tolerance / histogram). |
| `static void main(String[] args)` | Optional harness. |

Evidence-integrated drivers add the quantization helpers — see `quantized-helpers-cheatsheet.md`.

## Gotchas

- Never sample. Paratemporal drivers enumerate every branch; there is no `selectFrom` / `Random` in generated code.
- When converting legacy source that extends the plugin's `ParaDevsNode`, fold the inherited fields above into the node class, drop `cm` / `key` / `path`, and replace `ParaDevsTree bt` with `TreeFor{Domain} bt` (typed sets, no casts).

## Classpath

None. Compile with bare `javac`:

```bash
javac -d out {NodeClass}.java {DriverClass}.java
java -cp out {package}.{DriverClass}
```
