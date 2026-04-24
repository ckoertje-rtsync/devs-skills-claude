    /**
     * Two nodes are equivalent when they sit at the same depth and their
     * histograms of per-path quantized levels match exactly.
     *
     * <p>This is the symmetry-merging predicate that compresses the
     * {@code G^n} sequence space to {@code C(n+G-1, G-1)} distinct histograms
     * when the combining operator is order-independent under the quantizer
     * (EvidenceAppendix.pdf §C.1–C.2).
     *
     * <p>Requires {@code useHistogram = true} on the driver so nodes carry
     * {@code histogram != null}.
     * See resources/patterns/equivalence-merging.md for the scalar fallback.
     */
    public static boolean equivalent({{NODE_CLASS}} s, {{NODE_CLASS}} t) {
        if (s.level != t.level) return false;
        if (s.histogram == null || t.histogram == null) {
            // Fallback when histogram tracking is off.
            return s.evidenceLevel == t.evidenceLevel;
        }
        return java.util.Arrays.equals(s.histogram, t.histogram);
    }
