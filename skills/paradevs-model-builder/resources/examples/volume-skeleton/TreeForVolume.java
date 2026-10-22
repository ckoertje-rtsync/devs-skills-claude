package examples.volume;

import java.util.HashSet;

/**
 * Paratemporal tree driver for the Volume model.
 *
 * <p>Computes the volume of an n-sphere by enumerating granule combinations
 * for each coordinate dimension and merging equivalent partial-sum states.
 * No Monte Carlo — every branch is enumerated deterministically.
 *
 * <p>An instance of this class is the tree container (root + frontier sets);
 * the static methods build and query it.
 *
 * <p>Mirrors {@code ParaDEVSTreeForVolume} from the reference test suite.
 * See {@code resources/patterns/} for each idiom in the skill repo.
 */
public class TreeForVolume {

    /** Coordinates per dimension (uniform granules on [-1, 1]). */
    public static int numGranules = 10;

    /** Drop candidates whose partial state is already outside the sphere. */
    public static boolean earlyElimination = true;

    /** Merge children with matching partialSumSq (within EPS). */
    public static boolean useEquivalence = true;

    /** Default expansion depth (number of coordinate dimensions). */
    public static int defaultDepth = 5;

    // ------------------------------------------------------------------ //
    //  Tree container
    // ------------------------------------------------------------------ //

    public ParaDevsNodeForVolume root;
    public HashSet<ParaDevsNodeForVolume> leaves    = new HashSet<>();
    public HashSet<ParaDevsNodeForVolume> interiors = new HashSet<>();
    public HashSet<ParaDevsNodeForVolume> terminals = new HashSet<>();

    // ------------------------------------------------------------------ //
    //  makeTreeToDepth
    // ------------------------------------------------------------------ //

    public static TreeForVolume makeTreeToDepth(int maxDepth) {
        TreeForVolume bt = new TreeForVolume();
        ParaDevsNodeForVolume root = new ParaDevsNodeForVolume("root");
        root.level = 0;
        root.pathProb = 1.0;
        root.partialSumSq = 0.0;
        bt.root = root;
        bt.leaves.add(root);

        for (int d = 0; d < maxDepth; d++) {
            advanceLeafFront(bt, d);
        }
        return bt;
    }

    // ------------------------------------------------------------------ //
    //  advanceLeafFront
    // ------------------------------------------------------------------ //

    public static void advanceLeafFront(TreeForVolume bt, int dim) {
        HashSet<ParaDevsNodeForVolume> newLeaves = new HashSet<>();

        double[] reps = new double[numGranules];
        double dx = 2.0 / numGranules;
        double uniform = 1.0 / numGranules;
        for (int i = 0; i < numGranules; i++) {
            reps[i] = -1.0 + dx * (i + 0.5);
        }

        for (ParaDevsNodeForVolume n : bt.leaves) {
            bt.interiors.add(n);

            for (int i = 0; i < numGranules; i++) {
                double rep = reps[i];
                if (earlyElimination && n.partialSumSq + rep * rep > 1.0) continue;
                ParaDevsNodeForVolume child = n.setBranchDim(dim, rep, uniform);

                if (useEquivalence) {
                    ParaDevsNodeForVolume equiv = equivalentTo(child, newLeaves);
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

    // ------------------------------------------------------------------ //
    //  Equivalence (tolerance on partialSumSq)
    // ------------------------------------------------------------------ //

    public static final double EPS = 1e-4;

    public static ParaDevsNodeForVolume equivalentTo(ParaDevsNodeForVolume leaf,
                                                     HashSet<ParaDevsNodeForVolume> nodes) {
        for (ParaDevsNodeForVolume n : nodes) {
            if (equivalent(n, leaf)) return n;
        }
        return null;
    }

    public static boolean equivalent(ParaDevsNodeForVolume s, ParaDevsNodeForVolume t) {
        if (s.level != t.level) return false;
        return Math.abs(s.partialSumSq - t.partialSumSq) < EPS;
    }

    // ------------------------------------------------------------------ //
    //  Aggregation
    // ------------------------------------------------------------------ //

    /** Volume estimate = sum of in-sphere leaf pathProbs times hypercube volume 2^n. */
    public static double computeVolume(TreeForVolume bt, int dimensions) {
        double hitProb = 0.0;
        for (ParaDevsNodeForVolume n : bt.leaves) {
            if (n.partialSumSq < 1.0) hitProb += n.pathProb;
        }
        return Math.pow(2.0, dimensions) * hitProb;
    }

    // ------------------------------------------------------------------ //
    //  main harness
    // ------------------------------------------------------------------ //

    public static void main(String[] args) {
        int dims = (args.length > 0) ? Integer.parseInt(args[0]) : defaultDepth;
        if (args.length > 1) numGranules = Integer.parseInt(args[1]);

        long start = System.currentTimeMillis();
        TreeForVolume bt = makeTreeToDepth(dims);
        long end = System.currentTimeMillis();

        double volume = computeVolume(bt, dims);

        System.out.println("TreeForVolume:"
                + " dims=" + dims
                + " numGranules=" + numGranules
                + " leaves=" + bt.leaves.size()
                + " volume=" + String.format("%.6f", volume)
                + " elapsedMs=" + (end - start));
    }
}
