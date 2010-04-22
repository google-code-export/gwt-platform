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

package com.philbeaudoin.gwtp.dispatch.server.guice;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.philbeaudoin.gwtp.dispatch.server.AbstractDispatchServlet;
import com.philbeaudoin.gwtp.dispatch.server.Dispatch;

/**
 * Default servlet that you should use to serve your web application. If you
 * want to make your own implementation, use {@link AbstractDispatchServlet}
 * 
 * @author Christian Goudreau
 * 
 */
@Singleton
public class GuiceDispatchServlet extends AbstractDispatchServlet {
    private static final long serialVersionUID = 3243014503050529029L;
    private final Dispatch dispatch;

    @Inject
    public GuiceDispatchServlet(final Dispatch dispatch) {
        this.dispatch = dispatch;
    }

    @Override
    protected Dispatch getDispatch() {
        return dispatch;
    }
}