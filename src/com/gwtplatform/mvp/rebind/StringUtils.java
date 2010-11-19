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

package com.gwtplatform.mvp.rebind;

import java.util.Collection;
import java.util.Iterator;

/**
 * Useful methods within generators.
 * 
 * @author Philippe Beaudoin
 */
public class StringUtils {

  /**
   * Joins a collection of string with a given delimiter.
   * 
   * @param strings The collection of strings to join.
   * @param delimiter The delimiter to use to join them.
   * @return The string built by joining the string with the delimiter.
   */
  static String join(Collection<String> strings, String delimiter) {
    StringBuilder builder = new StringBuilder();
    Iterator<String> iter = strings.iterator();
    while (iter.hasNext()) {
        builder.append(iter.next());
        if (!iter.hasNext()) {
          break;                  
        }
        builder.append(delimiter);
    }
    return builder.toString();
  }
}
