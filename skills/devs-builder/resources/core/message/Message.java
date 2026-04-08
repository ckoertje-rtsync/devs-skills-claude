package com.ms4systems.devs.core.message;

import java.io.Serializable;

import com.ms4systems.devs.core.message.impl.MessageImpl;
import com.ms4systems.devs.core.model.AtomicModel;


/***
 * A message is a generic object used to hold data that is being sent between models.  
 * The generic type can be any Serializable object. 
 *  
 * @author bcoop
 *
 * @param <T> The type of data object that will be contained in this message.  Must implement Serializable
 */
public interface Message<T extends Serializable> extends Serializable{
	/*** 
	 * @return The the port in which this message is currently contained 
	 */
	Port<?> getPort();
	
	/**
	 * Convenience for getPort().getModel()
	 * @return
	 */
	AtomicModel getModel();
	
	/***
	 * @return The contents of the message
	 */
	T getData();
	
	/***
	 * Retrieve the class of the data in the message
	 * @return The class of the data
	 */
	Class<T> getType();
	
	/***
	 * @return True if the data is not null, false if the data is null
	 */
	boolean hasData();
	
	/***
	 * @return True if the port is not null, false if the port is null
	 */
	boolean hasPort();
	
	/***
	 * @return True if this message has a non-null port and non-null message
	 */
	boolean isValid();

	/***
	 * Set the data
	 * @param data The data of the message
	 */
	void setData(T data);

	/***
	 * Set the port to use
	 * @param port The name of the port
	 */
	void setPort(Port<?> port);
	
	/***
	 * Create a copy of a message using a different port.  Sets new message's messageTrace
	 * @param port The new port to use for the message
	 * @return A message with the same content but using the new port
	 */
	Message<T> replicateOnPort(Port<?> port);
	
	/***
	 * @return True if this message has no port or data
	 */
	boolean isEmpty();
	
	Message<T> getMessageTrace();

	void setMessageTrace(Message<T> messageTrace);

	/***
	 * A default EMPTY message
	 */
	public static final Message<Serializable> EMPTY =
		new MessageImpl<Serializable>();
}
