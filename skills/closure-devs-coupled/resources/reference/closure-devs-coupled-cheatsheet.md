# Closure-DEVS coupled API cheatsheet

Maven coordinates: `com.ms4systems:Closure-DEVS:1.0.0`
Default JAR path: `~/.m2/repository/com/ms4systems/Closure-DEVS/1.0.0/Closure-DEVS-1.0.0.jar`

Every signature below was verified against the shipped jar via `javap -p`. If you see examples elsewhere that import `com.ms4systems.devs.core.model.impl.CoupledModelImpl` or use `Port<T>` generics, those are from a separate MS4ME jar, **not** from Closure-DEVS — don't import them into Closure-DEVS code.

## Package map (coupled-relevant subset)

| Package | Purpose |
|---|---|
| `com.ms4systems.devs.core.model.impl` | `DEVS` (common ancestor), `Atomic`, `Coupled`, `ClosureToAtomic` (wraps a coupled as an atomic) |
| `com.ms4systems.devs.core.message.impl` | `Port` (raw, non-generic), `Coupling`, `Message`, `MessageBag`, `RoutingTableImpl` |
| `com.ms4systems.devs.core.simulation.impl` | `AtomicSimulator`, `CoupledCoordinator` (what the main harness uses) |

## `Coupled` — the base class

```java
public class Coupled extends DEVS {
    public Coupled();
    public Coupled(String name);

    // Children
    public void addChildModel(DEVS model);            // Atomic or Coupled both legal
    public void addCoupledChildModel(Coupled model);  // dedicated coupled variant (readable)
    public void removeChildModel(Atomic model);       // note: removal takes Atomic
    public ArrayList<Atomic> getChildren();

    // Couplings — generic form (port style)
    public void addCoupling(Port fromPort, Port toPort);
    public void removeCoupling(Port fromPort, Port toPort);
    public void removeCoupling(Coupling coupling);
    public ArrayList<Coupling> getCouplings();
    public List<Coupling> getCouplingsFor(Atomic child);

    // Couplings — string-based dedicated forms (string style)
    public void addCoupling(String fromChild, String fromPort, String toChild, String toPort);
    public void addInternalCoupling(String fromChild, String fromPort, String toChild, String toPort);
    public void addExternalInputCoupling(String fromOuterPort, String toChild, String toPort);
    public void addExternalOutputCoupling(String fromChild, String fromPort, String toOuterPort);

    // Lifecycle
    public void initialize();    // inherited from DEVS, no override needed

    // Child lookup (rarely needed — prefer local variable references)
    public Atomic getComponentWithName(String nm);
    public Atomic getComponentWithPartName(String nm);
    public ArrayList<Atomic> getComponentsWithPartName(String nm);

    // Utility
    public String writeCoupling();
    public String writePorts();
    public int getDepth();
}
```

Port declaration comes from `DEVS` (the ancestor of both `Atomic` and `Coupled`):

```java
public Port addInputPort(String name);
public Port addInputPort(String name, Class type);
public Port addInputPort(Port port);
public Port addOutputPort(String name);
public Port addOutputPort(String name, Class type);
public Port addOutputPort(Port port);
public Port getInputPort(String name);
public Port getOutputPort(String name);
```

**Important:** `Coupled` does not override `initialize` / `internalTransition` / `externalTransition` / `getOutput` / `getTimeAdvance` — the coordinator drives children directly. Do not try to implement those methods on a coupled; they're handled for you.

## Typical constructor shape

```java
public MyCoupled(String name) {
    super(name);

    ChildA a = new ChildA("a");
    ChildB b = new ChildB("b", 1.5);        // whatever constructor the child exposes

    addChildModel(a);
    addChildModel(b);

    // Port style
    addCoupling(this.in,  a.in);            // EIC
    addCoupling(a.out,    b.in);            // IC
    addCoupling(b.out,    this.out);        // EOC

    // — or —
    // String style (equivalent wiring)
    addExternalInputCoupling("in",  "a", "in");
    addInternalCoupling("a", "out", "b", "in");
    addExternalOutputCoupling("b", "out", "out");
}
```

## Running a coupled — stock harness

```java
import com.ms4systems.devs.core.simulation.impl.CoupledCoordinator;

public static void main(String[] args) {
    MyCoupled top = new MyCoupled();
    CoupledCoordinator coord = new CoupledCoordinator(top);
    coord.initialize(0.0);
    coord.simulate(100);                    // run N iterations
    System.out.println("Simulation complete at t=" + coord.getNextEventTime());
}
```

`CoupledCoordinator` methods worth knowing:

```java
public CoupledCoordinator(Coupled model);
public void initialize(double t0);
public void simulate();                     // one event
public void simulate(int iterations);
public void simulateDelta(double dt);
public void simulateDelta(double dt, int iterations);
public double getNextEventTime();
public MessageBag callOutputAndSimulate();
public ArrayList<AtomicSimulator> getChildren();
public ArrayList<Coupling> getCouplings();
```

There is no `SimulationImpl`, `SimViewer`, or `SimulationOptionsImpl` in the Closure-DEVS jar. If a user needs those (e.g. for MS4ME integration), they live in separate jars — add to the classpath separately.

## Port lookup — field vs string

```java
// Field access (requires child to expose `public final Port foo` fields):
addCoupling(genr.out, transd.ariv);

// String lookup via the dedicated coupling methods:
addInternalCoupling("genr", "out", "transd", "ariv");

// Port lookup via getInputPort / getOutputPort (equivalent to field access when you
// have only a child reference, not a typed handle):
addCoupling(genr.getOutputPort("out"), transd.getInputPort("ariv"));
```

String forms throw at construction time if the name is misspelled or missing.

## Compiling a coupled

```bash
javac -cp ~/.m2/repository/com/ms4systems/Closure-DEVS/1.0.0/Closure-DEVS-1.0.0.jar \
      -d build \
      MyCoupled.java ChildA.java ChildB.java
```

If the children are already in a jar:

```bash
javac -cp ~/.m2/repository/com/ms4systems/Closure-DEVS/1.0.0/Closure-DEVS-1.0.0.jar:/path/to/children.jar \
      MyCoupled.java
```

If the coupled and children share a package, compile from the directory containing the package root and supply package-relative paths.

## See also

- `../patterns/coupling-kinds.md` — port-style vs string-style decision and direction cheatsheets
- `../patterns/child-port-discovery.md` — pulling ports out of a child `.java`
- `../patterns/port-compatibility.md` — payload-type rules
- `../patterns/hierarchical-composition.md` — coupled-inside-coupled via `addCoupledChildModel`
