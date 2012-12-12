package com.appjangle.rsm.client.commands.v01;

import com.appjangle.rsm.client.commands.ComponentOperation;
import com.appjangle.rsm.client.commands.OperationCallback;

import de.mxro.server.ComponentContext;
import de.mxro.server.ShutdownCallback;
import de.mxro.server.manager.ComponentManager;

/**
 * Stops a component and removes it from the server.
 * 
 * @author Max
 * 
 */
public class RemoveOperation implements ComponentOperation {

	private static final long serialVersionUID = 1L;

	/**
	 * The id of the component to be removed.
	 */
	public String componentId;

	@Override
	public void perform(final ComponentManager manager,
			final ComponentContext context, final OperationCallback callback) {

		try {

			manager.stopComponent(componentId, new ShutdownCallback() {

				@Override
				public void onShutdownComplete() {
					removeComponent(manager, componentId, callback);
				}

				@Override
				public void onFailure(final Throwable t) {
					removeComponent(manager, componentId, callback);
				}
			});

		} catch (final Throwable t) {
			callback.onFailure(t);
		}

	}

	protected void removeComponent(final ComponentManager manager,
			final String componentId2, final OperationCallback callback) {
		try {
			manager.removeComponent(componentId2);
			callback.onSuccess();
		} catch (final Throwable t) {
			callback.onFailure(t);
		}
	}

	public RemoveOperation(final String componentId) {
		super();
		this.componentId = componentId;
	}

	/**
	 * only for deser
	 */
	@Deprecated
	public RemoveOperation() {
		super();
	}

}
