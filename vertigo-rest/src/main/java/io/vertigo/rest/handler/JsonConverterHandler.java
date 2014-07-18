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
package io.vertigo.rest.handler;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.rest.EndPointDefinition;
import io.vertigo.rest.EndPointParam;
import io.vertigo.rest.engine.GoogleJsonEngine;
import io.vertigo.rest.engine.UiContext;
import io.vertigo.rest.engine.UiObject;
import io.vertigo.rest.exception.SessionException;
import io.vertigo.rest.exception.VSecurityException;
import io.vertigo.rest.security.UiSecurityTokenManager;

import java.util.Date;

import spark.Request;
import spark.Response;

/**
 * Params handler. Extract and Json convert.
 * @author npiedeloup
 */
final class JsonConverterHandler implements RouteHandler {
	private static final String ACCESS_TOKEN_MANDATORY = "AccessToken mandatory";
	private static final String FORBIDDEN_OPERATION_FIELD_MODIFICATION = "Can't modify field:";

	private static final GoogleJsonEngine jsonWriterEngine = new GoogleJsonEngine();
	private static final GoogleJsonEngine jsonReaderEngine = jsonWriterEngine;

	private final UiSecurityTokenManager uiSecurityTokenManager;
	private final EndPointDefinition endPointDefinition;

	JsonConverterHandler(final UiSecurityTokenManager uiSecurityTokenManager, final EndPointDefinition endPointDefinition) {
		Assertion.checkNotNull(uiSecurityTokenManager);
		Assertion.checkNotNull(endPointDefinition);
		//---------------------------------------------------------------------
		this.uiSecurityTokenManager = uiSecurityTokenManager;
		this.endPointDefinition = endPointDefinition;
	}

	/** {@inheritDoc}  */
	public Object handle(final Request request, final Response response, final RouteContext routeContext, final HandlerChain chain) throws VSecurityException, SessionException {
		for (final EndPointParam endPointParam : endPointDefinition.getEndPointParams()) {
			final Object value;
			switch (endPointParam.getParamType()) {
				case Body:
					value = readValue(request.body(), endPointParam, uiSecurityTokenManager);
					break;
				case Path:
					value = readPrimitiveValue(request.params(endPointParam.getName()), endPointParam);
					break;
				case Query:
					value = readPrimitiveValue(request.queryParams(endPointParam.getName()), endPointParam);
					break;
				default:
					throw new IllegalArgumentException("RestParamType : " + endPointParam.getParamType());
			}
			routeContext.setParamValue(endPointParam, value);
		}
		final Object result = chain.handle(request, response, routeContext);
		return writeValue(result, endPointDefinition, uiSecurityTokenManager);
	}

	private static Object readPrimitiveValue(final String json, final EndPointParam endPointParam) {
		final Class<?> paramClass = endPointParam.getType();
		if (json == null) {
			return null;
		} else if (paramClass.isPrimitive()) {
			return jsonReaderEngine.fromJson(json, paramClass);
		} else if (String.class.isAssignableFrom(paramClass)) {
			return json;
		} else if (Integer.class.isAssignableFrom(paramClass)) {
			return Integer.valueOf(json);
		} else if (Long.class.isAssignableFrom(paramClass)) {
			return Long.valueOf(json);
		} else if (Date.class.isAssignableFrom(paramClass)) {
			return jsonReaderEngine.fromJson(json, paramClass);
		} else {
			throw new RuntimeException("Unsupported type " + paramClass.getSimpleName());
		}
		//return jsonReaderEngine.fromJson(json, paramClass);
	}

	private static Object readValue(final String json, final EndPointParam endPointParam, final UiSecurityTokenManager uiSecurityTokenManager) throws VSecurityException {
		final Class<?> paramClass = endPointParam.getType();
		if (json == null) {
			return null;
		} else if (String.class.isAssignableFrom(paramClass)) {
			return json;
		} else if (Integer.class.isAssignableFrom(paramClass)) {
			return Integer.valueOf(json);
		} else if (Long.class.isAssignableFrom(paramClass)) {
			return Long.valueOf(json);
		} else if (DtObject.class.isAssignableFrom(paramClass)) {
			final UiObject<DtObject> uiObject = jsonReaderEngine.<DtObject> uiObjectFromJson(json, (Class<DtObject>) paramClass);
			uiObject.setInputKey("");
			checkUnauthorizedFieldModifications(uiObject, endPointParam);

			if (endPointParam.isNeedServerSideToken()) {
				final String accessToken = uiObject.getServerSideToken();
				if (accessToken == null) {
					throw new VSecurityException(ACCESS_TOKEN_MANDATORY); //same message for no AccessToken or bad AccessToken
				}
				final DtObject serverSideObject;
				if (endPointParam.isConsumeServerSideToken()) {
					serverSideObject = uiSecurityTokenManager.getAndRemove(accessToken); //TODO if exception : token is consume ?
				} else {
					serverSideObject = uiSecurityTokenManager.get(accessToken);
				}
				if (serverSideObject == null) {
					throw new VSecurityException(ACCESS_TOKEN_MANDATORY); //same message for no AccessToken or bad AccessToken
				}
				uiObject.setServerSideObject(serverSideObject);
			}
			return uiObject;
		} else if (UiContext.class.isAssignableFrom(paramClass)) {
			throw new RuntimeException("Not implemented yet");
		} else {
			return jsonReaderEngine.fromJson(json, paramClass);
		}
	}

	private static void checkUnauthorizedFieldModifications(final UiObject<DtObject> uiObject, final EndPointParam endPointParam) throws VSecurityException {
		for (final String excludedField : endPointParam.getExcludedFields()) {
			if (uiObject.isModified(excludedField)) {
				throw new VSecurityException(FORBIDDEN_OPERATION_FIELD_MODIFICATION + excludedField);
			}
		}
	}

	private static String writeValue(final Object value, final EndPointDefinition endPointDefinition, final UiSecurityTokenManager uiSecurityTokenManager) {
		if (endPointDefinition.isServerSideSave()) {
			if (UiContext.class.isInstance(value)) {
				throw new RuntimeException("Not implemented yet");
				//} else if (UiList.class.isInstance(value)) {
				//	throw new RuntimeException("Not implemented yet");
			} else if (DtObject.class.isInstance(value)) {
				final String tokenId = uiSecurityTokenManager.put((DtObject) value);
				return jsonWriterEngine.toJsonWithTokenId(value, tokenId, endPointDefinition.getExcludedFields());
			} else {
				throw new RuntimeException("Return type can't be protected :" + (value != null ? value.getClass().getSimpleName() : "null"));
			}
		}
		//If value is null (and no exception occured), we tell client it's OK
		return jsonWriterEngine.toJson(value != null ? value : "OK", endPointDefinition.getExcludedFields());
	}
}