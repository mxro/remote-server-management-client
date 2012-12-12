package com.appjangle.rsm.client.commands;

import com.appjangle.rsm.client.commands.v01.UpdateOperation;
import com.appjangle.rsm.client.commands.v01.RestartOperation;

import de.mxro.server.ComponentConfiguration;

public class Operations {

	/**
	 * Calls stop and then start for this component.
	 * 
	 * @return
	 */
	public static ComponentOperation restart() {
		return new RestartOperation();
	}

	/**
	 * Calls stop, injects the specified configuration for the component and
	 * then starts the component again.
	 * 
	 * @param conf
	 * @return
	 */
	public static ComponentOperation update(final ComponentConfiguration conf) {
		return new UpdateOperation(conf);
	}

}