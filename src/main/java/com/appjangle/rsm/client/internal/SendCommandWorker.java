package com.appjangle.rsm.client.internal;

import io.nextweb.Link;
import io.nextweb.LinkList;
import io.nextweb.Node;
import io.nextweb.Session;
import io.nextweb.common.MonitorContext;
import io.nextweb.fn.Closure;

import java.util.concurrent.atomic.AtomicBoolean;

import com.appjangle.rsm.client.ClientConfiguration;
import com.appjangle.rsm.client.commands.ComponentOperation;
import com.appjangle.rsm.client.commands.OperationCallback;
import com.appjangle.rsm.client.commands.v01.FailureResponse;
import com.appjangle.rsm.client.commands.v01.SuccessResponse;
import com.appjangle.rsm.client.internal.ClearResponseNodeProcess.ResponseNodeCleared;
import com.appjangle.rsm.client.internal.CreateResponsesNodeProcess.ResponsesNodeCallback;
import com.appjangle.rsm.client.internal.InstallMonitorProcess.MonitorInstalledCallback;
import com.appjangle.rsm.client.internal.ResponseSeekerWorker.ResponseReceived;
import com.appjangle.rsm.client.internal.StopMonitorProcess.StopMonitorCallback;
import com.appjangle.rsm.client.internal.SubmitCommandProcess.CommandSubmittedCallback;

public class SendCommandWorker {

	final ComponentOperation operation;
	final ClientConfiguration conf;

	final Session session;

	public SendCommandWorker(final ComponentOperation operation,
			final ClientConfiguration conf, final Session session) {
		super();
		this.operation = operation;
		this.conf = conf;
		this.session = session;
	}

	public void run(final OperationCallback callback) {
		// prepare response node
		final Link responsesLink = session.node(conf.getResponsesNode(),
				conf.getResponseNodeSecret());

		responsesLink.selectAllLinks().get(new Closure<LinkList>() {

			@Override
			public void apply(final LinkList responses) {
				step1(callback, responsesLink, responses);
			}

		});
	}

	private void step1(final OperationCallback callback,
			final Link responsesLink, final LinkList responses) {
		new CreateResponsesNodeProcess().createResponsesNode(responsesLink,
				responses, new ResponsesNodeCallback() {

					@Override
					public void onSuccess(final Node response) {
						step2(callback, responsesLink, response);
					}

					@Override
					public void onFailure(final Throwable t) {
						callback.onFailure(t);
					}
				});
	}

	private void step2(final OperationCallback callback,
			final Link responsesLink, final Node response) {
		new SubmitCommandProcess(operation, conf, session).submitCommand(
				response, new CommandSubmittedCallback() {

					@Override
					public void onSuccess() {
						step3(callback, responsesLink, response);
					}

					@Override
					public void onFailure(final Throwable t) {
						callback.onFailure(t);

					}
				});
	}

	private void step3(final OperationCallback callback,
			final Link responsesLink, final Node response) {
		new InstallMonitorProcess().installMonitor(responsesLink, response,
				new MonitorInstalledCallback() {

					@Override
					public void onFailure(final Throwable t) {
						callback.onFailure(t);
					}

					@Override
					public void onChangeDetected(final MonitorContext ctx,
							final AtomicBoolean responseReceived) {
						step4(callback, responsesLink, response, ctx);
					}

				});
	}

	private void step4(final OperationCallback callback,
			final Link responsesLink, final Node response,
			final MonitorContext ctx) {
		new ResponseSeekerWorker().checkForResponses(session, ctx.node(),
				new ResponseReceived() {

					@Override
					public void onSuccessReceived(
							final SuccessResponse successResponse) {

						new StopMonitorProcess().stop(ctx,
								new StopMonitorCallback() {

									@Override
									public void onSuccess() {
										new ClearResponseNodeProcess()
												.clearResponse(
														responsesLink,
														response,
														new ResponseNodeCleared() {

															@Override
															public void onSuccess() {
																callback.onSuccess();

															}

															@Override
															public void onFailure(
																	final Throwable t) {
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

					@Override
					public void onFailureReceived(
							final FailureResponse failureResponse) {
						new StopMonitorProcess().stop(ctx,
								new StopMonitorCallback() {

									@Override
									public void onSuccess() {
										new ClearResponseNodeProcess()
												.clearResponse(
														responsesLink,
														response,
														new ResponseNodeCleared() {

															@Override
															public void onSuccess() {
																callback.onFailure(failureResponse.exception);
															}

															@Override
															public void onFailure(
																	final Throwable t) {
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

				});
	}
}
