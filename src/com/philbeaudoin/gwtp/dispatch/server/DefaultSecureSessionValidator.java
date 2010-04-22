package com.philbeaudoin.gwtp.dispatch.server;

public class DefaultSecureSessionValidator implements SecureSessionValidator {
	@Override
	public boolean isValid(String sessionId) {
		return true;
	}
}