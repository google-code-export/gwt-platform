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

package com.philbeaudoin.gwtp.dispatch.client;

import com.google.gwt.user.client.Cookies;
import com.google.inject.Inject;
import com.philbeaudoin.gwtp.dispatch.shared.SecurityCookie;

/**
 * This class provides access to the session id client side
 * by looking into a cookie on the browser. This will work
 * to prevent XSRF attack.
 * <p />
 * To use this class you have to bind a constant string annotated
 * with {@code @}{@link SecurityCookie} to your desired cookie name.
 * 
 * @author Philippe Beaudoin
 */
public class DefaultSecurityCookieAccessor implements SecurityCookieAccessor {

  @Inject(optional=true)
  public @SecurityCookie String cookieName = null;

  public String getCookieContent() {
    if( cookieName == null )
      return null;
    return Cookies.getCookie( cookieName );
  }
}