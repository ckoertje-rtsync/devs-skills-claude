# MS4 Me PES (Pruning) Grammar

A `.pes` file selects one configuration out of the space described by a `.ses`. Its statements are imperative directives. Each ends with `!`, and they are parsed top to bottom. The minimum viable PES is three lines: `set name`, `pruned from`, and `write class files!`. Between the second and the last line go any restructure, multiplicity, select, inherit, and exclusion directives.

The guide usually prints a space before the `!` (`write class files !`). The skill emits no space, and the validator requires none.

Regexes are POSIX-extended (ERE) and match whole lines. Page numbers refer to the MS4 Me User's Guide.

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

## 3. `mergeAll from {SES_FILENAME}.ses!`

**Purpose.** Appears right after `pruned from` in two guide examples (`ZooChimpsTest.pes`, p. 27; `Try.pes`, p. 129). The guide shows the line but does not explain it. The skill does not emit it; the validator accepts it so that guide-style files validate.

**Regex.**

```
^mergeAll from [A-Za-z_][A-Za-z0-9_-]*\.ses!$
```

---

## 4. `restructure multiaspects using {LABEL}[ then {LABEL}]*!`

**Purpose.** Expand multi-aspects, using the named specialization label to identify the copies. Required whenever the SES contains a multi-aspect that should produce concrete instances.

Restructuring generates copies named after the identifying option and an index: `Person can be id in index!` yields `id0_Person`, `id1_Person`, `id2_Person` (p. 121). The copies become children of the unit's grandparent, inside the grandparent's aspect, and the multi-aspect is removed (p. 121). When the label has several options, as in `height`, the copies carry the option as a prefix (`tall_Boy`, p. 124). The guide shows those names but not the PES that produces them.

**Examples.**

```
restructure multiaspects using index!
restructure multiaspects using clientIndex then enclaveIndex!
```

The guide says it is "permissible, and desirable" to use the same label (here `index`) for every multi-aspect (p. 126). If the multi-aspects use different labels, list them in one `restructure` line, **bottom-up**, joined by `then` (p. 126).

**Regex.**

```
^restructure multiaspects using [A-Za-z_][A-Za-z0-9_-]*( then [A-Za-z_][A-Za-z0-9_-]*)*!$
```

Must precede any `set multiplicity ... for` directive referencing one of its labels.

---

## 5. `set multiplicity of {LABEL} as [{N}] for {ENTITY}!`

**Purpose.** Fix the number of copies of a multi-aspect unit under the named specialization label. `{N}` is a positive integer literal; the brackets are part of the syntax.

**Examples.**

```
set multiplicity of index as [5] for Processor!
set multiplicity of index as [2] for Enclave!
set multiplicity of index as [3] for Client!
```

**Regex.**

```
^set multiplicity of [A-Za-z_][A-Za-z0-9_-]* as \[[0-9]+\] for [A-Za-z_][A-Za-z0-9_-]*!$
```

**Constraints.**

- `{LABEL}` must appear in a `restructure multiaspects using ...!` line earlier in the same PES.
- `{ENTITY}` must be the unit entity of a multi-aspect in the referenced `.ses`.
- For nested multi-aspects, every parent copy gets the same number of children. The guide calls this a limitation of the current version (p. 127).
- Do not set the multiplicity of a label you use to match copies in `each`/`each` couplings. The guide's DatingGame matches on `height` and works "provided you don't use height as the basis for identifying multi-entity copies" (p. 124).

---

## 6. `select {OPT} from {LABEL} for {ENTITY}!` (specialization pick)

**Purpose.** Resolve the specialization `{LABEL}` of `{ENTITY}` by choosing option `{OPT}`. Choices left unmade in the PES are made at random when the model runs (pp. 122, 168).

**Examples** (pp. 122, 157–158):

```
select FiveKGWeight from ObjectType for PhysicalObject!
select GravitationalAcceleration from AccelerationType for ConstantAcceleration!
select medium from height for id1_Person!
```

**Regex.**

```
^select [A-Za-z_][A-Za-z0-9_-]* from [A-Za-z_][A-Za-z0-9_-]* for [A-Za-z_][A-Za-z0-9_-]*!$
```

**Constraints.**

- The `from {LABEL}` part is required. The guide never shows `select {OPT} for {ENTITY}!`, and the validator rejects that form.
- For a multi-aspect unit, select on the **copies** after restructuring (`id1_Person`), not on the unit (`Person`). Each copy inherits the unit's specializations and can be pruned independently (p. 122).
- `{ENTITY}` may itself be a selected option that has its own specialization (`ConstantAcceleration` above, p. 158).
- An entity with more than one aspect also needs an aspect choice, which is made in the interactive pruning view (p. 168). The guide documents no text directive for it, so a PES written by hand leaves that choice to random selection.

---

## 7. `inherit from {OPT}!`

