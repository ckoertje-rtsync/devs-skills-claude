# Identifiers, keywords and names

## Identifier form

```
[A-Za-z_][A-Za-z0-9_]*
```

Letters, digits and underscore; must not start with a digit. No dashes, spaces or periods (`3strikes`, `Initial State`, `Initial.State`, `Initial-State` are all invalid; User's Guide p. 170). This is stricter than SES, which allows dashes; because the SES entity name must equal the DNL file name, use DNL's rule everywhere.

Names are case-sensitive: `hungry` and `Hungry` are two different ports.

## What a name becomes

| DNL name | Java |
|---|---|
| file base name `Name.dnl` | `public class Name` |
| state `S` | the string `"S"` in `holdIn`/`passivateIn`/`phaseIs` |
| input message `M` | field `inM` |
| output message `M` | field `outM` |
| variable `V` | field `V` |
| class `C`, field `f` | `class C` with field `f`, `getF()`, `setF()` |

So every name must be a valid Java identifier and not a Java reserved word (p. 161).

## Java reserved words (never use as a name)

```
abstract assert boolean break byte case catch char class const continue default do double else enum
extends final finally float for goto if implements import instanceof int interface long native new
package private protected public return short static strictfp super switch synchronized this throw
throws transient try void volatile while true false null
```

Common collisions and replacements: `default` → `defaultValue`, `new` → `fresh`, `class` → `kind`, `int` → `count`, `double` → `value`, `final` → `last`, `package` → `parcel`.

## DNL keywords

Grammatical words of the language: `to start hold in for time passivate from go when and receive then eventually after output accepts input on generates with type use default has range of is the initialize variables internal external event add library additional code`.

The Langium grammar lets most of them double as names when the statement shape stays unambiguous, so a port named `Start`, `Stop`, `Output` or `Event` is fine. Three cannot be names: **`has`, `with`, `and`**. Prefer names that are not keywords anyway; a state named `in` or a port named `to` reads badly.

## Conventions seen in the guide and corpus

- States: lower camel case verbs or verb phrases (`waitForJob`, `sendJob`, `observe`, `done`); the guide also uses capitalised nouns (`Hungry`, `Satisfied`).
- Messages: upper camel case nouns (`Job`, `Stop`, `Hello`, `ReleaseTwoMolecules`).
- Variables: lower camel case (`count`, `storedJob`, `observationTime`).
- Classes: upper camel case (`WorkToDo`, `IntEnt`).
- Model names: upper camel case, often `<Role>Of<Thing>` (`GeneratorOfJobs`, `ProcessorOfJobs`) or a plain role (`Transducer`).
