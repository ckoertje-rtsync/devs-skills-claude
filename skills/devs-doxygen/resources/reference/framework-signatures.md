# Where the DEVS elements live, per language and framework

Use this in the DISCOVER stage to find each element of the formal specification
in the source. "Cue" is what `scripts/detect-devs-model.py` greps for to name
the framework. Always read the source itself; the tables say where to look, not
what the model does.

The formal elements to extract for every **atomic** model:
ports (X, Y), state (S: phase, sigma, other variables), initial state,
ta, delta_int, delta_ext, delta_con, lambda, parameters.
For every **coupled** model: external ports (X, Y), components (D, M_d),
EIC, IC, EOC, and Select if the framework has one.

---

## Java

### Closure-DEVS (MS4 Systems) — cue `com.ms4systems.devs`

| Element | Look for |
|---|---|
| Atomic base | `extends Atomic` (deterministic) or `extends ContinuousTimeMarkov` (Markov) |
| Coupled base | `extends Coupled` |
| Ports | `Port` fields initialised with `addInputPort("name", Type.class)` / `addOutputPort(...)` |
| State | `phase` and `sigma` fields declared in the subclass (not inherited) plus any other fields |
| Initial state | `initialize()`: `holdIn(phase, sigma)` or `passivateIn(phase)` |
| ta | `getTimeAdvance()` (usually returns `sigma`) |
| delta_int | `internalTransition()` |
| delta_ext | `externalTransition(double timeElapsed, MessageBag input)`; inputs via `input.getMessages(port)`; `sigma -= timeElapsed` is the elapsed-time bookkeeping |
| delta_con | `confluentTransition(MessageBag input)` |
| lambda | `getOutput()` building a `MessageBag`; `output.add(port, data)` |
| Couplings | `addChildModel(m)`; `addCoupling(src.port, dst.port)`. EIC when the source port belongs to the coupled itself, EOC when the destination port does, IC otherwise |
| Markov atomics | `addTransitionInfo(from, to, rate)` rows: phases are the states, ta is exponential with the outgoing rate sum. Document delta_int as a rate table |

### DEVSJAVA / DEVS-Suite — cue `GenCol`, `ViewableAtomic`, `ViewableDigraph`, `genDevs`

| Element | Look for |
|---|---|
| Atomic base | `extends ViewableAtomic` or `extends atomic` |
| Coupled base | `extends ViewableDigraph` or `extends digraph` |
| Ports | `addInport("name")`, `addOutport("name")` in the constructor |
| State | `phase`, `sigma` inherited; `holdIn(phase, sigma)`, `passivate()`, `passivateIn(phase)` |
| Initial state | `initialize()` |
| ta | `ta()` inherited, returns `sigma` |
| delta_int | `deltint()` |
| delta_ext | `deltext(double e, message x)`; `messageOnPort(x, "port", i)`; `Continue(e)` keeps sigma consistent |
| delta_con | `deltcon(double e, message x)` |
| lambda | `out()` returning a `message`; `makeContent("port", value)` |
| Couplings | `add(model)`; `addCoupling(src, "port", dst, "port")` with `this` for the coupled's own ports |

### xDEVS (Java) — cue `xdevs.core.modeling`

| Element | Look for |
|---|---|
| Bases | `extends Atomic`, `extends Coupled` |
| Ports | `Port<T>` fields; `addInPort(p)`, `addOutPort(p)` |
| State | `phase`, `sigma` inherited; `holdIn(phase, sigma)`, `passivate()` |
| ta | `ta()` |
| delta_int / delta_ext / delta_con | `deltint()`, `deltext(double e)`, `deltcon(double e)`; inputs via `port.getValues()` |
| lambda | `lambda()`; `port.addValue(v)` |
| Couplings | `addComponent(m)`, `addCoupling(srcPort, dstPort)` |

### ParaDEVS tree nodes — cue `ParaDevsNodeFor`

Not DEVS atomics (they are paratemporal tree nodes with branch expansion). Out
of scope for the DEVS tuple pages; if asked, document the class API only
(Doxygen will index it from the source) and say so.

---

## C++

### adevs — cue `adevs::`, `#include "adevs.h"`

| Element | Look for |
|---|---|
| Atomic base | `public adevs::Atomic<X>` where `X` is the port/value type (often `adevs::PortValue<T>`) |
| Coupled base | `adevs::Digraph<T>`, `adevs::SimpleDigraph<T>`, `adevs::Network<X>` |
| Ports | `static const int in, out` (Digraph models); value type from `PortValue<T>` |
| State | private members; sigma is usually a member, passive = `DBL_MAX` or `adevs_inf<double>()` |
| ta | `double ta()` |
| delta_int | `void delta_int()` |
| delta_ext | `void delta_ext(double e, const adevs::Bag<X>& xb)` |
| delta_con | `void delta_conf(const adevs::Bag<X>& xb)` |
| lambda | `void output_func(adevs::Bag<X>& yb)`; `yb.insert(X(port, value))` |
| Couplings | `add(model)`; `couple(src, srcPort, dst, dstPort)` with `this` for external ports |
| Not formal | `gc_output(...)` is memory management; mention under Notes, not in the tuple |

### Cadmium — cue `cadmium`

Cadmium v1 (template/struct style):

