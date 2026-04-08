package Models.java;

import com.ms4systems.devs.core.model.impl.CoupledModelImpl;
import com.ms4systems.devs.helpers.impl.SimulationOptionsImpl;
import com.ms4systems.devs.simviewer.standalone.SimViewer;
import com.ms4systems.devs.core.simulation.impl.SimulationImpl;
import com.ms4systems.devs.core.util.sesRelation;
import com.ms4systems.devs.core.util.sesRelationExtend;

public class ComputerSystemWController extends CoupledModelImpl {
    private static final long serialVersionUID = 1L;
    protected SimulationOptionsImpl options = new SimulationOptionsImpl();

    public ComputerSystemWController() {
        this("ComputerSystemWController");
    }

    public ComputerSystemWController(String name) {
        super(name);

        ComputerAtomic computer = new ComputerAtomic("computer");
        DiskControllerAtomic disk = new DiskControllerAtomic("disk");

        addChildModel(computer);
        addChildModel(disk);

        // Couplings: Computer → Disk
        addCoupling(computer.getOutputPort("outMemReq"), disk.getInputPort("inMemReq"));
        addCoupling(computer.getOutputPort("outPortReq"), disk.getInputPort("inPortReq"));
        addCoupling(computer.getOutputPort("outTimerReq"), disk.getInputPort("inTimerReq"));

        // Couplings: Disk → Computer
        addCoupling(disk.getOutputPort("outMemResp"), computer.getInputPort("inMemResp"));
        addCoupling(disk.getOutputPort("outPortResp"), computer.getInputPort("inPortResp"));
        addCoupling(disk.getOutputPort("outTimerResp"), computer.getInputPort("inTimerResp"));
        addCoupling(disk.getOutputPort("irq"), computer.getInputPort("inIrq"));
    }

    public static void main(String[] args) throws ClassNotFoundException {
        // Build coupled model
        ComputerSystemWController top = new ComputerSystemWController();
        top.initialize();

        // SES mapping
        sesRelation ses = myDEVSToSES.mapDEVSModelToPlainSES(top);
        System.out.println("=== SES Tree ===");
        System.out.println(ses.printTreeString());

        sesRelationExtend rses = new sesRelationExtend(ses);
        CoupledModelImpl cm = myDEVSToSES.pruneNTransformForInstance(rses);
        System.out.println("=== PlantUML Coupled Model ===");
        System.out.println(myDEVSToSES.mapCoupledToPlantUML(cm));

        // Simulation options
        SimulationOptionsImpl options = new SimulationOptionsImpl(args, true);
        top.options = options;

        if (options.isDisableViewer()) {
            SimulationImpl sim = new SimulationImpl("Coupled Model Simulation", top, options);
            sim.startSimulation(0);
            sim.simulateIterations(20); // run a finite number of iterations for demo
            System.out.println("Simulation complete.");
        } else {
            SimViewer viewer = new SimViewer();
            viewer.open(top, options);
        }
    }
}