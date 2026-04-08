package com.ms4systems.devs.analytics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Random;
import java.util.Set;

public class Bag {
	protected Hashtable<Object, Integer> h;
	protected String name;

	public Bag() {
		this("Bag");
	}

	public Bag(String name) {
		this.name = name;
		h = new Hashtable<Object, Integer>();
	}

	public Bag(boolean b) {
		h = new Hashtable<Object, Integer>();
	}

	public void add(Object s) {
		if (h.containsKey(s)) {
			int size = h.get(s).intValue();
			h.put(s, new Integer(size + 1));
		} else {
			h.put(s, new Integer(1));
		}
	}

	public void add0(Object s) {
		if (!h.containsKey(s)) {
			h.put(s, new Integer(0));
		}
	}

	public int numberOf(Object s) {
		if (h.containsKey(s)) {
			int size = h.get(s).intValue();
			return size;
		} else {
			return 0;
		}
	}

	public int size() {
		int sum = 0;
		for (Object k : keySet()) {
			sum += numberOf(k);
		}
		return sum;
	}

	public Set keySet() {
		return h.keySet();

	}

	public String toString() {
		return h.toString();
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

	public PDF getProbDistribution() {
		PDF p = new PDF();
		int size = size();
		for (Object k : keySet()) {
			double prob = numberOf(k) / (double) size;
			p.put((String) k, prob);
		}
		return p;
	}

	public PMF getProbMassDistribution() {
		PMF p = new PMF();
		int size = size();
		for (Object k : keySet()) {
			double prob = numberOf(k) / (double) size;

			p.put((Double) ((Integer) k * 1.0), prob);
		}
		return p;
	}

	public static Bag pdfToBag(PDF pdf, int numDecPts) {
		Bag b = new Bag(true);
		double max = 0;
		for (Double o : pdf.valueSet()) {
			double v = Double.valueOf(o);
			if (v > max) {
				max = v;
			}
		}
		HashSet sigElems = new HashSet();
		for (String k : pdf.keySet()) {
			Double o = pdf.get(k);
			double v = Double.valueOf(o);
			double div = v / max;
			if (div > Math.pow(10, -numDecPts)) {
				sigElems.add(k);
			} else {
				b.add0(k);
			}
		}
		double min = Double.MAX_VALUE;
		for (Object k : sigElems) {
			Double o = pdf.get(k.toString());
			double v = Double.valueOf(o);
			if (v < min) {
				min = v;
			}
		}
		for (Object k : sigElems) {
			Double o = pdf.get(k.toString());
			double v = Double.valueOf(o);
			int num = (int) Math.round(v / min);

			for (int i = 0; i < num; i++) {
				b.add(k);
			}
		}
		return b;
	}

	public static Bag pdfToBag(PDF pdf) {
		return pdfToBag(pdf, 1);
	}

	public static Bag computeDistribution(ArrayList<Double> al, double cellSize) {
		Bag b = new Bag();
		for (Double d : al) {
			double cell = (double) Math.floor(d / cellSize);
			b.add(cellSize * cell);
		}
		return b;
	}

	public static void main(String[] args) {
		Random r = new Random();
		ArrayList<Double> al = new ArrayList<Double>();
		for (int i = 0; i < 10000; i++) {
			double d = r.nextDouble();
			al.add(d * 100);
		}
		Bag b = computeDistribution(al, 10);
		PMF pmf = b.getProbMassDistribution();
		pmf.print();
	}

}
