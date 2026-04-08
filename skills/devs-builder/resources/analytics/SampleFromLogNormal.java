package  com.ms4systems.devs.analytics;
public class SampleFromLogNormal extends SampleFromDistribution {

			double mean = 1;
			double sigma = 1;

			
			public SampleFromLogNormal(double mean, double sigma) {
				this.mean = mean;
				this.sigma = sigma;
			}

			public SampleFromLogNormal() {
				this(1, 1);
			}
			public SampleFromLogNormal(double coef) {
				this(0, Math.sqrt(Math.log(coef+1)));
			}
			public double getSample() {
				return Math.exp( mean + sigma*rand.nextGaussian());
			}
			public double getMean(){
				return mean;
			}
			public double computeCoeff(){
				return Math.exp(sigma*sigma)-1;
			}
			public double computeSigma(double coef){
				return Math.sqrt(Math.log(coef+1));
			}
			public static void main(String[] args) {
				SampleFromLogNormal x = new SampleFromLogNormal(0,3);
					System.out.println(x.computeCoeff());
					System.out.println(x.computeSigma(x.computeCoeff()));
				}

}
