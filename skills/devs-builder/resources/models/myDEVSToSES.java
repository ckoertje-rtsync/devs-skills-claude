package Models.java;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import com.ms4systems.devs.analytics.InternalUseSeS;
import com.ms4systems.devs.core.message.Coupling;
import com.ms4systems.devs.core.message.impl.CouplingImpl;
//import com.ms4systems.AtomicModel.core.util.Pair;
//import com.ms4systems.AtomicModel.core.util.sesRelation;
import com.ms4systems.devs.core.model.AtomicModel;
import com.ms4systems.devs.core.model.CoupledModel;
import com.ms4systems.devs.core.model.impl.CoupledModelImpl;
import com.ms4systems.devs.core.simulation.Simulation;
import com.ms4systems.devs.core.util.PESToDEVSOnTheFly;
import com.ms4systems.devs.core.util.contextPrune;
import com.ms4systems.devs.core.util.sesRelation;
import com.ms4systems.devs.core.util.sesRelationExtend;
import com.ms4systems.devs.helpers.impl.SimulationOptionsImpl;
import com.ms4systems.devs.simviewer.standalone.SimViewer;



public class myDEVSToSES {
	public static String getClassNm(AtomicModel comp) {
	    String s = comp.getClass().getName();
	    int index = s.lastIndexOf(".");  //bpz
	    if (index > - 1) {
	        s = s.substring(index + 1, s.length());
	    }
	    return s;
	}
 public static sesRelation mapDEVSModelToPlainSES(AtomicModel m) {
     sesRelation ses = new sesRelation();
     String entNm = m.getName();
     ses.setRoot(entNm);
     if (!(m instanceof CoupledModel)) {
         return ses;
     }
     else {
     	return mapPlainCoupledBase(ses, (CoupledModel) m);
     }
 }

 public static AtomicModel getComponentWithName(CoupledModel m,String nm){
     for(AtomicModel mm:m.getChildren()) {
     if (mm.getName().equals(nm))return mm;
     }
     return null;
 }
public static sesRelation mapPlainCoupledBase(sesRelation ses, CoupledModel m) {
     String entNm = m.getName();
     ses.addAspectToEntity(entNm + "Dec", entNm);
     ArrayList<AtomicModel> comps = (ArrayList)m.getChildren();
     Iterator it = comps.iterator();
     while (it.hasNext()) {
         AtomicModel comp = (AtomicModel) it.next();
         String compinstNm = comp.getName();
         ses.addEntityToAspect(compinstNm, entNm + "Dec");
         if (comp instanceof CoupledModel) {
             mapPlainCoupledBase(ses, (CoupledModel) comp);
         }
     }
     ArrayList cp = (ArrayList)m.getCouplings();
     Iterator ic = cp.iterator();
     while (ic.hasNext()) {
         Hashtable<Object,Object> f = new Hashtable<Object,Object>();
         Object o =  ic.next();
         CouplingImpl couple = (CouplingImpl)o;
         f.put("source", couple.getSource().getName());
         f.put("outport", couple.getSourcePort().getName());
         f.put("destination",couple.getDestination().getName());
         f.put("inport", couple.getDestinationPort().getName());
         ses.addCouplingToAspect(f, entNm + "Dec");
     }
     return ses;
}

	public static CoupledModelImpl pruneNTransformForInstance(sesRelationExtend ses) throws ClassNotFoundException {
		InternalUseSeS cm = new InternalUseSeS();
		contextPrune.createPruneDoc(ses);
		PESToDEVSOnTheFly.toDEVS(cm);
		PESToDEVSOnTheFly.removeSelfCoupling(cm);
		contextPrune.transferPairCoupling(cm);
		return (CoupledModelImpl)cm;
	}
	
