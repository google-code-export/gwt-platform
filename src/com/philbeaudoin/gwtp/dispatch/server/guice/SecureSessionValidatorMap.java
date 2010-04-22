package com.philbeaudoin.gwtp.dispatch.server.guice;

import com.philbeaudoin.gwtp.dispatch.server.SecureSessionValidator;
import com.philbeaudoin.gwtp.dispatch.shared.Action;
import com.philbeaudoin.gwtp.dispatch.shared.Result;

public interface SecureSessionValidatorMap<A extends Action<R>, R extends Result> {
	public Class<A> getActionClass();
	public Class<? extends SecureSessionValidator> getSecureSessionValidatorClass();
}
