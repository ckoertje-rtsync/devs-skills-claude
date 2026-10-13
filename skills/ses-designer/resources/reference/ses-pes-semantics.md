# What SES and PES text means downstream

What MS4 Me (and the VS Code extension in `devs-vscode-extensions`, which targets the Closure-DEVS runtime) makes of each statement: the root, the ports a coupling names, the DEVS coupling kind, the names restructuring produces, the Java `write class files!` writes, and how `.dnl`, `.ses` and `.pes` find each other. Pages cite the MS4 Me User's Guide; "verified" means confirmed with the extension's generator on the guide's own examples.

## 1. Root and file identity

- One `.ses` is one entity structure. Its base name is what a PES writes in `pruned from NAME.ses!`, so it must be a valid Java identifier (p. 165).
- **The first statement must be an aspect**, and its parent is the root system (p. 166). The extension reports anything else as `ses.root-first`. The root's name need not equal the file name (corpus: `Interfered.ses` has root `SatComs`).
- Every entity name is a class name. A leaf entity resolves to `Models/java/<Entity>.java`, generated from `<Entity>.dnl` or hand-written. An entity with an aspect becomes a generated coupled class after pruning. The specialization option `id` in `Unit can be id in index!` is the one exception: it only numbers copies and is no model.

## 2. Ports: what a coupling names

MS4 Me adds `out` on the sender and `in` on the receiver (pp. 123, 162–164):

| Statement | Sender port | Receiver port |
|---|---|---|
| `X sends M to Y!` | `outM` | `inM` |
| `X sends M to Y as R!` | `outM` | `inR` |
| `X sends outM to Y as inR!` | `outM` | `inR` |

The prefix is added **unless the name already starts with `out` or `in` followed by a capital letter, digit or underscore** (extension rule, from the generated code). So `outJob` stays `outJob`, but a message named `output` would become `outoutput` and `index` would become `inindex`. The guide's own `GeneratorOfJobs sends outJob to Transducer as inAriv!` (p. 127) therefore wires `outJob` to `inAriv`.

Consequences:

- Write the bare message name (`Job`) when the DNL models declare `generates output on Job!` and `accepts input on Job!`. Use `as` only when the receiver's port has another name.
- Hand-written Java models with explicit port names are coupled by writing those names, prefixed or not; the extension's port check accepts an unprefixed port (`arrive`) when the Java declares it that way.
- Inside an MS4 Me project the extension compares every coupling with the `.dnl` ports (`ses.unknown-port`) and flags leaf entities with no `.dnl` or `.java` (`ses.no-model`).

## 3. Which DEVS coupling a statement becomes

For `from the A perspective, X sends M to Y!`, with `P` the parent entity of aspect `A`:

| Sender | Receiver | Coupling | Generated line (verified) |
|---|---|---|---|
| child of `A` | child of `A` | internal coupling (IC) | `addCoupling(X.outM, Y.inM);` |
| `P` | child of `A` | external input coupling (EIC) | `addCoupling(this.inM, Y.inM);` |
| child of `A` | `P` | external output coupling (EOC) | `addCoupling(X.outM, this.outM);` |

A parent-to-child coupling therefore gives the coupled model an input port `inM`, and a child-to-parent one an output port `outM`; that is how a nested coupled entity gets ports at all (`DBServer sends DBUpdate to Enclave!`, p. 126).

## 4. Restructuring and copy names

`restructure multiaspects using LABEL!` replaces every `Parent is made of more than one Unit!` whose unit carries a specialization labelled `LABEL` (p. 121):

- `Parent` disappears; the copies become children of `Parent`'s parent, inside that entity's aspect. This is why couplings over a multi-aspect name the enclosing aspect, never the multi-aspect perspective.
- **Single-option label with a multiplicity**: `Unit can be OPT in LABEL!` plus `set multiplicity of LABEL as [N] for Unit!` gives `OPT0_Unit … OPT(N-1)_Unit`. The prefix is the option name, whatever it is: `Processor can be id in index!` gives `id0_Processor`; `Enclave can be enclaveid in index!` gives `enclaveid0_Enclave` (verified).
- **Multi-option label without a multiplicity**: `Boy can be shortish, medium or tall in height!` restructured on `height` gives one copy per option, `shortish_Boy`, `medium_Boy`, `tall_Boy` (p. 124), and `each`/`each` couplings pair copies that share the prefix.
- Copies are selectable (`select medium from height for id1_Person!`, p. 122) and may be coupling endpoints in the `.ses` itself (`all Person sends Bye to id0_Person!`, p. 123); such couplings are kept as written. The extension resolves any `<prefix>_<Unit>` to its unit; `validate-ses.sh` is stricter and wants the prefix to be an option of the unit.
- Nested multi-aspects get the same count under every parent copy (p. 127, a stated limitation). Labels that differ by level are listed bottom-up: `restructure multiaspects using clientIndex then enclaveIndex!` (p. 126). Each label must be a specialization label of the unit it is used with; the extension reports `pes.label-not-in-ses` otherwise.

