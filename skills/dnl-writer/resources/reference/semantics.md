# From DNL to DEVS to the generated Java

The generated class (`AtomicModelImpl` in MS4 Me, `Atomic` in Closure-DEVS) keeps state as `(phase, sigma, variables)` with `ta = sigma`.

| DNL | DEVS | Generated Java |
|---|---|---|
| `to start hold in S for time T!` | initial state, `ta = T` | `String phase = "S"; Double sigma = T;` and `holdIn("S", T)` in `initialize()` |
| `to start passivate in S!` | initial state, `ta = ∞` | `passivateIn("S")` in `initialize()` |
| `hold in S for time T!` | `ta(S) = T` | `holdIn("S", T)` wherever `S` is entered |
| `passivate in S!` | `ta(S) = ∞` | `passivateIn("S")` wherever `S` is entered |
| `after S output M!` | `λ(S)` includes a message on `outM` | branch of `getOutput()` on `phaseIs("S")`: `output.add(outM, null)` by default, or the `output event for S` tag |
| `from S go to N!` | `δ_int(S) = N` | branch of `internalTransition()` on `phaseIs("S")`: `holdIn("N", ta(N))` or `passivateIn("N")`, then the `internal event for S` tag |
| `when in S and receive M go to N!` | `δ_ext((S, e), x)` with `x` on `inM` gives `N` | in `externalTransition(e, bag)`: `sigma -= e;` then inside `phaseIs("S")`: `if (bag.hasMessages(inM)) { holdIn("N", ta(N)); <tag>; return; }` |
| `… go to S eventually!` | `δ_ext((S, e), x) = (S, σ − e)` | the same branch without the `holdIn` |
| `external event with M <%…%>!` | a port handler applied in every state before the state-specific part | `if (bag.hasMessages(inM)) { <tag> }` before the phase checks, no `return` |
| (always) | `δ_con(s, x) = δ_ext(δ_int(s), 0, x)` | `confluentTransition`: `internalTransition(); externalTransition(0, input);` |
| `accepts input on M [with type T]!` | element of `X` | `public final Port<T> inM = addInputPort("inM", T.class);` |
| `generates output on M [with type T]!` | element of `Y` | `public final Port<T> outM = addOutputPort("outM", T.class);` |
| `use V with type T and default "E"!` | component of `S` | `protected T V = E;` reset to `E` in `initialize()` |
| `C has f1, f2!` + `range of C's f1 is T!` | message type | `C.java`: fields, no-arg and all-fields constructors, getters, setters, `toString` |

## Event ordering

1. **Internal event**: `getOutput()` runs first, keyed on the phase being left; then `internalTransition()`; then `getTimeAdvance()` returns the new `sigma`.
2. **External event**: `sigma` is reduced by the elapsed time; the phase-less `external event with M` blocks run for every port present in the bag, in file order; then the first `when in S and receive M` whose port has messages fires (file order is priority) and the method returns. Other ports in the same bag are ignored by the state-specific part.
3. **Confluent event**: internal first, then external with elapsed time 0. Not configurable from DNL.

Consequences for the author:

- Output is produced only by hold states, only when their time elapses. "Reply to an input" is an external transition into a `hold in X for time 0` state that outputs and moves on.
- `go to S` from `S` restarts the timer; `eventually` keeps it.
- Two `when in S and receive …` lines for the same state are prioritised by file order when both ports have messages in one bag.

## Port naming across files

MS4 Me prefixes `in` and `out` (pp. 162–164). The DNL writes the bare message name; the Java field is `inM` / `outM`; an SES coupling `X sends M to Y` resolves to `outM` on `X` and `inM` on `Y`, and `… to Y as R` to `inR`. Keep the bare name identical across the `.dnl` and the `.ses`.

## Generated-file identity

The generated Java begins with a comment naming the source `.dnl` and a hash, which must not be edited. Regions `//ID:SVAR:n`, `//ID:INP:n`, `//ID:OUTP:n`, `//ID:INIT`, `//ID:TRA:S`, `//ID:INT:S`, `//ID:OUT:S`, `//ID:EXT:S:inM`, `//ID:EXT::inM`, `//ID:LIB:0`, `//ID:CUST:0`, `//ID:VAR:C:n`, each closed by `//ENDID`, hold the user's fragments. A `javac` error inside such a region points back to the tag block or statement that produced it.
