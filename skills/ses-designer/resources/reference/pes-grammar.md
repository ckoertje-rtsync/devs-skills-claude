# MS4 Me PES (Pruning) Grammar

A `.pes` file selects one configuration out of the space described by a `.ses`. Its statements are imperative directives — each ends with `!` and is parsed top-to-bottom. The minimum viable PES is four lines: `set name`, `pruned from`, then any number of restructure / multiplicity / select directives, then `write class files!` to finalize.

Regexes are POSIX-extended (ERE) and match whole lines.

---

## 1. `set name of model as {NAME}!`

**Purpose.** Name the resulting coupled DEVS model that will be synthesised from the PES.

**Examples.**

```
set name of model as HelloWorld!
set name of model as SimpleWorkFlow!
```

**Regex.**

```
^set name of model as [A-Za-z_][A-Za-z0-9_-]*!$
```

Always emitted once, as the first line of the PES.

---

## 2. `pruned from {SES_FILENAME}.ses!`

**Purpose.** Declare the source SES file this PES is derived from. `{SES_FILENAME}` is the base name *with* the `.ses` suffix, matching the sibling file on disk.

**Examples.**

```
pruned from HelloWorldSES.ses!
pruned from SimpleWorkFlow.ses!
```

**Regex.**

```
^pruned from [A-Za-z_][A-Za-z0-9_-]*\.ses!$
```

Always emitted second, immediately after `set name of model`.

---

## 3. `restructure multiaspects using {LABEL}!`

**Purpose.** Declare that the multi-aspect replicas should be expanded using the named specialization label as the instance-differentiator tag. Required whenever the SES contains a multi-aspect that resolves to more than one concrete instance.

**Example.**

```
restructure multiaspects using index!
restructure multiaspects using height!
```

**Regex.**

```
^restructure multiaspects using [A-Za-z_][A-Za-z0-9_-]*!$
```

Must precede any `set multiplicity ... for` directive referencing the same label.

---

## 4. `set multiplicity of {LABEL} as [{N}] for {ENTITY}!`

**Purpose.** Fix the number of replicas for a multi-aspect entity under the named specialization label. `{N}` is a positive integer literal; brackets are part of the syntax.

**Examples.**

```
set multiplicity of index as [5] for Processor!
set multiplicity of height as [3] for Boy!
set multiplicity of height as [3] for Girl!
```

**Regex.**

```
^set multiplicity of [A-Za-z_][A-Za-z0-9_-]* as \[[0-9]+\] for [A-Za-z_][A-Za-z0-9_-]*!$
```

**Constraints.**

- `{LABEL}` must match a `restructure multiaspects using {LABEL}!` line earlier in the same PES.
- `{ENTITY}` must be the unit entity of a multi-aspect in the referenced `.ses`.

---

## 5. `select {OPT} for {ENTITY}!` (specialization pick)

**Purpose.** Resolve a specialization choice point by selecting one option. Used when the SES contains `{ENTITY} can be A or B in {LABEL}!` with two or more options and the user wants to lock in one.

**Examples.**

```
select Automatic for Transmission!
select ElectricEngine for Engine!
```

**Regex.**

```
^select [A-Za-z_][A-Za-z0-9_-]* for [A-Za-z_][A-Za-z0-9_-]*!$
```

**Note.** The MS4 Me User's Guide documents specialization selection primarily through the interactive pruning UI (p. 168). The textual `select ... for ...!` form is what the pruning workflow emits into the PES when you save; verify against your guide version before shipping new directives that depend on it.

---

## 6. `write class files!`

**Purpose.** Terminator directive. Instructs MS4 Me to generate the Java class files for the pruned coupled model.

**Regex.**

```
^write class files!$
```

Always emitted as the last line of the PES.

---

## Canonical PES shapes

**Minimal PES** (SES with no multi-aspects, no specializations):

```
set name of model as {NAME}!
pruned from {NAME_OR_SES_ROOT}.ses!
write class files!
```

**PES with one multi-aspect** (the SimpleWorkFlow case from guide p. 128):

```
set name of model as SimpleWorkFlow!
pruned from SimpleWorkFlow.ses!
restructure multiaspects using index!
set multiplicity of index as [5] for Processor!
write class files!
```

**PES with a specialization pick**:

```
set name of model as CarDemo!
pruned from CarSES.ses!
select ElectricEngine for Engine!
select Automatic for Transmission!
write class files!
```

**PES with both a specialization pick and a multi-aspect**:

```
set name of model as DatingGame!
pruned from DatingSES.ses!
restructure multiaspects using height!
set multiplicity of height as [3] for Boy!
set multiplicity of height as [3] for Girl!
select medium for Boy!
select tall for Girl!
write class files!
```

---

## Ordering rules

1. `set name of model as ...!` — always first.
2. `pruned from ...!` — always second.
3. `restructure multiaspects using {L}!` — any, must precede its matching `set multiplicity ... as [N] for ...!`.
4. `set multiplicity ... as [N] for ...!` — any, one per multi-aspect unit entity, after its `restructure`.
5. `select {OPT} for {ENTITY}!` — any, after restructure blocks.
6. `write class files!` — always last.

Blank lines are permitted between directives; `#`-prefixed comments are common but unofficial — MS4 Me may tolerate them in recent versions. The validator treats both as skippable.
