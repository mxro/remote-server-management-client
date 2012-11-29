package com.appjangle.rsm.client.commands.v01;

import com.appjangle.rsm.client.commands.ComponentCommandResponse;

/**
 * In case an operation was successfully completed.
 * 
 * @author mroh004
 * 
 */
public class SuccessResponse implements ComponentCommandResponse {

	private static final long serialVersionUID = -5119242018368471614L;

	public SuccessResponse() {
		super();
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		return obj instanceof SuccessResponse;
	}

}
