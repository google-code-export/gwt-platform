package com.philbeaudoin.gwtp.dispatch.server.guice;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.philbeaudoin.gwtp.dispatch.server.Dispatch;
import com.philbeaudoin.gwtp.dispatch.server.secure.AbstractSecureDispatchServlet;

@Singleton
public class GuiceDispatchServlet extends AbstractSecureDispatchServlet{
	private static final long serialVersionUID = 3243014503050529029L;
	private final Dispatch dispatch;
	
	@Inject
	public GuiceDispatchServlet(final Dispatch dispatch) {
		this.dispatch = dispatch;
	}
	
	@Override
	protected Dispatch getDispatch() {
		return dispatch;
	}
}