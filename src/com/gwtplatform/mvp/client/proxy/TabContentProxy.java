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
 
package com.gwtplatform.mvp.client.proxy;

import com.gwtplatform.mvp.client.Presenter;

/**
 * Proxy for presenters that are associated with a tab. If the presenter 
 * is also associated with a place, use {@link TabContentProxyPlace}.
 * This interface assumes the use of a {@link TabDescriptionText}. 
 * For more flexibility, implement directly {@link TabContentProxyGeneric}.
 * 
 * @author Philippe Beaudoin
 *
 * @param <P> The type of the {@link Presenter} attached to this {@link TabContentProxyPlace}.
 */
public interface TabContentProxy<P extends Presenter> 
extends TabContentProxyGeneric<TabDescriptionText, P> {
}
