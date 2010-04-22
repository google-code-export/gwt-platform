package com.philbeaudoin.gwtp.dispatch.server;

import com.philbeaudoin.gwtp.dispatch.shared.Action;
import com.philbeaudoin.gwtp.dispatch.shared.Result;

public interface InstanceSecureSessionValidatorRegistry extends SecureSessionValidatorRegistry {
    public <A extends Action<R>, R extends Result> void addSecureSessionValidator(Class<A> actionClass, SecureSessionValidator secureSessionValidator);
    public <A extends Action<R>, R extends Result> boolean removeSecureSessionValidator(Class<A> actionClass);
}
