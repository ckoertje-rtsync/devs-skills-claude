    // ------------------------------------------------------------------ //
    //  Log-space probability accumulation helpers
    //  (spliced when prob_accumulation = log; see
    //   resources/patterns/log-space-accumulation.md)
    // ------------------------------------------------------------------ //

    /** Log of zero probability mass. */
    public static final double LOG_ZERO = Double.NEGATIVE_INFINITY;

    /**
     * Numerically stable {@code log(exp(a) + exp(b))}.
     *
     * <p>Used wherever the product-mode code would add two {@code pathProb}
     * values (equivalence merging, leaf/terminal aggregation).
     */
    public static double logAdd(double a, double b) {
        if (a == LOG_ZERO) return b;
        if (b == LOG_ZERO) return a;
        double m = Math.max(a, b);
        return m + Math.log1p(Math.exp(Math.min(a, b) - m));
    }

    /**
     * Log-sum-exp over every node in {@code nodes}; returns
     * {@code log(sum(exp(n.logPathProb)))} without leaving log space.
     */
    public static double logSumPathProb(java.util.Collection<{{NODE_CLASS}}> nodes) {
        double acc = LOG_ZERO;
        for ({{NODE_CLASS}} n : nodes) {
            acc = logAdd(acc, n.logPathProb);
        }
        return acc;
    }