**Purpose.** Give the pruned model the behavior of the selected option. The guide (p. 27): "without 'inherit from BetaChimpTwo! and inherit from AlphaChimpTwo!', the selected models have empty behaviors." `PhysicsModelPM1.pes` adds one `inherit from` line after each `select` (p. 158).

**Example.**

```
select FiveKGWeight from ObjectType for PhysicalObject!
inherit from FiveKGWeight!
```

**Regex.**

```
^inherit from [A-Za-z_][A-Za-z0-9_-]*!$
```

**Constraints.**

- `{OPT}` must have been selected by an earlier `select {OPT} from ... for ...!`.
- The skill emits `inherit from {OPT}!` right after every `select`. When several copies select the same option, it emits the line once, after the first of those selects. The guide shows no case with copies sharing an option.

---

## 8. `don't select {OPT} under {ENTITY}!`

**Purpose.** Exclude an option for every copy of a multi-aspect unit. In `Try.pes`, "'don't select CC under AA!' means 10 instances of AAs do not select CC entities" (p. 129). Use an ASCII apostrophe.

**Regex.**

```
^don't select [A-Za-z_][A-Za-z0-9_-]* under [A-Za-z_][A-Za-z0-9_-]*!$
```

The skill does not emit this directive by default; the validator accepts it.

---

## 9. `write class files!`

**Purpose.** Terminator directive. Instructs MS4 Me to generate the Java class files for the pruned coupled model.

**Regex.**

```
^write class files!$
```

Always emitted as the last line of the PES.

---

## Canonical PES shapes

**Minimal PES** (SES with no multi-aspects and no specializations):

```
set name of model as {NAME}!
pruned from {NAME_OR_SES_ROOT}.ses!
write class files!
```

**PES with one multi-aspect** (`SimpleWorkFlow.pes`, p. 128):

```
set name of model as SimpleWorkFlow!
pruned from SimpleWorkFlow.ses!
restructure multiaspects using index!
set multiplicity of index as [5] for Processor!
write class files!
```

**PES with specialization picks** (`PhysicsModelPM1.pes`, p. 158):

```
set name of model as PhysicsModelPM1!
pruned from PhysicsModel.ses!
select FiveKGWeight from ObjectType for PhysicalObject!
inherit from FiveKGWeight!
select ConstantAcceleration from MotionType for MotionModel!
inherit from ConstantAcceleration!
select RestingObject from PossibleConditions for InitialConditions!
inherit from RestingObject!
select GravitationalAcceleration from AccelerationType for ConstantAcceleration!
inherit from GravitationalAcceleration!
write class files!
```

**PES with a multi-aspect and a per-copy pick.** The SES is the guide's `World` (pp. 120–122): `People are made of more than one Person!`, `Person can be id in index!`, `Person can be short, medium, or tall in height!`. The guide prints the restructure, multiplicity, and select lines; the PES around them is assembled here:

```
set name of model as WorldTest!
pruned from World.ses!
restructure multiaspects using index!
set multiplicity of index as [3] for Person!
select medium from height for id1_Person!
inherit from medium!
write class files!
```

**PES with nested multi-aspects** (`MultiEnclaveNet`, p. 126). Same label for both levels:

```
set name of model as MultiEnclaveNet!
pruned from MultiEnclaveNet.ses!
restructure multiaspects using index!
set multiplicity of index as [2] for Enclave!
set multiplicity of index as [3] for Client!
write class files!
```

With different labels, restructure bottom-up in one line:

```
restructure multiaspects using clientIndex then enclaveIndex!
set multiplicity of enclaveIndex as [2] for Enclave!
set multiplicity of clientIndex as [3] for Client!
```

**PES excluding an option for every copy** (`Try.pes`, p. 129):

```
set name of model as Try!
pruned from Try.ses!
mergeAll from Try.ses!
restructure multiaspects using index!
set multiplicity of index as [10] for AA!
don't select CC under AA!
write class files!
```

---

## Ordering rules

1. `set name of model as ...!` — always first.
2. `pruned from ...!` — always second.
3. `mergeAll from ...!` — optional; the guide places it right after `pruned from`.
4. `restructure multiaspects using ...!` — one line naming every label used, bottom-up. It must precede the matching `set multiplicity` lines.
5. `set multiplicity ... as [N] for ...!` — one per multi-aspect unit, after its `restructure`.
6. `select {OPT} from {LABEL} for {ENTITY}!` — after the `restructure` when it targets a copy.
7. `inherit from {OPT}!` — after the `select` of `{OPT}`.
8. `don't select {OPT} under {ENTITY}!` — the guide places it after the multiplicities.
9. `write class files!` — always last.

`validate-pes.sh` enforces rules 1, 2, and 9, checks that every multiplicity label appears in an earlier `restructure` and that every `inherit from` follows its `select`, and rejects any line that matches none of the directives above. It does not read the `.ses`, so it cannot tell whether a `select` targets a multi-aspect unit instead of its copies.

Blank lines are permitted between directives. `#`-prefixed comments are common but unofficial, and MS4 Me may tolerate them in recent versions. The validator skips both.
