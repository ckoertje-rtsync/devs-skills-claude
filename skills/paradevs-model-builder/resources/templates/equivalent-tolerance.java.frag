    /** Equivalence tolerance on continuous accumulators. */
    public static final double EPS = {{TOLERANCE_EPS}};

    /**
     * Two nodes are equivalent when they sit at the same depth and their
     * continuous state fields agree to within {@code EPS}.
     *
     * <p>Use when state is a floating-point accumulator prone to drift.
     * Combine with exact checks for any discrete fields.
     * See resources/patterns/equivalence-merging.md for details.
     */
    public static boolean equivalent({{NODE_CLASS}} s, {{NODE_CLASS}} t) {
        if (s.level != t.level) return false;
{{TOLERANCE_FIELD_CHECKS}}
        return true;
    }
