package Models.java;
import com.ms4systems.devs.core.model.impl.CoupledModelImpl;
import com.ms4systems.devs.core.simulation.Simulation;
import com.ms4systems.devs.core.util.sesRelation;
import com.ms4systems.devs.core.util.sesRelationExtend;
import com.ms4systems.devs.extensions.StateVariableBased;
import com.ms4systems.devs.core.model.AtomicModel;
import com.ms4systems.devs.core.model.CoupledModel;

import java.util.ArrayList;


public class SimpleWorkFlow extends CoupledModelImpl implements StateVariableBased{ 
	private static final long serialVersionUID = 1L;
	
	public SimpleWorkFlow(){
		this("SimpleWorkFlow");
	}
	public SimpleWorkFlow(String nm) {
		super(nm);
		make();
	}
	public void make(){

		GeneratorOfJobs GeneratorOfJobs = new GeneratorOfJobs();
		addChildModel(GeneratorOfJobs);
		Transducer Transducer = new Transducer();
		addChildModel(Transducer);
		ProcessorOfJobs ProcessorOfJobs = new ProcessorOfJobs();
		addChildModel(ProcessorOfJobs);
		addCoupling(GeneratorOfJobs.outJob,Transducer.inAriv);
		addCoupling(ProcessorOfJobs.outJob,Transducer.inSolved);
		addCoupling(GeneratorOfJobs.outJob,ProcessorOfJobs.inJob);
		addCoupling(Transducer.outStop,GeneratorOfJobs.inStop);

	}
    @Override
    public String[] getStateVariableNames() {
        ArrayList<String> lst = new ArrayList<String>();
        for (AtomicModel child : getChildren())
            if (child instanceof StateVariableBased)
                for (String childVar : ((StateVariableBased) child)
                        .getStateVariableNames())
                    lst.add(child.getName() + "." + childVar);
        return lst.toArray(new String[0]);
    }

    @Override
    public Object[] getStateVariableValues() {
        ArrayList<Object> lst = new ArrayList<Object>();
        for (AtomicModel child : getChildren())
            if (child instanceof StateVariableBased)
                for (Object childVar : ((StateVariableBased) child)
                        .getStateVariableValues())
                    lst.add(childVar);
        return lst.toArray();
    }

    @Override
    public Class<?>[] getStateVariableTypes() {
        ArrayList<Class<?>> lst = new ArrayList<Class<?>>();
        for (AtomicModel child : getChildren())
            if (child instanceof StateVariableBased)
                for (Class<?> childVar : ((StateVariableBased) child)
                        .getStateVariableTypes())
                    lst.add(childVar);
        return lst.toArray(new Class<?>[0]);
    }

    @Override
    public void setStateVariableValue(int index, Object value) {
        int i = 0;
        for (AtomicModel child : getChildren())
            if (child instanceof StateVariableBased)
                for (int childIndex = 0; childIndex < ((StateVariableBased) child)
                        .getStateVariableNames().length; childIndex++) {
                    if (i == index) {
                        ((StateVariableBased) child).setStateVariableValue(
                                childIndex, value);
                        return;
                    }
                    i++;
                }
    }
    
	public static void main(String[] args) throws ClassNotFoundException{
		SimpleWorkFlow model = new SimpleWorkFlow();
		Simulation sim = new com.ms4systems.devs.core.simulation.impl.SimulationImpl("SendCount Simulation", model);
			sim.startSimulation(0);
			sim.simulateIterations(Long.MAX_VALUE);

        sesRelation ses = myDEVSToSES.mapDEVSModelToPlainSES(model);
        System.out.println(ses.printTreeString());
        sesRelationExtend rses = new sesRelationExtend(ses);
        CoupledModel cm = myDEVSToSES.pruneNTransformForInstance(rses);
        System.out.println(myDEVSToSES.mapCoupledToPlantUML(cm));

	}
}