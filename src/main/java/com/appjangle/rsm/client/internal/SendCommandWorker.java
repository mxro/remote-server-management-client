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
	final OperationCallback callback;

	public SendCommandWorker(final ComponentOperation operation,
			final ClientConfiguration conf, final Session session,
			final OperationCallback callback) {
		super();
		this.operation = operation;
		this.conf = conf;
		this.session = session;
		this.callback = callback;
	}

	public void run() {
		// prepare response node
		final Link responsesLink = session.node(conf.getResponsesNode(),
				conf.getResponseNodeSecret());

		responsesLink.selectAllLinks().get(new Closure<LinkList>() {

			@Override
			public void apply(final LinkList responses) {
				step1_create_response_node(responsesLink, responses);
			}

		});
	}

	private void step1_create_response_node(final Link responsesLink,
			final LinkList responses) {
		new CreateResponsesNodeProcess().createResponsesNode(responsesLink,
				responses, new ResponsesNodeCallback() {

					@Override
					public void onSuccess(final Node response) {
						step2_submit_command(responsesLink, response);
					}

					@Override
					public void onFailure(final Throwable t) {
						callback.onFailure(t);
					}
				});
	}

	private void step2_submit_command(final Link responsesLink,
			final Node response) {
		new SubmitCommandProcess(operation, conf, session).submitCommand(
				response, new CommandSubmittedCallback() {

					@Override
					public void onSuccess() {
						step3_install_monitor(responsesLink, response);
					}

					@Override
					public void onFailure(final Throwable t) {
						callback.onFailure(t);

					}
				});
	}

	private void step3_install_monitor(final Link responsesLink,
			final Node response) {
		new InstallMonitorProcess().installMonitor(responsesLink, response,
				new MonitorInstalledCallback() {

					@Override
					public void onChangeDetected(final MonitorContext ctx,
							final AtomicBoolean responseReceived) {
						step4_check_for_valid_responses(responsesLink,
								response, ctx, responseReceived);
					}

					@Override
					public void onFailure(final Throwable t) {
						callback.onFailure(t);
					}

				});
	}

	private void step4_check_for_valid_responses(final Link responsesLink,
			final Node response, final MonitorContext ctx,
			final AtomicBoolean responseReceived) {
		new ResponseSeekerWorker().checkForResponses(session, ctx.node(),
				new ResponseReceived() {

					@Override
					public void onSuccessReceived(
							final SuccessResponse successResponse) {
						responseReceived.set(true);
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
						responseReceived.set(true);

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
