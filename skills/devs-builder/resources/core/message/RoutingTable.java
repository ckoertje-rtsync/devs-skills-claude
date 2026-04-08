package com.ms4systems.devs.core.message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.ms4systems.devs.core.model.AtomicModel;
import com.ms4systems.devs.core.simulation.Simulator;

/**
 * A routing table encapsulates the logic needed to couple ports between models within a CoupledModel
 * and handles the actual routing algorithm.
 * @author bcoop
 *
 */
public interface RoutingTable extends Serializable {
	
	/**
	 * Checks to see if sendingPort can be coupled to recievingPort.
	 * TODO Describe checks made
	 * 
	 * @param sendingPort Port that will be creating output messages
	 * @param receivingPort Port that will be receiving input messages
	 * @return True iff couple(sendingPort,recievingPort) will successfully complete
	 */
	boolean isValidCoupling(Port<?> sendingPort, Port<?> receivingPort);
	
	/**
	 * Couple two ports together.  Throws an exception if the coupling is not valid.
	 * @param sendingPort Port that will be creating output messages
	 * @param receivingPort Port that will be receiving input messages
	 */
	void addCoupling(Port<?> sendingPort, Port<?> receivingPort);
	
	void addCoupling(Coupling coupling);
	
	void removeCoupling(Port<?> sendingPort, Port<?> receivingPort);
	void removeCoupling(Coupling coupling);
	
	public void removeCouplings(ArrayList<Coupling> couplingsToRemove);

	/**
	 * Returns a list of all receiving ports that are coupled to the sending port
	 * @param sendingPort Port that will be creating output messages
	 * @return A list of all ports that receive data from sendingPort
	 */
	ArrayList<Port<?>> getReceivingPorts(Port<?> sendingPort);

	/**
	 * Returns a list of all sending ports that are coupled to the receiving port
	 * @param receiving Port that will be receiving output messages
	 * @return A list of all ports that send data to receivingPort
	 */
	ArrayList<Port<?>> getSendingPorts(Port<?> receivingPort);
	
	/**
	 * Perform message routing.  For each message in input, an entry is added to the map
	 * for each Simulator containing a port that is coupled to the input message's port
	 * @param input MessageBag containing messages with ports set to the port that created the message
	 * @return Mapping that maps each simulator receiving data to a MessageBag containing messages with ports 
	 * 		set to a port on that Simulator's model
	 */
	Map<Simulator, MessageBag> routeMessages(MessageBag input);

	ArrayList<Coupling> getCouplings();
	
	void  setCouplings(ArrayList<Coupling> couplings);

	HashMap<Port<?>, ArrayList<Port<?>>> getReceiveSendMap();

    HashMap<Port<?>, ArrayList<Port<?>>> getSendReceiveMap();

	void removeCouplingsFor(Port<?> portToRemove);
	
	void removeCouplingsFor(AtomicModel modelToRemove);
	
	void clearAll();
	
	ArrayList<Coupling> getCouplingsFor(AtomicModel model);

	ArrayList<Coupling> getCouplingsFor(Port<?> port);
	
	ArrayList<Coupling> getCouplingsFor(AtomicModel sendingModel, AtomicModel receivingModel);
	
}
