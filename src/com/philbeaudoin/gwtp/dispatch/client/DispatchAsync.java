/**
 * Copyright 2010 Philippe Beaudoin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.philbeaudoin.gwtp.dispatch.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.philbeaudoin.gwtp.dispatch.shared.Action;
import com.philbeaudoin.gwtp.dispatch.shared.InvalidSessionException;
import com.philbeaudoin.gwtp.dispatch.shared.Result;

/**
 * This class is the default implementation of {@link DefaultDispatchAsync}, which is
 * essentially the client-side access to the
 * {@link com.philbeaudoin.gwtp.dispatch.server.Dispatch} class on the
 * server-side.
 * 
 * @author David Peterson
 * @author Christian Goudreau
 */
public class DispatchAsync extends AbstractDispatchAsync {
    private static final DispatchServiceAsync realService = GWT.create(DispatchService.class);

    private final SessionAccessor secureSessionAccessor;

    public DispatchAsync(ExceptionHandler exceptionHandler, SessionAccessor secureSessionAccessor) {
        super(exceptionHandler);
        this.secureSessionAccessor = secureSessionAccessor;
    }

    public <A extends Action<R>, R extends Result> void execute(final A action, final AsyncCallback<R> callback) {
        String sessionId = secureSessionAccessor.getSessionId();

        realService.execute(sessionId, action, new AsyncCallback<Result>() {
            public void onFailure(Throwable caught) {
                DispatchAsync.this.onFailure(action, caught, callback);
            }

            @SuppressWarnings("unchecked")
            public void onSuccess(Result result) {
                // Note: This cast is a dodgy hack to get around a GWT 1.6 async
                // compiler issue
                DispatchAsync.this.onSuccess(action, (R) result, callback);
            }
        });
    }

    protected <A extends Action<R>, R extends Result> void onFailure(A action, Throwable caught, final AsyncCallback<R> callback) {
        if (caught instanceof InvalidSessionException) {
            secureSessionAccessor.clearSessionId();
        }

        super.onFailure(action, caught, callback);

    }
}