| Element | Look for |
|---|---|
| Atomic | `struct Model { ... }` with `using input_ports = std::tuple<...>`, `using output_ports = ...` |
| Ports | `struct in : public cadmium::in_port<T> {}` (or `out_port`) inside `struct defs` |
| State | `struct state_type { ... } state;` |
| ta | `TIME time_advance() const` |
| delta_int | `void internal_transition()` |
| delta_ext | `void external_transition(TIME e, typename make_message_bags<input_ports>::type mbs)`; `cadmium::get_messages<typename defs::in>(mbs)` |
| delta_con | `void confluence_transition(TIME e, ... mbs)` |
| lambda | `typename make_message_bags<output_ports>::type output() const` |
| Coupled | `cadmium::dynamic::modeling::coupled<TIME>` with `iports`, `oports`, `submodels`, `eics`, `ics`, `eocs` built from `make_EIC<...>()`, `make_IC<...>()`, `make_EOC<...>()` |

Cadmium v2 (class style):

| Element | Look for |
|---|---|
| Atomic | `class Model : public cadmium::Atomic<State>` |
| Ports | `cadmium::Port<T> in, out;` registered with `addInPort`, `addOutPort` |
| ta | `double timeAdvance(const State&) const` |
| delta_int | `void internalTransition(State&) const` |
| delta_ext | `void externalTransition(State&, double e) const`; `in->getBag()` |
| delta_con | `confluentTransition(State&, double e) const` (default: internal then external) |
| lambda | `void output(const State&) const`; `out->addMessage(v)` |
| Coupled | `class C : public cadmium::Coupled`; `addComponent<T>(...)`, `addCoupling(from, to)`, `addInPort`, `addOutPort` |

### xDEVS C++ — cue `xdevs`

Same member names as xDEVS Java: `deltint`, `deltext(double e)`, `deltcon(double e)`,
`lambda`, `ta`; `Port` members; `addComponent`, `addCoupling`.

---

## Python

### PythonPDEVS (pypdevs) — cue `pypdevs`

| Element | Look for |
|---|---|
| Bases | `AtomicDEVS`, `CoupledDEVS` from `pypdevs.DEVS` |
| Ports | `self.addInPort("name")`, `self.addOutPort("name")` in `__init__` |
| State | `self.state`, frequently a separate state class; document its attributes as the state variables |
| Initial state | assignments in `__init__` / the state class constructor |
| ta | `timeAdvance(self)`; `INFINITY` from `pypdevs.infinity` |
| delta_int | `intTransition(self)` returns the new state |
| delta_ext | `extTransition(self, inputs)`; `inputs[port]` is a list of values; elapsed time is `self.elapsed` |
| delta_con | `confTransition(self, inputs)`; when not overridden the framework default is internal then external |
| lambda | `outputFnc(self)` returns `{port: [values]}` |
| Couplings | `self.addSubModel(m)`; `self.connectPorts(src, dst)`. EIC when `src` is a port of `self`, EOC when `dst` is, IC otherwise |

### xDEVS Python — cue `xdevs`

| Element | Look for |
|---|---|
| Bases | `Atomic`, `Coupled` from `xdevs.models` |
| Ports | `Port(type, "name")`; `self.add_in_port(p)`, `self.add_out_port(p)` |
| State | `self.phase`, `self.sigma`; `self.hold_in(phase, sigma)`, `self.passivate()` |
| ta | `ta()` |
| delta_int / delta_ext / delta_con | `deltint()`, `deltext(e)`, `deltcon(e)`; `port.values` |
| lambda | `lambdaf()`; `port.add(value)` |
| Couplings | `add_component(m)`, `add_coupling(p_from, p_to)` |

---

## Julia — no established framework, generic cues

| Element | Look for |
|---|---|
| Atomic | `mutable struct X [<: AbstractAtomic]` holding `phase` and `sigma` (or `ta`) fields |
| Ports | usually implicit: Symbol keys of the output `Dict`, the port argument of the external transition, or a `ports` field. Document them as conventions and say so in the ports table |
| ta | `ta(m)`, `time_advance(m)` |
| delta_int | `delta_int!(m)`, `deltint!(m)`, `internal_transition!(m)` |
| delta_ext | `delta_ext!(m, e, x)`; `x` is a bag, vector or Dict keyed by port |
| delta_con | `delta_con!(m, x)`, `delta_conf!(m, x)` |
| lambda | `lambda(m)`, `output(m)` returning `Dict(port => values)` |
| Coupled | a struct holding components plus a couplings vector of `(src, port) => (dst, port)` pairs, or `connect!(...)` calls |

Doxygen cannot parse Julia. `scripts/julia-doxygen-filter.py` turns the file's
skeleton into Python-style pseudo-source so structs, fields, functions and
docstrings appear in the API pages; Doxygen names the namespace after the file
stem (`JobProcessor.jl` -> namespace `JobProcessor`).

---

## Generic fallback (any language)

If no cue matches, find the DEVS functions by name (the `DEVS_FUNCS` pattern in
`detect-devs-model.py`), read the state struct or fields, and treat each distinct
value assigned to the phase variable as a phase. When there is no phase variable
(only sigma), name the phases after the branches of delta_int and say in the
Notes section that the phases are inferred.

## Reading transitions into table rows

- One row per `if`/`switch` branch that changes phase or sigma. A branch that
  only updates variables without changing phase is still a row (`to` = same phase).
- Record the condition literally (`messages on in non-empty`), not paraphrased.
- `sigma -= e` at the top of delta_ext is elapsed-time bookkeeping, not a transition; mention it once under the external-transition table.
- If delta_con is missing, say which default the framework applies.
- Output rows come from the branches of lambda; a phase without a row emits nothing.
