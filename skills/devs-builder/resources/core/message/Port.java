package com.ms4systems.devs.core.message;

import java.io.Serializable;
import java.util.ArrayList;

import com.ms4systems.devs.core.model.AtomicModel;

public interface Port<T extends Serializable> extends Serializable {
	/***
	 * Direction specifier for input ports and output ports 
	 *
	 */
	enum Direction {
		INPUT,OUTPUT
	}

	/***
	 * @return The name of the port
	 */
	String getName();
	
	/***
	 * @param name Name to use for this port
	 */
	void setName(String name);
	
	/***
	 * Create a new message on this port
	 * @param data Contents of the message
	 * @return A message on this port with contents data
	 */
	Message<T> createMessage(T data);
	
	/***
	 * @return The class of objects exchanged on this port
	 */
	Class<T> getType();
	
	/***
	 * @return The model to which this port belongs
	 */
	AtomicModel getModel();
	
	/***
	 * @param model Set the model to which this port belongs
	 */
	void setModel(AtomicModel model);
	
	/***
	 * @return The direction of this port (input or output)
	 */
	Direction getDirection();

	/***
	 * @param direction The direction of this port (input or output)
	 */
	void setDirection(Direction direction);
	
	/**
	 * Create a copy of all messages in input, but set the port to this port
	 * @param input
	 * @return
	 */
	MessageBag replicateOnPort(MessageBag input);

	/**
	 * Gets all of the messages for this port and returns a list of properly-typed messages.
	 * 
	 * @param input MessageBag to retrieve messages from
	 * @return List of all messages on this port, cast to this port's type
	 */
	ArrayList<Message<T>> getMessages(MessageBag input);
	
	/**
	 * Gets all of the messages for this port and returns a list of properly-typed messages.
	 * 
	 * @param input MessageBag to retrieve messages from
	 * @return List of all messages on this port, cast to this port's type
	 */
	ArrayList<T> getData(MessageBag input);
}
