# Pattern — Variables and Ranges

Variables parameterize entities without adding structural branches. They inherit down the tree per the attached-variables axiom, so a variable declared on an ancestor is visible to every descendant.

## Declaring variables

```
{ENTITY} has {VAR_1}, {VAR_2} and {VAR_N}!
```

- `has` lists any number of variables on one line.
- Variable names follow the identifier rules (`identifier-rules.md`).
- A second `has` on the same entity adds to the list; it does not replace.

## Typing and range

By default, a variable is type-free (its type is inferred from first use). To constrain a variable, add a range statement:

```
the range of {ENTITY}'s {VAR} is {TYPE} with values {RANGE_EXPR}!
```

`{TYPE}` is one of `number`, `integer`, `real`, or `string`.

`{RANGE_EXPR}` takes two forms:

- **Interval** (numeric types): `[MIN, MAX]`, `(MIN, MAX)`, `[MIN, MAX)`, `(MIN, MAX]`. `[`/`]` are inclusive, `(`/`)` exclusive.
- **Enumeration**: `v1, v2, v3` (comma-separated, `and` before the last). Values must be valid identifiers or numeric literals — no quoted strings, no spaces inside a value.

## Inheritance

A variable declared on an ancestor entity is readable by every descendant without re-declaration. When a descendant declares the *same* variable name, its range locally overrides the ancestor's range for that subtree.

```
Vehicle has maxSpeed!
the range of Vehicle's maxSpeed is number with values [0, 300]!
Car can be Sedan or Truck in bodyType!
Truck has maxSpeed!
the range of Truck's maxSpeed is number with values [0, 150]!
```

The `Truck` variant inherits `maxSpeed` from `Vehicle` implicitly, but then locally tightens the range to `[0, 150]` — sedans retain the ancestor's `[0, 300]`.

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
