package com.appjangle.rsm.client;

import io.nextweb.Session;
import io.nextweb.fn.Closure;
import io.nextweb.fn.ExceptionListener;
import io.nextweb.fn.ExceptionResult;
import io.nextweb.fn.Result;
import io.nextweb.fn.Success;
import io.nextweb.jre.Nextweb;

import com.appjangle.rsm.client.commands.ComponentOperation;
import com.appjangle.rsm.client.commands.OperationCallback;
import com.appjangle.rsm.client.internal.SendCommandProcess;

public class RsmClient {

    public static int defaultTimeoutInS = 110;

    /**
     * Run an operation on the server for a specific component.
     * 
     * @param operation
     * @param forId
     * @param conf
     * @param callback
     */
    public static void performCommand(final ComponentOperation operation,
            final ClientConfiguration conf, final OperationCallback callback) {

        assert operation != null;

        final Session session = Nextweb.createSession();

        new SendCommandProcess(operation, conf, session,
                new OperationCallback() {

                    @Override
                    public void onSuccess() {
                        final Result<Success> closeRequest = session.close();

                        closeRequest.catchExceptions(new ExceptionListener() {

                            @Override
                            public void onFailure(final ExceptionResult r) {
                                callback.onFailure(r.exception());
                            }
                        });

                        closeRequest.get(new Closure<Success>() {

                            @Override
                            public void apply(final Success o) {
                                callback.onSuccess();
                            }
                        });
                    }

                    @Override
                    public void onFailure(final Throwable t) {
                        final Result<Success> closeRequest = session.close();

                        final Exception exception = new Exception(
                                "Failure while sending command.", t);

                        closeRequest.catchExceptions(new ExceptionListener() {

                            @Override
                            public void onFailure(final ExceptionResult r) {
                                callback.onFailure(r.exception());
                            }
                        });

                        closeRequest.get(new Closure<Success>() {

                            @Override
                            public void apply(final Success o) {

                                callback.onFailure(exception);
                            }
                        });
                    }
                }).run();

    }
}
