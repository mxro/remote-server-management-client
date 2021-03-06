package com.appjangle.rsm.client.internal;

import io.nextweb.Link;
import io.nextweb.Node;
import io.nextweb.common.Interval;
import io.nextweb.common.Monitor;
import io.nextweb.common.MonitorContext;
import io.nextweb.fn.Closure;
import io.nextweb.fn.Result;
import io.nextweb.fn.Success;
import io.nextweb.fn.exceptions.ExceptionListener;
import io.nextweb.fn.exceptions.ExceptionResult;
import io.nextweb.operations.callbacks.NodeListener;

import java.util.concurrent.atomic.AtomicBoolean;

import com.appjangle.rsm.client.RsmClient;

public class InstallMonitorProcess {

    public static interface MonitorInstalledCallback {
        public void onChangeDetected(MonitorContext ctx,
                AtomicBoolean responseReceived);

        public void onFailure(Throwable t);
    }

    public final void installMonitor(final Link responsesLink,
            final Node response, final MonitorInstalledCallback callback) {

        final AtomicBoolean responseReceived = new AtomicBoolean(false);

        // monitor node for response from server
        final Result<Monitor> monitor = response.monitor()
                .setInterval(Interval.FAST).addListener(new NodeListener() {

                    @Override
                    public void onWhenNodeChanged(final MonitorContext ctx) {
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
                        // System.out.println("awaiting ..");
                        Thread.sleep(1000);
                        roundsLeft--;
                    }

                    if (responseReceived.get()) {
                        return;
                    }

                    // System.out.println("error");

                    final ExceptionListener el = new ExceptionListener() {

                        @Override
                        public void onFailure(final ExceptionResult r) {
                            callback.onFailure(r.exception());
                        }
                    };

                    assert monitor.get() != null;
                    final Result<Success> stop = monitor.get().stop();
                    stop.catchExceptions(el);

                    // System.out.println("stopping monitor");
                    stop.get(new Closure<Success>() {

                        @Override
                        public void apply(final Success o) {
                            // System.out.println("monitor stopped");
                            final Result<Success> removeSafe = responsesLink
                                    .removeSafe(response);

                            removeSafe.catchExceptions(el);

                            removeSafe.get(new Closure<Success>() {

                                @Override
                                public void apply(final Success o) {

                                    callback.onFailure(new Exception(
                                            "No response from server received in timeout (5 min)."));

                                }
                            });

                        }
                    });

                    response.getSession().commit().get();

                } catch (final Exception e) {
                    callback.onFailure(e);
                }

            }

        }.start();
    }

}
