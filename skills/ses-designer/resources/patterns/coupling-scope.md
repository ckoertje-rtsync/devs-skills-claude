# Pattern — Coupling Scope

MS4 Me restricts where a coupling may run (User's Guide p. 176). The `ses-designer` skill enforces these rules at DISCOVER time, and `validate-ses.sh` reports violations as structural errors.

## Legal coupling endpoints

The guide's rule (p. 176): messages pass only between a subsystem and its parent, or between siblings in the same aspect. "The only exception to this rule is that the subsystem of a multi-aspect can send messages to siblings of its parent system."

The exception follows from restructuring. When the `.pes` expands a multi-aspect, the unit's copies become children of the unit's grandparent, inside the grandparent's aspect, and the multi-aspect is removed (p. 121). So in a coupling `from the {ASPECT} perspective, {X} sends {M} to {Y}!`, each of `{X}` and `{Y}` must be:

1. the parent entity of `{ASPECT}` — external input / external output coupling with a child, or
2. a child entity of `{ASPECT}` — internal coupling with a sibling, or
3. the unit of a multi-aspect whose parent is a child of `{ASPECT}` — the exception. The unit then couples with its parent's siblings, with `{ASPECT}`'s parent, and with other such units, usually through `all`/`each` qualifiers.

A generated copy name (`id0_Person`, `tall_Boy`) counts as its unit, and a specialization option counts as its general entity. If no aspect with the named label contains both endpoints this way, the coupling is illegal, even if both entities exist. The multi-aspect's own perspective (`multiProcessor`, `multiPerson`) is never the right aspect to name, because restructuring removes it.

## How the skill picks endpoints

When DISCOVER asks *"who sends a message to whom?"*, the choices offered to the user are filtered by scope:

- The user picks an aspect first.
- The list of candidate senders is that aspect's parent, its children, and the units of any multi-aspects hanging from those children.
- The list of candidate receivers is the same set, minus the chosen sender (unless both ends are `all X`).
- For multi-aspect endpoints, the skill offers `all X` / `each X` qualifiers only when `X` is a multi-aspect unit.

## No self-coupling

`{X} sends M to {X}!` is rejected. The `all X sends M to all X!` form is legal but MS4 Me silently drops the diagonal (a replica never sends to itself).

## Cross-aspect messaging — how to do it properly

If two entities truly need to exchange messages but live in different aspects of a shared parent, restructure: introduce a joint aspect that contains both (using `is made of` with both as children) and couple there. Do not try to bridge with a direct coupling under one of the original aspects.

## Examples

**Legal** (siblings under aspect `Dialog`, p. 17):

```
from the Dialog perspective, HelloWorldSES is made of HelloWorld and BackAtYou!
from the Dialog perspective, HelloWorld sends Hello to BackAtYou!
```

**Legal** (parent ↔ child under aspect `enclave`, p. 126):

```
from the enclave perspective, Enclave is made of DBServer and Clients!
from the enclave perspective, DBServer sends DBUpdate to Enclave!
from the enclave perspective, Enclave sends DBUpdate to DBServer!
```

**Legal** (multi-aspect unit and its parent's siblings, the exception, p. 127). `Processor`'s parent is `Processors`, and `GeneratorOfJobs` and `Transducer` are siblings of `Processors` under `top`:

```
from the top perspective, SimpleWorkFlow is made of GeneratorOfJobs, Processors and Transducer!
from the multiProcessor perspective, Processors are made of more than one Processor!
Processor can be id in index!
from the top perspective, GeneratorOfJobs sends Job to all Processor!
from the top perspective, all Processor sends outJob to Transducer as inSolved!
```

**Legal** (units of two different multi-aspects, and the enclosing parent, p. 125):

```
from the dating perspective, all Girl sends NiceToMeetYou to all Boy!
from the dating perspective, DatingGame sends MakeInvitation to all Boy!
```

**Illegal** (cross-aspect — no single aspect contains both endpoints):

```
from the structural perspective, Car is made of Engine and Chassis!
from the electrical perspective, Car is made of Battery and Harness!
from the structural perspective, Engine sends Power to Battery!   <-- REJECTED
```

`Battery` is a child of `Car` under `electrical`, not `structural`. Fix by moving `Battery` (or a proxy) into the `structural` aspect, or by introducing a third aspect containing both.
