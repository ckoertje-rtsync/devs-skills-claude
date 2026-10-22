package examples.safety;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Paratemporal tree node for a quantized-evidence safety model.
 *
 * <p>Each node carries an integer evidence level in {@code [0, G-1]} and a
 * histogram of per-path level counts. Branches combine a new quantized
 * observation into the running evidence via a precomputed homomorphic lookup
 * table; the histogram enables symmetry-merging equivalence.
 *
 * <p>References:
 *   - resources/patterns/quantized-evidence-integration.md
 *   - docs/paradevs/EvidenceAppendix.pdf §§A, C, F
 */
public class ParaDevsNodeForSafety {

    public final HashMap<String, ParaDevsNodeForSafety> children = new HashMap<>();
    public String  name;
    public int     level;
    public double  prob = 1.0;
    public double  pathProb = 1.0;
    public boolean terminated;

    public int   G;
    public int   evidenceLevel;
    public int[] histogram;

    public ParaDevsNodeForSafety(String nm, int G) {
        name = nm;
        this.G = G;
        this.evidenceLevel = 0;
        this.histogram = new int[G];
    }

    public ParaDevsNodeForSafety setBranchEvidence(String label, int evidenceIn,
                                                   int[][] T, double prob) {
        ParaDevsNodeForSafety n = new ParaDevsNodeForSafety(name + ":" + label, this.G);
        children.put(label, n);
        n.level = level + 1;
        n.prob = prob;
        n.pathProb = pathProb * prob;
        n.evidenceLevel = T[this.evidenceLevel][evidenceIn];
        n.histogram = this.histogram.clone();
        n.histogram[evidenceIn] += 1;
        return n;
    }

    @Override
    public String toString() {
        return "ParaDevsNodeForSafety{"
                + "name=" + name
                + ", level=" + level
                + ", evidenceLevel=" + evidenceLevel
                + ", pathProb=" + String.format("%.6f", pathProb)
                + ", histogram=" + Arrays.toString(histogram)
                + "}";
    }
}
