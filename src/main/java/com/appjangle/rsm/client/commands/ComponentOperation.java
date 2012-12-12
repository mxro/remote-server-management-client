package com.appjangle.rsm.client.commands;

import java.io.Serializable;

import de.mxro.server.ComponentContext;
import de.mxro.server.manager.ComponentManager;

public interface ComponentOperation extends Serializable {

	/**
	 * Perform an operation on a server.
	 * 
	 * @param manager
	 * @param callback
	 */
	public void perform(ComponentManager manager, ComponentContext context,
			OperationCallback callback);

}
