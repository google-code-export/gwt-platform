package com.philbeaudoin.gwtp.dispatch.server;

import com.philbeaudoin.gwtp.dispatch.shared.Action;
import com.philbeaudoin.gwtp.dispatch.shared.Result;

public interface SecureSessionValidatorRegistry {
	public <A extends Action<R>, R extends Result> SecureSessionValidator findSecureSessionValidator(A action);
	public void clearSecureSessionValidators();
}