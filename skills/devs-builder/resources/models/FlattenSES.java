package Models.java;

import java.lang.reflect.*;

import java.io.*;
import java.util.*;

import com.ms4systems.devs.analytics.InternalUseSeS;
import com.ms4systems.devs.core.model.AtomicModel;
import com.ms4systems.devs.core.model.CoupledModel;
import com.ms4systems.devs.core.simulation.Simulation;
import com.ms4systems.devs.core.simulation.impl.SimulationImpl;
//import com.ms4systems.devs.core.model.impl.Coupled;
//import com.ms4systems.devs.core.model.impl.DEVS;
//import com.ms4systems.devs.core.simulation.impl.CoupledCoordinator;
import com.ms4systems.devs.core.util.*;

public class FlattenSES {
	static sesRelationExtend rses; // assumed public access to basic variables
	static String pesContents;

	String folder = System.getProperty("user.dir") + File.separator;
	String foldertxt = folder + "src\\Models\\txt" + File.separator;
	String folderpes = folder + "src\\Models\\pes" + File.separator;
	String folderses = folder + "src\\Models\\ses" + File.separator;

	public FlattenSES() {
		super();
	}

	public static HashSet getChildrenOfParent(sesRelationExtend rses, String entity) {
		Hashtable aspects = rses.entityHasAspect();
		Hashtable entities = rses.aspectHasEntity();
		HashSet aspectsOfRoot = (HashSet) aspects.get(entity);
		Object aspectOfRoot = aspectsOfRoot.iterator().next();
		return (HashSet) entities.get(aspectOfRoot);
	}

	public static HashSet getCouplingsOfParent(sesRelationExtend rses, String entity) {
		Hashtable aspects = rses.entityHasAspect();
		Hashtable entities = rses.aspectHasEntity();
		Hashtable couplings = rses.aspectHasCoupling();
		HashSet aspectsOfRoot = (HashSet) aspects.get(entity);
		Object aspectOfRoot = aspectsOfRoot.iterator().next();
		return (HashSet) couplings.get(aspectOfRoot);
	}

	public static void replaceEntityByChildren(sesRelationExtend rses, String entity) {
		HashSet childrenOfEF = getChildrenOfParent(rses, entity);
		for (Object o : childrenOfEF) {
			rses.entityNames.add(o);
		}
		rses.entityNames.remove(entity);
	}
	public static HashSet removeCouplingsOfChild(sesRelationExtend rses, String parent,String entity) {
		HashSet couplingsOfEFP = getCouplingsOfParent(rses, parent);
      	HashSet newCouplings = new HashSet();
		for (Object o : couplingsOfEFP) {
			Hashtable ht = (Hashtable) o;
		    	if (!ht.get("destination").equals(entity)&&
		    			!ht.get("source").equals(entity)) 
		    		newCouplings.add(ht);
		}
		return newCouplings;
	}
	public static Object getNonAtomic(sesRelationExtend rses, String parent) {
		HashSet childrenOfEFP = getChildrenOfParent(rses, parent);
		for (Object o : childrenOfEFP) {
			Hashtable aspects = rses.entityHasAspect();
			Hashtable entities = rses.aspectHasEntity();
			Hashtable couplings = rses.aspectHasCoupling();
			HashSet aspectsOfRoot = (HashSet) aspects.get(o);
 			if (aspectsOfRoot != null ) return o;
 		}
		return null;
	}
	public static sesRelationExtend flattenOutChildren(sesRelationExtend ses, String parent) {
		while(true) {
			Object o = getNonAtomic(rses,parent);
			if (o == null)break;		
			rses = flattenOutChild(rses,parent,o.toString());
		}
		return rses;
	}
	public static sesRelationExtend flattenTop(sesRelationExtend rses){
		return flattenOutChildren(rses,rses.getRootEntityName());
	}
						
