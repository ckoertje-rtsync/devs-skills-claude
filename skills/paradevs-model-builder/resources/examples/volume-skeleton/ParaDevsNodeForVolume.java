package examples.volume;

import java.util.HashMap;

/**
 * Paratemporal tree node for the Volume model (n-sphere volume by granule
 * discretization).
 *
 * <p>Mirrors {@code ParaDEVSNodeForVolume} in the reference test suite
 * ({@code test/Models/java/reference/paradevs/}). Single continuous
 * accumulator {@code partialSumSq} equals the sum of squared coordinates so
 * far; the termination predicate {@code partialSumSq > 1.0} identifies points
 * outside the unit sphere and can be early-eliminated during expansion.
 */
public class ParaDevsNodeForVolume {

    // ------------------------------------------------------------------ //
    //  Tree bookkeeping
    // ------------------------------------------------------------------ //

    public final HashMap<String, ParaDevsNodeForVolume> children = new HashMap<>();
    public String  name;
    public int     level;
    public double  prob = 1.0;
    public double  pathProb = 1.0;
    public boolean terminated;

    // ------------------------------------------------------------------ //
    //  Domain state
    // ------------------------------------------------------------------ //

    public double partialSumSq;

    public ParaDevsNodeForVolume(String nm) {
        name = nm;
        partialSumSq = 0.0;
    }

    /**
     * Branch by adding one coordinate dimension. {@code rep} is the granule
     * representative in {@code [-1, 1]}.
     */
    public ParaDevsNodeForVolume setBranchDim(int dim, double rep, double prob) {
        String label = dim + ":" + String.format("%+.3f", rep);
        ParaDevsNodeForVolume n = new ParaDevsNodeForVolume(name + label);
        children.put(label, n);
        n.level = level + 1;
        n.prob = prob;
        n.pathProb = pathProb * prob;
        n.partialSumSq = this.partialSumSq + rep * rep;
        return n;
    }

    @Override
    public String toString() {
        return "ParaDevsNodeForVolume{"
                + "name=" + name
                + ", level=" + level
                + ", pathProb=" + String.format("%.6f", pathProb)
                + ", partialSumSq=" + String.format("%.6f", partialSumSq)
                + "}";
    }
}
