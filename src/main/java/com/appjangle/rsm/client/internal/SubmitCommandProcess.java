package com.appjangle.rsm.client.internal;

import io.nextweb.Node;
import io.nextweb.Session;
import io.nextweb.fn.Closure;
import io.nextweb.fn.ExceptionListener;
import io.nextweb.fn.ExceptionResult;
import io.nextweb.fn.Result;
import io.nextweb.fn.Success;
import io.nextweb.jre.Nextweb;

import com.appjangle.rsm.client.ClientConfiguration;
import com.appjangle.rsm.client.commands.ComponentOperation;
import com.appjangle.rsm.client.commands.v01.ComponentCommandData;

public class SubmitCommandProcess {

	final ComponentOperation operation;

	final ClientConfiguration conf;

	final Session session;

	public static interface CommandSubmittedCallback {
		public void onSuccess();

		public void onFailure(Throwable t);
	}

	public final void submitCommand(final Node response,
			final CommandSubmittedCallback callback) {
		// preparing command
		final ComponentCommandData command = new ComponentCommandData();

		// System.out.println("submitting to: " + conf.getCommandsNode());

		command.setOperation(operation);
		command.setPort(Nextweb
				.getEngine()
				.getFactory()
				.createPort(session, response.uri(),
						conf.getResponseNodeSecret()));

		// synchronizing all changes with server
		session.commit().get(new Closure<Success>() {

			@Override
			public void apply(final Success o) {

				// add to commands node
				final Result<Success> postRequest = session.post(command,
						conf.getCommandsNode(), conf.getCommandsNodeSecret());

				postRequest.catchExceptions(new ExceptionListener() {

					@Override
					public void onFailure(final ExceptionResult r) {
						callback.onFailure(r.exception());
					}
				});

				postRequest.get(new Closure<Success>() {

					@Override
					public void apply(final Success o) {
						callback.onSuccess();
					}
				});

			}
		});

	}

	public SubmitCommandProcess(final ComponentOperation operation,
			final ClientConfiguration conf, final Session session) {
		super();
		this.operation = operation;
		this.conf = conf;
		this.session = session;
	}

}