	public static sesRelationExtend flattenOutChild(sesRelationExtend rses, String parent,
			String entity) {
		replaceEntityByChildren(rses, entity);
		HashSet newcouplingsOfEFP = removeCouplingsOfChild(rses,parent,entity);
		newcouplingsOfEFP.addAll(getMatchingExtInput(rses, parent, entity));
		newcouplingsOfEFP.addAll(getMatchingExtOutput(rses, parent, entity));
		HashSet couplingsOfEFP = getCouplingsOfParent(rses, parent);
		HashSet childrenOfEF = getChildrenOfParent(rses, entity);

		HashSet newcouplingsOfEF = new HashSet();
		HashSet couplingsOfEF = getCouplingsOfParent(rses, entity);
		for (Object o : couplingsOfEF) {
			Hashtable ht = (Hashtable) o;
			if (ht.get("source").equals(entity) || ht.get("destination").equals(entity))
				continue;
			newcouplingsOfEF.add(o);
		}
		Hashtable aspects = rses.entityHasAspect();
		Hashtable entities = rses.aspectHasEntity();
		Hashtable couplings = rses.aspectHasCoupling();
		HashSet aspectsOfChild = (HashSet) aspects.get(entity);
		Object aspectOfChild = aspectsOfChild.iterator().next();
		HashSet aspectsOfParent = (HashSet) aspects.get(parent);
		Object aspectOfParent = aspectsOfParent.iterator().next();
		HashSet ents = (HashSet) entities.get(aspectOfParent);
		ents.remove(entity);
		for (Object o : childrenOfEF) {
			ents.add(o);
		}
		entities.put(aspectOfParent, ents);
		couplings.remove(aspectOfChild);
		couplings.remove(aspectOfParent);
		newcouplingsOfEFP.addAll(newcouplingsOfEF);
		couplings.put(aspectOfParent, newcouplingsOfEFP);
		aspects.remove(entity);
		entities.remove(aspectOfChild);
		rses.aspectNames.remove(aspectOfChild);
		return rses;
	}

	public static HashSet getMatchingExtInput(sesRelationExtend rses, String parent, String entity) {
		HashSet coups = new HashSet();
		HashSet couplingsOfEFP = getCouplingsOfParent(rses, parent);
		HashSet couplingsOfEF = getCouplingsOfParent(rses, entity);
		for (Object o : couplingsOfEFP) {
			Hashtable ht = (Hashtable) o;
			for (Object o1 : couplingsOfEF) {
				Hashtable ht1 = (Hashtable) o1;
				if (ht.get("destination").equals(ht1.get("source")) && ht.get("inport").equals(ht1.get("outport"))) {
					Hashtable ht3 = new Hashtable();
					ht3.put("source", ht.get("source"));
					ht3.put("destination", ht1.get("destination"));
					ht3.put("outport", ht.get("outport"));
					ht3.put("inport", ht1.get("inport"));
					coups.add(ht3);
				}
			}
		}
		return coups;
	}

	public static HashSet getMatchingExtOutput(sesRelationExtend rses, String parent, String entity) {
		HashSet coups = new HashSet();
		HashSet couplingsOfEFP = getCouplingsOfParent(rses, parent);
		HashSet couplingsOfEF = getCouplingsOfParent(rses, entity);
		for (Object o : couplingsOfEF) {
			Hashtable ht = (Hashtable) o;
			for (Object o1 : couplingsOfEFP) {
				Hashtable ht1 = (Hashtable) o1;
				if (ht.get("destination").equals(ht1.get("source")) && ht.get("inport").equals(ht1.get("outport"))) {
					Hashtable ht3 = new Hashtable();
					ht3.put("source", ht.get("source"));
					ht3.put("destination", ht1.get("destination"));
					ht3.put("outport", ht.get("outport"));
					ht3.put("inport", ht1.get("inport"));
					coups.add(ht3);
				}
			}
		}
		return coups;
	}

	public static CoupledModel pruneNTransformForInstance(sesRelationExtend ses) throws ClassNotFoundException {
		InternalUseSeS cm = new InternalUseSeS();
		contextPrune.createPruneDoc(ses);
		PESToDEVSOnTheFly.toDEVS(cm);
		PESToDEVSOnTheFly.removeSelfCoupling(cm);
		contextPrune.transferPairCoupling(cm);
		return cm;
	}

	public static void main(String[] args) throws ClassNotFoundException {

		FlattenSES sm = new FlattenSES();
		//String modelName = "EFP";
		String modelName = "BCDE";
		String sesfile = modelName + ".ses";
		String pesfile = modelName + ".pes";

		String sesContents = fileHandler.getContentsAsString(sm.folderses + sesfile);
		String pesContents = fileHandler.getContentsAsString(sm.folderpes + pesfile);

		rses = InternalUseSeS.getSesFromContents(sesContents, pesContents);
		rses = flattenTop(rses);
		System.out.println("\n===============================================new rses");
		rses.printTree();
		CoupledModel cm = pruneNTransformForInstance(rses);

		cm = cm;
;

		CoupledModel EFS = (CoupledModel) InternalUseSeS
				.pruneNTransToGetModelInstanceWContents(rses, pesContents);

EFS = EFS;
for (AtomicModel am : EFS.getChildren()) {
	System.out.println("\n"+am.getName());
	 if (am instanceof CoupledModel) {
	  for (AtomicModel amm : ((CoupledModel)am).getChildren()) {
		 System.out.println("\n"+amm.getName());
	  }
	}
}
Simulation sim = new SimulationImpl("SimpleWorkFlow Simulation", EFS);
sim.startSimulation(0);
sim.simulateIterations(Long.MAX_VALUE);

	}
}
