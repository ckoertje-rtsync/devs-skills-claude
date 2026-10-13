# Structures from the MS4 Me User's Guide

Verbatim transcriptions of the guide's SES and PES examples (as kept in `devs-vscode-extensions/packages/language/test/fixtures/guide`). They show constructs the skill's two small examples do not, and they show forms the guide writes that the skill does not emit. `check-ms4me.sh` (the real parser) accepts all of them; `validate-ses.sh` is a linter for the skill's own output and rejects two.

| Files | Pages | Shows | `validate-*.sh` | `check-ms4me.sh` |
|---|---|---|---|---|
| `World.ses`, `WorldTest.pes` | 120–123 | one unit with two specializations (`index` to number copies, `height` to vary them); a copy name as a coupling endpoint (`id0_Person`); `all` to `all`; a per-copy `select` with `inherit from` | pass | pass; `--generate` wants a `select` for `id0_Person` and `id2_Person` too |
| `MultiEnclaveNet.ses`, `MultiEnclaveNet.pes` | 125–126 | nested multi-aspects sharing the label `index`; parent-to-child and child-to-parent couplings that give the nested coupled model its ports; `//` comments | pass | pass; `--generate` writes `MultiEnclaveNet.java` and `Enclave.java` |
| `Try.ses`, `Try.pes` | 129 | a unit with a tagging (`index`) and a variant (`type`) specialization; `mergeAll from`; `don't select` | pass | pass |
| `DatingGame.ses` | 123–125 | `each` to `each` matching on `height`; `all` to `all`; the parent sending to every copy; written with an article (`the DatingGame`) and a copy name (`short_Boy`) whose prefix is not an option | fails (article, copy prefix) | pass |
| `Car.ses` | 175–178 | articles before every name; a specialization with articles; intervals with mixed brackets; an enumerated string range | fails (articles) | pass |

The matching `.pes` for `DatingGame` and `Car` is not printed in the guide. A `DatingGame.pes` restructures on `height` without a multiplicity (p. 124); a `Car.pes` is the minimal three lines plus one `select … from engineType for Engine!` and one `… from gearShift for Transmission!`, each followed by `inherit from`.
