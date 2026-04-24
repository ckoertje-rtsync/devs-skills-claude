# Closure-DEVS API cheatsheet

Maven coordinates: `com.ms4systems:Closure-DEVS:1.0.0`
Default JAR path: `~/.m2/repository/com/ms4systems/Closure-DEVS/1.0.0/Closure-DEVS-1.0.0.jar`

## Package map

| Package | Purpose |
|---|---|
| `com.ms4systems.devs.core.model.impl` | `Atomic`, `Coupled`, `DEVS` base classes |
| `com.ms4systems.devs.core.message.impl` | `Port`, `Message`, `MessageBag` |
| `com.ms4systems.devs.core.simulation.impl` | `AtomicSimulator`, `CoupledSimulator` |
| `com.ms4systems.devs.extensions` | `PhaseBased`, `StateVariableBased` marker interfaces |
| `com.ms4systems.devs.markovmodel` | `ContinuousTimeMarkov`, `DiscreteTimeMarkov`, `MarkovMat` |
| `com.ms4systems.devs.markov` | `Matrix`, `TimeInState`, `TransitionInfo`, `TimeArray`, `TimeInfo` |

## Atomic — signatures to override

```java
public class Atomic extends DEVS {
    public Atomic();
    public Atomic(String nm);

    public void initialize();                                      // override
    public void internalTransition();                              // override
    public void externalTransition(double timeElapsed, MessageBag input);  // override
    public void confluentTransition(MessageBag input);             // override (default: int + ext(0, input))
    public Double getTimeAdvance();                                // override
    public MessageBag getOutput();                                 // override

    public MessageBag inputBag;
    public double elapsedTime;
    public Coupled getParent();
    public void setParent(Coupled parent);
}
```

**Important:** `phase`, `sigma`, `holdIn()`, `phaseIs()`, `passivate()`, `getPhase()` are **NOT** provided by the base class (commented out in source). Deterministic subclasses must declare them — see `patterns/phase-sigma-idiom.md`.

## Port / MessageBag

```java
// On Atomic (inherited from DEVS):
public final Port addInputPort(String name);
public final Port addInputPort(String name, Class<?> type);
public final Port addOutputPort(String name);
public final Port addOutputPort(String name, Class<?> type);

// MessageBag:
public ArrayList<Message> getMessages(Port p);   // empty list if none
public boolean hasMessages(Port p);
public void add(Port p, Object data);
public void add(Message m);

// Port:
public Message createMessage(Object data);
```

## ContinuousTimeMarkov — already provides phase/sigma helpers

```java
public class ContinuousTimeMarkov extends Atomic implements PhaseBased, StateVariableBased {
    // Inherited (concretely defined here, unlike in Atomic):
    public void holdIn(String phase, Double sigma);
    protected void passivate();
    protected void passivateIn(String phase);
    public boolean phaseIs(String phase);
    public String getPhase();

    // Markov-specific state:
    protected Random Rand;
    protected ArrayList<TimeInState> TimeInStateList;
    protected ArrayList<TransitionInfo> TransitionInfoList;
    protected MarkovMat mm;

    // Built-in output ports:
    public final Port outTimeInState     = addOutputPort("outTimeInState", String.class);
    public final Port outTimeInStateList = addOutputPort("outTimeInStateList", ArrayList.class);

    // Rate matrix population:
    public void addTransitionInfo(String state, String[] successors, double[] rates);
    public void setInitialStateAndTransitionMatrix(MarkovMat mm);

    // Sampling:
    public double timeToNextEvent(double lambda);   // -ln(U)/lambda
    public void internalTransitionFor(String state);

    // Lookup:
    public ArrayList<String> getSuccs(String state);
    public TransitionInfo getTransitionInfoFor(String state, String succ);
    public HashSet<String> getStates();
}
```

## Compiling a model

```bash
javac -cp ~/.m2/repository/com/ms4systems/Closure-DEVS/1.0.0/Closure-DEVS-1.0.0.jar MyModel.java
```

If the model is in a package (e.g. `package models.myproj;`), compile from the directory that contains the `models/` folder and supply the package-relative path:

```bash
javac -cp Closure-DEVS-1.0.0.jar models/myproj/MyModel.java
```

## Running with AtomicSimulator (optional, not invoked by this skill)

```java
AtomicSimulator sim = new AtomicSimulator(model);
sim.initialize(0.0);
for (int i = 0; i < 10; i++) {
    sim.computeOutput();
    sim.transition();
}
```
