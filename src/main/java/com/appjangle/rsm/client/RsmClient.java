package com.appjangle.rsm.client;

import io.nextweb.Link;
import io.nextweb.Node;
import io.nextweb.Query;
import io.nextweb.Session;
import io.nextweb.common.Interval;
import io.nextweb.fn.Closure;
import io.nextweb.jre.Nextweb;

import com.appjangle.rsm.client.commands.ComponentOperation;
import com.appjangle.rsm.client.commands.OperationCallback;
import com.appjangle.rsm.client.commands.v01.ComponentCommandData;

public class RsmClient {

	public static void performCommand(final ComponentOperation operation,
			final String forId, final OperationCallback callback,
			final ClientConfiguration conf) {
		assert forId != null;
		assert operation != null;

		final Session session = Nextweb.createSession();

		// prepare response node
		final Link responsesLink = session.node(conf.getResponsesNode(),
				conf.getResponseNodeSecret());
		final Query responseQuery = responsesLink.appendSafe("resp");

		final Node response = responseQuery.get();

		final Link commands = session.node(conf.getCommandsNode(),
				conf.getCommandsNodeSecret());

		// preparing command
		final ComponentCommandData command = new ComponentCommandData();
		command.setId(forId);
		command.setOperation(operation);
		command.setPort(Nextweb
				.getEngine()
				.getFactory()
				.createPort(session, response.uri(),
						conf.getResponseNodeSecret()));

		// add to commands node
		commands.append(command);
		session.commit().get();

		response.monitor(Interval.EXTRA_FAST, new Closure<Node>() {

			@Override
			public void apply(final Node o) {

			}
		});

	}
}
