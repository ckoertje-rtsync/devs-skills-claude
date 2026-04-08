package Models.java;

import com.ms4systems.devs.core.message.Message;
import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.message.Port;
import com.ms4systems.devs.core.message.impl.MessageBagImpl;
import com.ms4systems.devs.core.model.impl.AtomicModelImpl;

public class Proc extends AtomicModelImpl {
	private static final long serialVersionUID = 1L;
	
	public final Port<Job> in = addInputPort("in", Job.class);
	public final Port<Job> out = addOutputPort("out", Job.class);
	
	private Job val;
	private double processingTime;
	private double sigma;
	
	public Proc(String name, double processingTime) {
		super(name);
		this.processingTime = processingTime;
	}
	
	@Override
	public void initialize() {
		val = null;
		sigma = Double.POSITIVE_INFINITY;
		super.initialize();
	}
	@Override
	public void internalTransition() {
		val = null;
		sigma = Double.POSITIVE_INFINITY;
	}
	
	@Override
	public void externalTransition(double timeElapsed, MessageBag input) {
		if (sigma < Double.POSITIVE_INFINITY)
			sigma -= timeElapsed; 
		else 
			for (Message<?> msg : input.getMessages(in)) { 
				val = (Job) msg.getData();
				sigma = processingTime;
			}
	}
	
	@Override
	public MessageBag getOutput() {
		MessageBag output = new MessageBagImpl();
		output.add(out.createMessage(val));
		return output;
	}
	
	@Override
	public Double getTimeAdvance() {
		return sigma;
	}
}
