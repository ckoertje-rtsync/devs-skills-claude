package  com.ms4systems.devs.analytics;
public class SampleFromUniform extends SampleFromDistribution {

			double Lower = 0;
			double Upper = 1;

			
			public SampleFromUniform(double Lower, double Upper) {
				this.Lower = Lower;
				this.Upper = Upper;
			}

			public SampleFromUniform() {
				this(0, 1);
			}

			public double getSample() {
				return  Lower + (Upper-Lower)*rand.nextDouble();
			}
			public double getMean(){
				double mean = (Upper+Lower)/2;
				return mean;
			}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
