package com.philbeaudoin.gwtp.dispatch.server;

import com.philbeaudoin.gwtp.dispatch.shared.Action;
import com.philbeaudoin.gwtp.dispatch.shared.Result;

public interface ClassSecureSessionValidatorRegistry extends SecureSessionValidatorRegistry {
	public <A extends Action<R>, R extends Result> void addSecureSessionValidatorClass(Class<A> actionClass, Class<? extends SecureSessionValidator> secureSessionValidatorClass);
	public <A extends Action<R>, R extends Result> void removeSecureSessionValidatorClass( Class<A> actionClass, Class<? extends SecureSessionValidator> secureSessionValidatorClass);
}