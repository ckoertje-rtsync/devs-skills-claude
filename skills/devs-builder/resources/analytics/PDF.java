package com.ms4systems.devs.analytics;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Random;
import java.util.TreeSet;


public class PDF {// elements are symbols

	protected Hashtable<String, Double> h;
	protected int size;
	protected String name;

	public PDF() {
		this(true);
	}

	public PDF(boolean b) {
		this("PDF", b);
	}

	public PDF(String nameOfPDF, boolean b) {
		this.name = nameOfPDF;
		h = new Hashtable<String, Double>();
		size = 0;
	}

	public PDF(Hashtable<String, Double> hs) {
		h = hs;
		size = hs.size();
	}

	public Hashtable<String, Double> getHashtable() {
		return h;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public int size() {
		return size;
	}

	public double get(String element) {
		if (h.get(element) == null) {
			return Double.NaN;
		} else {
			return h.get(element);
		}
	}

	public synchronized void put(String element, double p) {
		if (p > 0) {
			h.put(element, p);
		} else {
			h.put(element, 0.);
		}
	}

	public HashSet<String> keySet() {
		return new HashSet<String>(h.keySet());
	}

	public HashSet<Double> valueSet() {
		return new HashSet<Double>(h.values());
	}

	public String toString() {
		return name + " " + h.toString();
	}

	public String toNoLabelString() {
		String s = "";
		for (Object key : h.keySet()) {
			Double p = (Double) h.get(key);
			s += p + " ";
		}
		return s;
	}

	public void print() {
		System.out.println(toString());
	}

	public void printNoLabel() {
		System.out.println(toNoLabelString());
	}

	public double getTotalOfFreqs() {
		double total = 0;
		for (Object k : keySet()) {
			total += h.get(k);
		}
		return total;
	}

	public void normalize() {
		double total = getTotalOfFreqs();
		if (total <= 0)
			return;
		for (Object k : keySet()) {
			h.put(k.toString(), h.get(k) / total);
		}
	}

	public static PDF makePDF(String[] elements, double[] probabilities) {
		PDF pdf = new PDF(true);
		for (int i = 0; i < elements.length; i++) {
			pdf.put(elements[i], probabilities[i]);
		}
		return pdf;
	}

	public static PDF makePDF(String nameOfPDF, String[] elements,
			double[] probabilities) {
		PDF pdf = new PDF(nameOfPDF, true);
		for (int i = 0; i < elements.length; i++) {
			pdf.put(elements[i], probabilities[i]);
		}
		return pdf;
	}

	public static PDF makeJointPDF(String[] elx, String[] ely,
			double[][] probabilities) {
		PDF pdf = new PDF();
		for (int i = 0; i < elx.length; i++) {
			for (int j = 0; j < ely.length; j++) {
				pdf.put(elx[i] + "_" + ely[j], probabilities[i][j]);
			}
		}
		return pdf;
	}

	public PDF getMarginalXPDF(String[] elx, String[] ely) {
		PDF pdf = new PDF();
		for (int i = 0; i < elx.length; i++) {
			double sumi = 0;
			for (int j = 0; j < ely.length; j++) {
				String el = elx[i] + "_" + ely[j];
				if (this.keySet().contains(el))
					sumi += this.get(el);
			}
			pdf.put(elx[i], sumi);
		}
		return pdf;
	}

	public PDF getMarginalYPDF(String[] elx, String[] ely) {
		PDF pdf = new PDF();
		for (int i = 0; i < ely.length; i++) {
			double sumi = 0;
			for (int j = 0; j < elx.length; j++) {
				String el = elx[j] + "_" + ely[i];
				if (this.keySet().contains(el))
					sumi += this.get(el);
			}
			pdf.put(ely[i], sumi);
		}
		return pdf;
	}

	public static PDF makeJointIndependent(PDF pdfx, PDF pdfy, String[] elx,
			String[] ely) {
		PDF pdf = new PDF();
		for (int i = 0; i < elx.length; i++) {
			for (int j = 0; j < ely.length; j++) {
				double x = pdfx.get(elx[i]);
				double y = pdfy.get(ely[j]);
				pdf.put(elx[i] + "_" + ely[j], x * y);
			}
		}
		return pdf;
	}

	public Bag getSamples(Random rand, int samples) {
		Bag dest = new Bag();
		for (int i = 0; i < samples; i++) {
			String x = ProbabilityChoice.makeSelectionFromPDF(rand, this);
			dest.add(x);
		}
		return dest;
	}

	public double vectorProduct(PDF other) {
		double total = 0;
		HashSet elements = new HashSet(this.keySet());
		HashSet others = other.keySet();
		elements.retainAll(others);
		for (Object element : elements) {
			double p = (double) this.get(element.toString())
					* (double) other.get(element.toString());
			total += p;
		}
		return total;
	}

	public double bernouliCombination() {
		double prod = 1;
		for (Object key : keySet()) {
			Double p = (Double) h.get(key);
			prod *= (1 - p);
		}
		return 1 - prod;
	}

	public PMF mapToPMF(Hashtable<String, Double> map) {
		PMF pmf = new PMF();
		for (Object key : keySet()) {
			Double p = (Double) h.get(key);
			pmf.put(map.get(key), p);
		}
		return pmf;
	}

	public PMF mapToPMF() {
		TreeSet ts = new TreeSet(this.keySet());
		PMF pmf = new PMF();
		double d = 0;
		for (Object key : ts) {
			Double p = (Double) h.get(key);
			pmf.put(d, p);
			d++;
		}
		return pmf;
	}

	public double sumSquareDifferences(PDF other) {
		double total = 0;
		HashSet elements = new HashSet(this.keySet());
		HashSet others = other.keySet();
		elements.retainAll(others);
		for (Object element : elements) {
			double p = (double) this.get(element.toString());
			double q = (double) other.get(element.toString());
			total += (p - q) * (p - q);
		}
		return total;
	}

	public Hashtable sortedDifferences(PDF other) {
		TreeSet<Double> ts = new TreeSet<Double>();
		Hashtable ht1 = new Hashtable();
		Hashtable ht2 = new Hashtable();
		HashSet elements = new HashSet(this.keySet());
		HashSet others = other.keySet();
		elements.retainAll(others);
		int inc = 0;
		for (Object element : elements) {
			double p = (double) this.get(element.toString());
			double q = (double) other.get(element.toString());
			ts.add(Math.abs(p - q) + .0000001 * inc);
			ht1.put(Math.abs(p - q) + .0000001 * inc, element);
			inc++;
		}
		
		double dif = ts.last();
		System.out.println(ht1.get(dif) + " " + dif);
		return ht2;
	}

	public double measureOfIndependence(String[] elx, String[] ely) {
		
		PDF pdfx = this.getMarginalXPDF(elx, ely);
		PDF pdfy = this.getMarginalYPDF(elx, ely);
		PDF pdfInd = makeJointIndependent(pdfx, pdfy, elx, ely);
		double dif = this.sumSquareDifferences(pdfInd);

		return dif;
	}

	public void confidenceOfIndependence(String[] elx, String[] ely,
			double dif, int samples) {
		int ntrials = 10000;
		int count = 0;
		double total = 0;
		for (int i = 0; i < ntrials; i++) {
			Bag b = this.getSamples(new Random(), samples);
			PDF pdfrand = b.getProbDistribution();
			double squdif = pdfrand.measureOfIndependence(elx, ely);
			total += squdif;
			if (squdif >= dif)
				count++;
		}
		System.out.println("Prob>observedDif " + count / (double) ntrials);
		System.out.println("avg square dif " + total / (double) ntrials);
	}

	public static void main(String[] args) {
		PDF pdf = makeJointPDF(new String[] { "a", "b" }, new String[] { "a",
				"b" }, new double[][] { { 0.25, 0.25 }, { 0.25, .25 } });

		pdf.confidenceOfIndependence(new String[] { "a", "b" }, new String[] {
				"a", "b" }, 0.1, 5);
	}

}
