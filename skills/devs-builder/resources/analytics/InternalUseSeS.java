package com.ms4systems.devs.analytics;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import com.ms4systems.devs.core.util.*;
import com.ms4systems.devs.core.model.AtomicModel;
import com.ms4systems.devs.core.model.CoupledModel;
import com.ms4systems.devs.core.model.impl.CoupledModelImpl;

public class InternalUseSeS extends BasicExecution {
	public InternalUseSeS() {

	}

	public InternalUseSeS(String sesfile, String pesfile) {
		workspace = System.getProperty("user.dir") + "\\src\\";
		projectNm = "Tools";// "CHAPV3";//
		folderJava = workspace + projectNm + "\\Models\\java\\";
		packageNm = projectNm + ".Models.java.";
		folderTxt = workspace + projectNm + "\\Models\\txt\\";

	}

	public static String getSeSFolder() {

		new InternalUseSeS();
		return workspace + projectNm + File.separator+"Models"+File.separator+"ses"+File.separator;
	}
	public void makeFolderPath(){
		if(projectNm != null){
			folderSes = workspace + projectNm + File.separator+"Models"+File.separator+"ses"+File.separator;
			folderPes = workspace + projectNm + File.separator+"Models"+File.separator+"pes"+File.separator;
		}else{
			String OS = System.getProperty("os.name").toLowerCase();
			if (OS.indexOf("win") >= 0) {
				String folder = System.getProperty("user.dir");
				folderSes = folder +File.separator+"Models"+File.separator+"ses"+File.separator;
				folderPes = folder+ File.separator+"Models"+File.separator+"pes"+File.separator;
			}else{
				String folder = System.getProperty("user.dir");
				folderSes = folder +"/Models/ses/";
				folderPes = folder+ "/Models/pes/";
			}

		}
	}
	public static sesRelationExtend getSesFromFile(String sesfile) {
		sesRelationExtend rses = new sesRelationExtend(getSeSFolder(), sesfile);
		
		return rses;
	}
	public static sesRelationExtend getSesFromFileInstance(String sesfile) {
		InternalUseSeS pe = new InternalUseSeS();
		sesRelationExtend rses = new sesRelationExtend(getSeSFolder(), sesfile);
		rses.doExtendedCoupling(getSeSFolder()+sesfile);
        // Add Multiplicity (cs 5/7/2017) 
		pruningRuleForMultiplicity(rses);
		return rses;
	}
	
	// Full capability of pruning processes (5/19/2017 cseo)
	public static sesRelationExtend getSesFromFileInstance(String sesfile, String pesfile) {
		PESToDEVS.isMS4MeEnv = false;
		InternalUseSeS pe = new InternalUseSeS();
		pe.makeFolderPath();
				
		sesRelationExtend rses = pe.doWorkInstance(sesfile, pesfile);

		rses.printTree();
		return rses;
	}
	
	// Full capability of pruning processes with contents (6/18/2018 cseo)
	public static sesRelationExtend getSesFromContents(String sesContent, String pesContent) {
		PESToDEVS.isMS4MeEnv = false;
		InternalUseSeS pe = new InternalUseSeS();
		pe.makeFolderPath();
				
		sesRelationExtend rses = pe.doWorkInstanceWContent(sesContent, pesContent);

		rses.printTree();
		return rses;
	}
	// Provide a folder directory and a ses file (4/25/2017 cs)
	public static sesRelationExtend getSesFromFileInstance(String folder, String sesfile, String pesfile) {
		InternalUseSeS pe = new InternalUseSeS();
		sesRelationExtend rses = new sesRelationExtend(folder, sesfile);
		rses.doExtendedCoupling(folder+sesfile);
        
		return rses;
	}
	public static Set getEntitiesOfSpec(sesRelationExtend rses, String spec) {
		Relation r = new Relation(rses.specHasEntity());
		for (Object o : r.keySet()) {

		}
		return r.getSet(spec);
	}

	public static ArrayList entityBelongsToWhichSpec(sesRelationExtend rses,
			String entity) {
		Relation r = new Relation(rses.specHasEntity());
		Relation conv = r.getConverse();
		return new ArrayList(conv.getSet(entity));
	}

	public static Set getChoosableEntities(sesRelationExtend rses) {
		Relation r = new Relation(rses.specHasEntity());
		Relation conv = r.getConverse();
		return conv.keySet();
	}

