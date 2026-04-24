# MS4 Me SES Natural-Language Grammar

Reference for the six SES constructs accepted by MS4 Me's natural-language editor. All statements end with `!`. Identifiers follow the rules in `identifier-rules.md`. Axioms that govern legal structures live in `ses-axioms.md`.

Pattern shape conventions:

- `{UPPER}` — required identifier (entity, aspect, message, variable, label).
- `[ ... ]` — optional clause in the natural-language pattern.
- `...` — repeatable.

Regexes below are POSIX-extended (ERE) and assume the whole line matches end-to-end.

---

## 1. Aspect — perspective-based decomposition

**Purpose.** Declare that, viewed from a named perspective, a system is composed of two or more named subsystems.

**Natural-language shape.**

```
from the {ASPECT} perspective, {SYSTEM} is made of {SUB_1}, {SUB_2}, ... and {SUB_N}!
```

Article `the` is optional; `is` may appear as `are` when the subject is plural (e.g. "`Processors are made of ...`"). There is **no comma before `and`** when exactly two children are listed.

**Examples** (from MS4 Me User's Guide pp. 16–17, 120, 174):

```
from the Dialog perspective, HelloWorldSES is made of HelloWorld and BackAtYou!
from the structural perspective, Car is made of Engine, Transmission, and Chassis!
from the top perspective, SimpleWorkFlow is made of GeneratorOfJobs, Processors and Transducer!
```

**Regex.**

```
^[Ff]rom( the)? [A-Za-z_][A-Za-z0-9_-]* perspective, [A-Za-z_][A-Za-z0-9_-]* (is|are) made of [A-Za-z_][A-Za-z0-9_-]*(, [A-Za-z_][A-Za-z0-9_-]*)*( and [A-Za-z_][A-Za-z0-9_-]*)?!$
```

**Constraints.**

- Single-child aspects are flattened by MS4 Me; author at least 2 children.
- The parent entity must exist (declared as root or as a child in a prior aspect).
- Each child becomes a new entity. The skill's validator tracks these as declared entities.

---

## 2. Multi-aspect — replicated identical subsystem

**Purpose.** Declare that a system is made of an arbitrary number of identical subsystems of one type. Multiplicity is set during pruning in the `.pes`.

**Natural-language shape.**

```
from the {ASPECT} perspective, {SYSTEM} is made of more than one {UNIT}!
```

**Examples** (pp. 121, 174–175):

```
from the multiProcessor perspective, Processors are made of more than one Processor!
from the physical perspective, a book is made of more than one page!
from the multiPerson perspective, People are made of more than one Person!
```

**Regex.**

```
^[Ff]rom( the)? [A-Za-z_][A-Za-z0-9_-]* perspective, [A-Za-z_][A-Za-z0-9_-]* (is|are) made of more than one [A-Za-z_][A-Za-z0-9_-]*!$
```

**Constraints.**

- Pair with a specialization on the unit entity (e.g. `Processor can be id in index!`) to distinguish replicas during pruning.
- Couplings into/out of the unit use `all {UNIT}` or `each {UNIT}` (see construct 5).

---

## 3. Specialization — mutually exclusive variants

**Purpose.** Declare that an entity has two or more alternative implementations; exactly one is chosen during pruning.

**Natural-language shape.**

```
{ENTITY} can be {OPT_1}, {OPT_2}, ... or {OPT_N} in {LABEL}!
```

**No comma before `or`** when exactly two options are listed. For a single option (used to tag multi-aspect replicas) the pattern is `{ENTITY} can be {OPT_1} in {LABEL}!` (no `or`).

**Examples** (pp. 122, 175):

```
Engine can be ElectricEngine or GasolineEngine in engineType!
Transmission can be Automatic or Manual in gearShift!
Engine can be ElectricEngine, GasolineEngine or NaturalGasEngine in engineType!
Processor can be id in index!
```

**Regex.**

```
^[A-Za-z_][A-Za-z0-9_-]* can be [A-Za-z_][A-Za-z0-9_-]*(, [A-Za-z_][A-Za-z0-9_-]*)*( or [A-Za-z_][A-Za-z0-9_-]*)? in [A-Za-z_][A-Za-z0-9_-]*!$
```

**Constraints.**

- The entity must be declared elsewhere (as root, aspect child, or multi-aspect unit).
- Each option becomes a new entity name; the label is a choice-point name referenced later in the `.pes` by `select {OPT} for {ENTITY}!` or by multiplicity directives for multi-aspect replicas.

---

## 4. Coupling — inter-entity message flow

**Purpose.** Wire a message from one entity's output to another entity's input within a named aspect.

**Natural-language shape.**

```
from the {ASPECT} perspective, {SENDER} sends {MSG} to {RECEIVER}!
from the {ASPECT} perspective, {SENDER} sends {MSG} to {RECEIVER} as {RECV_MSG}!
```

The optional `as {RECV_MSG}` clause renames the message on the receiver side when the sender's output port name differs from the receiver's input port name.

**Examples** (pp. 17, 124, 176):

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

- Sender and receiver must be siblings under the same aspect (or the parent/child pair of that aspect).
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

**Examples** (pp. 125, 176):

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
- `each`/`each` requires matching specialization labels on both sides.
- All-to-all over the same entity name is legal but expands to a complete graph excluding self-loops (MS4 Me drops self-couplings automatically).

---

## 6. Variables and ranges — entity parameters

**Purpose.** Attach named, typed parameters to entities. Variables inherit down the hierarchy per the attached-variables axiom.

**Natural-language shapes.**

Variable declaration:

```
{ENTITY} has {VAR_1}, {VAR_2}, ... and {VAR_N}!
```

Range declaration (optional, one per variable):

```
the range of {ENTITY}'s {VAR} is {TYPE} with values {MIN}, {MID}, ... and {MAX}!
the range of {ENTITY}'s {VAR} is {TYPE} with values [{MIN}, {MAX}]!
the range of {ENTITY}'s {VAR} is {TYPE} with values ({MIN}, {MAX})!
the range of {ENTITY}'s {VAR} is {TYPE} with values [{MIN}, {MAX})!
the range of {ENTITY}'s {VAR} is {TYPE} with values ({MIN}, {MAX}]!
```

`[` / `]` denote inclusive bounds; `(` / `)` denote exclusive bounds; mixed pairs are legal. `{TYPE}` is `number`, `integer`, `real`, or `string`.

**Examples** (pp. 126, 177):

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
- The entity must be declared. The variable must appear in a prior `has` statement for the same entity or an ancestor.

---

## Page-by-page citation summary

| Construct | MS4 Me User's Guide pages |
|---|---|
| Aspect | 16–17, 120, 166, 174 |
| Multi-aspect | 121, 174–175 |
| Specialization | 122, 166, 175 |
| Coupling | 17, 124, 167, 176 |
| Multi-aspect coupling | 125, 176 |
| Variables and ranges | 126, 167, 177 |
| File layout + naming | 165, 173 |
| Pruning (SES → PES) | 17, 120–121, 127, 168–169 |

Read alongside `pes-grammar.md` (pruning directives), `ses-axioms.md` (structural rules MS4 Me enforces), and `identifier-rules.md` (valid names).
