package com.ms4systems.devs.markov;

import com.ms4systems.devs.analytics.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;


public class ContinuousTimeMarkov {
	public static long Seed = 2349991;
	public static Random Rand = new Random(Seed);
    protected double AccLifeTime = 0;
    protected ArrayList<TimeInState> TimeInStateList;
    protected ArrayList<TransitionInfo> TransitionInfoList;
    protected double AvgLifeTime = 0;
    
    protected String nextState = "";
    protected double timeToNextEvent;
    
    protected boolean isOutput;
    
    protected HashSet<String> endStateSet;
    
    protected String initialState ="";
    public ContinuousTimeMarkov() {
    	nextState = "";
    	AccLifeTime = 0;
    	AvgLifeTime = 0;
    	TimeInStateList=new ArrayList<TimeInState>();
    	TransitionInfoList =new ArrayList<TransitionInfo>();
    	endStateSet = new HashSet<String>();
    	isOutput = false;
    }
    //BPZ
    public double getHoldTime(String phase,double mean) {
    	TransitionInfo ti = new TransitionInfo();
    	ti.sfd = new SampleFromExponential(1);
    	return mean*ti.sfd.getSample();
    }

    public double timeToNextEvent(double lambda) {
        double sample = Rand.nextDouble();
        return -(1 / lambda) * Math.log(sample);
    }
    
    // Get a sample time from time information in TransitionInfo (9/17/2017 cseo)
    public double timeToNextEvent(String state, String succ, double norm) {
    	if(state.equals(succ)) return Double.MAX_VALUE;
    	TransitionInfo ti = getTransitionInfoFor(state, succ);
    	if(ti.getTInfo().getType().equals("None")){
    		return ti.sfd.getSample(norm);
    	}
    	return ti.sfd.getSample();
    }
    
    public double getMeanValue(String state, String succ){
    	if(state.equals(succ)) return -1.0;
    	TransitionInfo ti = getTransitionInfoFor(state, succ);
    	if(ti.getTInfo().getType().equals("None")){
    		return -1.0;
    	}
    	return ti.sfd.getMean();
    }
    
    public void incCount(TimeInState tm) {
        int ct = tm.getCountInState() + 1;
        tm.setCountInState(ct);
    }

    public void updateElapsedTime(TimeInState tm, double e) {
        double ct = tm.getElapsedTime() + e;
        tm.setElapsedTime(ct);
    }

    public TimeInState getTimeInState(String state) {
        for (TimeInState tm : TimeInStateList) {
            if (tm.getStateName().equals(state)) {
                return tm;
            }
        }
        return null;
    }

    public void printTimeInState() {
    	HashSet hs = getStates();
        for (TimeInState tm : TimeInStateList) {
        	hs.remove(tm.getStateName());
            System.out.println(tm.getStateName() + " " + tm.getElapsedTime() +
                " " + tm.getCountInState());
        }
        System.out.println("Unreached states "+hs);
    }

    public String TimeInStateString(String state) {
        String s = "" + state + " ";
        for (TimeInState tm : TimeInStateList) {
            s += (tm.getStateName() + " " + tm.getElapsedTime() + " " +
            tm.getCountInState());
        }
        return s;
    }
    ///BPZ
    public void addTransitionInfoForExponential(String state, String[] successors,
        double[] probabilities,double[] mean) {
        int i = 0;
        for (String succ : successors) {
            TransitionInfo ti = new TransitionInfo();
            ti.setStartState(state);
            ti.setEndState(succ);
            ti.setProbValue(probabilities[i]);
            TransitionInfoList.add(ti);
            ///BPZ
            TimeInfo tf = new TimeInfo();
            tf.setType("Exponential");
            tf.setMean(mean[i]);
            ti.setTInfo(tf);
            i++;
        }
    }
    ///BPZ
    public void addTransitionInfoForExponential(String state, String[] successors,
            double[] probabilities) {
    	double[] means = new double[probabilities.length];
    	for (int i=0;i<means.length;i++) means[i] = 1.;
    	addTransitionInfoForExponential(state,successors,probabilities,means);
    }
    ///BPZ
    public void addTimeInfo(String state, String successor,String type,double mean){{
            TransitionInfo ti = this.getTransitionInfoFor(state, successor);
            TimeInfo tf = new TimeInfo();
            tf.setType(type);
            tf.setMean(mean);
            ti.setTInfo(tf);
        }
    }
    public void addTransitionInfo(String state, String[] successors,
            double[] probabilities) {
            int i = 0;
            for (String succ : successors) {
                TransitionInfo ti = new TransitionInfo();
                ti.setStartState(state);
                ti.setEndState(succ);
                ti.setProbValue(probabilities[i]);
                TransitionInfoList.add(ti);
                i++;
            }
        }
    public void replaceTransitions(ArrayList<TransitionInfo> tis) {
        for (TransitionInfo ti : tis) {
            replaceTransition(ti);
        }
    }

