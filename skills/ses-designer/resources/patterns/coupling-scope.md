# Pattern — Coupling Scope

MS4 Me restricts where a coupling may run. The `ses-designer` skill enforces these rules at DISCOVER time; violations are hard errors in the validator.

## Legal coupling endpoints

A coupling `from the {ASPECT} perspective, {X} sends {M} to {Y}!` is legal only when one of these is true:

1. `{X}` is the parent of `{Y}` in `{ASPECT}`, or `{Y}` is the parent of `{X}` — external input / external output coupling.
2. `{X}` and `{Y}` are siblings under `{ASPECT}` — internal coupling.
3. `{X}` and `{Y}` are two replicas (or groups of replicas) inside the same multi-aspect whose containing aspect is `{ASPECT}` — multi-aspect coupling (with `all`/`each` qualifiers).

If the lowest common ancestor of `{X}` and `{Y}` has them under *different* aspects, the coupling is illegal even if both entities exist.

## How the skill picks endpoints

When DISCOVER asks *"who sends a message to whom?"*, the choices offered to the user are filtered by scope:

- The user picks an aspect first.
- The list of candidate senders is that aspect's parent plus its children (plus any multi-aspect unit beneath them).
- The list of candidate receivers is the same set, minus the chosen sender.
- For multi-aspect endpoints, the skill offers `all X` / `each X` qualifiers only when `X` is a multi-aspect unit.

## No self-coupling

`{X} sends M to {X}!` is rejected. The `all X sends M to all X!` form is legal but MS4 Me silently drops the diagonal (a replica never sends to itself).

## Cross-aspect messaging — how to do it properly

If two entities truly need to exchange messages but live in different aspects of a shared parent, restructure: introduce a joint aspect that contains both (using `is made of` with both as children) and couple there. Do not try to bridge with a direct coupling under one of the original aspects.

## Examples

**Legal** (sibling under aspect `top`):

```
from the top perspective, SimpleWorkFlow is made of GeneratorOfJobs, Processors and Transducer!
from the top perspective, GeneratorOfJobs sends Job to Processors!
```

**Legal** (parent → child under aspect `Dialog`):

```
from the Dialog perspective, HelloWorldSES is made of HelloWorld and BackAtYou!
from the Dialog perspective, HelloWorld sends Hello to BackAtYou!
```

**Legal** (multi-aspect, all-to-all):

```
from the top perspective, SimpleWorkFlow is made of GeneratorOfJobs, Processors and Transducer!
from the multiProcessor perspective, Processors are made of more than one Processor!
Processor can be id in index!
from the top perspective, GeneratorOfJobs sends Job to all Processor!
```

**Illegal** (cross-aspect — no shared ancestor aspect):

```
from the structural perspective, Car is made of Engine and Chassis!
from the electrical perspective, Car is made of Battery and Harness!
from the structural perspective, Engine sends Power to Battery!   <-- REJECTED
```

`Battery` is a child of `Car` under `electrical`, not `structural`. Fix by moving `Battery` (or a proxy) into the `structural` aspect, or by introducing a third aspect containing both.
