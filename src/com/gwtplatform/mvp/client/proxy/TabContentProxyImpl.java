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

import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;

import com.gwtplatform.mvp.client.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.RequestTabsEvent;
import com.gwtplatform.mvp.client.RequestTabsHandlerGeneric;
import com.gwtplatform.mvp.client.Tab;

/**
 * @param <P> {@link Presenter}'s type.
 * 
 * @author Philippe Beaudoin
 */
public class TabContentProxyImpl<T extends TabDescription, P extends Presenter> 
    extends ProxyImpl<P> implements TabContentProxyGeneric<T, P> {

  protected Type<? extends RequestTabsHandlerGeneric<T>> requestTabsEventType;
  protected T tabDescription = null;
  private Tab tab = null;

  /**
   * Creates a {@link Proxy} for a {@link Presenter} that 
   * is meant to be contained within at {@link TabContainerPresenter}.
   * As such, these proxy hold a string that can be displayed on the 
   * tab. 
   */
  public TabContentProxyImpl() {
  }

  @Override
  public T getTabDescription() {
    return tabDescription;
  }
  
  @SuppressWarnings("unchecked")
  @Inject
  protected void bind( EventBus eventBus ) {
    eventBus.addHandler((Type<RequestTabsHandlerGeneric<T>>)requestTabsEventType, new RequestTabsHandlerGeneric<T>(){
      @Override
      public void onRequestTabs(RequestTabsEvent<T> event) {
        tab = event.getTabContainer().addTab( getTabDescription() );
      }
    } );
  }   

}
