package com.appjangle.rsm.client.internal;

import io.nextweb.Node;

public class ClearResponseNodeProcess {

	public static interface ResponseNodeCleared {
		public void onSuccess();

		public void onFailure(Throwable t);
	}

	public void clearResponse(final Node responsesNode, final Node responseNode) {

	}

}
