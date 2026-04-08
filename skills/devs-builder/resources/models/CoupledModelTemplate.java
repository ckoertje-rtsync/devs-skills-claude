//Package Declarations; DO NOT MODIFY
package Models.java;

import com.ms4systems.devs.core.model.impl.CoupledModelImpl;
import com.ms4systems.devs.helpers.impl.SimulationOptionsImpl;
import com.ms4systems.devs.simviewer.standalone.SimViewer;
import com.ms4systems.devs.core.simulation.impl.SimulationImpl;
import com.ms4systems.devs.core.util.sesRelation;
import com.ms4systems.devs.core.util.sesRelationExtend;

public class CoupledModelTemplate extends CoupledModelImpl {
    private static final long serialVersionUID = 1L;
    protected SimulationOptionsImpl options = new SimulationOptionsImpl();

    public CoupledModelTemplate() {
        this("CoupledModelName");
    }

    //Coupled Model Constructor; MODIFY ATOMIC MODELS AND COUPLINGS ONLY
    public CoupledModelTemplate(String name) {
        super(name);

        AtomicModelTemplate AtomicModelOne = new AtomicModelTemplate("AtomicModelOneName");
        AtomicModelTemplate AtomicModelTwo = new AtomicModelTemplate("AtomicModelTwoName");

        addChildModel(AtomicModelOne);
        addChildModel(AtomicModelTwo);

        // Couplings: AtomicModelOne → AtomicModelTwo; List all port couplings generated and being sent from Atomic Model One to Atomic Model Two
        addCoupling(AtomicModelOne.getOutputPort("outPortnameOne"), AtomicModelTwo.getInputPort("inPortnameOne"));


        // Couplings: AtomicModelTwo → AtomicModelOne; List all port couplings generated and being sent from Atomic Model two to Atomic Model One
        addCoupling(AtomicModelTwo.getOutputPort("outPortnameTwo"), AtomicModelOne.getInputPort("inPortnameTwo"));
    }

    
    //main method for the coupled model; DO NOT MODIFY 
    public static void main(String[] args) throws ClassNotFoundException {
        // Build coupled model
        CoupledModelTemplate top = new CoupledModelTemplate();
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
            SimulationImpl sim = new SimulationImpl("CoupledModelName Simulation", top, options);
            sim.startSimulation(0);
            sim.simulateIterations(20); // run a finite number of iterations for demo
            System.out.println("Simulation complete.");
        } else {
            SimViewer viewer = new SimViewer();
            viewer.open(top, options);
        }
    }
}