package com.philbeaudoin.gwtp.dispatch.server;

import java.util.Map;

import com.philbeaudoin.gwtp.dispatch.shared.Action;
import com.philbeaudoin.gwtp.dispatch.shared.Result;

public class LazySecureSessionValidatorRegistry implements ClassSecureSessionValidatorRegistry {
	private final Map<Class<? extends Action<?>>, Class<? extends SecureSessionValidator>> validatorClasses;
	private final Map<Class<? extends Action<?>>, SecureSessionValidator> validators;
	
	public LazySecureSessionValidatorRegistry() {
		validatorClasses = new java.util.HashMap<Class<? extends Action<?>>, Class<? extends SecureSessionValidator>>(100);
		validators = new java.util.HashMap<Class<? extends Action<?>>, SecureSessionValidator>(100);
	}
	
	@Override
	public <A extends Action<R>, R extends Result> void addSecureSessionValidatorClass(Class<A> actionClass, Class<? extends SecureSessionValidator> secureSessionValidatorClass) {
		validatorClasses.put(actionClass, secureSessionValidatorClass);
	}

	@Override
	public <A extends Action<R>, R extends Result> void removeSecureSessionValidatorClass(Class<A> actionClass, Class<? extends SecureSessionValidator> secureSessionValidatorClass) {
		Class<? extends SecureSessionValidator> oldValidatorClass = validatorClasses.get(actionClass);
		
		if (oldValidatorClass == secureSessionValidatorClass) {
			validatorClasses.remove(actionClass);
			validators.remove(actionClass);
		}
	}

	protected SecureSessionValidator createInstance(Class<? extends SecureSessionValidator> validatorClass) {
		try {
			return validatorClass.newInstance();
	    } catch ( InstantiationException e ) {
	        e.printStackTrace();
	    } catch ( IllegalAccessException e ) {
	        e.printStackTrace();
	    }	
		return null;
	}

	@Override
	public void clearSecureSessionValidators() {
		validators.clear();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <A extends Action<R>, R extends Result> SecureSessionValidator findSecureSessionValidator(A action) {
		SecureSessionValidator validator = validators.get(action.getClass());
		
		if (validator == null) {
			Class<? extends SecureSessionValidator> validatorClass = validatorClasses.get(action.getClass());
			if (validatorClass != null) {
				validator = createInstance(validatorClass);
			}
			if (validator != null) {
				validators.put((Class<? extends Action<?>>) action.getClass(), validator);
			}
		}
		
		return validator;
	}

}