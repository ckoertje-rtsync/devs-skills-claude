package com.ms4systems.devs.core.model;

import java.io.Serializable;
import java.util.ArrayList;

import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.message.Port;
import com.ms4systems.devs.core.simulation.Simulator;
import com.ms4systems.devs.visitor.ModelVisitor;

 

public interface AtomicModel extends Serializable{
		
	/***
	 * @return The name of the model
	 */
	String getName();
	
	/**
	 * This method will throw an exception if the model has been initialized.
	 * @param name Name to set for the model 
	 */
	void setName(String name);
	
	
	/***
	 * This returns the amount of time until the model undergoes an
	 * internal event.  This must not modify the state of the model
	 * and must be non-negative.  If the model does not have a pending
	 * internal event this should be Double.POSITIVE_INFINITY
	 * @return
	 */
	Double getTimeAdvance();
	
	/***
	 * Perform an internal event for the model
	 */
	void internalTransition();
	
	/***
	 * Notify model of an external event triggered by some input.
	 * @param timeElapsed The amount of time that has elapsed since the last internal event
	 * @param input The input to the model which triggered the external event
	 */
	void externalTransition(double timeElapsed, MessageBag input);
	
	/***
	 * Notify model that it has received input at the same time as it scheduled 
	 * an internal event to occur.  Model must resolve conflict (e.g. by performing
	 * an internal event then an external event).
	 * @param input The input to the model which triggered the confluent event
	 */
	void confluentTransition(MessageBag input);
	
	/***
	 * Calculate output of the model.  This is called immediately _prior_ to an
	 * internal event triggering.  Model should return MessageBag.EMPTY instead of 
	 * null if no output is generated.
	 * @return
	 */
	MessageBag getOutput();
	
	/***
	 * Initialize the model to initial conditions.  This should completely
	 * reset the model for a new simulation.
	 */
	void initialize();
	
	/**
	 * @return True if this model has been initialized.
	 */
	boolean isInitialized();
	
	/**
	 * @return The simulator being used to manage this model
	 */
	Simulator getSimulator();
	
	/**
	 * Sets the simulator to use for managing this model.  Will throw exception if model has been initialized.
	 * @param simulator The simulator to use t
	 */
	void setSimulator(Simulator simulator);
	
	/***
	 * @return A list of all input ports for this model. This may be actual list used by the model,
	 * 	so undefined behavior will occur if the list or its ports are modified. 
	 */
	ArrayList<Port<? extends Serializable>> getInputPorts();
	/**
	 * @param name Name of an input port
	 * @return True iff there is at least one input port on the model with the given name
	 */
	boolean hasInputPort(String name);
	/**
	 * @param name Name of an input port
	 * @return The first port found that has the given name.  This may be the actual port object, so
	 * 		undefined behavior will occur if this object is modified.
	 */
	Port<? extends Serializable> getInputPort(String name);
	/**
	 * @param name Name of an input port
	 * @param portType The type of messages received on the input port
	 * @return True if and only if this port is contained in the model
	 */
	boolean hasInputPort(String name, Class<? extends Serializable> portType);
	/**
	 * @param name Name of an input port
	 * @param portType The type of messages received on the input port
	 * @return The port matching the name and class.  This may be the actual port object, so
	 * 		undefined behavior will occur if this object is modified.
	 */
	<T extends Serializable> Port<T> getInputPort(String name, Class<T> portType);
	/**
	 * @param portType A type of (serializable) data
	 * @return True iff at least one port on the model can receive this type of data
	 */
	boolean hasCompatibleInputPort(Class<? extends Serializable> portType);
	/**
	 * 
	 * @param <T> The type of data
	 * @param portType A type of (serializable) data
	 * @return A list of ports that can receive this data type
	 */
	<T extends Serializable> ArrayList<Port<T>> getCompatibleInputPorts(Class<T> portType);

	/***
	 * @return A list of all output ports for this model. This may be actual list used by the model,
	 * 	so undefined behavior will occur if the list or its ports are modified. 
	 */
	ArrayList<Port<? extends Serializable>> getOutputPorts();
	/**
	 * @param name Name of an output port
	 * @return True iff there is at least one output port on the model with the given name
	 */
	boolean hasOutputPort(String name);
	/**
	 * @param name Name of an output port
	 * @return The first port found that has the given name.  This may be the actual port object, so
	 * 		undefined behavior will occur if this object is modified.
	 */
	Port<? extends Serializable> getOutputPort(String name);
	/**
	 * @param name Name of an output port
	 * @param portType The type of messages sent from the output port
	 * @return True iff this port is contained in the model
	 */
	boolean hasOutputPort(String name, Class<? extends Serializable> portType);
	/**
	 * @param name Name of an output port
	 * @param portType The type of messages sent from the output port
	 * @return The port matching the name and class.  This may be the actual port object, so
	 * 		undefined behavior will occur if this object is modified.
	 */
	<T extends Serializable> Port<T> getOutputPort(String name, Class<T> portType);
	/**
	 * @param portType A type of (serializable) data
	 * @return True iff at least one port on the model can output this type of data
	 */
	boolean hasCompatibleOutputPort(Class<? extends Serializable> portType);
	/**
	 * 
	 * @param <T> The type of data
	 * @param portType A type of (serializable) data
	 * @return A list of ports that can output this data type
	 */
	<T extends Serializable> ArrayList<Port<T>> getCompatibleOutputPorts(Class<T> portType);
	
	/**
	 * Returns the parent CoupledModel that contains this model, or null is this model has no parent
	 */
	CoupledModel getParent();

	/***
	 * Sets the CoupledModel that this model belongs to; should only be called by CoupledModel.
	 */
	void setParent(CoupledModel parent);

	void accept(ModelVisitor visitor);
	
}
