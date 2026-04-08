package Models.java;

import java.io.Serializable;

public class Job implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int id;
	private double time;
	private String name;
	
	public Job() {
		this(0);
	}
	
	public Job(int id) {
		this.id = id;
		this.name = Integer.toString(id);
		this.time = 0;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
		this.name = Integer.toString(id);
	}
	public double getTime() {
		return time;
	}
	public void setTime(double time) {
		this.time = time;
	}
	public String getName() {
		return name;
	}
	@Override
	public String toString() {
		return "Job " + Integer.toString(id);
	}
}
