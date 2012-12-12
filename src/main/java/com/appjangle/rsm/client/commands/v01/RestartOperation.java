package com.appjangle.rsm.client.commands.v01;

import com.appjangle.rsm.client.commands.ComponentOperation;
import com.appjangle.rsm.client.commands.OperationCallback;

import de.mxro.server.ComponentContext;
import de.mxro.server.ShutdownCallback;
import de.mxro.server.StartCallback;
import de.mxro.server.manager.ComponentManager;

public class RestartOperation implements ComponentOperation {

	private static final long serialVersionUID = 1L;

	/**
	 * Which component is to be restarted?
	 */
	public String componentId;

	@Override
	public void perform(final ComponentManager manager,
			final ComponentContext context, final OperationCallback callback) {

		manager.stopComponent(componentId, new ShutdownCallback() {

			@Override
			public void onShutdownComplete() {
				manager.startComponent(componentId, new StartCallback() {

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
				manager.startComponent(componentId, new StartCallback() {

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

	public RestartOperation(final String componentId) {
		super();
		this.componentId = componentId;
	}

	/**
	 * Use for deserialization
	 */
	@Deprecated
	public RestartOperation() {
		super();

	}

}