    public void replaceTransition(TransitionInfo ti) {
        if (ti.getStartState() != null && ti.getEndState() != null) {
            replaceTransition(ti.getStartState(), ti.getEndState(),
                ti.getProbValue());
        }
    }

    public void replaceTransition(String state, String succ, double prob) {
        TransitionInfo ti = this.getTransitionInfoFor(state, succ);
        if (ti != null) {
            double p = ti.getProbValue();
            ti.setProbValue(prob);
            return;
        }
        ti = new TransitionInfo();
        ti.setStartState(state);
        ti.setEndState(succ);
        ti.setProbValue(prob);
        TransitionInfoList.add(ti);
    }

    public ArrayList<TransitionInfo> lumpTransitionInfoList(String state,
        String endState, TransitionInfo[] tis, double[] occurProbs) {
        TransitionInfo tbase = getTransitionInfoFor(state, endState);
        if (tbase == null) {
            return TransitionInfoList;
        }
        double pbase = tbase.getProbValue();
        double sumOfOccur = 0;
        double sumOfWeightedTransProb = 0;
        int i = 0;
        for (TransitionInfo ti : tis) {
            double pv = ti.getProbValue();
            double pt = occurProbs[i];
            sumOfWeightedTransProb += pt * pv;
            sumOfOccur += pt;
            i++;
        }
        double pbleft = 1 - sumOfOccur;
        sumOfWeightedTransProb += pbleft * pbase;
        ArrayList<TransitionInfo> newtl = new ArrayList<TransitionInfo>();
        TransitionInfo tn = new TransitionInfo();
        tn.setStartState(state);
        tn.setEndState(endState);
        tn.setProbValue(sumOfWeightedTransProb);
        for (TransitionInfo ti : TransitionInfoList) {
            if (!ti.getStartState().equals(state)) {
                newtl.add(ti);
            } else {
                newtl.add(tn);
            }
        }
        return newtl;
    }

    public ArrayList<TransitionInfo> getTransitionInfoFor(String state) {
        ArrayList<TransitionInfo> sublist = new ArrayList<TransitionInfo>();
        ArrayList<TransitionInfo> l = getTransitionInfoList();
        for (TransitionInfo ti : l) {
            if (ti.getStartState().equals(state)) {
                sublist.add(ti);
            }
        }
        return sublist;
    }

    public TransitionInfo getTransitionInfoFor(String state, String succ) {
        ArrayList<TransitionInfo> l = getTransitionInfoFor(state);
        for (TransitionInfo ti : l) {
            if (ti.getEndState().equals(succ)) {
                return ti;
            }
        }
        return null;
    }
    public double[] probabilitiesfor(String state){
    	double probs[] = new double[getStates().size()];
        for (int i = 0; i < probs.length; i++) {
            probs[i] = 0;
        }
        for (int i = 0; i < TransitionInfoList.size(); i++) {
            TransitionInfo ti = TransitionInfoList.get(i);
            if (ti.getStartState().equals(state)) {
                int index = getIndex(ti.getEndState());
                probs[index] = ti.getProbValue();
            }
        }
        double sum = 0;
        for (int i = 0; i < probs.length; i++) {
            sum += probs[i];
        }
        if (sum <1) probs[getIndex(state)] = 1 - sum;
    	
    	return probs;
    }
    public double[] TransitionMeansfor(String state){
    	double means[]= new double[getStates().size()];
        for (int i = 0; i < means.length; i++) {
        	means[i] = 0;
        }
        for (int i = 0; i < TransitionInfoList.size(); i++) {
            TransitionInfo ti = TransitionInfoList.get(i);
            if (ti.getStartState().equals(state)) {
            	if(ti.getTInfo().getType().equals("none")){
            		int index = getIndex(ti.getEndState());
                    means[index] = 1/normalFactor(state);
            	}else{
            		int index = getIndex(ti.getEndState());
                    means[index] = ti.sfd.getMean();
            	}                
            }
        }            	
    	return means;
    }
    public double normalFactor(String state) {
        double sum = 0;
        for (String succ : getSuccs(state)) {
            if (succ.equals(state)) {
                continue;
            }
            TransitionInfo ti = getTransitionInfoFor(state, succ);
            sum += ti.getProbValue();
        }
        return sum;
    }
    
