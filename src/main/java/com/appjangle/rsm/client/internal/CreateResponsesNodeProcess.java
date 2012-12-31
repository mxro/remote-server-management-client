package com.appjangle.rsm.client.internal;

import io.nextweb.Link;
import io.nextweb.LinkList;
import io.nextweb.Node;
import io.nextweb.Query;
import io.nextweb.fn.Closure;
import io.nextweb.operations.exceptions.ImpossibleListener;
import io.nextweb.operations.exceptions.ImpossibleResult;

import java.util.Random;

public class CreateResponsesNodeProcess {

	public static interface ResponsesNodeCallback {
		public void onSuccess(Node response);

		public void onFailure(Throwable t);
	}

	public final void createResponsesNode(final Link responsesLink,
			final LinkList ll, final ResponsesNodeCallback callback) {
		final Query responseQuery = responsesLink.appendSafe("r"
				+ (new Random().nextInt()));

		responseQuery.catchImpossible(new ImpossibleListener() {

			@Override
			public void onImpossible(final ImpossibleResult ir) {

				// just try again
				createResponsesNode(responsesLink, ll, callback);

			}
		});

		responseQuery.get(new Closure<Node>() {

			@Override
			public void apply(final Node response) {
				callback.onSuccess(response);
			}
		});

	}

}
