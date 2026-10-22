package examples.safety;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.DoubleBinaryOperator;

/**
 * Paratemporal tree driver for a quantized-evidence safety model.
 *
 * <p>Demonstrates the full quantized pipeline: per-branch quantized
 * observations are combined via a probabilistic-sum table; histograms
 * enable symmetry-merging; terminals are classified into action labels by
 * the highest-exceeded-threshold rule.
 *
 * <p>An instance of this class is the tree container (root + frontier sets);
 * the static methods build and query it.
 */
public class TreeForSafety {

    public static final int G = 10;
    public static final DoubleBinaryOperator U = (v1, v2) -> v1 + v2 - v1 * v2;
    public static final int[][] T = buildTable(G, U);
    public static final int[]    THRESHOLDS    = {3, 6, 8};
    public static final String[] ACTION_LABELS = {"normal", "caution", "warning", "critical"};

    static {
        checkClassifier(THRESHOLDS, ACTION_LABELS);
    }

    public static boolean useEquivalence = true;
    public static int defaultDepth = 4;

    // ------------------------------------------------------------------ //
    //  Tree container
    // ------------------------------------------------------------------ //

    public ParaDevsNodeForSafety root;
    public HashSet<ParaDevsNodeForSafety> leaves    = new HashSet<>();
    public HashSet<ParaDevsNodeForSafety> interiors = new HashSet<>();
    public HashSet<ParaDevsNodeForSafety> terminals = new HashSet<>();

    // ------------------------------------------------------------------ //
    //  Quantization helpers (EvidenceAppendix.pdf §§A, F.5)
    // ------------------------------------------------------------------ //

    /** {@code Q_G(x) = min(G-1, floor(G*x))}, with {@code x} clamped to {@code [0,1]}. */
    public static int quantize(double x, int G) {
        if (x <= 0.0) return 0;
        if (x >= 1.0) return G - 1;
        return Math.min(G - 1, (int) Math.floor(G * x));
    }

    /** Bin-center representative {@code r_G(k) = (k + 0.5) / G}. */
    public static double representative(int k, int G) {
        return (k + 0.5) / G;
    }

    /** Homomorphic operator table {@code T[i][j] = Q_G(U(r_G(i), r_G(j)))}. */
    public static int[][] buildTable(int G, DoubleBinaryOperator U) {
        int[][] t = new int[G][G];
        for (int i = 0; i < G; i++) {
            for (int j = 0; j < G; j++) {
                t[i][j] = quantize(U.applyAsDouble(representative(i, G), representative(j, G)), G);
            }
        }
        return t;
    }

    /** Highest-exceeded-threshold rule; {@code ACTION_LABELS[0]} when none is exceeded. */
    public static String labelFor(int k) {
        for (int i = THRESHOLDS.length - 1; i >= 0; i--) {
            if (THRESHOLDS[i] <= k) return ACTION_LABELS[i + 1];
        }
        return ACTION_LABELS[0];
    }

    /** Fails class load on a label/threshold length mismatch or unsorted thresholds. */
    static void checkClassifier(int[] thresholds, String[] labels) {
        if (labels.length != thresholds.length + 1) {
            throw new IllegalStateException("ACTION_LABELS.length must be THRESHOLDS.length + 1");
        }
        for (int i = 1; i < thresholds.length; i++) {
            if (thresholds[i] <= thresholds[i - 1]) {
                throw new IllegalStateException("THRESHOLDS must be strictly ascending at i=" + i);
            }
        }
    }

    // ------------------------------------------------------------------ //
    //  makeTreeToDepth / advanceLeafFront
    // ------------------------------------------------------------------ //

    public static TreeForSafety makeTreeToDepth(int maxDepth) {
        TreeForSafety bt = new TreeForSafety();
        ParaDevsNodeForSafety root = new ParaDevsNodeForSafety("root", G);
        root.level = 0;
        root.pathProb = 1.0;
        bt.root = root;
        bt.leaves.add(root);

        for (int d = 0; d < maxDepth; d++) {
            advanceLeafFront(bt, d);
        }
        return bt;
    }

    public static void advanceLeafFront(TreeForSafety bt, int layer) {
        HashSet<ParaDevsNodeForSafety> newLeaves = new HashSet<>();
        double[] probs = evidenceProbs(layer);

        for (ParaDevsNodeForSafety n : bt.leaves) {
            bt.interiors.add(n);

            for (int levelIn = 0; levelIn < G; levelIn++) {
                double p = probs[levelIn];
                if (p <= 0.0) continue;
                ParaDevsNodeForSafety child =
                        n.setBranchEvidence(Integer.toString(levelIn), levelIn, T, p);

                if (useEquivalence) {
                    ParaDevsNodeForSafety equiv = equivalentTo(child, newLeaves);
                    if (equiv == null) {
                        newLeaves.add(child);
                    } else {
                        equiv.pathProb = equiv.pathProb + child.pathProb;
                    }
                } else {
                    newLeaves.add(child);
                }
            }
        }
        bt.leaves = newLeaves;
    }

    /** Uniform over the G levels. Replace with a domain-specific PMF as needed. */
    public static double[] evidenceProbs(int layer) {
        double[] p = new double[G];
        double u = 1.0 / G;
        for (int k = 0; k < G; k++) p[k] = u;
        return p;
    }

    public static ParaDevsNodeForSafety equivalentTo(ParaDevsNodeForSafety leaf,
                                                     HashSet<ParaDevsNodeForSafety> nodes) {
        for (ParaDevsNodeForSafety n : nodes) {
            if (equivalent(n, leaf)) return n;
        }
        return null;
    }

    /** Histogram equivalence — symmetry-merging under the homomorphic operator. */
    public static boolean equivalent(ParaDevsNodeForSafety s, ParaDevsNodeForSafety t) {
        if (s.level != t.level) return false;
        if (s.histogram == null || t.histogram == null) {
            return s.evidenceLevel == t.evidenceLevel;
        }
        return Arrays.equals(s.histogram, t.histogram);
    }

    public static HashMap<String, Double> actionPMF(TreeForSafety bt) {
        HashMap<String, Double> pmf = new HashMap<>();
        for (String lbl : ACTION_LABELS) pmf.put(lbl, 0.0);
        for (ParaDevsNodeForSafety n : bt.leaves) {
            String action = labelFor(n.evidenceLevel);
            pmf.merge(action, n.pathProb, Double::sum);
        }
        return pmf;
    }

    public static void main(String[] args) {
        int depth = (args.length > 0) ? Integer.parseInt(args[0]) : defaultDepth;

        long start = System.currentTimeMillis();
        TreeForSafety bt = makeTreeToDepth(depth);
        long end = System.currentTimeMillis();

        HashMap<String, Double> pmf = actionPMF(bt);
        System.out.println("TreeForSafety: depth=" + depth
                + " G=" + G
                + " leaves=" + bt.leaves.size()
                + " elapsedMs=" + (end - start));
        for (String lbl : ACTION_LABELS) {
            System.out.printf("  %s = %.6f%n", lbl, pmf.get(lbl));
        }
    }
}
