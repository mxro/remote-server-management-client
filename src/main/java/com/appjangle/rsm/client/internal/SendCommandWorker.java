package com.appjangle.rsm.client.internal;

import io.nextweb.Link;
import io.nextweb.LinkList;
import io.nextweb.ListQuery;
import io.nextweb.Node;
import io.nextweb.NodeList;
import io.nextweb.Query;
import io.nextweb.Session;
import io.nextweb.common.Interval;
import io.nextweb.common.Monitor;
import io.nextweb.common.MonitorContext;
import io.nextweb.fn.Closure;
import io.nextweb.fn.ExceptionListener;
import io.nextweb.fn.ExceptionResult;
import io.nextweb.fn.Result;
import io.nextweb.fn.Success;
import io.nextweb.jre.Nextweb;
import io.nextweb.operations.exceptions.ImpossibleListener;
import io.nextweb.operations.exceptions.ImpossibleResult;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import com.appjangle.rsm.client.ClientConfiguration;
import com.appjangle.rsm.client.RsmClient;
import com.appjangle.rsm.client.commands.ComponentOperation;
import com.appjangle.rsm.client.commands.OperationCallback;
import com.appjangle.rsm.client.commands.v01.ComponentCommandData;
import com.appjangle.rsm.client.commands.v01.FailureResponse;
import com.appjangle.rsm.client.commands.v01.SuccessResponse;

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
			public void apply(final LinkList ll) {
				createResponsesNode(responsesLink, ll,
						new ResponsesNodeCallback() {

							@Override
							public void onSuccess(final Node response) {

								submitCommand(response,
										new CommandSubmittedCallback() {

											@Override
											public void onSuccess() {
												installMonitor(
														responsesLink,
														response,
														new MonitorInstalledCallback() {

															@Override
															public void onFailure(
																	final Throwable t) {
																callback.onFailure(t);
															}

															@Override
															public void onChangeDetected(
																	final MonitorContext ctx,
																	final AtomicBoolean responseReceived) {
																SendCommandWorker
																		.checkForResponses(
																				callback,
																				session,
																				responsesLink,
																				response,
																				responseReceived,
																				ctx);
															}
														});
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

	private static interface ResponsesNodeCallback {
		public void onSuccess(Node response);

		public void onFailure(Throwable t);
	}

	private final void createResponsesNode(final Link responsesLink,
			final LinkList ll, final ResponsesNodeCallback callback) {
		final Query responseQuery = responsesLink.appendSafe("r"
				+ (new Random().nextInt()));

		responseQuery.catchImpossible(new ImpossibleListener() {

			@Override
			public void onImpossible(final ImpossibleResult ir) {

				// just try again
				createResponsesNode(responsesLink, ll, callback);

			}
		});

		responseQuery.get(new Closure<Node>() {

			@Override
			public void apply(final Node response) {
				callback.onSuccess(response);
			}
		});

	}

	private static interface CommandSubmittedCallback {
		public void onSuccess();

		public void onFailure(Throwable t);
	}

	private final void submitCommand(final Node response,
			final CommandSubmittedCallback callback) {
		// preparing command
		final ComponentCommandData command = new ComponentCommandData();

		// System.out.println("submitting to: " + conf.getCommandsNode());

		command.setOperation(operation);
		command.setPort(Nextweb
				.getEngine()
				.getFactory()
				.createPort(session, response.uri(),
						conf.getResponseNodeSecret()));

		// synchronizing all changes with server
		session.commit().get(new Closure<Success>() {

			@Override
			public void apply(final Success o) {

				// add to commands node
				final Result<Success> postRequest = session.post(command,
						conf.getCommandsNode(), conf.getCommandsNodeSecret());

				postRequest.catchExceptions(new ExceptionListener() {

					@Override
					public void onFailure(final ExceptionResult r) {
						callback.onFailure(r.exception());
					}
				});

				postRequest.get(new Closure<Success>() {

					@Override
					public void apply(final Success o) {
						callback.onSuccess();
					}
				});

			}
		});

	}

	private static interface MonitorInstalledCallback {
		public void onChangeDetected(MonitorContext ctx,
				AtomicBoolean responseReceived);

		public void onFailure(Throwable t);
	}

	private final void installMonitor(final Link responsesLink,
			final Node response, final MonitorInstalledCallback callback) {

		final AtomicBoolean responseReceived = new AtomicBoolean(false);

		// monitor node for response from server
		final Result<Monitor> monitor = response.monitor(Interval.FAST,
				new Closure<MonitorContext>() {

					@Override
					public void apply(final MonitorContext ctx) {

						callback.onChangeDetected(ctx, responseReceived);

					}

				});

		monitor.catchExceptions(new ExceptionListener() {

			@Override
			public void onFailure(final ExceptionResult r) {
				callback.onFailure(r.exception());
			}
		});

		// trigger once after startup
		monitor.get(new Closure<Monitor>() {

			@Override
			public void apply(final Monitor o) {
				callback.onChangeDetected(new MonitorContext() {

					@Override
					public Node node() {
						return response;
					}

					@Override
					public Monitor monitor() {

						return o;
					}
				}, responseReceived);
			}
		});

		// to assure that monitor does not wait infinitely
		checkForTimeout(responsesLink, response, callback, responseReceived,
				monitor);

	}

	private void checkForTimeout(final Link responsesLink, final Node response,
			final MonitorInstalledCallback callback,
			final AtomicBoolean responseReceived, final Result<Monitor> monitor) {
		new Thread() {

			@Override
			public void run() {

				try {
					int roundsLeft = RsmClient.defaultTimeoutInS;

					while (!responseReceived.get() && roundsLeft > 0) {
						Thread.sleep(1000);
						roundsLeft--;
					}

					if (responseReceived.get()) {
						return;
					}

					final ExceptionListener el = new ExceptionListener() {

						@Override
						public void onFailure(final ExceptionResult r) {
							callback.onFailure(r.exception());
						}
					};

					assert monitor.get() != null;
					final Result<Success> stop = monitor.get().stop();
					stop.catchExceptions(el);

					final Result<Success> removeSafe = responsesLink
							.removeSafe(response);
					removeSafe.catchExceptions(el);

					final Result<Success> close = session.close();

					close.catchExceptions(el);

					monitor.get(new Closure<Monitor>() {

						@Override
						public void apply(final Monitor o) {

							removeSafe.get(new Closure<Success>() {

								@Override
								public void apply(final Success o) {

									close.get(new Closure<Success>() {

										@Override
										public void apply(final Success o) {
											callback.onFailure(new Exception(
													"No response from server received in timeout (5 min)."));
										}
									});

								}
							});

						}
					});

				} catch (final Exception e) {
					callback.onFailure(e);
				}

			}

		}.start();
	}

	public static void checkForResponses(final OperationCallback callback,
			final Session session, final Link responsesLink,
			final Node response, final AtomicBoolean responseReceived,
			final MonitorContext ctx) {
		if (!ctx.node().exists()) {
			return;
		}

		final ListQuery allQuery = ctx.node().selectAll();

		allQuery.get(new Closure<NodeList>() {

			@Override
			public void apply(final NodeList o) {

				for (final Node n : o.nodes()) {

					final Object obj = n.value();
					if (obj instanceof SuccessResponse) {
						responseReceived.set(true);

						ctx.monitor().stop().get(new Closure<Success>() {

							@Override
							public void apply(final Success o) {
								response.removeSafe(n);
								responsesLink.removeSafe(response);

								session.close().get(new Closure<Success>() {

									@Override
									public void apply(final Success o) {

										callback.onSuccess();
									}
								});

							}
						});

						return;
					}

					if (obj instanceof FailureResponse) {
						responseReceived.set(true);

						response.remove(n);
						responsesLink.remove(response);
						ctx.monitor().stop().get(new Closure<Success>() {

							@Override
							public void apply(final Success o) {
								session.close().get(new Closure<Success>() {

									@Override
									public void apply(final Success o) {
										final FailureResponse failureResponse = (FailureResponse) obj;
										callback.onFailure(failureResponse
												.getException());
									}
								});
							}
						});

						return;
					}

				}

			}
		});
	}

}
