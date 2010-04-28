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

package com.philbeaudoin.gwtp.dispatch.server;

import java.util.List;

import com.philbeaudoin.gwtp.dispatch.server.actionHandler.ActionHandler;
import com.philbeaudoin.gwtp.dispatch.server.actionHandler.ActionHandlerRegistry;
import com.philbeaudoin.gwtp.dispatch.server.actionHandler.ActionResult;
import com.philbeaudoin.gwtp.dispatch.server.sessionValidator.SessionValidator;
import com.philbeaudoin.gwtp.dispatch.server.sessionValidator.SessionValidatorRegistry;
import com.philbeaudoin.gwtp.dispatch.shared.Action;
import com.philbeaudoin.gwtp.dispatch.shared.ActionException;
import com.philbeaudoin.gwtp.dispatch.shared.Result;
import com.philbeaudoin.gwtp.dispatch.shared.ServiceException;
import com.philbeaudoin.gwtp.dispatch.shared.UnsupportedActionException;

/**
 * Abstract implementation of {@link Dispatch} interface. This is the core of
 * {@link Dispatch} server implementation.
 * 
 * @author Christian Goudreau
 * @author David Peterson
 */
public abstract class AbstractDispatch implements Dispatch {
  private static class DefaultExecutionContext implements ExecutionContext {
    private final AbstractDispatch dispatch;
    private final String sessionId;
    private final List<ActionResult<?, ?>> actionResults;

    private DefaultExecutionContext(String sessionId, AbstractDispatch dispatch) {
      this.sessionId = sessionId;
      this.dispatch = dispatch;
      this.actionResults = new java.util.ArrayList<ActionResult<?, ?>>();
    }

    @Override
    public <A extends Action<R>, R extends Result> R execute(A action)
        throws ActionException, ServiceException {
      R result = dispatch.doExecute(sessionId, action, this);
      actionResults.add(new ActionResult<A, R>(action, result, true));
      return result;
    }

    @Override
    public <A extends Action<R>, R extends Result> void undo(A action, R result)
        throws ActionException, ServiceException {
      dispatch.doExecute(sessionId, action, this);
      actionResults.add(new ActionResult<A, R>(action, result, false));
    }

    /**
     * Rolls back all logged executed actions.
     * 
     * @throws ActionException
     *           If there is an action exception while rolling back.
     * @throws ServiceException
     *           If there is a low level problem while rolling back.
     */
    private void rollback() throws ActionException, ServiceException {
      DefaultExecutionContext ctx = new DefaultExecutionContext(sessionId, dispatch);
      for (int i = actionResults.size() - 1; i >= 0; i--) {
        ActionResult<?, ?> actionResult = actionResults.get(i);
        rollback(actionResult, ctx);
      }
    }

    private <A extends Action<R>, R extends Result> void rollback(
        ActionResult<A, R> actionResult, ExecutionContext ctx)
        throws ActionException, ServiceException {
      if (actionResult.isExecuted())
        dispatch
            .doUndo(sessionId, actionResult.getAction(), actionResult.getResult(), ctx);
      else
        dispatch.doExecute(sessionId, actionResult.getAction(), ctx);
    }
  };

  protected abstract ActionHandlerRegistry getHandlerRegistry();
  protected abstract SessionValidatorRegistry getSecureSessionValidatorRegistry();

  @Override
  public <A extends Action<R>, R extends Result> R execute(A action) throws ActionException, ServiceException {
      return execute(null, action);
  } 
  
  @Override
  public <A extends Action<R>, R extends Result> R execute(String sessionId, A action)
      throws ActionException, ServiceException {
    DefaultExecutionContext ctx = new DefaultExecutionContext(sessionId, this);
    try {
      return doExecute(sessionId, action, ctx);
    } catch (ActionException e) {
      ctx.rollback();
      throw e;
    } catch (ServiceException e) {
      ctx.rollback();
      throw e;
    }
  }
  
  @Override
  public <A extends Action<R>, R extends Result> void undo(A action, R result) throws ActionException, ServiceException {
      undo(null, action, result);
  }

  @Override
  public <A extends Action<R>, R extends Result> void undo(String sessionId, A action, R result)
      throws ActionException, ServiceException {
    DefaultExecutionContext ctx = new DefaultExecutionContext(sessionId, this);
    try {
      doUndo(sessionId, action, result, ctx);
    } catch (ActionException e) {
      ctx.rollback();
      throw e;
    } catch (ServiceException e) {
      ctx.rollback();
      throw e;
    }
  }

  /**
   * Every single action will be executed by this function and validated by the
   * {@link SessionValidator}.
   * 
   * @param <A>
   *          Type of associated {@link Action}
   * @param <R>
   *          Type of associated {@link Result}
   * @param action
   *          The {@link Action} to execute
   * @param ctx
   *          The {@link ExecutionContext} associated with the {@link Action}
   * @return The {@link Result} to the client
   * @throws ActionException
   * @throws ServiceException
   */
  private <A extends Action<R>, R extends Result> R doExecute(String sessionId, A action,
      ExecutionContext ctx) throws ActionException, ServiceException {
    ActionHandler<A, R> handler = findHandler(action);
    
    SessionValidator secureSessionValidator = findSecureSessionValidator(action);

    try {
      if (secureSessionValidator.isValid(sessionId))
        return handler.execute(action, ctx);
      else
        throw new ActionException("Insufficient rights");
    } catch (ActionException e) {
      throw e;
    } catch( Exception e ) {
      String newMessage = "Service exception executing action \"" + action.getClass().getSimpleName() + "\"";
      if( e.getMessage() != null )
        newMessage += ": " + e.getMessage();
      ServiceException rethrown = new ServiceException( newMessage ); 
      rethrown.initCause(e);
      throw rethrown;
    }
  }

  private <A extends Action<R>, R extends Result> void doUndo(String sessionId, A action,
      R result, ExecutionContext ctx) throws ActionException, ServiceException {
      
    SessionValidator secureSessionValidator = findSecureSessionValidator(action);
      
    ActionHandler<A, R> handler = findHandler(action);
    try {
      if (secureSessionValidator.isValid(sessionId))
        handler.undo(action, result, ctx);
    } catch (ActionException e) {
      throw e;
    } catch (Exception cause) {
      throw new ServiceException(cause);
    }
  }

  private <A extends Action<R>, R extends Result> ActionHandler<A, R> findHandler(
      A action) throws UnsupportedActionException {
    ActionHandler<A, R> handler = getHandlerRegistry().findHandler(action);
    if (handler == null)
      throw new UnsupportedActionException(action);

    return handler;
  }

  private <A extends Action<R>, R extends Result> SessionValidator findSecureSessionValidator(
      A action) throws UnsupportedActionException {
    SessionValidator secureSessionValidator = getSecureSessionValidatorRegistry()
        .findSecureSessionValidator(action);

    if (secureSessionValidator == null)
      throw new UnsupportedActionException(action);

    return secureSessionValidator;
  }
}