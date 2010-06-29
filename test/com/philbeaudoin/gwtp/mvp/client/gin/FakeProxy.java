package com.philbeaudoin.gwtp.mvp.client.gin;

import com.philbeaudoin.gwtp.mvp.client.PresenterImpl;
import com.philbeaudoin.gwtp.mvp.client.proxy.Proxy;

/**
 * A fake proxy used by {@link ReflectionUtilsTest}
 */
interface FakeProxy extends Proxy<PresenterImpl<?, ?>> {
  // Intentionally empty
}