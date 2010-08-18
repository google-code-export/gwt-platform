package com.gwtplatform.mvp.client;

import com.gwtplatform.mvp.client.proxy.TabDescription;

/**
 * The interface of a {@link View} that is also a {@link TabPanel}. This
 * generic interface can use any {@link TabDescription} class. For tabs that
 * are only identified by a label you can use {@link TabView}.
 * 
 * @author Philippe Beaudoin
 */
public interface TabViewGeneric<T extends TabDescription> extends View, TabPanel<T> {
}