	public static boolean isInt(String s) {
		try {
			int n = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public static String stripIntegerSuffix(String s) {
		if (isInt(s.substring(s.length() - 1, s.length()))) {
			return stripIntegerSuffix(s.substring(0, s.length() - 1));
		}
		return s;
	}

	public static Set getCoreEntities(sesRelationExtend rses) {
		Set choosable = getChoosableEntities(rses);
		HashSet hs = new HashSet();
		for (Object o : choosable) {
			String entity = o.toString();
			hs.add(stripIntegerSuffix(entity));
		}
		return hs;
	}

	public static Set getChoosableEntitiesEquivTo(sesRelationExtend rses,
			String coreEnt) {
		Set choosable = getChoosableEntities(rses);
		HashSet hs = new HashSet();
		for (Object o : choosable) {
			String entity = o.toString();
			if (coreEnt.equals(stripIntegerSuffix(entity)))
				hs.add(entity);
		}
		return hs;
	}

	public static Hashtable computeEntityToSpec(sesRelationExtend rses) {
		Hashtable entityToSpec = new Hashtable();
		Set choosable = InternalUseSeS.getChoosableEntities(rses);
		for (Object o : choosable) {
			String s = o.toString();
			ArrayList al = InternalUseSeS.entityBelongsToWhichSpec(rses, s);
			if (al != null && !al.isEmpty()) {
				entityToSpec.put(InternalUseSeS.stripIntegerSuffix(s),
						al.get(0));
			}
		}
		return entityToSpec;
	}

	public static Bag getDistributionOfEntities(sesRelationExtend rses,
			String spec) {
		Bag b = new Bag(true);
		Relation r = new Relation(rses.specHasEntity());
		for (Object s : rses.getEnsembleSet("specNames")) {
			if (s.toString().contains(spec)) {
				Set choosable = r.getSet(s);
				for (Object c : choosable) {
					b.add(stripIntegerSuffix(c.toString()));
				}
			}
		}
		return b;
	}

	public static sesRelationExtend setDistributionOfEntities(
			sesRelationExtend rses, String spec, Bag distribution) {

		Set entities = distribution.keySet();
		for (Object o : entities) {
			String entity = o.toString();
			ArrayList al = entityBelongsToWhichSpec(rses, o.toString());
			if (al == null || al.isEmpty())
				continue;
			String specNm = al.get(0).toString();
			if (specNm.contains(spec)) {
				int cur = getChoosableEntitiesEquivTo(rses, entity).size();
				int num = distribution.numberOf(entity);
				if (num <= 0) {
					Hashtable specHasEntity = rses.specHasEntity();
					HashSet s = (HashSet) specHasEntity.get(specNm);
					Iterator it = s.iterator();
					s.remove(entity);
					specHasEntity.remove(specNm);
					if (s.size() > 0) {
						specHasEntity.put(specNm, s);
					}
				} else
					for (int i = 0; i < num - cur; i++) {
						rses.addEntityToSpec(entity + i, specNm);
					}
			}
		}
		return rses;
	}


	public void pruneAndTransform(sesRelationExtend ses,
			pruningTable pruningTable, sesRelationExtend.pruningRules pr) {

		try {
			pruneNTransform((sesRelationExtend) ses, this, pruningTable,
					contextPrune.folderJava, packageNm);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public com.ms4systems.devs.core.util.pruningTable getPruneInfoNL(
			String natLangFile, sesRelationExtend ses) {
		if (natLangFile.equals("")) {
			return new com.ms4systems.devs.core.util.pruningTable();
		}
		String contents = fileHandler.getContentsAsString(natLangFile);
		if (contents == null) {
			System.out.println("wrong file path");
			return null;
		}
		return parseNInterpret(contents, ses);
	}
	// using a pes string content (cseo 6/29/2018)
	public com.ms4systems.devs.core.util.pruningTable getPruneInfoNLContents(
			String natLangContents, sesRelationExtend ses) {
		if (natLangContents.equals("")) {
			return new com.ms4systems.devs.core.util.pruningTable();
		}
		
		return parseNInterpret(natLangContents, ses);
	}
	public static AtomicModel pruneNTransToGetModel(String sesfile,
			String pesfile) {
		sesRelationExtend rses = getSesFromFile(sesfile);
		return pruneNTransToGetModel(rses, pesfile);
	}
	public static AtomicModel pruneNTransToGetModelInstance(String sesfile,
			String pesfile) {
		sesRelationExtend rses = getSesFromFileInstance(sesfile);
        pruningRuleForMultiplicity(rses);
		return pruneNTransToGetModelInstance(rses, pesfile);
	}
	// Provide a folder directory, a ses file, and a pes file (4/25/2017 cs)
	public static AtomicModel pruneNTransToGetModelInstance(String folder, String sesfile,
			String pesfile) {
		sesRelationExtend rses = getSesFromFileInstance(folder, sesfile);
		return pruneNTransToGetModelInstance(rses, pesfile);
	}
	public static AtomicModel pruneNTransToGetModel(sesRelationExtend rses,
			String pesfile) {
		InternalUseSeS pe = new InternalUseSeS();
		String folder = getSeSFolder();
		folder = folder.replaceAll("ses", "pes");
		pruningTable pruningTable =
		pe.getPruneInfoNL(folder + pesfile, rses);
		try {
			String packNm = pe.packageNm
					.substring(1, pe.packageNm.length() - 1);
			contextPrune.pruneNTransformForData(rses, (CoupledModelImpl) pe,
					pruningTable, folderJava, packNm);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pe;
	}
	// Modified to get multiple spec (5/19/2017 cseo)
	public static AtomicModel pruneNTransToGetModelInstance(sesRelationExtend ses,
			String pesfile) {
		InternalUseSeS pe = new InternalUseSeS();
		pe.makeFolderPath();
		com.ms4systems.devs.core.util.pruningTable pruningTable = pe.getPruneInfoNL(pe.folderPes+pesfile, ses);
		sesRelationExtend.pruningRules pr= ses.PruningRules;
		
		if(pruningTable.getMultiSpec().size()!=0){
    		for(String parent : pruningTable.getMultiSpec()){
    			
    			for(Object o : ses.getEnsembleSet("entityNames")){
    				String entity = o.toString();
    				if(entity.contains("_"+parent)){
    					String[] pairs = pruningTable.getPairs(parent);
    					String[] newPairs = new String[pairs.length];
    					for(int i = 0; i < pairs.length ; i++){
    						String newPair =pairs[i].replace(parent, entity);
    						newPairs[i]=newPair;
    					}
    					pruningTable.addPairs(entity, newPairs);
    				} 
    			}    			
    		}
    	}
        HashSet<Object> toAdd = new HashSet<Object>();
        for (Object o : ses.getEnsembleSet("entityNames")) {
            String entity = o.toString();
            String[] pairs = pruningTable.getPairs(entity);
            if (pairs != null && pairs.length > 0) {
                for (String str : pairs) {
                    HashSet<Object> es = pr.getAction(entity, str);
                    for (Object oo:es){
	                    Pair act = (Pair)oo;
	                    if (act != null) {
	                        toAdd.add(act);
	                    }
                    }
                }
            }
            for (Object oo : toAdd) {
                Pair act = (Pair) oo;
                pruningTable.addPair((String) act.getKey(), (String) act.getValue());
            }
        }		
		
		String folder = getSeSFolder();
		folder = folder.replaceAll("ses", "pes");

		try {
			String packNm = pe.packageNm
					.substring(1, pe.packageNm.length() - 1);
			contextPrune.pruneNTransformForInstance(ses, (CoupledModelImpl) pe,
					pruningTable, folderJava, packNm);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pe;
	}
	// using pes contents (cs 6/29/2018)
	public static AtomicModel pruneNTransToGetModelInstanceWContents(sesRelationExtend ses,
			String pesContents) {
		InternalUseSeS pe = new InternalUseSeS();
		pe.makeFolderPath();
		com.ms4systems.devs.core.util.pruningTable pruningTable = pe.getPruneInfoNLContents(pesContents, ses);
		sesRelationExtend.pruningRules pr= ses.PruningRules;
		
		if(pruningTable.getMultiSpec().size()!=0){
    		for(String parent : pruningTable.getMultiSpec()){
    			
    			for(Object o : ses.getEnsembleSet("entityNames")){
    				String entity = o.toString();
    				if(entity.contains("_"+parent)){
    					String[] pairs = pruningTable.getPairs(parent);
    					String[] newPairs = new String[pairs.length];
    					for(int i = 0; i < pairs.length ; i++){
    						String newPair =pairs[i].replace(parent, entity);
    						newPairs[i]=newPair;
    					}
    					pruningTable.addPairs(entity, newPairs);
    				} 
    			}    			
    		}
    	}
        HashSet<Object> toAdd = new HashSet<Object>();
        for (Object o : ses.getEnsembleSet("entityNames")) {
            String entity = o.toString();
            String[] pairs = pruningTable.getPairs(entity);
            if (pairs != null && pairs.length > 0) {
                for (String str : pairs) {
                    HashSet<Object> es = pr.getAction(entity, str);
                    for (Object oo:es){
	                    Pair act = (Pair)oo;
	                    if (act != null) {
	                        toAdd.add(act);
	                    }
                    }
                }
            }
            for (Object oo : toAdd) {
                Pair act = (Pair) oo;
                pruningTable.addPair((String) act.getKey(), (String) act.getValue());
            }
        }		
		
		String folder = getSeSFolder();
		folder = folder.replaceAll("ses", "pes");

		try {
			String packNm = pe.packageNm
					.substring(1, pe.packageNm.length() - 1);
			contextPrune.pruneNTransformForInstance(ses, (CoupledModelImpl) pe,
					pruningTable, folderJava, packNm);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pe;
	}
	// Provide a folder directory (4/25/2017 cs)
	public static AtomicModel pruneNTransToGetModelInstance(String folder, sesRelationExtend rses,
			String pesfile) {
		InternalUseSeS pe = new InternalUseSeS();
		folder = folder.replaceAll("ses", "pes");
		pruningTable pruningTable =
		pe.getPruneInfoNL(folder + pesfile, rses);
		try {
			String packNm = pe.packageNm
					.substring(1, pe.packageNm.length() - 1);
			contextPrune.pruneNTransformForInstance(rses, (CoupledModelImpl) pe,
					pruningTable, folderJava, packNm);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pe;
	}
	public static HashSet<AtomicModel> getAtomics(CoupledModel model) {
		HashSet hs = new HashSet<AtomicModel>();
		ArrayList<AtomicModel> ar = model.getChildren();
		for (Object o : ar) {
			if (!(o instanceof CoupledModel)) {
				hs.add(o);
			} else {
				hs.addAll(getAtomics((CoupledModel) o));
			}
		}
		return hs;
	}

	//
	public static String[] getParts(String sentence) {
		Pattern p = Pattern.compile("_");
		String[] groups = p.split(sentence);
		for (int i = 0; i < groups.length; i++) {
			groups[i] = groups[i].trim();
		}
		return groups;
	}

	//
	public static String getSpecParentOfEntity(sesRelationExtend rses,
			String entity) {
		Relation r = new Relation(rses.specHasEntity());
		Set keys = r.keySet();
		for (Object k : keys) {
			Set vals = r.getSet(k);
			for (Object v : vals) {
				if (v.equals(entity)) {
					return k.toString();
				}
			}
		}
		return null;
	}

	// //
	public static String getAspectParentOfEntity(sesRelationExtend rses,
			String entity) {
		Relation r = new Relation(rses.aspectHasEntity());
		Set keys = r.keySet();
		for (Object k : keys) {
			Set vals = r.getSet(k);
			for (Object v : vals) {
				if (v.equals(entity)) {
					return k.toString();
				}
			}
		}
		return null;
	}

	public static sesRelationExtend configureSeSToDistributions(String sesfile,
			PDF[] pdfs) {
		sesRelationExtend rses = InternalUseSeS.getSesFromFile(sesfile);
		for (PDF pdf : pdfs) {
			Bag Dist = Bag.pdfToBag(pdf);
			rses = InternalUseSeS.setDistributionOfEntities(rses,
					pdf.getName(), Dist);
		}
		Hashtable specHasEntity = rses.specHasEntity();
		HashSet keepers = new HashSet();
		for (PDF pdf : pdfs) {
			keepers.add(pdf.getName());
		}
		Set specs = specHasEntity.keySet();
		HashSet remnms = new HashSet();
		for (Object spec : specs) {
			remnms.add(InternalUseSeS.extractSpec(spec.toString()));
		}
		remnms.removeAll(keepers);
		for (Object sp : remnms) {
			String spstr = sp.toString();
			for (Object o : specs) {
				String backToSpec = o.toString();
				if (backToSpec.contains(spstr))
					rses.removeSpec(backToSpec);
			}
		}
		return rses;
	}

	public static sesRelationExtend configureSeSToDistributions(String sesfile,
			Hashtable pdfs) {
		HashSet pds = new HashSet(pdfs.values());
		PDF probs[] = new PDF[pds.size()];
		int i = 0;
		for (Object o : pds) {
			PDF pdf = (PDF) o;
			probs[i] = pdf;
			i++;
		}
		return configureSeSToDistributions(sesfile, probs);
	}

	public static double computeExpectedRisk(PDF[] ProbPdfs, PDF[] RiskPdfs) {
		String nms[] = new String[ProbPdfs.length];
		double res[] = new double[ProbPdfs.length];
		int i = 0;
		for (PDF pdf : ProbPdfs) {
			nms[i] = pdf.getName();
			res[i] = pdf.vectorProduct(RiskPdfs[i]);
			i++;
		}
		PDF er = PDF.makePDF(nms, res);
		return er.bernouliCombination();
	}

	public static double computeExpectedRisk(Hashtable ProbPdfs,
			Hashtable RiskPdfs) {
		PDF probs[] = new PDF[ProbPdfs.size()];
		PDF rsks[] = new PDF[RiskPdfs.size()];
		Set ProbKeys = ProbPdfs.keySet();
		Set RiskKeys = RiskPdfs.keySet();
		ProbKeys.retainAll(RiskKeys);
		int i = 0;
		for (Object o : ProbKeys) {
			PDF pdf = (PDF) ProbPdfs.get(o);
			probs[i] = pdf;
			pdf = (PDF) RiskPdfs.get(o);
			rsks[i] = pdf;
			i++;
		}
		return computeExpectedRisk(probs, rsks);
	}

	public static String getProfile(String pesfile, sesRelationExtend rses,
			Hashtable entityToSpec) {
		HashSet specs = new HashSet();
		return getProfile(pesfile, rses, entityToSpec, specs);
	}

	public static String getProfile(String pesfile, sesRelationExtend rses,
			Hashtable entityToSpec, HashSet specs) {
		AtomicModel pe = InternalUseSeS.pruneNTransToGetModel(rses, pesfile);
		CoupledModelImpl c = (CoupledModelImpl) pe;
		String nm = "";
		ArrayList<AtomicModel> cd = c.getChildren();
		for (AtomicModel a : cd) {
			String[] parts = InternalUseSeS.getParts(a.getName());
			for (String part : parts) {
				part = InternalUseSeS.stripIntegerSuffix(part);
				Object o = entityToSpec.get(part);
				if (o == null)
					continue;
				String spec = InternalUseSeS.extractSpec(o.toString());
				if (specs.isEmpty() || specs.contains(spec)) {
					nm += part;
				}
			}
		}
		return nm;
	}

	public static double[] getRisk(int count, double actualRisk,
			String profile, Hashtable riskpdfs, Set<String> elements,
			Hashtable entityToSpec, Random rand) {

		ArrayList al = new ArrayList();
		for (String el : elements) {
			if (profile.contains(el)) {
				al.add(el);
			}
		}
		double[] risks = new double[al.size()];
		boolean[] selections = new boolean[al.size()];
		for (boolean b : selections) {
			b = false;
		}
		PDF genpdf = new PDF(true);
		for (int i = 0; i < al.size(); i++) {
			Object o = al.get(i);
			Object oo = entityToSpec.get(o);
			if (oo == null)
				continue;
			String spec = oo.toString();
			spec = extractSpec(spec);
			PDF rskpdf = (PDF) riskpdfs.get(spec);
			double risk = rskpdf.get(o.toString());
			genpdf.put(o.toString(), risk);
			int choice = ProbabilityChoice.makeSelectionFrom2(rand, risk);
			if (choice == 1)
				selections[i] = true;
		}
		boolean RiskOccurrence = false;
		for (boolean sel : selections) {
			if (sel) {
				RiskOccurrence = true;
				break;
			}
		}

		double RiskOccur = RiskOccurrence ? 1 : 0;

		actualRisk = (count * actualRisk + RiskOccur) / (count + 1);

		double genrisk = genpdf.bernouliCombination();
		return new double[] { genrisk, RiskOccur, actualRisk };
	}

	public static double getProbID(String profile, Hashtable idpdfs,
			Set<String> elements, Hashtable entityToSpec) {

		ArrayList al = new ArrayList();
		for (String el : elements) {
			if (profile.contains(el)) {
				al.add(el);
			}
		}
		PDF genpdf = new PDF(true);
		for (int i = 0; i < al.size(); i++) {
			Object o = al.get(i);
			Object oo = entityToSpec.get(o);
			if (oo == null)
				continue;
			String spec = oo.toString();
			spec = extractSpec(spec);
			PDF idpdf = (PDF) idpdfs.get(spec);
			double idprob = idpdf.get(o.toString());
			genpdf.put(o.toString(), idprob);
		}

		return genpdf.bernouliCombination();
	}

	public static HashSet enumerate(String projectName, String sesfile,
			String pesfile) {
		return new InternalUseSeS().EnumeratePruningsIterate(projectName,
				sesfile, pesfile);
	}

	public HashSet EnumeratePruningsIterate(String projectName, String sesfile,
			String pesfile) {
		HashSet samples = new HashSet();
		InternalUseSeS pe = new InternalUseSeS(sesfile, pesfile);
		sesRelationExtend ses = getSesFromFile(sesfile);
		enumeratePrunings.assignEnumerate(ses);
		String folder = getSeSFolder();
		folder = folder.replaceAll("ses", "pes");
		pruningTable pruningTable = pe.getPruneInfoNL(folder + pesfile, ses);

		int cycleLength = enumeratePrunings.getCycleLength();
		System.out.println("Number of PESs is bounded by :" + cycleLength);
		for (int i = 0; i < cycleLength; i++) {
			pe = new InternalUseSeS(sesfile, pesfile);
			enumeratePrunings.doFirstPart(ses, folderJava, packageNm);
			String selects = enumeratePrunings.getCurrent();
			System.out.println("Current selects are :" + selects);
			samples.add(selects);
		}
		return samples;
	}

	public static String extractSpec(String spec) {
		int ind = spec.indexOf("-");
		if (ind < 0)
			return spec;
		return spec.substring(ind + 1, spec.length() - 4);
	}

	public static double sumOverDomain(Hashtable f, Hashtable entityToSpec,
			HashSet samples) {
		Set elements = entityToSpec.keySet();
		double sum = 0;
		for (Object sample : samples) {
			Hashtable ht = new Hashtable();
			for (Object el : elements) {
				String elem = el.toString().trim();
				if (sample.toString().contains(elem)) {
					Object spec = entityToSpec.get(el);
					ht.put(extractSpec(spec.toString()), elem);
				}
			}

			Double val = (Double) f
					.get(new Pair(ht.get("size"), ht.get("age")));
			sum += val;
		}
		return sum;
	}

	public static double sumOverSubDomain(String spec, String entity,
			Hashtable entityToSpec, HashSet entitiesOfOtherSpec, Hashtable f,
			HashSet samples) {
		double sum = 0;
		for (Object sample : samples) {
			Hashtable ht = new Hashtable();
			for (Object el : entitiesOfOtherSpec) {
				String elem = el.toString().trim();
				if (sample.toString().contains(entity)
						&& sample.toString().contains(elem)) {
					Object otherspec = entityToSpec.get(el);
					ht.put(spec, entity);
					ht.put(extractSpec(otherspec.toString()), elem);
				}
			}
			if (ht.isEmpty())
				continue;
			Double val = (Double) f
					.get(new Pair(ht.get("size"), ht.get("age")));
			if (val != null)
				sum += val;
		}
		return sum;
	}

	public static void main(String argv[]) throws InterruptedException {
		
		String sesfile = "MultiDemo.ses";
		String pesfile = "MultiDemo2.pes";
		
		sesRelationExtend ses = InternalUseSeS.getSesFromFileInstance(sesfile,pesfile);
		CoupledModelImpl ExampleSES = (CoupledModelImpl) InternalUseSeS
				.pruneNTransToGetModelInstance(ses, pesfile);
		for (AtomicModel am : ExampleSES.getChildren()) {
			System.out.println("/t"+am.getName());
		}
	
	}
}
