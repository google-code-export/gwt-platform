/**
 * Copyright 2010 Gwt-Platform
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

package com.gwtplatform.mvp.client;

import com.google.gwt.event.shared.EventHandler;
import com.gwtplatform.mvp.client.proxy.TabDescription;

/**
 * This handler class can use any {@link TabDescription} class. For tabs that
 * are only identified by a label you can use {@link RequestTabsHandler}.
 * 
 * @author Philippe Beaudoin
 *
 * @param <T> The specific type of the {@link TabDescription}.
 */
public interface RequestTabsHandlerGeneric<T extends TabDescription> extends EventHandler {

	/**
	 * Called whenever the {@link TabContainerPresenter} is instantiated
	 * and needs to know which tabs it contains.
	 * 
	 * @param event The event.
	 */
	public void onRequestTabs( RequestTabsEvent<T> event );
	
}
