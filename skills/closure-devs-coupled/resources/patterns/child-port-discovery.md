# Child port discovery

To wire children accurately, the skill needs each child's port inventory: name, type, direction, and **whether the port is exposed as a `public` field**. Field-exposure decides whether the coupled can use port-style `addCoupling(Port, Port)` or has to fall back to string-style (`addExternalInputCoupling(...)`, etc. — see `coupling-kinds.md`).

## Primary source: the child's `.java`

When the user hands the skill a child `.java` source path, scan for the following patterns. These cover every idiom seen in Closure-DEVS models.

### Typed, field-exposed port (preferred)

```java
public final Port in  = addInputPort("in",  Job.class);
public final Port out = addOutputPort("out", Job.class);
```

Extract:
- `name` — the left-hand identifier (`in`, `out`).
- `direction` — `input` if `addInputPort`, `output` if `addOutputPort`.
- `type` — the `Class` literal argument (`Job.class` → `Job`). The port stores this in its `type` field (`Port.getType()`).
- `access` — `field` (reachable as `child.in`, `child.out`).

**Note:** `Port` in Closure-DEVS is **not generic**. There is no `Port<Job>` — always plain `Port`. Do not mistake MS4ME-derived examples (which use `Port<Job>` from a separate `CoupledModelImpl` jar) for Closure-DEVS conventions.

### Untyped, field-exposed port

```java
public final Port start = addInputPort("start");
public final Port stop  = addInputPort("stop");
```

Extract with `type = (untyped)` and `access = field`. Couplings on an untyped port will work with any payload.

### Type-only (not field-exposed)

Some generated models (especially those originating from MS4ME → Closure-DEVS conversion) register ports in the constructor without exposing a field:

```java
public ComputerAtomic(String name) {
    super(name);
    addInputPort("inMemReq",   MemReq.class);
    addOutputPort("outMemResp", MemResp.class);
}
```

Extract `name`, `direction`, `type` from the call. `access = string`. The caller must reference these ports by name — either via `getInputPort("inMemReq")` / `getOutputPort("outMemResp")` for the port-style API, or by passing the string directly to `addExternalInputCoupling` / `addInternalCoupling` / `addExternalOutputCoupling`.

Mixed declarations are possible — a child may expose some ports as fields and register others silently. Record per-port, not per-child.

### Child that is itself a coupled

A child whose class `extends Coupled` exposes its external ports the same way an atomic does — as `public final Port foo = addInputPort(...)` declarations in the class body, outside the constructor. Treat these identically to atomic external ports; the coupling semantics are the same. Mark the child with `kind = coupled` so the generated code can use `addCoupledChildModel(child)` for clarity.

## Fallback: ask the user

If the child `.java` is unreadable, not present, or the patterns above don't match, prompt the user for a port inventory of the form:

```
{child_var} : {ClassName}                  [atomic | coupled]
  in:  {port} : {type or "(untyped)"}
  out: {port} : {type or "(untyped)"}
```

Mark `access = string` by default in this case — safer to assume string-style, which always works, than to assume field-style and fail compile.

## Reconciling with the user's description

The description often references ports by role ("the generator's output", "the transducer's arrival port") rather than by exact name. The skill's job is to resolve each reference to an exact port. Resolution steps, in order:

1. If the description names a field (`genr.out`), and the port inventory has a matching name, use it.
2. If the description names a role, and the port inventory has one port in that direction with an obviously matching name (`out`, `output`, `emit`), suggest it — don't silently pick.
3. If the description references a port not in the inventory, add it to `unresolved[]` and ask in APPROVE.

## Effect on generated code

For each child:

| Every referenced port is `access=field` | Generate style |
|---|---|
| Yes for every child | `port` style — shorter, compile-checked field references |
| No for any child | `string` style — uniform across the whole coupled |

The user can override at APPROVE.
