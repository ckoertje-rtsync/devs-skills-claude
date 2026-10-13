# Accepted forms, validators and diagnostics

The SES and PES parsers accept more than the skill emits. This file lists the variants, says which checker accepts which, gives the authoritative reserved words, and maps the diagnostic codes of the `ms4me` checker (the VS Code extension's, run by `scripts/check-ms4me.sh`) to fixes. Sources: the MS4 Me User's Guide (pages cited), the corpus under `~/DEVS`, and the extension's grammars.

## 1. Readability variants

| Form | Example | MS4 Me and `check-ms4me.sh` | `validate-ses.sh` / `validate-pes.sh` | Skill emits |
|---|---|---|---|---|
| capitalised `From` | `From the top perspective, …` | yes (guide) | yes | no |
| no `the` after `from` | `from top perspective, …` | yes (corpus) | yes | no |
| articles before entity names | `a Car is made of an Engine, the Transmission, and a Chassis!` (p. 175) | yes | **no** | no |
| `is (are)` | `Processors is (are) made of …` (p. 174) | yes | **no** | no |
| Oxford comma | `A, B, and C!` (pp. 127, 175) | yes | yes | no |
| whitespace before `!` | `write class files !` (guide throughout) | yes | yes | no |
| no space after the comma | `perspective,X sends …` (corpus) | yes | **no** | no |
| a statement over several lines | long `is made of` lists | yes | **no** (line based) | no |
| `//` comment lines | `//sharing of updates among enclaves` (p. 125) | yes | yes | when useful |
| `#` comment lines | | **no** (parse error; neither grammar has `#`) | skipped | never |
| `a`, `an` as names | the Lorenz model couples ports `a` and `b` | yes | **no** (reserved list) | no |
| any identifier as a range type | `is int`, `is double`, `is boolean` (corpus) | yes | only `number`, `integer`, `real`, `string` with values | one of the four |
| `the` omitted before `range of` | `range of Engine's HoursRun is number!` | yes | **no** | with `the` |
| bracketed value list | `with values [A, B, C]!` (p. 178) | yes | **no** | plain list |
| numeric value list | `with values 1, 2 and 3!` | yes | yes | yes |
| interval with `(` or `)` | `(-6.3, 6.3]` | exactly two numbers, else `ses.bad-interval` | same | yes |
| copy name as a coupling endpoint | `all Person sends Bye to id0_Person!` (p. 123) | any `<prefix>_<Unit>` | prefix must be an option of the unit | no |
| PES `select OPT for E!` | corpus only | warning `pes.short-select` | **no** | no |
| PES `mergeAll from X.ses!` | pp. 27, 129 | yes | yes | no |
| PES `name`, `model`, `class`, `files` as names | | yes | n/a | no |

When a user hands over a guide-style or corpus `.ses`, run `check-ms4me.sh` on it; `validate-ses.sh` is a linter for the forms the skill writes, not a parser for the language.

## 2. Reserved words

Words with grammatical meaning (User's Guide Appendix 2 and the extension's grammars). Avoid them as entity, message, label and variable names even where the parser would cope.

SES: `from the a an perspective is are made of more than one can be in or sends to as all each has range with values and`.

PES: `set name of model as pruned from mergeAll restructure multiaspects using then multiplicity for select inherit don't under write class files`.

Parser exceptions: SES accepts `a` and `an` as names; PES accepts `name`, `model`, `class` and `files`. `number`, `integer`, `real` and `string` are not keywords; they are the type names the skill chooses.

## 3. What the `ms4me` checker reports

`scripts/check-ms4me.sh FILE-OR-DIR…` parses every `.dnl`, `.ses` and `.pes` under the paths together and prints `path:line:col: message`, then `N files, E errors, W warnings`; exit 0 iff `E` is 0.

### SES codes

| Code | Severity | Meaning | Fix |
|---|---|---|---|
| `ses.root-first` | error | first statement is not an aspect | move the root's aspect to line 1 |
| `ses.not-introduced` | error | an entity is used before any aspect, multi-aspect or specialization introduces it | introduce it, or fix the spelling (case matters) |
| `ses.duplicate-in-list` | error | a name twice in one `is made of`, `can be` or `has` list | remove the repeat |
| `ses.duplicate-aspect` | error | the same perspective declared twice for one entity | list every child in one statement |
| `ses.multi-aspect-arity` | error | `more than one` followed by several names | one unit per multi-aspect |
| `ses.cycle` | error | an entity appears below itself (strict hierarchy) | rename the lower occurrence |
| `ses.no-such-aspect` | error | a coupling names a perspective no aspect has | use the aspect that contains both endpoints |
| `ses.multi-aspect-perspective` | error | a coupling under a multi-aspect's own perspective | name the aspect that contains the multi-aspect's parent |
| `ses.out-of-scope` | error | endpoint not the aspect's parent, a child, or the unit of a child's multi-aspect | see `patterns/coupling-scope.md` |
| `ses.quantifier-on-non-unit` | error | `all`/`each` on an entity that is not a multi-aspect unit | drop the quantifier or make the entity a unit |
| `ses.self-coupling` | error | `X sends M to X` | couple two different entities; `all X … all X` is the broadcast form |
| `ses.duplicate-variable` | error | a variable declared twice for one entity | remove the repeat |
| `ses.unknown-variable` | error | a range for a variable the entity (or its general entity) never declares | add `E has V!` |
| `ses.bad-interval` | error | `(` or `)` with other than two numbers | `[0, 100]`, `(0, 1]`, or a plain list |
| `ses.no-model` | warning / info | inside an MS4 Me project: a leaf entity with no `.dnl` or `.java` (info when it is an unchosen option) | write the model, or rename the entity to match an existing class |
| `ses.unknown-port` | warning | inside an MS4 Me project: a coupling names a port the entity's `.dnl` does not declare | fix the message name or add `as` |

### PES codes

| Code | Severity | Meaning | Fix |
|---|---|---|---|
| `pes.missing` / `pes.duplicate` / `pes.order` | error | the three mandatory directives absent, repeated, or out of place | `set name` first, `pruned from` second, `write class files` last |
| `pes.not-ses-file` | error | `pruned from` / `mergeAll from` names something other than `X.ses` | add the `.ses` suffix |
| `pes.ses-not-found` | error | the SES is not in the sibling `ses/` folder, the same folder, or the workspace | fix the name or the location |
| `pes.unknown-label` | error | `set multiplicity of L` with no earlier `restructure … using L` | add or extend the `restructure` line |
| `pes.not-a-unit` | error | `set multiplicity … for X` / `don't select … under X` where `X` is not a multi-aspect unit in the SES | name the unit, not its collective parent |
| `pes.label-not-in-ses` | error | the label is not a specialization of that unit or entity in the SES | declare `Unit can be id in LABEL!` or use the declared label |
| `pes.unknown-entity` | error | `select … for X` names nothing in the SES, not even a copy | fix the name; copies are `<option><k>_<Unit>` |
| `pes.unknown-option` | error | the option is not an option of that label | use one of the listed options |
| `pes.inherit-without-select` | error | `inherit from OPT` before `select OPT …` | move it after the select |
| `pes.select-on-unit` | warning | `select … for Unit` although `set multiplicity` expands `Unit` into copies | select on `id0_Unit`, `id1_Unit`, … |
| `pes.copy-before-restructure` | warning | a copy name used before any `restructure` | move the `restructure` line up |
| `pes.short-select` | warning | `select OPT for E!` without `from LABEL` | add `from LABEL` |
| `pes.many-aspects` | info | an entity has several aspects; no directive chooses | expected; MS4 Me chooses in the pruning view |

## 4. Undocumented and unverified forms

Kept here so the skill neither emits them nor rejects them in user files (spec `docs/ms4me-dsl-spec.md`, section 7):

- `mergeAll from X.ses!`: shown on pp. 27 and 129, never explained, written by the GUI in every recent PES.
- `select OPT for ENTITY!` without `from LABEL`: corpus only, in files not produced by MS4 Me.
- Aspect selection has no textual directive; a hand-written PES cannot pin the aspect of an entity that has several.
- Similarities are named as an SES component (p. 173); no syntax is given anywhere.
- SES variables have no documented effect on generated code; the corpus uses them as documented parameters.
- `all` vs `each`: Appendix 2 calls them interchangeable; the chapter (pp. 122–125) and the corpus give them the broadcast vs matching meanings the skill uses.
- Dangling couplings are not caught by MS4 Me (a corpus SES couples to an entity it never introduces); the checkers here do catch them.
- Keyword case: the guide's examples imply case-insensitive keywords; names are case-sensitive (`hungry` and `Hungry` are two ports in the ZooChimps example, a bug in that example).
