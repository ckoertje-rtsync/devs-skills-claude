# Pattern — Variables and Ranges

Variables parameterize entities without adding structural branches. Each entity's variable names must be distinct (attached-variables axiom). Specialization options inherit their general entity's variables (inheritance axiom). See `../reference/ses-axioms.md`.

## Declaring variables

```
{ENTITY} has {VAR_1}, {VAR_2} and {VAR_N}!
```

- `has` lists any number of variables on one line.
- Variable names follow the identifier rules (`identifier-rules.md`).
- A second `has` on the same entity adds to the list; it does not replace. It must not repeat a name already declared on that entity, and the validator rejects a repeat.

## Typing and range

By default, a variable is type-free (its type is inferred from first use). To constrain a variable, add a range statement:

```
the range of {ENTITY}'s {VAR} is {TYPE} with values {RANGE_EXPR}!
```

To give only the type, omit `with values ...`: `the range of {ENTITY}'s {VAR} is {TYPE}!` (User's Guide p. 177).

`{TYPE}` is one of `number`, `integer`, `real`, or `string`.

`{RANGE_EXPR}` takes two forms:

- **Interval** (numeric types): `[MIN, MAX]`, `(MIN, MAX)`, `[MIN, MAX)`, `(MIN, MAX]`. `[`/`]` are inclusive, `(`/`)` exclusive.
- **Enumeration**: `v1, v2, v3` (comma-separated, `and` before the last). Values must be valid identifiers or numeric literals — no quoted strings, no spaces inside a value.

## Inheritance

Inheritance runs along specializations, not along aspects. The SES inheritance axiom says a specialization passes "all variables and Aspects from the parent Entity to the children Entities" (Traoré et al. 2019; see `../reference/ses-axioms.md`). Each option of a specialization therefore carries the variables of its general entity:

```
Vehicle has maxSpeed!
the range of Vehicle's maxSpeed is number with values [0, 300]!
Vehicle can be Sedan or Truck in bodyType!
```

Both `Sedan` and `Truck` have `maxSpeed`, without re-declaration. A child in an aspect does **not** inherit its parent's variables: in `Car is made of Engine and Chassis!`, `Engine` does not get `Car`'s variables.

The guide does not say whether an option may re-declare an inherited variable with a narrower range. The skill avoids that pattern; if an option needs its own parameter, give it a differently named variable.

## When to use a variable vs. a specialization

- Parameter differs numerically or by a string tag → **variable + range**.
- Behavior, ports, or sub-structure differs → **specialization**.

Rule of thumb: if the same DEVS atomic implementation works for every value of the parameter, it's a variable. If you would write a different `.java` atomic per value, it's a specialization.

## Common range patterns

```
Engine has HoursRun!
the range of Engine's HoursRun is number with values [0, 1000000]!

Processor has meanServiceTime, jitter!
the range of Processor's meanServiceTime is number with values (0, 60]!
the range of Processor's jitter is number with values [0, 5]!

Sensor has mode!
the range of Sensor's mode is string with values pressure, temperature and humidity!

Buffer has capacity!
the range of Buffer's capacity is integer with values [1, 4096]!
```

## Pitfalls the skill guards against

- **Missing `'s`.** `the range of Engine HoursRun ...` parses as a free-text statement and is rejected. The `'s` is load-bearing.
- **Whitespace inside enumerated values.** `the range of Sensor's mode is string with values read only, write only!` — the space inside `read only` breaks the regex. Use `read_only, write_only`.
- **Bracket mismatch.** `[0, 10)` is fine; `[0, 10>` is not. Only `[ ] ( )` are accepted.
- **Type/Bound mismatch.** `the range of Counter's n is integer with values [0.5, 10]!` — floating-point bounds on an integer variable. The validator flags this as a soft warning; MS4 Me itself may round silently.
