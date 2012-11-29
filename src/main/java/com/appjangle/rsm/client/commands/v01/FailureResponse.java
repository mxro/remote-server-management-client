package com.appjangle.rsm.client.commands.v01;

import com.appjangle.rsm.client.commands.ComponentCommandResponse;

/**
 * In case a failure occured during performing an operation.
 * 
 * @author mroh004
 * 
 */
public class FailureResponse implements ComponentCommandResponse {

	private static final long serialVersionUID = 1L;

	public Throwable exception;

	public Throwable getException() {
		return exception;
	}

	public void setException(final Throwable exception) {
		this.exception = exception;
	}

	public FailureResponse() {
		super();
	}

}
