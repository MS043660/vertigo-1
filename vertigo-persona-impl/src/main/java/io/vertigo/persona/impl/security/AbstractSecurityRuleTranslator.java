/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.persona.impl.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.vertigo.commons.peg.PegNoMatchFoundException;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;
import io.vertigo.persona.impl.security.dsl.model.DslMultiExpression;
import io.vertigo.persona.impl.security.dsl.rules.DslParserUtil;
import io.vertigo.util.StringUtil;

abstract class AbstractSecurityRuleTranslator<S extends AbstractSecurityRuleTranslator<S>> {

	private final List<DslMultiExpression> myMultiExpressions = new ArrayList<>();
	private Map<String, String[]> myUserCriteria;

	/**
	 * Set security pattern.
	 * @param securityRule security Pattern (not null, could be empty)
	 * @return this builder
	 */
	public final S withRule(final String securityRule) {
		Assertion.checkNotNull(securityRule);
		//-----
		try {
			final DslMultiExpression myMultiExpression = DslParserUtil.parseMultiExpression(securityRule);
			myMultiExpressions.add(myMultiExpression);
		} catch (final PegNoMatchFoundException e) {
			final String message = StringUtil.format("Echec de lecture de la securityRule {0}\n{1}", securityRule, e.getFullMessage());
			throw WrappedException.wrap(e, message);
		} catch (final Exception e) {
			final String message = StringUtil.format("Echec de lecture de la securityRule {0}\n{1}", securityRule, e.getMessage());
			throw WrappedException.wrap(e, message);
		}
		return (S) this;
	}

	/**
	 * Set criteria.
	 * @param userCriteria Criteria
	 * @return this builder
	 */
	public final S withCriteria(final Map<String, String[]> userCriteria) {
		Assertion.checkNotNull(userCriteria);
		Assertion.checkState(myUserCriteria == null, "criteria was already set : {0}", myUserCriteria);
		//-----
		myUserCriteria = userCriteria;
		return (S) this;
	}

	protected final List<DslMultiExpression> getMultiExpressions() {
		return myMultiExpressions;
	}

	protected final String[] getUserCriteria(final String userProperty) {
		return myUserCriteria.get(userProperty);
	}
}