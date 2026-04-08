package com.ms4systems.devs.analytics;

public class SampleFromPareto extends SampleFromDistribution{

		double alpha = 1;

		public SampleFromPareto(double alpha) {
			this.alpha = alpha;
		}

		public SampleFromPareto() {
			this(1);
		}

		public double getSample() {
			return 1/Math.pow(rand.nextDouble(), 1/alpha);
		}
		public double getMean(){
			return alpha;
		}

}
