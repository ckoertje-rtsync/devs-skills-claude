package com.ms4systems.devs.core.simulation;

import java.util.ArrayList;
import java.util.List;

import com.ms4systems.devs.core.message.Coupling;
import com.ms4systems.devs.core.message.Port;
import com.ms4systems.devs.core.message.RoutingTable;
import com.ms4systems.devs.core.model.AtomicModel;
import com.ms4systems.devs.core.model.CoupledModel;

public interface Coordinator extends Simulator {
	/**
	 * @return The coupled model associated with this Coordinator
	 */
	CoupledModel getCoupledModel();

	/**
	 * Set the coupled model to be used with this coordinator. Cannot be called after initialization
	 * @param value
	 */
	void setCoupledModel(CoupledModel model);
	
	/**
	 * Get the list of simulators (some of which may be coordinators) under this coordinator.
	 * @return
	 */
	ArrayList<Simulator> getChildren();

	/**
	 * Get the list of models (some of which may be coupled models) under this coordinator.
	 * @return
	 */
	ArrayList<AtomicModel> getChildModels();

	/**
	 * @param model
	 * @return True iff the model is the model for a child simulator 
	 */
	boolean isChildModel(AtomicModel model);
	
	/**
	 * Add a coupling to this coordinator.
	 * //TODO Describe net-model, model-model, model-net coupling requirements
	 *  
	 * @param fromPort The port at which messages will arrive. 
	 * @param toPort
	 */
	void addCoupling(Port<?> fromPort, Port<?> toPort);
	
	ArrayList<Coupling> getCouplings();
	
	RoutingTable getRoutingTable();

	public abstract void removeCoupling(Coupling coupling);
	public abstract void removeCoupling(Port<?> fromPort, Port<?> toPort);
	public abstract void removeCouplings(List<Coupling> couplings);

	public abstract void removeModelChild(AtomicModel model);

	public abstract void addModelChild(AtomicModel model);

	void refreshEventTime(Simulator coordinatorImpl, boolean isNewSimulator);

	void addSimulator(Simulator simulator);
	void removeSimulator(Simulator simulator);
	
}
