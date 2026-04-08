package com.ms4systems.devs.core.simulation;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;

import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.model.AtomicModel;
import com.ms4systems.devs.events.SimulationEventListener;
import com.ms4systems.devs.log.SimulationLogger;

/** Simulation 
 * Root interface for classes that support simulating a root model.
 *  
 * @author bcoop
 *
 */
public interface Simulation extends Serializable {


	/**
	 * @return The time (in milliseconds like System.currentTimeMillis()) that this simulation finished.
	 */
	long getFinishTime();

	/**
	 * @return The time (in milliseconds like System.currentTimeMillis()) that this simulation started.
	 */
	long getStartTime();

	/**
	 * @return True iff this simulation was once started and is now finished
	 */
	boolean isFinished();

	/**
	 * @return True iff this simulation is currently running (has started but is not yet finished)
	 */
	boolean isRunning();

	/**
	 * @return The amount of time (in milliseconds) since this simulation started.
	 */
	long getCurrentElapsedTime();

	/**
	 * @return The number of iterations that have been simulated
	 */
	long getCurrentIterationCount();

	/**
	 * @param maxIterationCount The maximum number of iterations to simulate.  
	 * 		Simulation will terminate after this number of iterations has run (only checked at 
	 * 		simulation calls).
	 */
	void setMaxIterationCount(long maxIterationCount);

	/**
	 * @return The maximum number of iterations to simulate.  
	 * 		Simulation will terminate after this number of iterations has run (only checked at 
	 * 		simulation calls).
	 */
	long getMaxIterationCount();

	/**
	 * @param maxElapsedTime The maximum amount of time (in milliseconds) that this simulation can run.
	 * 		Simulation will terminate this amount of time after startSimulation() is called (only checked at 
	 * 		simulation calls).
	 */
	void setMaxElapsedTime(long maxElapsedTime);

	long getMaxElapsedTime();

	void setMaxSimulationTime(long maxSimulationTime);

	double getMaxSimulationTime();

	double getCurrentSimulationTime();

	void setName(String name);

	String getName();

	/**
	 * @param rootModel Set the root model for this simulation.  Throws a
	 */
	void setRootModel(AtomicModel rootModel);

	AtomicModel getRootModel();

	/**
	 * @return The simulated time at which the last event occurred
	 */
	double getLastEventTime();

	/**
	 * @return The simulated time at which the next event is scheduled to occur
	 */
	double getNextEventTime();

	/**
	 * Input message into the simulation.  The messages in input must have their ports properly set to a valid
	 * 		input port of a model in the simulation
	 * @param simulationTime Simulated time at which the input should occur.  Throws a IllegalStateException 
	 * 	if this time is before lastEventTime or after nextEventTime
	 * @param input Input to inject
	 */
	void injectInput(double simulationTime, MessageBag input);

	/**
	 * @return The output that will be created as part of the next simulated iteration
	 */
	MessageBag getNextOutput();
	
	/**
	 * @return The output that was created as part of the last simulated iteration
	 */
	MessageBag getLastOutput();

	/**
	 * Run the simulation for some number of iterations
	 * @param numberOfIterations Number of iterations to run
	 * @return True if numberOfIterations were successfully run.  False if something caused the simulation to terminate early.
	 */
	boolean simulateIterations(long numberOfIterations);

	void restartSimulation(double initialSimulationTime);

	void stopSimulation();

	void startSimulation(double initialSimulationTime);
	
	Simulator getRootSimulator();
	
	void setRootSimulator(Simulator rootSimulator);

	double getInitialSimulationTime();
	
	/***
	 * @return The unique URI identifying this simulation
	 */
	URI getURI();

	/***
	 * @return A list of all components within this simulation
	 */
	ArrayList<URI> getAllContents();
	
	void addEventListener(SimulationEventListener eventListener, boolean addRecursively);

	SimulationLogger getSimulationLogger();

	public abstract int getSimulationHash();

}
