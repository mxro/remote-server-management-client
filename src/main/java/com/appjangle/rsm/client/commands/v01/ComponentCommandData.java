package com.appjangle.rsm.client.commands.v01;

import io.nextweb.nodes.Port;

import com.appjangle.rsm.client.commands.ComponentCommand;
import com.appjangle.rsm.client.commands.ComponentOperation;

public class ComponentCommandData implements ComponentCommand {

	private static final long serialVersionUID = 1L;

	public ComponentOperation operation;

	public Port port;

	public Port getPort() {
		return port;
	}

	public void setPort(final Port port) {
		this.port = port;
	}

	@Override
	public ComponentOperation getOperation() {
		return operation;
	}

	public void setOperation(final ComponentOperation operation) {
		this.operation = operation;
	}

	@Override
	public Port getResponsePort() {

		return port;
	}

	public ComponentCommandData() {
		super();
	}

}
