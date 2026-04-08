package com.ms4systems.devs.core.message;

import java.io.Serializable;

import com.ms4systems.devs.core.model.AtomicModel;
import com.ms4systems.devs.core.model.CoupledModel;

public interface Coupling extends Serializable {
	CoupledModel getParent();
	AtomicModel getSource();
	AtomicModel getDestination();
	Port<? extends Serializable> getSourcePort();
	Port<? extends Serializable> getDestinationPort();
}
