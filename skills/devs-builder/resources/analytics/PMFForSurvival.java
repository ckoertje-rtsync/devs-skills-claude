package com.ms4systems.devs.analytics;

import java.util.Random;
import java.util.Set;
import java.util.TreeSet;




public class PMFForSurvival extends PMF{

	 double binsize = 1;
	 
	public PMFForSurvival() {
		super();
	}

		
		public void setBinsize(double b){
			binsize = b;
		}
		public PMFForSurvival(double binsize){
			this();
			setBinsize(binsize);
		}
		public static double placeInBin(double binsize,double x){
			return binsize*Math.ceil(x/binsize);
		}
		
		
		
		public  void putBinSample(double binsize,double x){
			setBinsize(binsize);
			double binval = placeInBin(binsize,x);
			Double freq = h.get(binval);
			if (freq == null){
				h.put(binval, x);
			}
			else {
				h.put(binval, freq+x);
			}
		}
		public  void putBinSample(double x){
			putBinSample( binsize, x);
		}
		
		public  PMF makeHazzardFn(double binsize){
			PMF pmf = new PMF();
			

			double remainingPopulation = 0;
			for (double i = 1;i<= getHighestNonZero()/binsize;i++){
				double bini = binsize*i;
				if (h.get(bini) == null)h.put(bini, 0.);
				remainingPopulation += h.get(bini);
			}
			for (double j = 1;j<= getHighestNonZero()/binsize;j++){
				double binj = binsize*j;
				if (h.get(binj) != null){
				pmf.put(binj,h.get(binj)/remainingPopulation);
				remainingPopulation -=h.get(binj);
				}
			}
			return pmf;
		}
		
		public  PMF makeHazzardFn(){
			return makeHazzardFn(binsize);
		}
		
		public  PMF makeHistogram(double binsize){
			PMF pmf = new PMF();
			
			for (double i = 1;i<= getHighestNonZero()/binsize;i++){
				double bini = binsize*i;
				if (h.get(bini) == null)h.put(bini, 0.);
				double numInBini = Math.ceil(h.get(bini)/bini);
				pmf.put(bini,numInBini);
			}
			return pmf; 
		}
		public  PMF makeHistogram(){
			return makeHistogram(binsize);
		}
		
		public PMF makeProbabilityDistFromHistogram(double binsize){
			PMF pmf = makeHistogram( binsize);
			pmf.normalize();
			return pmf;
		}
		
		public double getCummulativeProbOfXLessThan(double x){
			double sum = 0;
			PMF pmf = makeProbabilityDistFromHistogram(binsize);
			TreeSet ts = new TreeSet(pmf.keySet());
			for (Object o : ts) {
				double d = pmf.get((Double) o);
                sum += d;
                if (d >= x)
				return  sum;
			}
			return 1.0;
		}
		
		public double getQuantile(double x){
			double sum = 0;
			PMF pmf = makeProbabilityDistFromHistogram(binsize);
			TreeSet ts = new TreeSet(pmf.keySet());
			for (Object o : ts) {
				double d = pmf.get((Double) o);
                sum += d;
                if (sum >= x)
				return  (Double)o;
			}
			return 1.0;
		}
		
		public static PMF computeWeibull(double lambda,  double k,double deltx) {
			PMF pmf = new PMF();
			for (double x = 0; x <= 3; x = x + deltx) {
				double f = (k/lambda)*Math.pow(x/lambda, k-1)*Math.exp(-Math.pow(x/lambda, k));
				pmf.put(x,f);
			}
			return pmf;
		}
				
		public static PMF generateWeibull(double lambda,  double k,int samples) {
			PMF pmf = new PMF();
			double deltax = 3.0*lambda/100;
			Random rand = new Random(299931);
			for (int i = 0; i < samples; i++) {
			for (double x = 0; x <= (3.0*lambda); x = x + deltax) {
				double rate = deltax*(k/lambda)*Math.pow(x/lambda, k-1);
				if (rand.nextDouble()<rate){
				  pmf.putSample(x);
				  break;
			}
			if (x>=3.0*lambda)pmf.putSample(3.0*lambda);
			}
			}
			return pmf;
		}
		public static void testSurvival() {

			PMFForSurvival pmf = new PMFForSurvival(2);
			pmf.putBinSample(3.1);
			pmf.putBinSample(3.2);
			pmf.putBinSample(3.2);
			pmf.putBinSample(2.2);
			pmf.putBinSample(1.2);
			pmf.putBinSample(5.2);
			pmf.putBinSample(15.4);
			pmf.putBinSample(15.9);
			pmf.print();
			System.out.println();
			PMF Hazpmf = pmf.makeHazzardFn();
			Hazpmf.print();
			System.out.println();
			PMF Histpmf = pmf.makeHistogram();
			Histpmf.print();
		}
		
		public static void testWeibull() {
		PMF weibpmf = generateWeibull(15,1,10000);
		System.out.println(weibpmf.getMean());
		System.out.println();
		weibpmf.printKeys();
		System.out.println();
		
		PMFForSurvival pmfForS = new PMFForSurvival(.3);
		Set<Double> keys = weibpmf.getHashtable().keySet();
		for (Double k : keys) {
			pmfForS.putBinSample(k);
		}
		PMF Histpmf = pmfForS.makeHistogram();
		Histpmf.print();
		System.out.println();
		PMF Hazzpmf = pmfForS.makeHazzardFn();
		Hazzpmf.print();
		}

		public static void main(String[] args) {
		//	testSurvival();
           testWeibull();
		}

	}
