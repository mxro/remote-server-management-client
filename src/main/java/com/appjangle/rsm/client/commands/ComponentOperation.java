package com.appjangle.rsm.client.commands;

import java.io.Serializable;

import one.utils.server.ServerComponent;

public interface ComponentOperation extends Serializable {

	public void perform(ServerComponent component, OperationCallback callback);

}
