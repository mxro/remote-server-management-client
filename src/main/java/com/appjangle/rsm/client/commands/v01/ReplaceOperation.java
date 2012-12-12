package com.appjangle.rsm.client.commands.v01;

import com.appjangle.rsm.client.commands.ComponentOperation;
import com.appjangle.rsm.client.commands.OperationCallback;

import de.mxro.server.ComponentConfiguration;
import de.mxro.server.ServerComponent;
import de.mxro.server.ShutdownCallback;
import de.mxro.server.StartCallback;

public class ReplaceOperation implements ComponentOperation {

	private static final long serialVersionUID = 1L;

	public ComponentConfiguration conf;

	@Override
	public void perform(final ServerComponent component,
			final OperationCallback callback) {

		component.stop(new ShutdownCallback() {

			@Override
			public void onShutdownComplete() {

				component.injectConfiguration(conf);
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
				// if component cannot be shut down, try to start anyway.
				component.injectConfiguration(conf);
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
		});

	}

	public ReplaceOperation(final ComponentConfiguration conf) {
		super();
		this.conf = conf;
	}

	/**
	 * for deser
	 */
	@Deprecated
	public ReplaceOperation() {
		super();
	}

}
