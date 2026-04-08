package com.ms4systems.devs.analytics;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Random;


public class PMF {// probability mass function: elements are real values
	protected Hashtable<Double, Double> h;
	// elements = h.keySet. freqs = h.values()
	protected String name;

	public PMF() {
		this(true);
	}

	public PMF(boolean b) {
		this("PMF", b);
	}

	public PMF(String nameOfPDF, boolean b) {
		this.name = nameOfPDF;
		h = new Hashtable<Double, Double>();
	}

	public PMF(Hashtable<Double, Double> hs) {
		h = hs;
		// size = hs.size();
	}

	public Hashtable<Double, Double> getHashtable() {
		return h;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isEmpty() {
		return h.isEmpty();
	}

	public int size() {
		return h.size();
	}

	public double get(Double element) {
		if (h.get(element) == null) {
			return Double.NaN;
		} else {
			return h.get(element);
		}
	}

	public synchronized void put(Double element, double p) {
		if (p > 0) {
			h.put(element, p);
		} else {
			h.put(element, 0.);
		}
	}

	public synchronized void putSample(double element) {
		int size = h.size();
		h.put(element, 1.);
		if (h.size() > size)
			return;
		else {
			Double newVal = element;
			while (h.size() == size) {
				newVal += .000000001;

				h.put(newVal, 1.);
			}
		}
	}

	public HashSet<Double> keySet() {
		return new HashSet<Double>(h.keySet());
	}

	public HashSet<Double> valueSet() {
		return new HashSet<Double>(h.values());
	}

	public String toString() {
		TreeSet<Double> ts = new TreeSet<Double>();
		Set<Double> keys = h.keySet();
		for (Double k : keys) {
			ts.add(k);
		}
		String s = "" + getTotalOfFreqs() + " " + this.getMean();
		return s;
	}

	public void print() {
		TreeSet<Double> ts = new TreeSet<Double>();
		Set<Double> keys = h.keySet();
		for (Double k : keys) {
			ts.add(k);
		}
		for (Double d : ts) {
			System.out.println(d + " " + h.get(d));
		}
	}

	public void printKeys() {
		TreeSet<Double> ts = new TreeSet<Double>();
		Set<Double> keys = h.keySet();
		for (Double k : keys) {
			ts.add(k);
		}
		for (Double d : ts) {
			System.out.println(d);
		}
	}

	public static PMF makePMF(double[] elements,
			double[] probabilities) {
		PMF pmf = new PMF(true);
		for (int i = 0; i < elements.length; i++) {
			pmf.put(elements[i], probabilities[i]);
		}
		return pmf;
	}

	public static PMF makePMF(String nameOfPDF,
			double[] elements, double[] probabilities) {
		PMF pmf = new PMF(
				nameOfPDF, true);
		for (int i = 0; i < elements.length; i++) {
			pmf.put(elements[i], probabilities[i]);
		}
		return pmf;
	}

	public double vectorProduct(PMF other) {
		double total = 0;
		HashSet elements = new HashSet(this.keySet());
		HashSet others = other.keySet();
		elements.retainAll(others);
		for (Object element : elements) {
			double p = (double) this.get((Double) element)
					* (double) other.get((Double) element);
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

	public double getTotalOfFreqs() {
		double total = 0;
		for (Double k : keySet()) {
			total += h.get(k);
		}
		return total;
	}

	public double getFreqWeightedSum() {
		double sum = 0;
		for (Double k : keySet()) {
			sum += k * h.get(k);
		}
		return sum;
	}

	public void normalize() {
		double total = getTotalOfFreqs();
		if (total <= 0)
			return;
		for (Double k : keySet()) {
			h.put(k, h.get(k) / total);
		}
	}

	public double getMean() {
		double total = getTotalOfFreqs();
		double sum = getFreqWeightedSum();
		return sum / total;
	}

	public double getStd() {
		double mean = getMean();
		double sum = 0;
		for (Double k : keySet()) {
			double delta = k - mean;
			sum += h.get(k) * delta * delta;
		}
		return Math.sqrt(sum / getTotalOfFreqs());
	}

	public double getHighestNonZero() {
		TreeSet ts = new TreeSet(keySet());
		Iterator it = ts.descendingIterator();
		while (it.hasNext()) {
			double d = ((Double) it.next()).doubleValue();
			double dd = get(d);
			if (dd == 0)
				continue;
			return d;
		}
		return 0;
	}

	public double getLowestNonZero() {
		TreeSet ts = new TreeSet(keySet());
		for (Object o : ts) {
			double d = get((Double) o);
			if (d == 0)
				continue;
			return (Double) o;
		}
		return 0;
	}

	public static PMF computeNormal(double mean,
			double sigma, double deltx) {
		PMF pmf = new PMF();
		double sump = 0;
		double sumplnp = 0;
		for (double x = -3 * sigma; x <= 3 * sigma; x = x + deltx) {
			double sigrtpi = sigma * Math.sqrt(2 * Math.PI);
			double exphalf = Math.pow(Math.E, -0.5 * (x / sigma) * (x / sigma));
			double p = deltx * exphalf / sigrtpi;
			pmf.put(x + mean, p);
		}
		return pmf;
	}

	public PMF getSamples(Random rand, int samples) {
		PMF dest = new PMF();

		for (int i = 0; i < samples; i++) {
			double x = 0;
			double choice = rand.nextDouble();
			double sum = 0;
			for (Double k : this.keySet()) {
				sum += (Double) this.get(k);
				if (choice < sum) {
					x = k;
					break;
				}
			}
			dest.putSample(x);
		}
		return dest;
	}

	public static void main(String[] args) {
		PMF pmf = computeNormal(2, 10, .01);

		double x = ProbabilityChoice.makeSelectionFromPMF(
				new java.util.Random(), pmf);
		x = x;
	}

}
