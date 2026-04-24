
package Models.java;



import java.io.Serializable;

import com.ms4systems.devs.core.message.impl.MessageBag;
import com.ms4systems.devs.core.message.impl.Port;
import com.ms4systems.devs.core.message.impl.MessageBag;
import com.ms4systems.devs.core.model.impl.Atomic;
//
//

public class Genr extends Atomic {
	private static final long serialVersionUID = 1L;
	
	public final Port start = addInputPort("start");
	public final Port stop = addInputPort("stop");
	public final Port out = addOutputPort("out",Job.class);
	
	private double sigma;
	private double period;
	private int count;
	
	public Genr(String name, double intArrTime) {
		super(name);
		period = intArrTime;
	}
	
	@Override
	public void initialize() {
		sigma = period;
		count = 0;
		super.initialize();
	}
	
	@Override
	public void internalTransition() {
		count++;
		sigma = period;
	}
	
	@Override
	public void externalTransition(double timeElapsed, MessageBag input) {
		sigma -= timeElapsed;
		if (!input.getMessages(start).isEmpty()) 
			sigma = period;
		if (!input.getMessages(stop).isEmpty())
			sigma = Double.POSITIVE_INFINITY;
	}

	@Override
	public MessageBag getOutput() {
		MessageBag output = new MessageBag();
		output.add(out.createMessage(new Job(count)));
		return output;
	}
	
	@Override
	public Double getTimeAdvance() {
		return sigma;
	}
    public static void main(String[] args) {
            Genr model = new Genr("Genr",10.);


            /* =
                new Simulation("Genr Simulation", model);
            sim.startSimulation(0);
            */ //*/ //*/ //sim.simulateIterations(Long.MAX_VALUE);
    }    
    }
