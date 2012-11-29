package com.appjangle.rsm.client.commands;

import java.io.Serializable;

import de.mxro.server.ServerComponent;


public interface ComponentOperation extends Serializable {

	public void perform(ServerComponent component, OperationCallback callback);

}
