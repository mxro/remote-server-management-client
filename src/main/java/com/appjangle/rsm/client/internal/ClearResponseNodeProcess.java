package com.appjangle.rsm.client.internal;

import io.nextweb.Link;
import io.nextweb.LinkList;
import io.nextweb.LinkListQuery;
import io.nextweb.Node;
import io.nextweb.Query;
import io.nextweb.fn.Closure;
import io.nextweb.fn.Result;
import io.nextweb.fn.Success;
import io.nextweb.fn.exceptions.ExceptionListener;
import io.nextweb.fn.exceptions.ExceptionResult;
import one.async.joiner.CallbackLatch;

public class ClearResponseNodeProcess {

    public static interface ResponseNodeCleared {
        public void onSuccess();

        public void onFailure(Throwable t);
    }

    public void clearResponse(final Link responsesLink,
            final Node responseNode, final ResponseNodeCleared callback) {

        final Query reloadRequest = responseNode.reload(1);
        reloadRequest.catchExceptions(new ExceptionListener() {

            @Override
            public void onFailure(final ExceptionResult r) {
                callback.onFailure(r.exception());
            }
        });

        reloadRequest.get(new Closure<Node>() {

            @Override
            public void apply(final Node o) {
                final LinkListQuery allChildren = responseNode.selectAllLinks();

                allChildren.catchExceptions(new ExceptionListener() {

                    @Override
                    public void onFailure(final ExceptionResult r) {
                        callback.onFailure(r.exception());
                    }
                });

                allChildren.get(new Closure<LinkList>() {

                    @Override
                    public void apply(final LinkList o) {

                        final CallbackLatch latch = new CallbackLatch(
                                o.size() + 1) {

                            @Override
                            public void onFailed(final Throwable t) {
                                callback.onFailure(t);
                            }

                            @Override
                            public void onCompleted() {
                                callback.onSuccess();
                            }
                        };

                        for (final Link l : o) {

                            final Result<Success> removeChildReq = responseNode
                                    .removeSafe(l);

                            removeChildReq
                                    .catchExceptions(createExceptionListener(
                                            "remove child " + l, latch));

                            removeChildReq.get(new Closure<Success>() {

                                @Override
                                public void apply(final Success o) {
                                    latch.registerSuccess();
                                }
                            });

                        }

                        final Result<Success> removeResponse = responsesLink
                                .removeSafe(responseNode);

                        removeResponse.catchExceptions(createExceptionListener(
                                "Removing response " + responseNode, latch));

                        removeResponse.get(new Closure<Success>() {

                            @Override
                            public void apply(final Success o) {
                                latch.registerSuccess();
                            }
                        });

                        responseNode.getSession().commit()
                                .get(new Closure<Success>() {

                                    @Override
                                    public void apply(final Success o) {

                                    }
                                });

                    }
                });
            }
        });

    }

    private ExceptionListener createExceptionListener(final String message,
            final CallbackLatch callback) {
        return new ExceptionListener() {

            @Override
            public void onFailure(final ExceptionResult r) {
                callback.registerFail(new Exception(message, r.exception()));
            }
        };
    }
}
