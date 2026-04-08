package Models.java;

import java.io.Serializable;

import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.message.Port;
import com.ms4systems.devs.core.message.impl.MessageBagImpl;
import com.ms4systems.devs.core.model.impl.AtomicModelImpl;
import com.ms4systems.devs.core.simulation.Simulation;
import com.ms4systems.devs.core.simulation.impl.SimulationImpl;

public class Genr extends AtomicModelImpl {
	private static final long serialVersionUID = 1L;
	
	public final Port<Serializable> start = addInputPort("start");
	public final Port<Serializable> stop = addInputPort("stop");
	public final Port<Job> out = addOutputPort("out",Job.class);
	
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
		MessageBag output = new MessageBagImpl();
		output.add(out.createMessage(new Job(count)));
		return output;
	}
	
	@Override
	public Double getTimeAdvance() {
		return sigma;
	}
    public static void main(String[] args) {
            Genr model = new Genr("Genr",10.);


            Simulation sim =
                new SimulationImpl("Genr Simulation", model);
            sim.startSimulation(0);
            sim.simulateIterations(Long.MAX_VALUE);
    }    
    }