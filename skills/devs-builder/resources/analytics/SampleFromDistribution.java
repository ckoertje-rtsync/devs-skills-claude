package  com.ms4systems.devs.analytics;
import java.util.ArrayList;
import java.util.Random;

public class SampleFromDistribution {

	public Random rand = new Random();

	public SampleFromDistribution() {
		rand = new Random();
	}
	public double getMean(){
		return 1.0;
	}
	public double getSample(double lambda) {// default is uniform
		double sample = rand.nextDouble();
        return -(1 / lambda) * Math.log(sample);
	}
	public double getSample() {// default is uniform
		
		return rand.nextDouble();
	}
	public double[] getMeanNVariance(int numSamples) {
	
		ArrayList<Double> pmf = new ArrayList<Double>();
		for (int j = 0; j < numSamples; j++) {
			pmf.add( getSample());
		}


		double avg = 0;
		for (Double el : pmf) {
			avg += el;
		}
		avg = avg / pmf.size();
		
		double var = 0;
		for (Double el : pmf) {
			double delta = el - avg;
			var += delta * delta;
		}
		var = var / pmf.size();
        double coeff = var/(avg*avg);
		//
		System.out.println(" avg sum, var, coeff of var " + numSamples + " " + avg + " "
				+ var + " "+(var/(avg*avg)));
		return new double[] { avg, var };
	}

	public static void main(String[] args) {
		SampleFromPareto x = new SampleFromPareto(1);
		System.out.println(x.getSample());
		x.getMeanNVariance(10000);
	}

}
