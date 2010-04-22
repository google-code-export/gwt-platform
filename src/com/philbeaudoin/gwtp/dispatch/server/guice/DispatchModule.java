package com.philbeaudoin.gwtp.dispatch.server.guice;

import com.google.inject.AbstractModule;
import com.google.inject.internal.UniqueAnnotations;
import com.philbeaudoin.gwtp.dispatch.server.ActionHandler;
import com.philbeaudoin.gwtp.dispatch.server.DefaultSecureSessionValidator;
import com.philbeaudoin.gwtp.dispatch.server.SecureSessionValidator;
import com.philbeaudoin.gwtp.dispatch.shared.Action;
import com.philbeaudoin.gwtp.dispatch.shared.Result;

public abstract class DispatchModule extends AbstractModule {
	private static class SecureSessionValidatorMapImpl<A extends Action<R>, R extends Result> implements SecureSessionValidatorMap<A, R> {
		private final Class<A> actionClass;
		private final Class<? extends SecureSessionValidator> secureSessionValidator;
		
		public SecureSessionValidatorMapImpl(Class<A> actionClass, Class<? extends SecureSessionValidator> secureSessionValidator) {
			this.actionClass = actionClass;
			this.secureSessionValidator = secureSessionValidator;
		}
		
		@Override
		public Class<A> getActionClass() {
			return actionClass;
		}

		@Override
		public Class<? extends SecureSessionValidator> getSecureSessionValidatorClass() {
			return secureSessionValidator;
		}
	}
	
    private static class ActionHandlerMapImpl<A extends Action<R>, R extends Result> implements ActionHandlerMap<A, R> {

        private final Class<A> actionClass;
        private final Class<? extends ActionHandler<A, R>> handlerClass;

        public ActionHandlerMapImpl(Class<A> actionClass, Class<? extends ActionHandler<A, R>> handlerClass) {
            this.actionClass = actionClass;
            this.handlerClass = handlerClass;
        }

        public Class<A> getActionClass() {
            return actionClass;
        }

        public Class<? extends ActionHandler<A, R>> getActionHandlerClass() {
            return handlerClass;
        }
    }	
	
	@Override
	protected final void configure() {
        install( new ServerDispatchModule() );

        configureHandlers();
	}

	protected abstract void configureHandlers();
	
	protected <A extends Action<R>, R extends Result> void bindSecureHandler(Class<A> actionClass, Class<? extends ActionHandler<A, R>> handlerClass, Class<? extends SecureSessionValidator> secureSessionValidator) {
		bind(SecureSessionValidatorMap.class).annotatedWith(UniqueAnnotations.create()).toInstance(new SecureSessionValidatorMapImpl<A, R>(actionClass, secureSessionValidator));
		bindHandler(actionClass, handlerClass);
	}
	
    protected <A extends Action<R>, R extends Result> void bindHandler(Class<A> actionClass, Class<? extends ActionHandler<A, R>> handlerClass) {
        bind(ActionHandlerMap.class).annotatedWith(UniqueAnnotations.create()).toInstance(new ActionHandlerMapImpl<A, R>(actionClass, handlerClass));
        bind(SecureSessionValidatorMap.class).annotatedWith(UniqueAnnotations.create()).toInstance(new SecureSessionValidatorMapImpl<A, R>(actionClass, DefaultSecureSessionValidator.class));
    }
}