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

package com.philbeaudoin.gwtp.dispatch.server.sessionValidator;

import java.util.Map;

import com.philbeaudoin.gwtp.dispatch.shared.Action;
import com.philbeaudoin.gwtp.dispatch.shared.Result;

/**
 * This is a lazy-loading implementation of the registry. It will only create
 * {@link SessionValidator}s when they are first used. All
 * {@link SessionValidator} implementations <b>must</b> have a public,
 * default constructor.
 * 
 * @author Christian Goudreau
 */
public class LazySessionValidatorRegistry implements ClassSessionValidatorRegistry {
    private final Map<Class<? extends Action<?>>, Class<? extends SessionValidator>> validatorClasses;
    private final Map<Class<? extends Action<?>>, SessionValidator> validators;

    public LazySessionValidatorRegistry() {
        validatorClasses = new java.util.HashMap<Class<? extends Action<?>>, Class<? extends SessionValidator>>(100);
        validators = new java.util.HashMap<Class<? extends Action<?>>, SessionValidator>(100);
    }

    @Override
    public <A extends Action<R>, R extends Result> void addSecureSessionValidatorClass(Class<A> actionClass, Class<? extends SessionValidator> secureSessionValidatorClass) {
        validatorClasses.put(actionClass, secureSessionValidatorClass);
    }

    @Override
    public <A extends Action<R>, R extends Result> void removeSecureSessionValidatorClass(Class<A> actionClass, Class<? extends SessionValidator> secureSessionValidatorClass) {
        Class<? extends SessionValidator> oldValidatorClass = validatorClasses.get(actionClass);

        if (oldValidatorClass == secureSessionValidatorClass) {
            validatorClasses.remove(actionClass);
            validators.remove(actionClass);
        }
    }

    /**
     * Will try to create and instance of {@link SessionValidator}.
     * 
     * @param validatorClass
     *            The class to instantiate
     * @return The class instantiated
     */
    protected SessionValidator createInstance(Class<? extends SessionValidator> validatorClass) {
        try {
            return validatorClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
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
    public <A extends Action<R>, R extends Result> SessionValidator findSecureSessionValidator(A action) {
        SessionValidator validator = validators.get(action.getClass());

        if (validator == null) {
            Class<? extends SessionValidator> validatorClass = validatorClasses.get(action.getClass());
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