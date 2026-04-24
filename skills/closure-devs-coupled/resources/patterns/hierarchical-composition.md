# Hierarchical composition (coupled inside coupled)

DEVS is closed under coupling: any coupled model is itself a legal child of another coupled. Closure-DEVS reflects this at the class level — `Atomic` and `Coupled` both extend the same base class `com.ms4systems.devs.core.model.impl.DEVS`. As a result, the `addChildModel` method is declared on `DEVS`:

```java
public class Coupled extends DEVS {
    public void addChildModel(DEVS model);            // accepts Atomic or Coupled
    public void addCoupledChildModel(Coupled model);  // dedicated coupled variant (optional)
    ...
}
```

Both methods add the child to the parent's simulation list. `addCoupledChildModel` is syntactic sugar that makes the composition visible in a diff — functionally equivalent to `addChildModel` when the argument is a `Coupled`.

## Canonical hierarchical pattern

`Efp` (top-level) embeds `Ef` (embeddable coupled) alongside `Proc` (atomic):

```java
public class Efp extends Coupled {
    public Efp() {
        super("Efp");
        Proc proc = new Proc("Proc", 25);
        Ef   ef   = new Ef("Ef",   10, 100);     // Ef extends Coupled

        addChildModel(proc);                      // atomic child
        addCoupledChildModel(ef);                 // coupled child — clarifies hierarchy

        addCoupling(ef.out,   proc.in);           // Coupled -> Atomic
        addCoupling(proc.out, ef.in);             // Atomic  -> Coupled
    }
}
```

From `Efp`'s perspective, `Ef` is opaque — its internal children and wiring are invisible, only its external ports matter.

## What the skill does

- Treat a child declared as `coupled` exactly like an atomic in Stage 1's Coupled Context — the port inventory is what counts, not the internal structure.
- Flag the child with `kind = coupled` in the APPROVE summary so the user sees the composition is hierarchical.
- Generate `addCoupledChildModel(child)` rather than `addChildModel(child)` for coupled children. Both work; the dedicated method reads better.
- Do **not** try to peek inside a child coupled's children. That's the outer coupled's problem, not this one's.

## Recursion guard

A coupled cannot include itself (directly or transitively) — this would be an infinite simulation tree. If the discovery stage sees the target class mentioned in its own children list, stop and ask. The same user-typed name showing up twice (e.g., two instances of the same atomic with different variable names) is fine — that's multiplicity, not recursion.

## Flat vs deep — when to split

This skill produces one coupled per invocation. If the composition has a natural sub-system that could be its own coupled, mention it in the APPROVE summary but don't force the split — the user decides whether to invoke the skill recursively to carve out sub-coupleds first, then wire them in at the top level. The `ses-designer` skill's aspect/specialization structure is a good upstream guide for where those splits should live.

## Related Closure-DEVS helper

`com.ms4systems.devs.core.model.impl.ClosureToAtomic` provides `ClosureToAtomic.convertAtomic(Coupled inner)` — it wraps a coupled as an opaque atomic for cases where the containing coordinator wants a flat children list. This is an optimization for static hierarchies; the skill does not emit it by default. The generated `addCoupledChildModel(inner)` call is the readable, idiomatic form.
