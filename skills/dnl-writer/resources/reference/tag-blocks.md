# Tag blocks: embedding Java in a DNL file

A tag block is a head, then Java between `<%` and `%>`, then `!` (User's Guide p. 29). The Java is copied verbatim into a marked region of the generated class (`//ID:…` … `//ENDID`) and survives regeneration.

```
external event for waitForJob with Job
<%
    storedJob = (WorkToDo) messageList.get(0).getData();
    holdIn("sendJob", storedJob.getProcessingTime());
%>!
```

## The seven heads

| Head | Where the Java lands | In scope | Typical use |
|---|---|---|---|
| `initialize variables` | end of `initialize()`, after the default variable values and the initial `holdIn`/`passivateIn` | all state variables, `holdIn`, `passivateIn`, `getName()` | build initial payloads, size collections, override the initial hold time |
| `internal event for S` | `internalTransition()`, in the `phaseIs("S")` branch, **after** the automatic transition for `from S go to N` | state variables, `sigma`, `phase`, `currentTime`, `getTimeAdvance()` | counters, next payload, override the next state with `holdIn`/`passivateIn` |
| `output event for S` | `getOutput()`, in the `phaseIs("S")` branch; **replaces** the default, which sends `null` on every `after S output M` port | `output` (the bag), the `outM` port fields, state variables | `output.add(outJob, storedJob);` |
| `external event for S with M` | `externalTransition()`, inside `phaseIs("S")` and `hasMessages(inM)`, **after** the automatic `holdIn` for the `go to` target, before the `return` | `messageList` (the messages on `inM`), `input` (the whole bag), `timeElapsed`, `currentTime`, state variables | store the payload, compute a data-dependent hold time |
| `external event with M` | **observed**: `externalTransition()` before any phase check, guarded only by `hasMessages(inM)`; runs in every state, no `return`, so the state-specific handler still runs after it | as above | record every port present in a bag, regardless of state |
| `add library` | the import section of the class | – | `import java.util.HashSet;` |
| `add additional code` | the class body | – | helper methods, extra fields, inner classes |

Rules that follow from where the code lands:

- An `internal`, `output` or `external event for S` tag needs `S` to be a declared **hold** state (internal, output) or any declared state (external); otherwise the generator ignores it or the checker reports `dnl.tag-on-passive`.
- An `external event for S with M` tag runs only when a `when in S and receive M …!` statement exists for the same pair.
- Because event tags run **after** the automatic transition, `holdIn("N", t)` or `passivateIn("N")` inside them overrides it (guide p. 143). `N` must be a declared state, spelled exactly.
- `output event for S` replaces the default output entirely: if the tag is present, only what it adds is sent.
- Every `output.add(outX, …)` needs `generates output on X!` (or an `after … output X!`); otherwise the field does not exist and the Java does not compile.

## API available in tag code

| Call | Meaning |
|---|---|
| `holdIn("S", t)` | enter state `S` with time advance `t` |
| `passivateIn("S")` | enter state `S` with time advance ∞ |
| `phaseIs("S")` | true when the current state is `S` |
| `messageList` | `ArrayList<Message<T>>` of the messages on the handled port; `messageList.get(0).getData()` is the first payload |
| `input` | the whole input bag; `input.hasMessages(inM)` and `input.getMessages(inM)` |
| `output.add(outM, payload)` | add a message to the output bag; `payload` may be `null` |
| `timeElapsed` | time since the last event, in an external event |
| `currentTime` | simulated time |
| `getTimeAdvance()` | the current `sigma` |
| `getName()` | the model instance name, useful for per-instance parameters |
| `getStateVariableNames()` / `getStateVariableValues()` | reflection over `use` variables, used for logging |

## Writing portable tag code

- Cast payloads: `WorkToDo job = (WorkToDo) messageList.get(0).getData();`. MS4 Me's runtime types `getData()`, Closure-DEVS returns `Object`; the cast works in both.
- Iterate the list when several messages can arrive on one port in a bag: `for (Message<?> msg : messageList) { … msg.getData() … }`.
- Keep the generated-region markers (`//ID:`, `//ENDID`, `//<CTM>`, `//<FPDEVS>`) out of hand-written tag code; MS4 Me uses them to regenerate.
- Put imports in `add library`, helper methods in `add additional code`, never inside event tags.
