package com.appjangle.rsm.client.internal;

import io.nextweb.ListQuery;
import io.nextweb.Node;
import io.nextweb.NodeList;
import io.nextweb.Session;
import io.nextweb.fn.Closure;

import com.appjangle.rsm.client.commands.v01.FailureResponse;
import com.appjangle.rsm.client.commands.v01.SuccessResponse;

public class ResponseSeekerWorker {

	public static interface ResponseReceived {
		public void onSuccessReceived(SuccessResponse response);

		public void onFailureReceived(FailureResponse response);
	}

	public void checkForResponses(final Session session, final Node atNode,
			final ResponseReceived callback) {
		if (!atNode.exists()) {
			return;
		}

		final ListQuery allQuery = atNode.selectAll();

		allQuery.get(new Closure<NodeList>() {

			@Override
			public void apply(final NodeList o) {

				for (final Node n : o.nodes()) {

					final Object obj = n.value();
					if (obj instanceof SuccessResponse) {

						callback.onSuccessReceived((SuccessResponse) obj);
						return;
					}

					if (obj instanceof FailureResponse) {

						final FailureResponse failureResponse = (FailureResponse) obj;
						callback.onFailureReceived(failureResponse);
						return;
					}

				}

			}
		});
	}

}
