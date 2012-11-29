package com.appjangle.rsm.client.commands.v01;

import one.utils.server.ServerComponent;
import one.utils.server.ShutdownCallback;
import one.utils.server.StartCallback;

import com.appjangle.rsm.client.commands.ComponentOperation;
import com.appjangle.rsm.client.commands.OperationCallback;

public class RestartOperation implements ComponentOperation {

	private static final long serialVersionUID = 1L;

	@Override
	public void perform(final ServerComponent component,
			final OperationCallback callback) {

		component.stop(new ShutdownCallback() {

			@Override
			public void onShutdownComplete() {
				component.start(new StartCallback() {

					@Override
					public void onStarted() {
						callback.onSuccess();
					}

					@Override
					public void onFailure(final Throwable t) {
						callback.onFailure(t);
					}
				});
			}

			@Override
			public void onFailure(final Throwable t) {
				callback.onFailure(t);
			}
		});

	}

}
