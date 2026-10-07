# MS4 Me SES Natural-Language Grammar

Reference for the six SES constructs accepted by MS4 Me's natural-language editor. All statements end with `!`. Identifiers follow the rules in `identifier-rules.md`. Axioms that govern legal structures live in `ses-axioms.md`.

Pattern shape conventions:

- `{UPPER}` — required identifier (entity, aspect, message, variable, label).
- `[ ... ]` — optional clause in the natural-language pattern.
- `...` — repeatable.

Regexes below are POSIX-extended (ERE) and assume the whole line matches end-to-end.

---

## 1. Aspect — perspective-based decomposition

**Purpose.** Declare that, viewed from a named perspective, a system is composed of one or more named subsystems.

**Natural-language shape.**

```
from the {ASPECT} perspective, {SYSTEM} is made of {SUB_1}, {SUB_2}, ... and {SUB_N}!
```

Article `the` before the aspect is optional; `is` may appear as `are` or `is (are)` (guide p. 174).

**Lists.** The canonical form puts **no comma before `and`**, however many children there are (guide p. 174), and the skill always emits it. The guide also shows the Oxford-comma variant `A, B, and C` (pp. 127, 175), and the validator accepts it. MS4 Me also accepts articles before names (`a car is made of an engine, ...`, p. 175), but the skill never emits them and the validator rejects them.

