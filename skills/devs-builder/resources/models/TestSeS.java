package Models.java;

import java.io.*;
import java.util.*;

import com.ms4systems.devs.core.model.AtomicModel;
import com.ms4systems.devs.core.model.CoupledModel;
import com.ms4systems.devs.core.model.impl.CoupledModelImpl;
import com.ms4systems.devs.analytics.InternalUseSeS;
import com.ms4systems.devs.core.util.*;


public class TestSeS  extends SimulateModel {
	static sesRelationExtend rses;
    static String pesContents;

	public static String folder = System.getProperty("user.dir") + File.separator;
	public static String folderpes = folder + "src\\Models\\pes"+ File.separator;
	public static String folderses = folder + "src\\Models\\ses"+ File.separator;	

	public TestSeS() {
		super();
	}

	
	
	public static void main(String[] args)  {

		TestSeS sm = new TestSeS();
		String modelName ="BC";
		String sesfile = modelName+".ses";
		String pesfile = modelName+".pes";
		

		sesRelationExtend ses = InternalUseSeS.getSesFromFile(sesfile);

		String sesContents = fileHandler.getContentsAsString(folderses + sesfile);
		String basepesContents = fileHandler.getContentsAsString(folderpes + pesfile);


 		HashSet<String> prunings = GenerateAllPrunings.generateAllPrunings(ses);
 		
	    Iterator it = prunings.iterator();
	    while (it.hasNext()) {
		pesContents = basepesContents+it.next();
		rses = InternalUseSeS.getSesFromContents(sesContents, pesContents);

		CoupledModelImpl ExampleSES = (CoupledModelImpl) InternalUseSeS
				.pruneNTransToGetModelInstanceWContents(rses, pesContents);
		for (AtomicModel am : ExampleSES.getChildren()) {
			System.out.println("\n"+am.getName());
			 if (am instanceof CoupledModel) {
			  for (AtomicModel amm : ((CoupledModel)am).getChildren()) {
				 System.out.println("\n"+amm.getName());
			  }
			}
		}

		}

 
	}
}
