package com.appjangle.rsm.client.commands;

import com.appjangle.rsm.client.commands.v01.AppendOperation;
import com.appjangle.rsm.client.commands.v01.RemoveOperation;
import com.appjangle.rsm.client.commands.v01.ReplaceOperation;
import com.appjangle.rsm.client.commands.v01.RestartOperation;

import de.mxro.server.ComponentConfiguration;
import de.mxro.server.ServerComponent;

public class Operations {

	/**
	 * Calls stop and then start for this component.
	 * 
	 * @return
	 */
	public static ComponentOperation restart(final String componentId) {
		return new RestartOperation(componentId);
	}

	/**
	 * Calls stop, injects the specified configuration for the component and
	 * then starts the component again.
	 * 
	 * @param conf
	 * @return
	 */
	public static ComponentOperation update(final String componentId,
			final ComponentConfiguration conf) {
		return new ReplaceOperation(componentId, conf);
	}

	/**
	 * Appends a new {@link ServerComponent} to an already running server.
	 * 
	 */
	public static AppendOperation append(final ComponentConfiguration conf) {
		return new AppendOperation(conf, -1);
	}

	/**
	 * Appends a new {@link ServerComponent} to an already running server at the
	 * specified index and starts the server.
	 * 
	 */
	public static AppendOperation append(final ComponentConfiguration conf,
			final int index) {
		return new AppendOperation(conf, index);
	}

	/**
	 * Stops a component and removes it from the server.
	 * 
	 * @param componentId
	 * @return
	 */
	public static ComponentOperation remove(final String componentId) {
		return new RemoveOperation(componentId);
	}

}
