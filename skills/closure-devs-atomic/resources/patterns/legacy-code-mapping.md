# Converting predefined code into a DEVS atomic

When the user supplies existing source code (any language) and asks to convert it into a Closure-DEVS atomic model, apply this heuristic mapping.

## Step 1 — identify the state manifold

Look for persistent fields / module-level variables in the source. These become either:

- **Phase** (a qualitative discrete mode, e.g. `"idle" | "working" | "error"`) — encoded as the `String phase` field.
- **Continuous state variables** — stay as typed instance fields alongside `phase`.

If the source has an explicit enum-like state variable (`State current`, `mode = "..."`), that maps directly onto `phase`. If it has a collection of booleans (`isRunning`, `hasError`), collapse the cartesian product into named phases.

## Step 2 — identify the event sources

- **User function calls / external API hits** → input ports. Each distinct entry point becomes one port (named after the entry function). The message payload is the function's argument.
- **Timers / `setTimeout` / scheduled callbacks / polling loops** → internal events, driven by `holdIn(phase, dt)`.
- **Side effects (logs, emits, callbacks out)** → output ports. Each distinct sink becomes one port.

## Step 3 — identify the time-advance rule

- Explicit sleep/delay constants → literal sigmas (`holdIn(phase, 2.5)`).
- `while(true) { work(); sleep(T); }` → `holdIn("working", T)` in internal transition.
- Event-driven, no internal timer → `passivate()`; activity is purely from externalTransition.
- Exponential/stochastic waits → this is a **Markov** model — use the Markov template, NOT the deterministic one.

## Step 4 — map control flow into transitions

| Source pattern | DEVS target |
|---|---|
| `if (msg == X) { state = A; timer = T; }` in an event handler | `if (!input.getMessages(xPort).isEmpty()) holdIn("A", T);` in `externalTransition` |
| `if (timer expires) { state = B; }` | `if (phaseIs("prev")) holdIn("B", ...);` in `internalTransition` |
| `emit(E, payload)` right before state change | `output.add(ePort, payload);` in `getOutput()` keyed on the outgoing phase |
| Early return / short-circuit error handling | Separate `"error"` phase with `passivate()` or recovery `holdIn` |

## Step 5 — flag what doesn't translate

Some source patterns don't map cleanly. Call these out in the APPROVE stage summary rather than silently dropping them:

- **Concurrency primitives** (threads, locks, async tasks): DEVS atomic models are sequential state machines. Multi-threaded sources usually decompose into *multiple* atomics within a coupled model (out of scope for this skill — defer to devs-builder).
- **Unbounded data structures with time-dependent mutations** (e.g., queues accumulating without a processing rate): fine as a state variable, but make sure the time-advance rule accounts for it (otherwise the model is ill-defined).
- **Floating-point state with feedback dynamics** (differential equations): DEVS atomic isn't the right formalism — use PowerDEVS instead.

## Naming convention for conversions

When the source class is named `Foo`, the generated DEVS atomic should be named `FooAtomic` unless the user specifies otherwise. This keeps the original and the DEVS version distinguishable if both live in the same package.
