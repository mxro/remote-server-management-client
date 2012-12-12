package com.appjangle.rsm.client.commands.v01;

import com.appjangle.rsm.client.commands.ComponentOperation;
import com.appjangle.rsm.client.commands.OperationCallback;

import de.mxro.server.ComponentConfiguration;
import de.mxro.server.ComponentContext;
import de.mxro.server.ServerComponent;
import de.mxro.server.StartCallback;
import de.mxro.server.manager.ComponentManager;

/**
 * Appends a new {@link ServerComponent} to an already running server.
 * 
 * @author Max
 * 
 */
public class AppendOperation implements ComponentOperation {

	private static final long serialVersionUID = 1L;

	/**
	 * The new configuration to be used.
	 */
	public ComponentConfiguration conf;

	/**
	 * The index at which the new component is inserted. If -1 will be appended
	 * at end.
	 */
	public int index;

	@Override
	public void perform(final ComponentManager manager,
			final ComponentContext context, final OperationCallback callback) {
		try {
			manager.addComponent(context, conf);

			manager.startComponent(conf.getId(), new StartCallback() {

				@Override
				public void onStarted() {
					callback.onSuccess();
				}

				@Override
				public void onFailure(final Throwable t) {
					callback.onFailure(t);
				}
			});

		} catch (final Throwable t) {
			callback.onFailure(t);
		}
	}

	public AppendOperation(final ComponentConfiguration conf, final int index) {
		super();
		this.conf = conf;
		this.index = index;
	}

	/**
	 * only for deser
	 */
	@Deprecated
	public AppendOperation() {
		super();
	}

}
