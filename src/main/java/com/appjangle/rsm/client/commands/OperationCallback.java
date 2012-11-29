package com.appjangle.rsm.client.commands;

public interface OperationCallback {
	public void onSuccess();

	public void onFailure(Throwable t);
}