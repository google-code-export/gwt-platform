package com.philbeaudoin.gwtp.dispatch.server.guice;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.philbeaudoin.gwtp.dispatch.server.LazySecureSessionValidatorRegistry;
import com.philbeaudoin.gwtp.dispatch.server.SecureSessionValidator;

@Singleton
public class GuiceLazySecureSessionValidatorRegistry extends LazySecureSessionValidatorRegistry {
	private final Injector injector;
	
	@Inject
	public GuiceLazySecureSessionValidatorRegistry(Injector injector) {
		this.injector = injector;
	}

	@Override
	protected SecureSessionValidator createInstance(Class<? extends SecureSessionValidator> validatorClass) {
		return injector.getInstance(validatorClass);
	}
}