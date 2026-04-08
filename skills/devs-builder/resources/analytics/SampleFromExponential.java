package  com.ms4systems.devs.analytics;



public class SampleFromExponential extends SampleFromDistribution{

		double mean = 1;

		public SampleFromExponential(double mean) {
			this.mean = mean;
		}

		public SampleFromExponential() {
			this(1);
		}

		public double getSample() {
			return -mean * Math.log(rand.nextDouble());
		}
	
		public double getMean(){
			return mean;
		}

	public static void main(String[] args) {
		for (
		int numComponents = 1;numComponents <100;numComponents++){
		int numSamples = 1000;

		PMFForSurvival pmfMax= new PMFForSurvival();
		pmfMax.setBinsize(.01);
		PMFForSurvival pmfMin= new PMFForSurvival();
		pmfMin.setBinsize(.01);

		for (int j=0;j<numSamples;j++){
			double max = -1;
			double min = Double.MAX_VALUE;
		for (int i=0;i<numComponents;i++){
		SampleFromExponential x = new SampleFromExponential(1);
		   double val = x.getSample();
		   if (val >max)max=val;
		   if (val <min)min=val;
		}
		pmfMax.putBinSample(max);
		pmfMin.putBinSample(min);
		}
		System.out.println(pmfMax.getMean()+" "+pmfMax.getLowestNonZero()+" "+pmfMax.getHighestNonZero());	
		}
	}
}
