# DNL statement catalogue

Source: MS4 Me User's Guide, Appendix 1 (pp. 170–172) and the elaboration chapter (pp. 133–144), cross-checked against the Langium grammar in `devs-vscode-extensions/packages/language/src/dnl/dnl.langium` and the corpus under `~/DEVS`. Forms marked **observed** are accepted by MS4 Me but absent from the guide's appendix.

## Lexical conventions

- A file is a sequence of statements. Every statement ends with `!`; whitespace before the `!` is allowed. Statements may span lines.
- `//` to end of line is a comment (undocumented, used throughout the guide's examples and the corpus).
- Keywords are matched case-insensitively (`To start`, `Hold in`, `Receive`, `Initialize variables` all occur in the guide). Names are case-sensitive.
- Identifiers: `[A-Za-z_][A-Za-z0-9_]*`. No dashes, spaces or periods. See `identifier-rules.md`.
- `then` after `after S` and after `receive M` is optional. `the` before `range of` is optional. A comma after `to start` is optional. An Oxford comma before `and` in a class declaration is accepted.
- Quoted strings (`"EXPR"` in `use … default` and `hold … for time`) are `"[^"]*"`: no escaping, so the expression itself cannot contain a double quote. A String variable gets its text in `initialize variables`, not in a default.
- Java types (`TYPE`) may be qualified and generic: `java.util.ArrayList<WorkToDo>`, `HashMap<String, Integer>`, `double[]`.

## Statements

| Form | Meaning | Example | Source |
|---|---|---|---|
| `passivate in S!` | `S` is a passive state: time advance ∞, leaves only on input | `passivate in waitForJob!` | p. 170 |
| `hold in S for time T!` | `S` is a hold state with time advance `T`, a non-negative number. `0` makes a transient state | `hold in sendJob for time 50!` | p. 171 |
| `hold in S for time "EXPR"!` | **observed**: `EXPR` is a Java expression, usually a `use` variable, pasted into `holdIn("S", EXPR)` | `hold in observe for time "observationTime"!` | pp. 31, 118, 144 |
| `to start passivate in S!` / `to start hold in S for time T!` | the initial state; exactly one per file; `to start, …` with a comma is also accepted | `to start, passivate in waitForJob!` | p. 171 |
| `from S go to N!` | internal transition of hold state `S`: after its time elapses, enter `N`. One per hold state (more than one is a warning; tag code must then choose) | `from sendJob go to waitForJob!` | p. 171 |
| `after S output M!` / `after S then output M!` | when `S`'s time elapses, emit on output port `outM` before the internal transition. Several per state allowed. Hold states only | `after sendJob output Job!` | p. 172 |
| `when in S and receive M go to N!` / `… then go to N!` | external transition: in `S`, a message on `inM` moves the model to `N` (restarting `N`'s timer) | `when in waitForJob and receive Job then go to sendJob!` | p. 172 |
| `when in S and receive M go to S eventually!` | **observed**: stay in `S` and keep the remaining time; no `holdIn` is generated | `when in observe and receive Ariv go to observe eventually!` | p. 144 |
| `accepts input on M!` | declares input port `inM`. Ports named in `when in … receive M` exist implicitly; use this for ports no transition names, and for typing | `accepts input on Stop!` | p. 163 |
| `accepts input on M with type T!` | typed input port; untyped ports carry `Serializable` | `accepts input on Job with type WorkToDo!` | p. 164 |
| `generates output on M!` | declares output port `outM`. Ports named in `after … output M` exist implicitly; required for ports only used in tag code | `generates output on Stop!` | p. 163 |
| `generates output on M with type T!` | typed output port | `generates output on Job with type WorkToDo!` | p. 164 |
| `use V with type T!` | state variable of Java type `T`, Java-default initialised | `use clock with type double!` | pp. 29, 136 |
| `use V with type T and default "E"!` | same, initialised to the Java expression `E` in the field declaration and again in `initialize()` | `use count with type int and default "0"!` | pp. 29, 136 |
| `C has F1, F2 and F3!` | declares message class `C` with fields; MS4 Me generates `C.java` with constructors, getters, setters and `toString` | `WorkToDo has id, startTime, and processingTime!` | pp. 29, 136 |
| `range of C's F is T!` / `the range of C's F is T!` | the Java type of field `F` of class `C` | `range of WorkToDo's id is int!` | pp. 29, 136 |
| `HEAD <% java %>!` | tag block; `HEAD` is one of the seven heads in `tag-blocks.md` | see `tag-blocks.md` | p. 29, pp. 133–144 |
| `//<DNLType>CTM<DNLType>!` | **observed**: first line of a Markov file written by newer MS4 Me versions (`CTM`, `DTM` or `MM`) | | corpus |

## Regular expressions (one statement, whitespace collapsed, keywords case-insensitive)

```
ID    = [A-Za-z_][A-Za-z0-9_]*
TYPE  = [A-Za-z_][A-Za-z0-9_.<>,? ]*(\[\])*
TIME  = ([0-9]+(\.[0-9]+)?|\.[0-9]+|"[^"]*")

passive       ^passivate in ID *!$
hold          ^hold in ID for time TIME *!$
initial       ^to start,? (passivate in ID|hold in ID for time TIME) *!$
internal      ^from ID go to ID *!$
output        ^after ID (then )?output ID *!$
external      ^when in ID and receive ID (then )?go to ID( eventually)? *!$
input port    ^accepts input on ID( with type TYPE)? *!$
output port   ^generates output on ID( with type TYPE)? *!$
variable      ^use ID with type TYPE( and default "[^"]*")? *!$
class         ^ID has ID(, ID)*(,? and ID)? *!$
class field   ^(the )?range of ID's ID is TYPE *!$
markov header ^//<DNLType>(CTM|DTM|MM)<DNLType> *!$
tag head      ^(initialize variables|internal event for ID|output event for ID|external event for ID with ID|external event with ID|add library|add additional code) *$
comment       ^//.*$
```

A tag block is a tag head, then `<%`, Java, `%>`, then `!`.

## Reading an existing file into the Model Context

1. Collect `accepts input` / `generates output` lines → `inputs` / `outputs` with types.
2. Collect `hold in` / `passivate in` lines → `states`; mark the `to start` one `initial`.
3. Attach `from S go to N` → `states[S].internal`; `after S output M` → `states[S].outputs` (and an implied output `M`); `when in S and receive M go to N [eventually]` → `states[S].externals` (and an implied input `M`).
4. Collect `use` → `variables`; `C has …` + `range of C's …` → `classes`.
5. Collect tag blocks verbatim into `tags`, keeping the Java untouched.
6. If the first line is a `//<DNLType>` header or any tag contains `<ProbDEVS>`, set `markov: true` and read `../patterns/markov.md`.
