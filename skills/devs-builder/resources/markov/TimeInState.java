package com.ms4systems.devs.markov;

import java.io.Serializable;

public class TimeInState implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//ID:VAR:TimeInState:0
    String StateName;

    //ENDIF
    //ID:VAR:TimeInState:1
    int CountInState = 0;

    //ENDIF
    //ID:VAR:TimeInState:2
    double ElapsedTime = 0;

    //ENDIF
    public TimeInState() {
    }

    public TimeInState(String StateName, int CountInState, double ElapsedTime) {
        this.StateName = StateName;
        this.CountInState = CountInState;
        this.ElapsedTime = ElapsedTime;
    }

    public void setStateName(String StateName) {
        this.StateName = StateName;
    }

    public String getStateName() {
        return this.StateName;
    }

    public void setCountInState(int CountInState) {
        this.CountInState = CountInState;
    }

    public int getCountInState() {
        return this.CountInState;
    }

    public void setElapsedTime(double ElapsedTime) {
        this.ElapsedTime = ElapsedTime;
    }

    public double getElapsedTime() {
        return this.ElapsedTime;
    }

    public String toString() {
        String str = "TimeInState";
        str += "\n\tStateName: " + this.StateName;
        str += "\n\tCountInState: " + this.CountInState;
        str += "\n\tElapsedTime: " + this.ElapsedTime;
        return str;
    }
}
