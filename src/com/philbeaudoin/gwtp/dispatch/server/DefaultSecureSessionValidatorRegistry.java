package com.philbeaudoin.gwtp.dispatch.server;

import java.util.HashMap;
import java.util.Map;

import com.philbeaudoin.gwtp.dispatch.shared.Action;
import com.philbeaudoin.gwtp.dispatch.shared.Result;

public class DefaultSecureSessionValidatorRegistry implements InstanceSecureSessionValidatorRegistry {
	private final Map<Class<? extends Action<? extends Result>>, SecureSessionValidator> validators;
	
	public DefaultSecureSessionValidatorRegistry() {
		validators = new HashMap<Class<? extends Action<? extends Result>>, SecureSessionValidator>(100);
	}
	
	@Override
	public <A extends Action<R>, R extends Result> void addSecureSessionValidator(Class<A> actionClass, SecureSessionValidator secureSessionValidator) {
		validators.put(actionClass, secureSessionValidator);
	}

	@Override
	public <A extends Action<R>, R extends Result> boolean removeSecureSessionValidator(Class<A> actionClass) {
		return validators.remove(actionClass) != null;
	}

	@Override
	public void clearSecureSessionValidators() {
		validators.clear();
	}

	@Override
	public <A extends Action<R>, R extends Result> SecureSessionValidator findSecureSessionValidator(A action) {
		return validators.get(action.getClass());
	}

}
