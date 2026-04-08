package Models.java;

import java.util.concurrent.atomic.AtomicInteger;

import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.message.Port;
import com.ms4systems.devs.core.message.impl.MessageBagImpl;
import com.ms4systems.devs.core.model.CoupledModel;
import com.ms4systems.devs.core.model.impl.CoupledModelImpl;
import com.ms4systems.devs.core.simulation.Simulation;
import com.ms4systems.devs.core.util.sesRelation;
import com.ms4systems.devs.core.util.sesRelationExtend;


/**
 * Bob–Alice ping-pong demo using the Basic-DEVS-Java-Markov-SES framework.
 *
 * This file defines:
 *  - PersonAtomic: an Atomic DEVS model that extends PhaseAtomic and can send/receive String messages.
 *  - BobAliceModel: a Coupled DEVS model wiring two PersonAtomic instances (Bob and Alice) together.
 *
 * Run the main() method to execute a short simulation demonstrating messages
 * bouncing back and forth between Bob and Alice.
 */
class PersonAtomic extends PhaseAtomic {
    // Ports
    private Port<String> in;
    private Port<String> out;

    // Behavior parameters
    private final String who;
    private final boolean initiator;     // true if this actor starts the conversation
    private final int maxExchanges;      // max number of messages this actor will send
    private final AtomicInteger sent = new AtomicInteger(0);

    public PersonAtomic(String name, boolean initiator, int maxExchanges) {
        super(name);
        this.who = name;
        this.initiator = initiator;
        this.maxExchanges = Math.max(0, maxExchanges);

        // Declare ports (String is Serializable and supported by the framework)
        this.in = addInputPort("in", String.class);
        this.out = addOutputPort("out", String.class);
    }

    // Expose ports for coupling if needed by name via getInputPort/getOutputPort on the base class

    @Override
    public void initialize() {
        super.initialize();
        // If initiator, schedule first send; otherwise idle until an external message arrives
        if (initiator && maxExchanges > 0) {
            holdIn("send", 1.0); // send after 1 time unit
        } else {
            passivate();
        }
    }

    @Override
    public Double getTimeAdvance() {
        // Drive internal scheduling via sigma maintained by PhaseAtomic
        return sigma;
    }

    @Override
    public void externalTransition(double timeElapsed, MessageBag input) {
        // Any received message triggers a response if we still have sends left
        // We don’t need to inspect contents for this simple demo
        if (sent.get() < maxExchanges) {
            holdIn("send", 1.0); // respond after a small delay
        } else {
            passivate();
        }
    }

    @Override
    public void internalTransition() {
        // After sending (output is produced in getOutput just prior to this),
        // either wait for the next incoming message or stop if limit reached.
        if (phaseIs("send")) {
            // Count this send; transition to passive and wait for the peer
            sent.incrementAndGet();
            passivate();
        } else {
            // Default: remain passive
            passivate();
        }
    }

    @Override
    public MessageBag getOutput() {
        // Produce output only when in "send" phase
        if (phaseIs("send")) {
            final String msg = "Hello from " + who + " #" + (sent.get() + 1);
            System.out.println("[" + who + " -> out] " + msg);

            MessageBagImpl bag = new MessageBagImpl();
            bag.add(out, msg);
            return bag;
        }
        return MessageBag.EMPTY;
    }
}

/**
 * Coupled model wiring Bob and Alice together.
 * Bob starts the conversation, Alice responds; they exchange messages up to a small limit.
 */
public class BobAliceModel extends CoupledModelImpl {

    public BobAliceModel() {
        this("BobAliceModel");
    }

    public BobAliceModel(String name) {
        super(name);

        // Create two atomic components
        // Bob will initiate; Alice will wait and then respond
        PersonAtomic bob = new PersonAtomic("Bob", true, 5);
        PersonAtomic alice = new PersonAtomic("Alice", false, 5);

        // Register children with the coordinator
        addChildModel(bob);
        addChildModel(alice);

        // Wire outputs to inputs (ping-pong)
        addInternalCoupling("Bob", "out", "Alice", "in");
        addInternalCoupling("Alice", "out", "Bob", "in");
    }

    public static void main(String[] args) throws ClassNotFoundException {
        // Build the coupled model and run a short simulation
        BobAliceModel model = new BobAliceModel();

        Simulation sim = new com.ms4systems.devs.core.simulation.impl.SimulationImpl("SendCount Simulation", model);
		sim.startSimulation(0);
		sim.simulateIterations(Long.MAX_VALUE);
        System.out.println(sim.getAllContents());

        // Initialize and simulate for a modest horizon
        // model.getCoordinator().initialize(0);
        // Simulate for 50 time units which is more than enough for 5 exchanges with 1.0 delays
        //model.getCoordinator().simulate(50.0);

        System.out.println("Simulation complete.");


        sesRelation ses = myDEVSToSES.mapDEVSModelToPlainSES(model);
        System.out.println(ses.printTreeString());
        sesRelationExtend rses = new sesRelationExtend(ses);
        CoupledModel cm = myDEVSToSES.pruneNTransformForInstance(rses);
        System.out.println(myDEVSToSES.mapCoupledToPlantUML(cm));

    }
}