**Examples** (from MS4 Me User's Guide pp. 16–17, 115, 120, 127):

```
from the Dialog perspective, HelloWorldSES is made of HelloWorld and BackAtYou!
from the dynamic perspective, AgentAndActor is made of Agent!
from the top perspective, SimpleWorkFlow is made of GeneratorOfJobs, Processors and Transducer!
from the structural perspective, Car is made of Engine, Transmission, and Chassis!
```

The last line is the Oxford-comma variant: accepted, but not what the skill emits.

**Regex.**

```
^[Ff]rom( the)? [A-Za-z_][A-Za-z0-9_-]* perspective, [A-Za-z_][A-Za-z0-9_-]* (is|are) made of [A-Za-z_][A-Za-z0-9_-]*(, [A-Za-z_][A-Za-z0-9_-]*)*(,? and [A-Za-z_][A-Za-z0-9_-]*)?!$
```

**Constraints.**

- A single child is legal (guide pp. 166, 174; example p. 115).
- List every child of one aspect in one statement. Declaring the same aspect twice violates valid brothers (`ses-axioms.md`).
- The parent entity must exist (declared as root or as a child in a prior aspect).
- Each child becomes a new entity. The skill's validator tracks these as declared entities.
- An entity may have several aspects. Pruning must then pick one (guide p. 168), and `pes-grammar.md` explains why a `.pes` cannot pin that choice in text.

---

## 2. Multi-aspect — replicated identical subsystem

**Purpose.** Declare that a system is made of an arbitrary number of identical subsystems of one type. Multiplicity is set during pruning in the `.pes`.

**Natural-language shape.**

```
from the {ASPECT} perspective, {SYSTEM} is made of more than one {UNIT}!
```

**Examples.** Name the collective parent with an explicit suffix rather than the plural of the unit (see `identifier-rules.md` → "Collective (multi-aspect parent) names"):

```
from the fleet perspective, UAVGroup is made of more than one UAV!
```

The guide's own examples, verbatim (pp. 120, 127, 129). The first two use plural parent names:

```
from the multiProcessor perspective, Processors are made of more than one Processor!
from the multiPerson perspective, People are made of more than one Person!
from the multicell perspective, CellGroup is made of more than one AA!
```

**Regex.**

```
^[Ff]rom( the)? [A-Za-z_][A-Za-z0-9_-]* perspective, [A-Za-z_][A-Za-z0-9_-]* (is|are) made of more than one [A-Za-z_][A-Za-z0-9_-]*!$
```

**Constraints.**

- Pair with a specialization on the unit entity (e.g. `Processor can be id in index!`) to distinguish replicas during pruning. Restructuring names the copies `id0_Processor`, `id1_Processor`, ... (guide p. 121).
- Couplings into/out of the unit use `all {UNIT}` or `each {UNIT}` (see construct 5). They name the aspect that contains the multi-aspect's parent, not the multi-aspect's own perspective (`ses-axioms.md` → Coupling scope).

---

## 3. Specialization — mutually exclusive variants

**Purpose.** Declare that an entity has two or more alternative implementations; exactly one is chosen during pruning.

**Natural-language shape.**

```
{ENTITY} can be {OPT_1}, {OPT_2}, ... or {OPT_N} in {LABEL}!
```

The canonical list form puts **no comma before `or`** (guide p. 176). The guide also shows the Oxford-comma variant (pp. 122, 129), and the validator accepts it. For a single option (used to tag multi-aspect replicas) the pattern is `{ENTITY} can be {OPT_1} in {LABEL}!` (no `or`).

**Examples** (pp. 127, 173, 175):

```
Engine can be ElectricEngine or GasolineEngine in engineType!
Transmission can be Automatic or Manual in gearShift!
Engine can be ElectricEngine, GasolineEngine or NaturalGasEngine in engineType!
Processor can be id in index!
```

**Regex.**

```
^[A-Za-z_][A-Za-z0-9_-]* can be [A-Za-z_][A-Za-z0-9_-]*(, [A-Za-z_][A-Za-z0-9_-]*)*(,? or [A-Za-z_][A-Za-z0-9_-]*)? in [A-Za-z_][A-Za-z0-9_-]*!$
```

**Constraints.**

- The entity must be declared elsewhere (as root, aspect child, or multi-aspect unit).
- Each option becomes a new entity name; the label is a choice-point name referenced later in the `.pes` by `select {OPT} from {LABEL} for {ENTITY}!` or by multiplicity directives for multi-aspect replicas.
- An entity may have several specializations, and aspects as well. The guide's `Person` has both `id in index` and `short, medium, or tall in height` (pp. 121–122). Alternating mode does not forbid this; see `ses-axioms.md`.

---

## 4. Coupling — inter-entity message flow

**Purpose.** Wire a message from one entity's output to another entity's input within a named aspect.

**Natural-language shape.**

```
from the {ASPECT} perspective, {SENDER} sends {MSG} to {RECEIVER}!
from the {ASPECT} perspective, {SENDER} sends {MSG} to {RECEIVER} as {RECV_MSG}!
```

The optional `as {RECV_MSG}` clause renames the message on the receiver side when the sender's output port name differs from the receiver's input port name.

**Examples** (pp. 17, 127; the rename line is illustrative):

```
from the Dialog perspective, HelloWorld sends Hello to BackAtYou!
from the Dialog perspective, BackAtYou sends Hi to HelloWorld!
from the top perspective, Transducer sends Stop to GeneratorOfJobs!
from the Dialog perspective, HelloWorld sends Hello to BackAtYou as Greeting!
```

**Regex.**

```
^[Ff]rom( the)? [A-Za-z_][A-Za-z0-9_-]* perspective, [A-Za-z_][A-Za-z0-9_-]* sends [A-Za-z_][A-Za-z0-9_-]* to [A-Za-z_][A-Za-z0-9_-]*( as [A-Za-z_][A-Za-z0-9_-]*)?!$
```

**Constraints.**

- Sender and receiver must be siblings under the same aspect, or the parent/child pair of that aspect. The one exception: a multi-aspect unit couples with the siblings of its parent (guide p. 176; see `ses-axioms.md` → Coupling scope).
- No self-coupling: `{X} sends ... to {X}` is invalid for atomic siblings.
- For multi-aspect endpoints (all/each semantics), use construct 5.

---

## 5. Multi-aspect coupling — broadcast / match across replicas

**Purpose.** Declare couplings that fan out or fan in over multi-aspect replicas, or match replicas pairwise.

**Natural-language shape.**

```
from the {ASPECT} perspective, all {X} sends {MSG} to all {Y}!
from the {ASPECT} perspective, all {X} sends {MSG} to {Y}!
from the {ASPECT} perspective, {X} sends {MSG} to all {Y}!
from the {ASPECT} perspective, each {X} sends {MSG} to each {Y}!
from the {ASPECT} perspective, each {X} sends {MSG} to each {Y} as {RECV_MSG}!
```

Semantics:

- `all {X} sends ... to all {Y}` — every replica of X to every replica of Y (broadcast matrix).
- `each {X} sends ... to each {Y}` — pairwise matching (same specialization tag on both sides).
- `all {X} sends ... to {Y}` — every replica of X to the single entity Y (fan-in).
- `{X} sends ... to all {Y}` — the single entity X to every replica of Y (fan-out).

**Examples** (pp. 123–127):

```
from the top perspective, GeneratorOfJobs sends Job to all Processor!
from the top perspective, all Processor sends outJob to Transducer as inSolved!
from the dating perspective, each Boy sends Invitation to each Girl!
from the top perspective, all Person sends Hello to all Person!
```

**Regex.**

```
^[Ff]rom( the)? [A-Za-z_][A-Za-z0-9_-]* perspective, (all |each )?[A-Za-z_][A-Za-z0-9_-]* sends [A-Za-z_][A-Za-z0-9_-]* to (all |each )?[A-Za-z_][A-Za-z0-9_-]*( as [A-Za-z_][A-Za-z0-9_-]*)?!$
```

**Constraints.**

- The `all`/`each` qualifier is only legal on an endpoint that is a multi-aspect unit.
- `each`/`each` matches copies by the prefix of their pruned names (`tall` in `tall_Boy`). No coupling is added for a copy with no match, for example when the counts differ (guide p. 124).
- All-to-all over the same entity name is legal but expands to a complete graph excluding self-loops (MS4 Me drops self-couplings automatically).

---

## 6. Variables and ranges — entity parameters

**Purpose.** Attach named, typed parameters to entities. Specialization options inherit their general entity's variables (inheritance axiom). Variable names must be distinct per entity (attached-variables axiom). See `ses-axioms.md`.

**Natural-language shapes.**

Variable declaration:

```
{ENTITY} has {VAR_1}, {VAR_2}, ... and {VAR_N}!
```

Range declaration (optional, one per variable):

```
the range of {ENTITY}'s {VAR} is {TYPE}!
the range of {ENTITY}'s {VAR} is {TYPE} with values {MIN}, {MID}, ... and {MAX}!
the range of {ENTITY}'s {VAR} is {TYPE} with values [{MIN}, {MAX}]!
the range of {ENTITY}'s {VAR} is {TYPE} with values ({MIN}, {MAX})!
the range of {ENTITY}'s {VAR} is {TYPE} with values [{MIN}, {MAX})!
the range of {ENTITY}'s {VAR} is {TYPE} with values ({MIN}, {MAX}]!
```

The first form gives only the type (guide p. 177). `[` / `]` denote inclusive bounds; `(` / `)` denote exclusive bounds; mixed pairs are legal. With `with values`, the validator accepts `{TYPE}` = `number`, `integer`, `real`, or `string`.

**Examples** (syntax from pp. 173, 177–178; names and values illustrative):

```
Engine has HoursRun!
Engine has HoursRun and Efficiency!
Processor has serviceTime, meanTime and jitter!
the range of Engine's HoursRun is number with values [0, 100000]!
the range of Engine's Temperature is number with values [-50, 100]!
the range of Pressure's Value is number with values (-6.3, 6)!
the range of Transmission's GearType is string with values Automatic, Manual and CVT!
```

**Regexes.**

Variable list:

```
^[A-Za-z_][A-Za-z0-9_-]* has [A-Za-z_][A-Za-z0-9_-]*(, [A-Za-z_][A-Za-z0-9_-]*)*( and [A-Za-z_][A-Za-z0-9_-]*)?!$
```

Type only:

```
^the range of [A-Za-z_][A-Za-z0-9_-]*'s [A-Za-z_][A-Za-z0-9_-]* is [A-Za-z_][A-Za-z0-9_-]*!$
```

Numeric range (bracketed interval):

```
^the range of [A-Za-z_][A-Za-z0-9_-]*'s [A-Za-z_][A-Za-z0-9_-]* is (number|integer|real) with values (\[|\()-?[0-9]+(\.[0-9]+)?, -?[0-9]+(\.[0-9]+)?(\]|\))!$
```

Enumerated range (`string` or enumeration of values):

```
^the range of [A-Za-z_][A-Za-z0-9_-]*'s [A-Za-z_][A-Za-z0-9_-]* is (number|integer|real|string) with values [A-Za-z0-9_.-]+(, [A-Za-z0-9_.-]+)*( and [A-Za-z0-9_.-]+)?!$
```

**Constraints.**

- `'s` is required after the entity name in the range statement.
- The entity must be declared. The variable must appear in a prior `has` statement for the same entity, or for the general entity of a specialization this entity is an option of (inheritance axiom). A child in an aspect does not inherit its parent's variables.

---

## Page-by-page citation summary

| Construct | MS4 Me User's Guide pages |
|---|---|
| Aspect | 16–17, 115, 120, 166, 174–175 |
| Multi-aspect | 120–121, 127, 129, 175 |
| Specialization | 121–122, 166, 175–176 |
| Coupling | 17, 123, 167, 176 |
| Multi-aspect coupling | 122–125, 176–177 |
| Variables and ranges | 167, 177–178 |
| File layout + naming | 165, 173 |
| Pruning (SES → PES) | 17, 120–121, 127, 168–169 |

Read alongside `pes-grammar.md` (pruning directives), `ses-axioms.md` (SES axioms and MS4 Me's coupling-scope rule), and `identifier-rules.md` (valid names).