	public static String mapCoupledToPlantUML(CoupledModelImpl cm){
		String res = "@startuml";
		res += "\nactor ExternalInput";
		   for(Object mm:cm.getChildren()) {
			   res += "\n"+"participant "+((AtomicModel)mm).getName();
		   }
		   res += "\n"+"participant ExternalOutput";
		   ArrayList cp = (ArrayList)cm.getCouplings();
		     
		     Iterator ic = cp.iterator();
		     while (ic.hasNext()) {
		         Object o =  ic.next();
		         Coupling couple = (Coupling)o;
		         String src,srcpt,dst,dstpt;
		         src = couple.getSource().getName();
		         if (src.equals(cm.getName()))
		        	 src = "ExternalInput";
		         srcpt = couple.getSourcePort().getName();
		         dst = couple.getDestination().getName();
		         if (dst.equals(cm.getName()))
		        	 dst = "ExternalOutput";
		         dstpt = couple.getDestinationPort().getName();
		         res += "\n"+src+"  -> "+dst+ " :"+dstpt;
		     }
		res += "\n@enduml";
		return res;
	}
	public static String mapCoupledToSES(CoupledModelImpl cm){
		String res = "";
		res += "\nFrom the "+cm.getName()+" perspective, "+cm.getName()+"Copy is made of ";
		   for(Object mm:cm.getChildren()) {
			   res += ((AtomicModel)mm).getName()+"Copy,";
		   }
		   res = res.substring(0,res.length()-1);
		   res += "!";
		   int ind = res.lastIndexOf(",");
		   res = res.substring(0,ind)+", and "+res.substring(ind+1);///// add comma
		   ArrayList cp = (ArrayList)cm.getCouplings();
		     
		     Iterator ic = cp.iterator();
		     while (ic.hasNext()) {
		         Object o =  ic.next();
		         Coupling couple = (Coupling)o;
		         String src,srcpt,dst,dstpt;
		         src = couple.getSource().getName();
//		         if (src.equals(cm.getName()))
//		        	 src = "ExternalInput";
		         srcpt = couple.getSourcePort().getName();
		         dst = couple.getDestination().getName();
//		         if (dst.equals(cm.getName()))
//		        	 dst = "ExternalOutput";
		         dstpt = couple.getDestinationPort().getName();
		        // res += "\n"+src+"  -> "+dst+ " :"+dstpt;
		          res += "\nFrom the "+cm.getName()+" perspective, "+src+"Copy sends "+srcpt+ " to "+dst+"Copy as "+dstpt;
		          res += "!";
		     }
		for (AtomicModel ch: cm.getChildren()) {
			if (ch instanceof CoupledModelImpl)
			 res += mapCoupledToSES((CoupledModelImpl)ch);
		}
		return res;
	}
	
//    public static void main(String argv[]) throws ClassNotFoundException {
//    	PITDevice model = new PITDevice();
//    	System.out.println(getClassNm(model));
//    	sesRelation ses = mapAtomicModelToPlainSES(model);
//    	ses.printTree();
//    	sesRelationExtend rses = new sesRelationExtend(ses) ;
//    	rses.printTree();
//    	CoupledModelImpl cm = (CoupledModelImpl)pruneNTransformForInstance(rses);
//
//		SimulationOptionsImpl options = new SimulationOptionsImpl(argv, true);
//		// Uncomment the following line to disable SimViewer for this model
//		// options.setDisableViewer(true);
//		// Uncomment the following line to disable plotting for this model
//		// options.setDisablePlotting(true);
//
//		// Uncomment the following line to disable logging for this model
//		// options.setDisableLogging(true);
//
////		model.options = options;
//		if(options.isDisableViewer()){ // Command Line output only
//			Simulation sim = new com.ms4systems.devs.core.simulation.impl.SimulationImpl("BCDE Simulation",model,options);
//			sim.startSimulation(0);
//			sim.simulateIterations(Long.MAX_VALUE);
//		}else { //Use SimViewer
//			SimViewer viewer = new SimViewer();
//			viewer.open(model,options);
//		}
    	
        //        String thisFolder = "C:/DDEProjWAtomicModel3.1NFDAtomicModel/src/endoMorph/";
//        // selfOther so = new selfOther();
//        //
//        ViewableComponent so = new mindArchitecture();// simEFM();
//        // //EFQueryModelOfIntentions();//actions();
//        sesRelation ses = mapAtomicModelToSES(so);
//        // ses = ses.substructure("mindArchitecture");
//        ses.printTree();
//        // ses.printRelations();
//        ses.writeSesDoc(thisFolder+"sesOther.xml");
//        SESOps.extractSesFromEntity("mindArchitecture", thisFolder);
//        SESOps.restoreSesDoc(thisFolder+"SesFormindArchitecture.xml");
//        sesToGenericSchemaOptions.writeSimpleSchemaToXML(thisFolder+"SelfOtherSchema");
//        sesToGenericSchemaOptions.writeModifiedXsdComplex(
//                thisFolder+"SelfOtherSchema.xsd", "mindArchitecture");
//        System.exit();
 //   }
}

