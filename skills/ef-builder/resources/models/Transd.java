package Models.java;

import java.util.HashMap;
import java.util.Map;

import com.ms4systems.devs.core.message.Message;
import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.message.Port;
import com.ms4systems.devs.core.message.impl.MessageBagImpl;
import com.ms4systems.devs.core.model.impl.AtomicModelImpl;

public class Transd extends AtomicModelImpl {
	private static final long serialVersionUID = 1L;
	
	public final Port<Job> ariv = addInputPort("ariv",Job.class);
	public final Port<Job> solved = addInputPort("solved",Job.class);
	public final Port<Job> out = addOutputPort("out",Job.class);
	
	private double sigma;
	private double observationTime;
	private double totalTa;
	private double clock;
	private Map<String,Job> jobsArrived;
	private Map<String,Job> jobsSolved;
	
	
	public Transd(String name, double observationTime) {
		super(name);
		this.observationTime = observationTime;
	}
	
	@Override
	public void initialize() {
		sigma = observationTime;
		totalTa = 0;
		clock = 0;
		jobsArrived = new HashMap<String, Job>();
		jobsSolved = new HashMap<String, Job>();
		super.initialize();
	}
	
	@Override
	public void internalTransition() {
		clock = clock + sigma;
		double throughput;
		double avg_ta_time;
		if(!jobsSolved.isEmpty()) {
			avg_ta_time = totalTa / jobsSolved.size();
			if (clock > 0.0) throughput = jobsSolved.size() / clock;
			else throughput = 0.0;
		}
		else {
			avg_ta_time = 0.0;
			throughput = 0.0;
		}
		System.out.println("End time: " + clock);
		System.out.println("jobs arrived : " + jobsArrived.size ());
		System.out.println("jobs solved : " + jobsSolved.size());
		System.out.println("AVERAGE TA = " + avg_ta_time);
		System.out.println("THROUGHPUT = " + throughput);
		
		sigma = Double.POSITIVE_INFINITY;
	}
	
	@Override
	public void externalTransition(double timeElapsed, MessageBag input) {
		clock = clock + timeElapsed;
		sigma -= timeElapsed;
		
		for (Message<Job> msg : ariv.getMessages(input)) {
			final Job job = msg.getData();
			job.setTime(clock);
			System.out.println("Start job " + job.getName() + " @ t = " + clock);
			jobsArrived.put(job.getName(), job);
			
		}
		
		for (Message<?> msg : input.getMessages(solved)) {
			final Job job = jobsArrived.get(((Job)msg.getData()).getName());
			if (job==null) continue;
			
			totalTa += (clock - job.getTime());
			System.out.println("Finish job " + job.getName() + " @ t = " + clock);
			job.setTime(clock);
			jobsSolved.put(job.getName(), job);
		}
	}
	
	@Override
	public MessageBag getOutput() {
		MessageBag output = new MessageBagImpl();
		output.add(out.createMessage(new Job()));
		return output;
	}
	
	@Override
	public Double getTimeAdvance() {
		return sigma;
	}

}
