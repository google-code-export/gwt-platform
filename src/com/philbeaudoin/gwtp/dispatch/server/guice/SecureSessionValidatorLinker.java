package com.philbeaudoin.gwtp.dispatch.server.guice;

import java.util.List;

import com.google.inject.Binding;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.philbeaudoin.gwtp.dispatch.server.ClassSecureSessionValidatorRegistry;
import com.philbeaudoin.gwtp.dispatch.server.InstanceSecureSessionValidatorRegistry;
import com.philbeaudoin.gwtp.dispatch.server.SecureSessionValidator;
import com.philbeaudoin.gwtp.dispatch.server.SecureSessionValidatorRegistry;

public class SecureSessionValidatorLinker {
	private SecureSessionValidatorLinker() {}
	
	@SuppressWarnings("unchecked")
	@Inject
	public static void linkValidators(Injector injector, SecureSessionValidatorRegistry registry) {
		List<Binding<SecureSessionValidatorMap>> bindings =  injector.findBindingsByType(TypeLiteral.get(SecureSessionValidatorMap.class));
	
		if (registry instanceof InstanceSecureSessionValidatorRegistry) {
			InstanceSecureSessionValidatorRegistry instanceRegistry = (InstanceSecureSessionValidatorRegistry) registry;
			
			for (Binding<SecureSessionValidatorMap> binding : bindings) {
				Class<? extends SecureSessionValidator> secureSessionValidatorClass = binding.getProvider().get().getSecureSessionValidatorClass();
				SecureSessionValidator secureSessionValidator = injector.getInstance(secureSessionValidatorClass);
				instanceRegistry.addSecureSessionValidator(binding.getProvider().get().getActionClass(), secureSessionValidator);
			}
		} else if (registry instanceof ClassSecureSessionValidatorRegistry) {
			ClassSecureSessionValidatorRegistry classRegistry = (ClassSecureSessionValidatorRegistry) registry;
			
			for (Binding<SecureSessionValidatorMap> binding : bindings) {
				SecureSessionValidatorMap  map = binding.getProvider().get();
				classRegistry.addSecureSessionValidatorClass(map.getActionClass(), map.getSecureSessionValidatorClass());
			}
		}
	}
}