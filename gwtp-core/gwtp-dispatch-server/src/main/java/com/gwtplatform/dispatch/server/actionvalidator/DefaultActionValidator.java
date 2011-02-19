/**
 * Copyright 2010 ArcBees Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.gwtplatform.dispatch.server.actionvalidator;

import com.google.inject.Singleton;

import com.gwtplatform.dispatch.shared.Action;
import com.gwtplatform.dispatch.shared.Result;

/**
 * The default {@link ActionValidator} implementation. It'll accept every
 * action.
 * 
 * @deprecated use {@link com.gwtplatform.dispatch.server.guice.actionvalidator.DefaultActionValidator} instead.
 * 
 * @author Christian Goudreau
 */
@Singleton
@Deprecated
public class DefaultActionValidator implements ActionValidator {
  @Override
  public boolean isValid(Action<? extends Result> action) {
    return true;
  }
}