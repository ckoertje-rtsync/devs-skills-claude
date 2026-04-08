package  com.ms4systems.devs.analytics;
public class SampleFromNormal extends SampleFromDistribution {

			double mean = 1;
			double sigma = 1;

			
			public SampleFromNormal(double mean, double sigma) {
				this.mean = mean;
				this.sigma = sigma;
			}

			public SampleFromNormal() {
				this(1, 1);
			}

			public double getSample() {
				return  mean + sigma*rand.nextGaussian();
			}
			
			public double getMean(){
				return mean;
			}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
