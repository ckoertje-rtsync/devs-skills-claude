package com.ms4systems.devs.analytics;

import java.util.ArrayList;
import java.util.Random;

public class ProbabilityChoice {

	public ProbabilityChoice() {

	}

	public static int makeSelectionFrom2(Random Rand, double p1) {
		double choice = Rand.nextDouble();
		if (choice < p1) {
			return 1;
		} else {
			return 2;
		}
	}

	public static int makeSelectionFrom3(Random Rand, double p1, double p2) {
		double choice = Rand.nextDouble();
		if (choice < p1) {
			return 1;
		} else if (choice < p1 + p2) {
			return 2;
		} else {
			return 3;
		}
	}

	public static int makeSelectionFrom(Random Rand, double[] p) {
		double choice = Rand.nextDouble();
		double sum = 0;
		for (int i = 0; i < p.length; i++) {
			sum += p[i];
			if (choice < sum) {
				return i + 1;
			}
		}
		return p.length + 1;
	}
	
	public static String selectPhase(Random Rand, double[] p,String[] phases) {
		double choice = Rand.nextDouble();
		double sum = 0;
		for (int i = 0; i < p.length; i++) {
			sum += p[i];
			if (choice < sum) {
				return phases[i];
			}
		}
		return phases[p.length-1] ;
	}

	public static String selectPhase(Random Rand, ArrayList<Double> p,ArrayList<String> phases) {
		double choice = Rand.nextDouble();
		double sum = 0;
		for (int i = 0; i < p.size(); i++) {
			sum += p.get(i);
			if (choice < sum) {
				return phases.get(i);
			}
		}
		return phases.get(phases.size()-1) ;
	}
	public static double makeSelectionFromPMF(Random Rand, PMF pmf) {
		double choice = Rand.nextDouble();
		double sum = 0;
		for (Double k : pmf.keySet()) {
			sum += (Double) pmf.get(k);
			if (choice < sum) {
				return k;
			}
		}
		return 0;
	}

	public static String makeSelectionFromPDF(Random Rand, PDF pdf) {
		double choice = Rand.nextDouble();
		double sum = 0;
		for (String k : pdf.keySet()) {
			sum += (Double) pdf.get(k);
			if (choice < sum) {
				return k;
			}
		}
		return "";
	}

	public static boolean lessThan(double choice, double p) {
		return choice < p;
	}

	public static void mainStub() {
	}

	// ENDID

	public static void main(String[] args) {

		PMF pmf = PMF.computeNormal(2, 10, .01);
		Random r = new Random();
		PMF dest = pmf.getSamples(r, 1000);
		System.out.println(dest.getMean() + " " + dest.getStd());
	}

}
