# Markov and probabilistic DNL files

MS4 Me's state-diagram editor generates three Markov kinds (User's Guide pp. 84–112, Appendix 3): Continuous Time Markov (CTM), Discrete Time Markov (DTM) and Markov Matrix (MM), plus finite-probabilistic DEVS (FP-DEVS, pp. 76–79). They are ordinary `.dnl` files with extra conventions. **This skill does not generate them.** It recognises them so that a revision does not break them, and it can still add deterministic parts (ports, variables, plain states) around them.

## Recognising one

- First line `//<DNLType>CTM<DNLType>!` (or `DTM`, `MM`); newer MS4 Me versions only.
- A tag containing a `<ProbDEVS>` XML table inside a Java comment between `<dnl>` markers.
- Hold times written as `hold in S for time "maxTA"!` with `use maxTA with type double and default "Double.MAX_VALUE"!`.
- A variable `use ctm with type ContinuousTimeMarkov and default "new ContinuousTimeMarkov()"!` and calls to `ctm.fillTransitionInfoList(...)` in `initialize variables`.
- Several `from S go to N!` lines for one state, one per probabilistic successor, with the chooser in the internal event: `internalTransitionForMarkov("S", maxTA, "")` (CTM) or `internalTransitionForFPDEVS("S", new String[]{…}, new double[]{…}, new String[]{…})` (FP-DEVS).
- Comment pairs `//<CTM>` … `//<CTM>` and `//<FPDEVS>` … `//<FPDEVS>` bracketing generated code inside tags.

The checker prints one `dnl.markov` info and disables the single-internal-transition rule for the file.

## When revising such a file

- Keep the header line, the `<dnl>` table, the bracketed generated code and every `from` line exactly as they are.
- The transition table is also stored in `src/Models/xml/<Name>.xml`; edit probabilities in MS4 Me's state-diagram editor, not by hand.
- Deterministic additions (new ports, variables, a passive sink, an `add library` block) are safe.

## When the user needs a new stochastic model

Offer, in this order:

1. Write the deterministic skeleton here (states, ports, variables) and let the user add the Markov table in MS4 Me's state-diagram editor.
2. `closure-devs-atomic` with its Markov family (`extends ContinuousTimeMarkov`) for hand-written Java.
3. `paradevs-model-builder` when the question is really about exploring the probability tree rather than sampling it.

## CTM semantics for reference (guide p. 183)

For transitions out of a state with probabilities `p'`, each candidate gets `σ' = −(1/p') ln r` with `r` uniform on (0, 1); the time advance is the minimum and the successor is the candidate that attained it; the self-transition does not compete. DTM uses a fixed step `h` and per-step probabilities `1 − exp(−h·p)` (p. 184). MM iterates a probability vector to a fixed point (pp. 181–182).
