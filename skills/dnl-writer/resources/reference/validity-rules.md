# Validity rules and checker diagnostics

## Rules (User's Guide pp. 161–172, plus observed generator behaviour)

| Rule | Severity | Source |
|---|---|---|
| exactly one `to start` state | error | p. 171 |
| every hold state has an internal transition `from S go to N!` | error when missing; warning when more than one (tag code must then choose) | p. 171 |
| `after S output M!` only on hold states | error | p. 172 |
| `internal event for S` / `output event for S` only on hold states | error | generator |
| `go to` targets and the states named in `from`, `after`, `when in` and tag heads are declared | error (unresolved reference) | p. 162 |
| states, ports (per direction), variables and classes are declared once | error | – |
| the model name and every identifier is a valid Java identifier and not a Java reserved word | error | p. 161 |
| a port used only in tag code (`output.add(outX, …)`) is declared with `generates output on X!` | warning; the Java will not compile otherwise | observed |
| `hold in S for time "name"` names a declared `use` variable | info; otherwise the Java will not compile | observed |

## Checker output

`resources/scripts/check-dnl.sh FILE…` runs `ms4me check` and prints one line per diagnostic:

```
/path/Model.dnl:LINE:COL: message
N files, E errors, W warnings
```

Exit 0 iff `E` is 0. Codes and fixes:

| Code | Severity | Message (abridged) | Fix |
|---|---|---|---|
| `dnl.no-initial-state` | error | No initial state | prefix one state with `to start` |
| `dnl.many-initial-states` | error | More than one initial state | keep one `to start` |
| `dnl.duplicate-state` / `-port` / `-variable` / `-class` | error | declared more than once | remove the duplicate line |
| `dnl.missing-internal-transition` | error | Hold state 'S' has no internal transition | add `from S go to N!` |
| `dnl.extra-internal-transition` | warning | more than one internal transition | keep one `from`, choose the next state in `internal event for S` with `holdIn`/`passivateIn`; or keep several on purpose (`patterns/idioms.md` §8) |
| `dnl.internal-on-passive` | error | passive state cannot have an internal transition | make the state a hold state, or delete the `from` line |
| `dnl.output-on-passive` | error | passive state cannot produce output | route through a zero-time hold state (`patterns/idioms.md` §1) |
| `dnl.tag-on-passive` | error | passive state has no internal/output event | move the tag to the hold state that does the work |
| `dnl.reserved-name` | error | Java reserved word | rename (`default` → `defaultValue`, `new` → `fresh`, …) |
| `dnl.tag-only-port` | warning | output port used in tag code but never declared | add `generates output on X!` |
| `dnl.unknown-time-variable` | info | hold time names no declared variable | add `use NAME with type double and default "…"!` or write a literal |
| `dnl.undocumented-form` | info | `eventually`, or `external event with M` without a state | intentional; no change needed |
| `dnl.markov` | info | Markov model recognised | see `patterns/markov.md` |
| (parser) | error | unexpected token / expecting '!' | a statement does not match any form; compare with `dnl-grammar.md` |
| (linker) | error | Could not resolve reference to StateDecl named 'X' | declare `X` with `hold in` or `passivate in` |

## Review checklist before WRITE

- [ ] One `to start`.
- [ ] Every `hold in S` has one `from S go to`.
- [ ] No `after`, `internal event` or `output event` on a `passivate in` state.
- [ ] Every port used in tag code is declared.
- [ ] Every `holdIn("N", …)` / `passivateIn("N")` in tag code names a declared state.
- [ ] Every `external event for S with M` has a matching `when in S and receive M`.
- [ ] Payload reads are cast; imports live in `add library`.
- [ ] File name equals the model name and is a valid Java identifier.
