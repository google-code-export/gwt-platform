package com.gwtplatform.mvp.client.proxy;

import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.Tab;

/**
 * Proxy for presenters that are associated with a tab. If the presenter 
 * is also associated with a place, use {@link TabContentProxyPlaceGeneric}.
 * For tabs that are only identified by a label you can use 
 * {@link TabContentProxy}.
 * 
 * @author Philippe Beaudoin
 *
 * @param <T> The type of the {@link TabDescription} used by this tab.
 * @param <P> The type of the {@link Presenter} attached to this {@link TabContentProxyPlaceGeneric}.
 */
public interface TabContentProxyGeneric<T extends TabDescription, P extends Presenter> extends Proxy<P> {

  /**
   * Retrieves the description for the tab associated with this presenter.
   * 
   * @return The {@link TabDescription}.
   */
  public T getTabDescription();
  
  /**
   * Retrieves the {@link Tab} object associated with this presenter.
   * 
   * @return The {@link Tab} object, or {@code null} if it had not yet been created.
   */
  public Tab getTab();
  
}
