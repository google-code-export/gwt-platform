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

/**
 * This class links any registered {@link SecureSessionValidator} instances with
 * the default {@link SecureSessionValidatorRegistry}
 * 
 * @author Christian Goudreau
 * 
 */
public class SecureSessionValidatorLinker {
    private SecureSessionValidatorLinker() {}

    @SuppressWarnings("unchecked")
    @Inject
    public static void linkValidators(Injector injector, SecureSessionValidatorRegistry registry) {
        List<Binding<SecureSessionValidatorMap>> bindings = injector.findBindingsByType(TypeLiteral.get(SecureSessionValidatorMap.class));

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
                SecureSessionValidatorMap map = binding.getProvider().get();
                classRegistry.addSecureSessionValidatorClass(map.getActionClass(), map.getSecureSessionValidatorClass());
            }
        }
    }
}