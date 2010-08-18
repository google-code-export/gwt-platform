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

package com.gwtplatform.mvp.client.proxy;

/**
 * This class describes the content of a tab to be added to a 
 * {@link com.gwtplatform.mvp.client.TabPanel}. You will usually
 * need to use a derived class to describe the specific attributes
 * of your tab, such as its icon or its text label. For a
 * text-only tab use {@link TabDescriptionText}.
 * 
 * @author Philippe Beaudoin
 */
public interface TabDescription {

  /**
   * Retrieves the history token to show when this tab is
   * displayed. In the fairly typical scenario where a tab directly
   * contains a {@link ProxyPlace}, this should return the name token
   * of the proxy place. In the case of tabs that contain other
   * tab presenters, this should return the name token of a leaf-level
   * proxy.
   * 
   * @return The default history token to show.
   */
  public String getHistoryToken();

  /**
   * A tab priority indicates where it should appear within the tab 
   * strip. A tab with low priority will be placed more towards the 
   * left of the strip. Two tabs with the same priority will be placed
   * in an arbitrary order.
   * 
   * @return The priority.
   */
  public float getPriority();
  
}
