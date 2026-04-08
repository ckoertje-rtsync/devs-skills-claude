package com.ms4systems.devs.markov;

public class TimeInfo {
	private String type;
	private double lower;
	private double upper;
	private double mean;
	private double sigma;
	private double alpha;
	
	public TimeInfo() {
		type = "None";
		lower=0;
		upper=1;
		mean=1;
		sigma=1;
		alpha=1;
	}
	public TimeInfo(String type, double lower, double upper, double mean, double sigma, double alpha){
		this.type = type;
		this.lower = lower;
		this.upper = upper;
		this.mean = mean;
		this.sigma = sigma;
		this.alpha = alpha;		
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public double getLower() {
		return lower;
	}
	public void setLower(double lower) {
		this.lower = lower;
	}
	public double getUpper() {
		return upper;
	}
	public void setUpper(double upper) {
		this.upper = upper;
	}
	public double getMean() {
		return mean;
	}
	public void setMean(double mean) {
		this.mean = mean;
	}
	public double getSigma() {
		return sigma;
	}
	public void setSigma(double sigma) {
		this.sigma = sigma;
	}
	public double getAlpha() {
		return alpha;
	}
	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

}
