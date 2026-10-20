# SimulateModel API cheat sheet

Verified against `com.ms4systems.devs.core.util.SimulateModel` in the Closure-DEVS source and jar (1.0.0), and against the MS4 Me run-control documentation ("Conducting Experiments on RandomForest Model with Run Control Experimental Framework", July 2022). Where the two differ, the **Flavor** column says which.

## Folders and files (static fields of `SimulateModel`)

| Field | Value | Note |
|---|---|---|
| `folder` | `user.dir` + `/` | The project root when run from the project |
| `folderses` | `folder + "src/Models/ses/"` | Where `defineModel()` reads the `.ses` |
| `folderpes` | `folder + "src/Models/pes/"` | Where it reads the `.pes` |
| `foldertxt` | `folder + "src/Models/txt/"` | Where results land |
| `resultsFile` (instance) | `foldertxt + "RunSims.txt"` by default | `setResultsFile("name")` sets it to `foldertxt + "name"` |
| `model` (instance) | `Coupled` | Assigned in `defineModel()` |

## Lifecycle

| Call | Flavor | What happens |
|---|---|---|
| `defineModel()` | both | Override. Must assign `model`. Called by every `execute` |
| `setParams()` | both | Override. Called once per run after the model exists |
| `getAndAnalyzeResults()` | both | Override. Called once per run after the simulation stops |
| `execute()` | ms4me | `defineModel()` → run without viewer → `getAndAnalyzeResults()`; resets time and regenerates the model each call |
| `executeWView()` | ms4me | Same with the SimViewer open; slow, for one debugging run |
| `execute(int numIterations)` | closure | `defineModel()`; `new CoupledCoordinator(model)`; `initialize(0)`; `setParams()`; `simulate(numIterations)`; `getAndAnalyzeResults()` |
| `execute(Coupled m, int n)` | closure | Same on a model you built yourself (skips `defineModel()`) |
| `setMaxSimulationTime(long)` | ms4me | Only acts when `sim` exists; in the Closure-DEVS jar `execute(int)` never sets `sim`, so it is a no-op there |
| `setResultsFile(String name)` | both | `resultsFile = foldertxt + name` |

All three overridable methods except `defineModel()` declare the reflection exceptions (`ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException`); `main()` must declare or catch them.

## Parameters and results (reflection by name)

| Call | Effect |
|---|---|
| `setValue(String model, String var, Object val, Class t)` | Finds the atomic named `model` in `this.model`, invokes `set<Var>(t)`. Exceptions printed, not thrown |
| `SetValue(...)` (capital S) | Same, exceptions thrown |
| `setValuesForAll(String namePart, String var, Object val, Class t)` | Every atomic whose name **contains** `namePart` |
| `getValue(String model, String var)` → `Object` | Invokes `get<Var>()`; cast the result. Exceptions printed, returns `null` |
| `GetValue(...)` (capital G) | Same, exceptions thrown |
| `getValuesForAll(String namePart, String var)` → `ArrayList` | One entry per matching atomic |
| static `setValue(Coupled cm, model, var, val, t)` / `getValue(Coupled cm, model, var)` | Same on an explicit coupled model |

Rules: the first letter of `var` is upper-cased for the accessor name, nothing else is changed; `t` must be the setter's exact parameter class (`int.class`, `double.class`, `String.class`, `int[].class`, `ArrayList.class`); the atomic must be a direct child of `model` (nested coupled models: use the static variants on the inner `Coupled`).

### Model names inside the pruned coupled model

| SES construct | Name to pass |
|---|---|
| Plain entity `E` | `"E"` |
| Specialization with `select X from S for E!` | `"X_E"` (documented example: `"mode_RunningTransd"`; corpus: `"GenDetect_Gen"`, `"TransdSuccess_Transd"`) |
| Multi-aspect `set multiplicity of i as [N] for E!` | Instances share the `E` name part; use `setValuesForAll("E", ...)` |

## SES / PES helpers

| Call | Returns |
|---|---|
| `fileHandler.getContentsAsString(folderses + sesfile)` | The SES text |
| `fileHandler.getContentsAsString(folderpes + pesfile)` | The PES text to edit |
| `InternalUseSeS.getSesFromFile(sesfile)` | `sesRelationExtend` for `getChoices` |
| `getChoices(sesRelationExtend ses, String spec)` → `String[]` | The entities the SES allows under specialization `spec` |
| `selectFrom(String[] choices)` | One at random (uses `ContinuousTimeMarkov.Rand`) |
| `InternalUseSeS.getSesFromContents(sesContents, pesContents)` | `sesRelationExtend` for the edited pruning |
| `InternalUseSeS.pruneNTransToGetModelInstanceWContents(rses, pesContents)` | The pruned model; cast to `CoupledModelImpl` (ms4me) or `Coupled` (closure) |

### PES lines the run control edits (end with `!`)

```
set name of model as NTNBase!
pruned from NTN.ses!
select CellSAT from resolution for X0Y0_SAT!          <- selection: "select {current} from {spec} for {entity}"
set multiplicity of index as [3] for ProcessorOfJobs!  <- multi-aspect: "[{current}] for {entity}"
write class files!
```

Edit with `String.replace` (literal). `replaceAll` treats `[3]` as a regex character class.

## fileHandler

| Call | Effect |
|---|---|
| `writeToFile(String path, String text)` | Overwrites. Use it to clear the results file and write the CSV header |
| `appendToFile(String path, String line)` | Reads the file, appends `"\n" + line`, rewrites it. O(file size) per call |
| `readFromFile(String path)` / `getContentsAsString(String path)` | Whole file as a `String` |

## MS4 Me project layout the defaults assume

```
<project>/
  src/Models/ses/   *.ses
  src/Models/pes/   *.pes
  src/Models/dnl/   *.dnl
  src/Models/java/  Run*.java (package Models.java) and generated atomics
  src/Models/txt/   results, request and shared files
```
