# Port payload compatibility

Closure-DEVS ports are **not generic**. `com.ms4systems.devs.core.message.impl.Port` is a plain class with an optional `Class type` field set via `addOutputPort(name, Class)` / `addInputPort(name, Class)`. The coupling APIs are:

```java
public void addCoupling(Port fromPort, Port toPort);
public void addInternalCoupling(String fromChild, String fromPort, String toChild, String toPort);
public void addExternalInputCoupling(String fromOuterPort, String toChild, String toPort);
public void addExternalOutputCoupling(String fromChild, String fromPort, String toOuterPort);
```

None of these check that the source port's type is assignable to the destination's. Payloads travel as `Object` through `Message.getData()`, and a downstream `externalTransition` cast will `ClassCastException` if the types don't match. The skill has to check this itself.

## The safe rule

A coupling from a port with type `A` to a port with type `B` is safe iff **`A` is assignable to `B`** — i.e. `B.isAssignableFrom(A)` at runtime. Practically:

- Same type on both sides → always safe.
- Subtype source, supertype sink → safe. A `Job` flowing into a port typed `Object` or `Serializable` works, because the recipient only sees it through `Message.getData()` and its own cast.
- Supertype source, subtype sink → **unsafe**. An `Object` reaching a port typed `Job` will blow up when the receiver casts.
- Unrelated types → **unsafe** and almost certainly a wiring bug.
- Either side untyped (no `Class` passed to `addInputPort` / `addOutputPort`) → assignability unknown; warn but don't block.

## Conservative assignability (what the skill enforces)

The skill has class names as strings, not the actual `Class` objects, so it uses a conservative textual check:

- Identical type names → OK.
- One side is untyped → warn, don't block.
- One side is `Object` or `Serializable` → OK (common for sinks).
- Names differ and neither is `Object` / `Serializable` → **stop and ask**. Reference this file in the prompt.

## Idiomatic workarounds

If the user really wants to wire an incompatible pair — common when a child uses a generic payload like `Message` or a wrapper type — document the intent in the description and confirm at APPROVE. The safer pattern is a small adapter atomic between the two children that consumes one type and emits the other.

## Why the API doesn't check

The coupling list is just a `RoutingTableImpl` of port pairs. The `Class` on each `Port` is used by the routing table for debugging and for `getCompatibleInputPorts(Class)` queries, but not for validating `addCoupling` calls at construction time. The first symptom of a type mismatch is a `ClassCastException` during `externalTransition` in a live run — expensive to trace. Catching it at APPROVE is cheap.

## About `Port<T>` (from other jars)

If you see `Port<Job>` generics in a codebase that references `com.ms4systems.devs.core.model.impl.CoupledModelImpl`, those come from a separate MS4ME jar (not the Closure-DEVS jar). Closure-DEVS ports are raw `Port`. Don't back-port the generics into generated Closure-DEVS code — it won't compile against the Closure-DEVS 1.0.0 jar.
