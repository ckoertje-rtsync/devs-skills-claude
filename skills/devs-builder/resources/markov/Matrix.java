package com.ms4systems.devs.markov;

import java.util.TreeSet;

public class Matrix {
	double[][] m;
	int size;

	public Matrix(int Size) {
		m = new double[Size][Size];
		size = Size;
	}

	public int getSize() {
		return size;
	}

	public void setColumn(int i, double[] column) {
		for (int j = 0; j < column.length; j++) {
			m[j][i] = column[j];
		}
	}

	public double[] matrixMultiply(double[] x) {
		double[] y = new double[x.length];
		for (int i = 0; i < x.length; i++) {
			double yv = 0.;
			for (int j = 0; j < x.length; j++) {
				yv += m[i][j] * x[j];
			}
			y[i] = yv;
		}
		return y;
	}

	public Matrix Plus(Matrix p) {
		Matrix res = new Matrix(size);
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				res.m[i][j] = p.m[i][j] + m[i][j];
			}
		}
		return res;
	}
	public boolean equals(Matrix p) {
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if( p.m[i][j] != m[i][j])return false;
			}
		}
		return true;
	}
	public static boolean equalsVector(double[]  p,double[] q) {
		for (int i = 0; i < p.length; i++) {
				if( p[i] != q[i])return false;
			}
		return true;
	}
	
	public void print() {
		for (int i = 0; i < size; i++) {

			System.out.println();
			for (int j = 0; j < size; j++) {
				System.out.print(m[i][j] + " ");
			}
		}
	}

	public boolean irreducible() {
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (!(m[i][j] > 0.0))
					return false;
			}
		}
		return true;
	}

	public double[] getColumn(int i) {
		double[] p = new double[size] ;
		for (int j = 0; j < size; j++) {
			 p[j]= m[j][i];
		}
		return p;
	}
	
	public boolean columnSumsToOne(int i) {
		double sum = 0;
		for (int j = 0; j < size; j++) {
			sum += m[j][i];
		}
		return sum == 1.;
	}
	public boolean matrixIsValid() {
		for (int i = 0; i < size; i++) {
			if (!columnSumsToOne(i))
				return false;
		}
		return true;
	}

	public boolean columnHasANonZero(int i) {
		for (int j = 0; j < size; j++) {
			if (m[j][i] > 0)
				return true;
		}
		return false;
	}

	public boolean everyColumnHasANonZero() {
		for (int i = 0; i < size; i++) {
			if (!columnHasANonZero(i))
				return false;
		}
		return true;
	}

	public boolean rowHasANonZero(int i) {
		for (int j = 0; j < size; j++) {
			if (m[i][j] > 0)
				return true;
		}
		return false;
	}

	public boolean everyRowHasANonZero() {
		for (int i = 0; i < size; i++) {
			if (!rowHasANonZero(i))
				return false;
		}
		return true;
	}

	public int blockInto(int j, int lumpedSize) {
		return (int) Math.floor((j + 0.) / lumpedSize);
	}

	public TreeSet<Integer> indicesInBlock(int b, int lumpedSize) {
		TreeSet<Integer> inds = new TreeSet<Integer>();
		for (int i = 0; i < size; i++) {
			if (blockInto(i, lumpedSize) == b)
				inds.add(i);
		}
		return inds;
	}

	public Matrix lump(int lumpedSize) {
		int blocksize = (int) Math.floor(size / lumpedSize);
		Matrix lum = new Matrix(lumpedSize);
		for (int blocki = 0; blocki < lumpedSize; blocki++) {
			for (int blockj = 0; blockj < lumpedSize; blockj++) {

				double sumij = 0;
				TreeSet<Integer> ids = indicesInBlock(blocki, blocksize);
				for (int i : ids) {
					TreeSet<Integer> jds = indicesInBlock(blockj, blocksize);
					for (int j : jds) {
						sumij += m[i][j];
					}
				}
				lum.m[blocki][blockj] = sumij / blocksize;
			}
		}
		lum.print();
		System.out.println("Valid matrix " + lum.matrixIsValid());
		return lum;
	}

	public double[] lumpStateVector(double[] statevector, int lumpedSize) {
		double[] lumpvect = new double[lumpedSize];
		int blocksize = (int) Math.floor(size / lumpedSize);
		for (int blockj = 0; blockj < lumpedSize; blockj++) {
			double sumj = 0;
			TreeSet<Integer> jds = indicesInBlock(blockj, blocksize);
			for (int j : jds) {
				sumj += statevector[j];
			}
			lumpvect[blockj] = sumj;
		}
		return lumpvect;
	}

	public void writeXML(String fname) {
		String s = "";
		// s+="<?xml version="1.0" encoding="UTF-8"?>";
		s += "<ProbDEVS>";
		for (int i = 0; i < size; i++) {
			s += "\r\n";
			for (int j = 0; j < size; j++) {
				s += "\r\n";
				s += "<TransitionInfo>";
				s += "\r\n";
				s += "<StartState>" + "s" + i + "</StartState>";
				s += "\r\n";
				s += "<EndState>" + "s" + j + "</EndState>";
				s += "\r\n";
				s += "<ProbValue>" + m[j][i] + "</ProbValue>";
				s += "\r\n";
				s += "</TransitionInfo>";
			}
			s += "\r\n";

		}
		s += "</ProbDEVS>";

	}

	public void writeXML() {
		String s = "";
		s += "<ProbDEVS>";
		for (int i = 0; i < size; i++) {
			s += "\r\n";
			for (int j = 0; j < size; j++) {
				s += "\r\n";
				s += "<TransitionInfo>";
				s += "\r\n";
				s += "<StartState>" + "s" + i + "</StartState>";
				s += "\r\n";
				s += "<EndState>" + "s" + j + "</EndState>";
				s += "\r\n";
				s += "<ProbValue>" + m[j][i] + "</ProbValue>";
				s += "\r\n";
				s += "</TransitionInfo>";
			}
			s += "\r\n";

		}
		s += "</ProbDEVS>";
		System.out.println(s);
	}

	public static void main(String[] args) {
		Matrix m = new Matrix(6);
		m.setColumn(0, new double[] { 0, 1, 2, 3, 4, 6 });
		m.setColumn(1, new double[] { 0, 1, 2, 3, 4, 6 });
		m.setColumn(2, new double[] { 0, 1, 2, 3, 4, 6 });
		m.setColumn(3, new double[] { 0, 1, 2, 3, 4, 6 });
		m.setColumn(4, new double[] { 0, 1, 2, 3, 4, 6 });
		m.setColumn(5, new double[] { 0, 1, 2, 3, 4, 6 });
		Matrix l = m.lump(3);
		double statev[] = new double[] { 0, 1, 2, 3, 4, 6 };
		statev = m.lumpStateVector(statev, 3);
		statev = statev;
	}

	public double[][] getM() {
		return m;
	}

	public void setM(double[][] m) {
		this.m = m;
	}

	public void setSize(int size) {
		this.size = size;
	}
	
}
