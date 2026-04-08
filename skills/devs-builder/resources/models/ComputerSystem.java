package Models.java;

import com.ms4systems.devs.core.model.impl.CoupledModelImpl;
import com.ms4systems.devs.helpers.impl.SimulationOptionsImpl;
import com.ms4systems.devs.simviewer.standalone.SimViewer;
import com.ms4systems.devs.core.simulation.impl.SimulationImpl;
import com.ms4systems.devs.core.simulation.impl.SimulatorImpl;
import com.ms4systems.devs.core.util.sesRelation;
import com.ms4systems.devs.core.util.sesRelationExtend;

/**
 * Coupled model that integrates ComputerAtomic and DeviceAtomic.
 * - ComputerAtomic issues memory/port/timer requests.
 * - DeviceAtomic responds with memory/port/timer responses and IRQs.
 */
public class ComputerSystem extends CoupledModelImpl {

    private static final long serialVersionUID = 1L;
    protected SimulationOptionsImpl options = new SimulationOptionsImpl();

    public ComputerSystem() {
        this("ComputerSystem");
    }

    public ComputerSystem(String name) {
        super(name);

        // Children
        ComputerAtomic computer = new ComputerAtomic("computer");
        DeviceAtomic device = new DeviceAtomic("device");

        // Register children
        addChildModel(computer);
        addChildModel(device);

        // Requests: Computer -> Device
        addCoupling(computer.getOutputPort("outMemResp"),   device.getInputPort("inMemReq"));
        addCoupling(computer.getOutputPort("outPortReq"),  device.getInputPort("inPortReq"));
        addCoupling(computer.getOutputPort("outTimerReq"), device.getInputPort("inTimerReq"));

        // Responses: Device -> Computer
        addCoupling(device.getOutputPort("outMemResp"),   computer.getInputPort("inMemResp"));
        addCoupling(device.getOutputPort("outPortResp"),  computer.getInputPort("inPortResp"));
        addCoupling(device.getOutputPort("outTimerResp"), computer.getInputPort("inTimerResp"));

        // Interrupt: Device -> Computer
        addCoupling(device.getOutputPort("irq"), computer.getInputPort("inIrq"));
    }




    // --- Main driver ---
    public static void main(String[] args) throws ClassNotFoundException {
        ComputerSystem top = new ComputerSystem();
        top.initialize();

        // SES mapping
        sesRelation ses = myDEVSToSES.mapDEVSModelToPlainSES(top);
        System.out.println(ses.printTreeString());

        sesRelationExtend rses = new sesRelationExtend(ses);
        CoupledModelImpl cm = (CoupledModelImpl) myDEVSToSES.pruneNTransformForInstance(rses);
        System.out.println(myDEVSToSES.mapCoupledToPlantUML(cm));
        // Simulation options
        SimulationOptionsImpl options = new SimulationOptionsImpl(args, true);
        top.options = options;

        if (options.isDisableViewer()) {
            SimulationImpl sim = new SimulationImpl("ComputerDeviceSystem Simulation", top, options);
            sim.startSimulation(0);
            sim.simulateIterations(Long.MAX_VALUE);
        } else {
            SimViewer viewer = new SimViewer();
            viewer.open(top, options);
        }
    }
}