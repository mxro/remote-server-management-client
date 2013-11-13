package com.appjangle.rsm.client.internal;

import io.nextweb.common.MonitorContext;
import io.nextweb.fn.Closure;
import io.nextweb.fn.Result;
import io.nextweb.fn.Success;
import io.nextweb.fn.exceptions.ExceptionResult;

public class StopMonitorProcess {

	public interface StopMonitorCallback {
		public void onSuccess();

		public void onFailure(Throwable t);
	}

	public void stop(final MonitorContext ctx,
			final StopMonitorCallback callback) {

		final Result<Success> stopRequest = ctx.monitor().stop();

		stopRequest.catchExceptions(new ExceptionListener() {

			@Override
			public void onFailure(final ExceptionResult r) {
				callback.onFailure(r.exception());
			}
		});

		stopRequest.get(new Closure<Success>() {

			@Override
			public void apply(final Success o) {
				callback.onSuccess();
			}
		});
	}

}
