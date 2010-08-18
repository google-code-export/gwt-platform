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

import com.google.gwt.event.shared.GwtEvent.Type;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.TabDescription;
import com.gwtplatform.mvp.client.proxy.TabContentProxyGeneric;
import com.gwtplatform.mvp.client.proxy.TabDescriptionText;

/**
 * A presenter that can display many tabs and the content of one of these tabs. This generic
 * version uses any {@link TabDescription}. If you're interested in tabs that are only
 * described by a text label you can use {@link TabContainerPresenterImpl}.
 *
 * @param <T> The specific type of the {@link TabDescription}.
 * @param <V> The specific type of the {@link View}. Must implement {@link TabPanel<T>}.
 * @param <Proxy_> The specific type of the proxy, must be a {@link TabContainerProxy}. 
 * 
 * @author Philippe Beaudoin
 */
public abstract class TabContainerPresenterGeneric<T extends TabDescription, V extends TabViewGeneric<T>, Proxy_ extends Proxy<?>> 
extends PresenterImpl<V, Proxy_> implements TabContainerPresenter<T>  {

  private final Object tabContentSlot;
  private final Type<RequestTabsHandlerGeneric<T>> requestTabsEventType;

  /**
   * Create a presenter that can display many tabs and the content of one of these tabs.
   * 
   * @param eventBus The {@link EventBus}.
   * @param view The {@link View}.
   * @param proxy The proxy, a {@link TabContainerProxy}.
   * @param tabContentSlot An opaque object identifying the slot in which the main content should be displayed.
   * @param requestTabsEventType The {@link Type} of the object to fire to identify all the displayed tabs.
   */
  @SuppressWarnings("unchecked")
  public TabContainerPresenterGeneric(
      final EventBus eventBus, 
      final V view, 
      final Proxy_ proxy, 
      final Object tabContentSlot,
      final Type<? extends RequestTabsHandlerGeneric<TabDescriptionText>> requestTabsEventType ) {
    super(eventBus, view, proxy);
    this.tabContentSlot = tabContentSlot;
    this.requestTabsEventType = (Type<RequestTabsHandlerGeneric<T>>) requestTabsEventType;
  }
  
  @Override
  public Tab addTab( final T tabDescription ) {
    return getView().addTab( tabDescription );
  }

  @Override
  public void setContent( Object slot, PresenterWidget content ) {
    super.setContent(slot, content);
    if( slot == tabContentSlot ) {
      Tab tab = ((TabContentProxyGeneric<?,?>)((Presenter)content).getProxy()).getTab();
      getView().setActiveTab( tab );
    }
  }

  @Override
  protected void onBind() {
    super.onBind();

    // The following call will trigger a series of call to addTab, so
    // we should make sure we clear all the tabs when unbinding.
    eventBus.fireEvent( new RequestTabsEvent<T>(requestTabsEventType, this) );
  }

  @Override
  protected void onUnbind() {  
    super.onUnbind();

    // The tabs are added indirectly in onBind() via the RequestTabsEvent, so we clear them now.
    getView().removeTabs();
  }

}
