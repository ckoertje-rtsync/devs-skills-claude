package com.ms4systems.devs.markov;
import com.ms4systems.devs.analytics.*;

import java.io.Serializable;
public class TransitionInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//ID:VAR:TransitionInfo:0
    double ProbValue;

    //ENDIF
    //ID:VAR:TransitionInfo:1
    String StartState;

    //ENDIF
    //ID:VAR:TransitionInfo:2
    String EndState;    

    //ENDIF
    
    TimeInfo TInfo;
    
    SampleFromDistribution sfd;    
    
    public TransitionInfo() {
    }

    public TransitionInfo(double ProbValue, String StartState, String EndState) {
        this.ProbValue = ProbValue;
        this.StartState = StartState;
        this.EndState = EndState;
        TInfo = new TimeInfo();
        setSampleFromDistribution();
    }
    
    public TransitionInfo(double ProbValue, String StartState, String EndState, TimeInfo TInfo) {
        this.ProbValue = ProbValue;
        this.StartState = StartState;
        this.EndState = EndState;
        this.TInfo = TInfo;
        setSampleFromDistribution();
    }

    public void setProbValue(double ProbValue) {
        this.ProbValue = ProbValue;
    }

    public double getProbValue() {
        return this.ProbValue;
    }

    public void setStartState(String StartState) {
        this.StartState = StartState;
    }

    public String getStartState() {
        return this.StartState;
    }

    public void setEndState(String EndState) {
        this.EndState = EndState;
    }

    public String getEndState() {
        return this.EndState;
    }

    public TimeInfo getTInfo() {
		return TInfo;
	}

	public void setTInfo(TimeInfo tInfo) {
		TInfo = tInfo;
		setSampleFromDistribution();
	}
	public void setSampleFromDistribution(){
		String type = TInfo.getType();
		if(type.equalsIgnoreCase("none")){
			sfd = new SampleFromDistribution();
		}else if(type.equalsIgnoreCase("uniform")){
			sfd = new SampleFromUniform(TInfo.getLower(),TInfo.getUpper());
		}else if(type.equalsIgnoreCase("normal")){
			sfd = new SampleFromNormal(TInfo.getMean(),TInfo.getSigma());
		}else if(type.equalsIgnoreCase("exponential")){
			sfd = new SampleFromExponential(TInfo.getMean());
		}else if(type.equalsIgnoreCase("logNormal")){
			sfd = new SampleFromLogNormal(TInfo.getMean(),TInfo.getSigma());
		}else if(type.equalsIgnoreCase("pareto")){
			sfd = new SampleFromPareto(TInfo.getAlpha());
		}
	}
	public String toString() {
        String str = "TransitionInfo";
        str += "\n\tProbValue: " + this.ProbValue;
        str += "\n\tStartState: " + this.StartState;
        str += "\n\tEndState: " + this.EndState;
        str += "\n\tTime Info";
        str += "\n\t\tType: "+this.TInfo.getType();
        str += "\n\t\tLower: "+this.TInfo.getLower();
        str += "\n\t\tUpper: "+this.TInfo.getUpper();
        str += "\n\t\tMean: "+this.TInfo.getMean();
        str += "\n\t\tSigma: "+this.TInfo.getSigma();
        str += "\n\t\tAlpha: "+this.TInfo.getAlpha();
        return str;
    }
}
