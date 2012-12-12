package com.appjangle.rsm.client.commands.v01;

import com.appjangle.rsm.client.commands.ComponentOperation;
import com.appjangle.rsm.client.commands.OperationCallback;

import de.mxro.server.ComponentConfiguration;
import de.mxro.server.ComponentContext;
import de.mxro.server.ShutdownCallback;
import de.mxro.server.StartCallback;
import de.mxro.server.manager.ComponentManager;

public class UpdateOperation implements ComponentOperation {

	private static final long serialVersionUID = 1L;

	/**
	 * The new configuration to be used.
	 */
	public ComponentConfiguration conf;

	/**
	 * Which component is to be updaded?
	 */
	public String componentId;

	@Override
	public void perform(final ComponentManager manager,
			final ComponentContext context, final OperationCallback callback) {
		manager.stopComponent(componentId, new ShutdownCallback() {

			@Override
			public void onShutdownComplete() {

				final int idx = manager.removeComponent(componentId);

				manager.addComponent(conf);
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

	public UpdateOperation(final String componentId,
			final ComponentConfiguration conf) {
		super();
		this.componentId = componentId;
		this.conf = conf;
	}

	/**
	 * for deser
	 */
	@Deprecated
	public UpdateOperation() {
		super();
	}

}
