package com.appjangle.rsm.client.commands;

import io.nextweb.nodes.Port;

import java.io.Serializable;

public interface ComponentCommand extends Serializable {

	/**
	 * For which component Id of the cloud node
	 * 
	 * @return
	 */
	public String forId();

	/**
	 * Which operation is to be performed.
	 * 
	 * @return
	 */
	public ComponentOperation getOperation();

	/**
	 * Node and access secret to where response is to be written.
	 * 
	 * @return
	 */
	public Port getResponsePort();
}
