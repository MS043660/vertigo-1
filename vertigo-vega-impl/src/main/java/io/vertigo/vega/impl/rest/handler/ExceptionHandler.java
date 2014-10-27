/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.vega.impl.rest.handler;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.VUserException;
import io.vertigo.vega.rest.engine.JsonEngine;
import io.vertigo.vega.rest.exception.SessionException;
import io.vertigo.vega.rest.exception.TooManyRequestException;
import io.vertigo.vega.rest.exception.VSecurityException;
import io.vertigo.vega.rest.validation.UiMessageStack;
import io.vertigo.vega.rest.validation.ValidationUserException;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import spark.Request;
import spark.Response;

import com.google.gson.JsonSyntaxException;

/**
 * Exceptions handler. Convert exception to response.
 * @author npiedeloup
 */
public final class ExceptionHandler implements RouteHandler {

	private static final int SC_UNPROCESSABLE_ENTITY = 422; //server understands the content syntaxe but not semanticly
	private static final int SC_TOO_MANY_REQUEST = 429; //RFC 6585 : TooManyRequest in time window
	private final Logger log = Logger.getLogger(getClass());
	private final JsonEngine jsonWriterEngine;

	ExceptionHandler(final JsonEngine jsonWriterEngine) {
		Assertion.checkNotNull(jsonWriterEngine);
		//---------------------------------------------------------------------
		this.jsonWriterEngine = jsonWriterEngine;
	}

	/** {@inheritDoc} */
	public Object handle(final Request request, final Response response, final RouteContext routeContext, final HandlerChain chain) {
		try {
			return chain.handle(request, response, routeContext);
		} catch (final ValidationUserException e) {
			final UiMessageStack uiMessageStack = routeContext.getUiMessageStack();
			e.flushToUiMessageStack(uiMessageStack);
			return sendJsonUiMessageStack(SC_UNPROCESSABLE_ENTITY, uiMessageStack, response);
		} catch (final VUserException e) {
			final UiMessageStack uiMessageStack = routeContext.getUiMessageStack();
			uiMessageStack.error(e.getMessage()); //With UserException only message is important
			return sendJsonUiMessageStack(SC_UNPROCESSABLE_ENTITY, uiMessageStack, response);
		} catch (final SessionException e) {
			return sendJsonError(HttpServletResponse.SC_UNAUTHORIZED, e, response);
		} catch (final VSecurityException e) {
			return sendJsonError(HttpServletResponse.SC_FORBIDDEN, e, response);
		} catch (final JsonSyntaxException e) {
			return sendJsonError(HttpServletResponse.SC_BAD_REQUEST, e, response);
		} catch (final TooManyRequestException e) {
			return sendJsonError(SC_TOO_MANY_REQUEST, e, response);
		} catch (final Throwable e) {//NOSONAR : In every situation we need to try to respond client that server got a pb
			log.error("Internal Server Error", e);
			return sendJsonError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e, response);
		}
	}

	private Object sendJsonUiMessageStack(final int statusCode, final UiMessageStack uiMessageStack, final Response response) {
		response.status(statusCode);
		response.type("application/json;charset=UTF-8");
		return jsonWriterEngine.toJson(uiMessageStack);
	}

	private Object sendJsonError(final int statusCode, final Throwable e, final Response response) {
		response.status(statusCode);
		response.type("application/json;charset=UTF-8");
		return jsonWriterEngine.toJsonError(e);
	}
}
