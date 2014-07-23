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
package io.vertigo.rest.impl.rest.handler;

import io.vertigo.kernel.lang.Activeable;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;
import io.vertigo.persona.security.KSecurityManager;
import io.vertigo.persona.security.UserSession;
import io.vertigo.rest.rest.exception.SessionException;
import io.vertigo.rest.rest.exception.TooManyRequestException;
import io.vertigo.rest.rest.exception.VSecurityException;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.inject.Named;

import spark.Request;
import spark.Response;

/**
 * Rate limit handler.
 * @author npiedeloup
 */
public final class RateLimitingHandler implements Activeable, RouteHandler {
	private static final long DEFAULT_LIMIT_VALUE = 150; //the rate limit ceiling value
	private static final long DEFAULT_WINDOW_SECONDS = 15 * 60; //the time windows use to limit calls rate
	private static final String RATE_LIMIT_LIMIT = "X-Rate-Limit-Limit"; //the rate limit ceiling for that given request
	private static final String RATE_LIMIT_REMAINING = "X-Rate-Limit-Remaining"; //the number of requests left for the M minute window
	private static final String RATE_LIMIT_RESET = "X-Rate-Limit-Reset"; //the remaining window before the rate limit resets in UTC epoch seconds

	private final KSecurityManager securityManager;
	private final long windowSeconds;
	private final long limitValue;

	private final ConcurrentHashMap<String, AtomicLong> hitsCounter = new ConcurrentHashMap<>();
	private Timer purgeTimer;
	private long lastRateLimitResetTime = System.currentTimeMillis();

	/**
	 * Constructor.
	 * @param windowSeconds the time windows use to limit calls rate
	 * @param limitValue the rate limit ceiling value
	 * @param securityManager Security Manager
	 */
	@Inject
	public RateLimitingHandler(final KSecurityManager securityManager, @Named("windowSeconds") final Option<Long> windowSeconds, @Named("limitValue") final Option<Long> limitValue) {
		Assertion.checkNotNull(securityManager);
		//---------------------------------------------------------------------
		this.securityManager = securityManager;
		this.limitValue = limitValue.getOrElse(DEFAULT_LIMIT_VALUE);
		this.windowSeconds = windowSeconds.getOrElse(DEFAULT_WINDOW_SECONDS);
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		purgeTimer = new Timer("RateLimitWindowReset", true);
		purgeTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				hitsCounter.clear();
				lastRateLimitResetTime = System.currentTimeMillis();
			}
		}, windowSeconds * 1000, windowSeconds * 1000);
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		purgeTimer.cancel();
	}

	/** {@inheritDoc}  */
	public Object handle(final Request request, final Response response, final RouteContext routeContext, final HandlerChain chain) throws VSecurityException, SessionException {
		Assertion.checkNotNull(request);
		Assertion.checkNotNull(response);
		Assertion.checkNotNull(routeContext);
		Assertion.checkNotNull(chain);
		//---------------------------------------------------------------------
		final String userKey = obtainUserKey(request, securityManager.getCurrentUserSession());
		response.header(RATE_LIMIT_LIMIT, String.valueOf(limitValue));
		response.header(RATE_LIMIT_RESET, String.valueOf(windowSeconds - (System.currentTimeMillis() - lastRateLimitResetTime) / 1000));

		final long hits = touch(userKey);
		if (hits > limitValue) {
			throw new TooManyRequestException("Rate limit exceeded");
		}
		response.header(RATE_LIMIT_REMAINING, String.valueOf(limitValue - hits));
		return chain.handle(request, response, routeContext);
	}

	private String obtainUserKey(final Request request, final Option<UserSession> userSession) {
		if (userSession.isDefined()) {
			return userSession.get().getSessionUUID().toString();
		}
		return request.ip() + ":" + request.headers("user-agent");
	}

	private long touch(final String userKey) {
		final AtomicLong value = new AtomicLong(0);
		final AtomicLong oldValue = hitsCounter.putIfAbsent(userKey, value);
		return (oldValue != null ? oldValue : value).incrementAndGet();
	}
}
