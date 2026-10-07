# SES Identifier Rules

Every entity, aspect label, specialization label, message, and variable in an SES or PES file must be a valid SES identifier.

## Syntax

Regex (POSIX-extended):

```
[A-Za-z_][A-Za-z0-9_-]*
```

- Must start with a letter or underscore.
- May contain letters, digits, underscores, and dashes.
- Dashes are **allowed in SES** (and PES). This differs from Atomic DEVS (`.dnl`), which forbids them.
- No spaces, no periods, no other punctuation.

## Valid examples

```
HelloWorld       hello_world      hello-world      _privateField
State7           Waiting_for_out  electric-engine  CVT
```

## Invalid examples

```
3strikes           (starts with digit)
Initial State     (contains space)
Initial.State     (contains period)
Initial/State     (contains slash)
A!                (`!` is a statement terminator, not part of the name)
```

## Reserved keywords — do not use as identifiers

These tokens are grammatical markers in the SES / PES natural language. The validator flags them when they appear where an identifier is expected.

```
from        the        perspective
is          are        made        of
more        than       one
can         be         in          or
sends       to         as
all         each
has
range       with       values
number      integer    real        string
set         name       model       pruned      restructure
multiaspects using     multiplicity for        select
write       class      files
```

Comparison tokens (used in future ranges): `and` is both a grammar connector and commonly reserved; avoid using `and` or `or` as identifiers.

## Case

MS4 Me is case-sensitive for identifiers but flexible for some grammar particles (e.g. `from` and `From` are both accepted at line starts). Keep identifiers in a consistent case (CamelCase or snake_case) within a single project to avoid accidental collisions.

## Collective (multi-aspect parent) names

Prefer an explicit collective suffix — `UAVGroup`, `ProcessorPool`, `TargetSet` — over a trailing-`s` plural of the unit name (`UAVs`, `Processors`). MS4 Me's natural-language parser intermittently singularizes a trailing `s` and can reject or collide the declaration. See `patterns/multi-aspect-idioms.md` → "Naming the collective parent".

## File names

`.ses` and `.pes` file base names must themselves be valid identifiers (Java-identifier rules apply to file names per MS4 Me User's Guide p. 165), even though the content inside may use dashes. The skill maps any dashes in `system_name` to underscores for the file name.

## Node IDs in Mermaid output

When the skill renders an entity hierarchy to Mermaid, dashes in identifiers are replaced with underscores in node IDs (Mermaid's grammar does not allow dashes in IDs). The display label retains the original dash. See `mermaid-label-safety.md`.
