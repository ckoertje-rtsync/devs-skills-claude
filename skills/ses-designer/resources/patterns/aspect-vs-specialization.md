# Pattern — Aspect vs. Specialization

Both aspects and specializations introduce branching in an SES, but they mean very different things.

## Aspect (`is made of`)

An aspect is a **decomposition**. The parent *is composed of* the listed children simultaneously — each child exists at the same time as the others. Use when your system genuinely has multiple concurrent parts.

```
from the structural perspective, Car is made of Engine, Transmission and Chassis!
```

Every instance of `Car` has exactly one `Engine`, one `Transmission`, and one `Chassis`, all live at once.

## Specialization (`can be`)

A specialization is an **alternative**. The entity has multiple mutually exclusive implementations; exactly one is chosen during pruning. Use when your system is one thing, and that one thing comes in several flavors.

```
Engine can be ElectricEngine, GasolineEngine or NaturalGasEngine in engineType!
```

Every pruned `Engine` is one of `ElectricEngine`, `GasolineEngine`, or `NaturalGasEngine` — never more than one, never a mixture.

## Decision rules

Ask the user these questions in order until one returns `yes`:

1. *"Does this entity have concurrent sub-parts (each present at the same time)?"* → aspect.
2. *"Does this entity come in several mutually exclusive flavors?"* → specialization.
3. *"Is this entity a group of identical units (count varies)?"* → multi-aspect.
4. *"Does this entity differ only in a configurable numeric/string value?"* → variable + range.
5. Otherwise → atomic leaf.

The first `yes` wins for the question at hand. An entity can still combine modes: one entity may have aspects *and* specializations, and several of each. In the User's Guide, `Enclave` has an `enclave` aspect and an `index` specialization (p. 125), and `Person` has two specializations, `index` and `height` (pp. 121–122). Alternating mode only says that entities alternate with aspects and specializations along every path (see `../reference/ses-axioms.md`).

## Combined example

```
from the structural perspective, Car is made of Engine, Transmission and Chassis!
Engine can be ElectricEngine, GasolineEngine or NaturalGasEngine in engineType!
Transmission can be Automatic or Manual in gearShift!
from the internal perspective, ElectricEngine is made of Battery and Motor!
Motor has ratedPower!
the range of Motor's ratedPower is number with values [10, 500]!
```

Reading: `Car` decomposes into three siblings; `Engine` specializes into three variants; one variant (`ElectricEngine`) further decomposes; `Motor` carries a numeric parameter.

## When the user is pulled toward the wrong choice

| Situation | Wrong construct | Right construct | Why |
|---|---|---|---|
| "There are 10 processors." | 10-child aspect | multi-aspect | Count should vary at pruning time. |
| "The engine has a small / medium / large size." | specialization | variable + enumerated range | Size is a parameter, not a structural variant. |
| "Engines come in electric and gasoline." | two siblings under an aspect | specialization | The car has *one* engine that is one flavor. |
| "The sensor reads pressure or temperature." | variable `mode` with values `pressure, temperature` | specialization on `Sensor` | Different behaviors, not different parameters of the same behavior. |
| "We might add more engines later." | dash-separated child names | specialization with a label | Future options can be added to the `can be` list; dash-separated names can't be pruned. |
