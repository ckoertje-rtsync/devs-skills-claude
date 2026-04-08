package Models.java;

import java.io.*;

import com.ms4systems.devs.core.model.AtomicModel;
import com.ms4systems.devs.core.model.CoupledModel;
import com.ms4systems.devs.core.model.impl.CoupledModelImpl;
import com.ms4systems.devs.core.simulation.Simulation;
import com.ms4systems.devs.core.simulation.impl.SimulationImpl;
import com.ms4systems.devs.analytics.InternalUseSeS;
import com.ms4systems.devs.core.util.*;


public class TestSimpleWorkFlow  extends SimulateModel {

	//public static String folder = "C:\\Users\\carbo\\OneDrive\\Documents\\Basic-DEVS-Java-DEVS-Markov-SeSTest\\";
	public static String folder = System.getProperty("user.dir") + File.separator;
	public static String folderpes = folder + "src\\Models\\pes"+ File.separator;
	public static String folderses = folder + "src\\Models\\ses"+ File.separator;	

	public TestSimpleWorkFlow() {
		super();
	}

	
	
	public static void main(String[] args) {

		TestSimpleWorkFlow sm = new TestSimpleWorkFlow();
		String modelName ="SimpleWorkFlow";
		String sesfile = modelName+".ses";

		

		sesRelationExtend ses = InternalUseSeS.getSesFromFile(sesfile);
		System.out.println(ses.getRootEntityName());

 		String sesContents = fileHandler.getContentsAsString(folderses + sesfile);
	
 		
		String pesfile = modelName+".pes";
 		String pesContents = fileHandler.getContentsAsString(folderpes + pesfile);

		sesRelationExtend rses = InternalUseSeS.getSesFromContents(sesContents, pesContents);

		CoupledModelImpl model = (CoupledModelImpl) InternalUseSeS
			.pruneNTransToGetModelInstanceWContents(rses, pesContents);
	
		for (AtomicModel am : model.getChildren()) {
			System.out.println("\n"+am.getName());
			 if (am instanceof CoupledModel) {
			  for (AtomicModel amm : ((CoupledModel)am).getChildren()) {
				 System.out.println("\n"+amm.getName());
			  }
			}
		}
		Simulation sim = new SimulationImpl("SimpleWorkFlow Simulation", model);
	    sim.startSimulation(0);
		sim.simulateIterations(Long.MAX_VALUE);
		}

}
