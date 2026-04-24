import com.ms4systems.devs.core.model.impl.Coupled;
import com.ms4systems.devs.core.simulation.impl.CoupledCoordinator;

/**
 * Efp — top-level Closure-DEVS coupled.
 *
 * Embeds an `Ef` (itself a Coupled) alongside a `TinySink` atomic. Wires Ef's
 * output into the sink. No external ports — this is a closed system with a
 * main() that runs the simulation.
 *
 * Demonstrates:
 *   - hierarchical composition via addCoupledChildModel(Coupled) for a Coupled child
 *   - addChildModel(DEVS) for an atomic child
 *   - port-style addCoupling(Port, Port) for an IC (coupled.out -> atomic.in)
 *   - CoupledCoordinator-based main (no SimulationImpl / SimViewer)
 */
public class Efp extends Coupled {
    private static final long serialVersionUID = 1L;

    public Efp() {
        this("Efp");
    }

    public Efp(String name) {
        super(name);

        Ef       ef   = new Ef("ef");
        TinySink sink = new TinySink("sink");

        addCoupledChildModel(ef);     // dedicated Coupled variant — reads clearly
        addChildModel(sink);          // atomic child

        // IC: Ef's external output -> the sink's input
        addCoupling(ef.out, sink.in);
    }

    public static void main(String[] args) {
        Efp top = new Efp();
        CoupledCoordinator coord = new CoupledCoordinator(top);
        coord.initialize(0.0);
        coord.simulate(5);
        System.out.println("Simulation complete at t=" + coord.getNextEventTime());
    }
}
