    /**
     * Two nodes are equivalent when they sit at the same depth and the
     * discrete state fields match exactly.
     *
     * <p>Use when state is enums, Strings, or deep-equatable collections.
     * See resources/patterns/equivalence-merging.md for details.
     */
    public static boolean equivalent({{NODE_CLASS}} s, {{NODE_CLASS}} t) {
        if (s.level != t.level) return false;
{{EXACT_FIELD_CHECKS}}
        return true;
    }
