package com.appjangle.rsm.client;

import io.nextweb.Session;
import io.nextweb.jre.Nextweb;

import com.appjangle.rsm.client.commands.ComponentOperation;
import com.appjangle.rsm.client.commands.OperationCallback;
import com.appjangle.rsm.client.internal.SendCommandWorker;

public class RsmClient {

	public static int defaultTimeoutInS = 60 * 3;

	/**
	 * Run an operation on the server for a specific component.
	 * 
	 * @param operation
	 * @param forId
	 * @param conf
	 * @param callback
	 */
	public static void performCommand(final ComponentOperation operation,
			final ClientConfiguration conf, final OperationCallback callback) {

		assert operation != null;

		final Session session = Nextweb.createSession();

		SendCommandWorker.performCommand(operation, conf, callback, session);

	}
}