    public HashSet<String> getStates() {
        HashSet<String> str = new HashSet<String>();
        for (int i = 0; i < TransitionInfoList.size(); i++) {
            TransitionInfo ti = TransitionInfoList.get(i);
            str.add(ti.getStartState());
        }
        return str;
    }

    public ArrayList<String> indexStates() {
        HashSet<String> h = getStates();
        ArrayList<String> al = new ArrayList<String>();
        for (String str : h) {
            al.add(str);
        }
        Collections.sort(al);
        return al;
    }
    public ArrayList<String> indexStatesNoSorting() {
        ArrayList<String> al = new ArrayList<String>();
        for (int i = 0; i < TransitionInfoList.size(); i++) {
            TransitionInfo ti = TransitionInfoList.get(i);
            al.add(ti.getStartState());
        }
       
        return al;
    }
    public int getIndex(String state) {
        ArrayList<String> al = indexStates();
        for (int i = 0; i < al.size(); i++) {
            if (al.get(i).equals(state)) {
                return i;
            }
        }
        return -1;
    }

    public double[] getProbs(String state) {
        double[] probs = new double[getStates().size()];
        for (int i = 0; i < probs.length; i++) {
            probs[i] = 0;
        }
        for (int i = 0; i < TransitionInfoList.size(); i++) {
            TransitionInfo ti = TransitionInfoList.get(i);
            if (ti.getStartState().equals(state)) {
                int index = getIndex(ti.getEndState());
                probs[index] = ti.getProbValue();
            }
        }
        double sum = 0;
        for (int i = 0; i < probs.length; i++) {
            sum += probs[i];
        }
        if (sum <1)
        probs[getIndex(state)] = 1 - sum;
        return probs;
    }
    public ArrayList<Double> getProbsArray(String state) {
        ArrayList<Double> ar = new ArrayList<Double>();
        ArrayList<TransitionInfo> l = getTransitionInfoFor(state);
        for (int i = 0; i < l.size(); i++) {
            TransitionInfo ti = l.get(i);
            ar.add(ti.getProbValue());
        }
        return ar;
    }

    public ArrayList<String> getSuccs(String state) {
        ArrayList<String> ar = new ArrayList<String>();
        ArrayList<TransitionInfo> l = getTransitionInfoFor(state);
        for (int i = 0; i < l.size(); i++) {
            TransitionInfo ti = l.get(i);
            ar.add(ti.getEndState());
        }
        return ar;
    }

    public String nextPhase(String phase) {
	return ProbabilityChoice.selectPhase(Rand, getProbsArray(phase), getSuccs(phase) );
    }
	

	public Random getRand() {
		return Rand;
	}

	public void setRand(Random rand) {
		Rand = rand;
	}

	public double getAccLifeTime() {
		return AccLifeTime;
	}

	public void setAccLifeTime(double accLifeTime) {
		AccLifeTime = accLifeTime;
	}

	public ArrayList<TimeInState> getTimeInStateList() {
		return TimeInStateList;
	}

	public void setTimeInStateList(ArrayList<TimeInState> timeInStateList) {
		TimeInStateList = timeInStateList;
	}

	public ArrayList<TransitionInfo> getTransitionInfoList() {
		return TransitionInfoList;
	}

	public void setTransitionInfoList(ArrayList<TransitionInfo> transitionInfoList) {
		TransitionInfoList = transitionInfoList;
	}

	public double getAvgLifeTime() {
		return AvgLifeTime;
	}

	public void setAvgLifeTime(double avgLifeTime) {
		AvgLifeTime = avgLifeTime;
	}

	public long getSeed() {
		return Seed;
	}

	public void setSeed(long seed) {
		Seed = seed;
	}

//	public MarkovMat getMm() {
//		return mm;
//	}
//
//	public void setMm(MarkovMat mm) {
//		this.mm = mm;
//	}

	public String getNextState() {
		return nextState;
	}

	public void setNextState(String nextState) {
		this.nextState = nextState;
	}

	public double getTimeToNextEvent() {
		return timeToNextEvent;
	}

	public void setTimeToNextEvent(double timeToNextEvent) {
		this.timeToNextEvent = timeToNextEvent;
	}

	public boolean isOutput() {
		return isOutput;
	}

	public void setOutput(boolean isOutput) {
		this.isOutput = isOutput;
	}

	public String getInitialState() {
		return initialState;
	}

	public void setInitialState(String initialState) {
		this.initialState = initialState;
	}    
    
}
