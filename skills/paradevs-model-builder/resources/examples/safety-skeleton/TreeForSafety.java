package examples.safety;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import com.ms4systems.devs.paradevs.ParaDevsTree;
import com.ms4systems.devs.paradevs.quantized.QuantizedOperatorTable;
import com.ms4systems.devs.paradevs.quantized.ThresholdClassifier;

/**
 * Paratemporal tree driver for a quantized-evidence safety model.
 *
 * <p>Demonstrates the full quantized pipeline: per-branch quantized
 * observations are combined via a probabilistic-sum table; histograms
 * enable symmetry-merging; terminals are classified into action labels by
 * the highest-exceeded-threshold rule.
 */
public class TreeForSafety {

    public static final int G = 10;
    public static final QuantizedOperatorTable T = QuantizedOperatorTable.forProbabilisticSum(G);
    public static final int[]    THRESHOLDS    = {3, 6, 8};
    public static final String[] ACTION_LABELS = {"normal", "caution", "warning", "critical"};
    public static final ThresholdClassifier<String> CLASSIFIER =
            new ThresholdClassifier<>(THRESHOLDS, ACTION_LABELS);

    public static boolean useEquivalence = true;
    public static int defaultDepth = 4;

    private TreeForSafety() {}

    public static ParaDevsTree makeTreeToDepth(int maxDepth) {
        ParaDevsTree bt = new ParaDevsTree();
        ParaDevsNodeForSafety root = new ParaDevsNodeForSafety("root", G);
        root.level = 0;
        root.pathProb = 1.0;
        bt.root = root;
        bt.leaves = new HashSet();
        bt.leaves.add(root);
        bt.interiors = new HashSet();
        bt.terminals = new HashSet();

        for (int d = 0; d < maxDepth; d++) {
            advanceLeafFront(bt, d);
        }
        return bt;
    }

    @SuppressWarnings("unchecked")
    public static void advanceLeafFront(ParaDevsTree bt, int layer) {
        HashSet<ParaDevsNodeForSafety> newLeaves = new HashSet<>();
        double[] probs = evidenceProbs(layer);

        for (Object o : bt.leaves) {
            ParaDevsNodeForSafety n = (ParaDevsNodeForSafety) o;
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

    public static HashMap<String, Double> actionPMF(ParaDevsTree bt) {
        HashMap<String, Double> pmf = new HashMap<>();
        for (String lbl : ACTION_LABELS) pmf.put(lbl, 0.0);
        for (Object o : bt.leaves) {
            ParaDevsNodeForSafety n = (ParaDevsNodeForSafety) o;
            String action = CLASSIFIER.labelFor(n.evidenceLevel);
            pmf.merge(action, n.pathProb, Double::sum);
        }
        return pmf;
    }

    public static void main(String[] args) {
        int depth = (args.length > 0) ? Integer.parseInt(args[0]) : defaultDepth;

        assert T.verifyHomomorphism((v1, v2) -> v1 + v2 - v1 * v2, 1)
                : "QuantizedOperatorTable failed homomorphism check";

        long start = System.currentTimeMillis();
        ParaDevsTree bt = makeTreeToDepth(depth);
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
