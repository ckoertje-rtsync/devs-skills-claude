package com.ms4systems.devs.core.message;

import java.io.Serializable;
import java.util.ArrayList;

import com.ms4systems.devs.core.message.impl.MessageBagImpl;
import com.ms4systems.devs.core.model.AtomicModel;

public abstract class MessageBag extends ArrayList<Message<? extends Serializable>> implements Comparable<MessageBag> {
	private static final long serialVersionUID = 1L;
	
	public MessageBag() {
		super();
	}
	
	public MessageBag(int size) {
		super(size);
	}
	
	public MessageBag(MessageBag other) {
		super(other);
	}

	/*** 
	 * Get all messages on a specific port 
	 * @param port The port to retrieve
	 * @return List of messages on that port.  Cannot be null.
	 */
	abstract public MessageBag getMessages(Port<?> port);
	
	/***
	 * Check a port to see if there are messages
	 * @param port The port to check
	 * @return True if the port has a message
	 */
	abstract public boolean hasMessages(Port<?> port);

	abstract public boolean hasMessages(AtomicModel model);
	abstract public MessageBag getMessages(AtomicModel model);
	
	/***
	 * @return A list of ports that have data
	 */
	abstract public ArrayList<Port<?>> portsWithMessages();
	
	/***
	 * @return The timestamp for the collection of messages
	 */
	abstract public double getMessageTime();

	/***
	 * @param messageTime The timestamp for the collection of messages
	 */
	abstract public void setMessageTime(double messageTime);
	
	/***
	 * @return List of models that have a message in this bag.  Should not return null.
	 */
	abstract public ArrayList<AtomicModel> getModelsWithMessages();
	
	/***
	 * Convenience method to add a message to the bag. Defers to add(port.createMessage(data)) 
	 * @param port Port that the message should use
	 * @param data Data contained in the message
	 */
	abstract public
	<T extends Serializable>
	 void add(Port<T> port, T data);
	
	/***
	 * This is a default empty MessageBag to prevent unneeded object creation
	 */
	static public final MessageBag EMPTY = new MessageBagImpl(); 
}
