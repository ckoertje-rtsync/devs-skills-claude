package com.ms4systems.devs.core.model;

import java.util.ArrayList;

import com.ms4systems.devs.core.message.Coupling;
import com.ms4systems.devs.core.message.Port;
import com.ms4systems.devs.core.simulation.Coordinator;

public interface CoupledModel extends AtomicModel 
{
	/**
	 * @return A list of all models that have been added to this one. This may be actual list used by the model,
	 * 	so undefined behavior will occur if the list or its contents are modified.
	 */
	ArrayList<AtomicModel> getChildren();
	
	/**
	 * @return The coordinator being used to manage this model
	 */
	Coordinator getCoordinator();
	
	/**
	 * Sets the coordinator to use for managing this model.  Will throw exception if model has been initialized.
	 * @param coordinator The coordinator to use t
	 */
	void setCoordinator(Coordinator coordinator);
	
	/**
	 * Adds a model to this model. 
	 * @param model
	 */
	void addChildModel(AtomicModel model);
	void removeChildModel(AtomicModel modelToRemove);
	
	/**
	 * Add a coupling to this model.
	 *  
	 * @param fromPort The port at which messages will arrive. 
	 * @param toPort
	 */
	void addCoupling(Port<?> fromPort, Port<?> toPort);
	
	void removeCoupling(Port<?> fromPort, Port<?> toPort);
	
	
	ArrayList<Coupling> getCouplings();
	
	ArrayList<AtomicModel>  getComponentsWithPartName(String nm);
	
	AtomicModel getComponentWithPartName(String nm);
			
	
}
