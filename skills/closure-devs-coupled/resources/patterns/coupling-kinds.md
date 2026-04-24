# Coupling kinds (EIC / IC / EOC)

Closure-DEVS provides **two** coupling APIs on `com.ms4systems.devs.core.model.impl.Coupled`, and both are fully supported. The skill picks one per generated coupled based on the APPROVE-stage `coupling_style` flag.

## API 1 — generic `addCoupling(Port, Port)` ("port style")

One method, three directional uses. The coupling's kind is inferred from whether each end is an external (`this.`) port or a child port:

```java
addCoupling(this.in,      transd.solved);   // EIC — this.* on the left
addCoupling(genr.out,     transd.ariv);     // IC  — neither end touches this.*
addCoupling(transd.out,   this.result);     // EOC — this.* on the right
```

Requires the child to expose its port as a `public final Port` field (or equivalent) so the caller can reference `child.fieldName`. The upside is a compile-time check that the field exists.

## API 2 — dedicated string-based methods ("string style")

Three separate methods, one per coupling kind. Each argument is a `String` naming a child or a port:

```java
// EIC — fromOuterPortName, toChildName, toChildPortName
addExternalInputCoupling("in",  "transd", "solved");

// IC — fromChildName, fromPortName, toChildName, toPortName
addInternalCoupling("genr", "out", "transd", "ariv");

// EOC — fromChildName, fromPortName, toOuterPortName
addExternalOutputCoupling("transd", "out", "result");
```

There is also a general 4-arg variant `addCoupling(String, String, String, String)` that's equivalent to `addInternalCoupling(...)` — prefer the dedicated name, it reads better.

Works regardless of whether the child exposes ports as public fields, because the simulator resolves names at construction time via `getComponentWithPartName`, `getInputPort`, and `getOutputPort`. The downside is that typos in child or port names don't surface until runtime (the constructor throws an exception when the lookup fails).

## Choosing between the two

The skill's default:
- If **every** child in the inventory exposes its referenced ports as public fields → `port` style.
- If **any** child hides its ports (registers them in the constructor with no corresponding field) → `string` style for the whole coupled, to keep the generated code consistent.

The user can override at APPROVE. Mixing styles within a single coupled is legal but discouraged — pick one.

## Direction cheatsheet (port style)

| Left operand starts with | Right operand starts with | Kind |
|---|---|---|
| `this.` | `<childVar>.` | EIC |
| `<childVar>.` | `<childVar>.` | IC |
| `<childVar>.` | `this.` | EOC |
| `this.` | `this.` | **bug** — two external ports wired together |
| `<childVar>.` | (same `<childVar>.`) | suspicious self-loop — flag it |

## Direction cheatsheet (string style)

| Call | Kind |
|---|---|
| `addExternalInputCoupling(outerPort, childName, childPort)` | EIC |
| `addInternalCoupling(childA, portA, childB, portB)` | IC |
| `addExternalOutputCoupling(childName, childPort, outerPort)` | EOC |

The method name **is** the kind — no ambiguity. This is why string style is often preferred for auto-generated code.

## Why separate blocks in the generated constructor

Even though the coordinator doesn't care, the generated code puts EIC, IC, and EOC in separately-labeled blocks because:

1. **Reviewability.** A human scanning the constructor can tell at a glance which edges cross the boundary of the coupled and which are purely internal.
2. **Typo detection.** An EOC string-call with its ports reversed reads wrong in the EOC block but would hide in an unordered list.
3. **Direction check.** For port-style, each block has a recognizable shape (EIC starts with `this.`, EOC ends with `this.`, IC never touches `this`). Deviations are wiring bugs.

## The `addCoupling` signatures (reference)

Declared on `com.ms4systems.devs.core.model.impl.Coupled`:

```java
public void addCoupling(Port fromPort, Port toPort);
public void addCoupling(String fromChild, String fromPort, String toChild, String toPort);
public void addInternalCoupling(String fromChild, String fromPort, String toChild, String toPort);
public void addExternalInputCoupling(String fromOuterPort, String toChild, String toPort);
public void addExternalOutputCoupling(String fromChild, String fromPort, String toOuterPort);
```

All forms return `void` and register one coupling each. Payload-type checking notes live in `port-compatibility.md`.