Quantified couplings expand over the copies (pp. 123–125): `all X sends M to Y` one line per copy; `X sends M to all Y` likewise; `all X sends M to all X` every ordered pair without the diagonal; `each X sends M to each Y` only matching prefixes, unmatched copies get nothing. In the generated Java the copy name is the instance name: `new Processor("id0_Processor")`.

## 5. What `write class files!` generates

One Java class per entity that has an aspect: the top one named by `set name of model as NAME!`, and one per nested coupled entity named after the entity (`MultiEnclaveNet.java` and `Enclave.java`, verified). Leaf classes must already exist. The shape, from the extension (MS4 Me's differs only in the base class, `CoupledModelImpl` instead of Closure-DEVS's `Coupled`):

```java
public class Enclave extends Coupled {
    public Enclave(String name) {
        super(name);
        // Components
        DBServer DBServer = new DBServer("DBServer");
        addChildModel(DBServer);                       // atomic child
        Client clientid0_Client = new Client("clientid0_Client");
        addChildModel(clientid0_Client);
        // a nested coupled child uses addCoupledChildModel(...)
        // External input couplings (EIC)
        addCoupling(this.inDBUpdate, DBServer.inDBUpdate);
        // Internal couplings (IC)
        addCoupling(clientid0_Client.outDBQuery, DBServer.inDBQuery);
        // External output couplings (EOC)
    }
}
```

The file starts with an identity comment naming the source `.pes` and `.ses` and a hash; it must not be edited.

Selection: `select OPT from LABEL for E!` substitutes the option for the entity; with `inherit from OPT!` the generated class for `E` extends `OPT`'s class and so has its behaviour; without it "the selected models have empty behaviors" (p. 27).

Two things the PES cannot or need not say:

- **Unmade choices.** MS4 Me picks at random for any specialization the PES leaves open (pp. 122, 168). The extension's generator instead refuses: `'id0_Person' needs 'select … from height for id0_Person!'` (verified). A PES meant for `check-ms4me.sh --generate` selects every label on every entity and copy.
- **Aspect choice.** An entity with several aspects is resolved in MS4 Me's pruning view; no directive exists (p. 168). The extension reports `pes.many-aspects` as an info.

## 6. How the three files link

| Link | Rule |
|---|---|
| SES entity → model class | same name: `Processor` in the SES is `Processor.dnl` → `Processor.java`, or a hand-written `Processor.java` |
| SES coupling → DNL ports | `X sends M to Y` needs `generates output on M!` (port `outM`) in `X` and `accepts input on M!` (port `inM`) in `Y`; with `as R`, `Y` needs `inR` |
| PES → SES | `pruned from FILE.ses!` names the file; `select … from LABEL for ENTITY` names a specialization declared in it; `set multiplicity … for UNIT` names a multi-aspect unit; `restructure … using LABEL` names the unit's specialization label |
| PES → generated Java | `set name of model as N` → `N.java`; every entity with an aspect → its own class |
| generated Java → source | the header comment names the `.dnl`, or the `.pes` and `.ses`, it came from |

Entity, DNL file, Java class and coupling endpoint share one name, so DNL's identifier rule (letters, digits, underscore; no dashes; not a Java reserved word) is the effective rule for every entity and message name. Dashes survive only in aspect labels, specialization labels and variable names.

## 7. Where the files live

An MS4 Me project keeps `src/Models/dnl/`, `src/Models/ses/`, `src/Models/pes/` and the generated `src/Models/java/` (plus `src/Models/xml/` for Markov tables). The corpus follows this layout (`sda-pwsa`, `devs-sf-paradevs`, the Closure-DEVS test trees). A PES finds its SES first in the `ses/` folder beside its own folder, then in its own folder, then anywhere in the workspace by base name; keeping a `.ses` and its `.pes` files in one folder, as the skill's examples do, also resolves.
