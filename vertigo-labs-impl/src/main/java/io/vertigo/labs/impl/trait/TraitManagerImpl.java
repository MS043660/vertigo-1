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
package io.vertigo.labs.impl.trait;

import io.vertigo.dynamo.kvdatastore.KVDataStoreManager;
import io.vertigo.dynamo.transaction.KTransactionManager;
import io.vertigo.dynamo.transaction.KTransactionWritable;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;
import io.vertigo.labs.trait.Trait;
import io.vertigo.labs.trait.TraitManager;

import javax.inject.Inject;

public final class TraitManagerImpl implements TraitManager {
	private final KVDataStoreManager kvDataStoreManager;
	private final KTransactionManager transactionManager;

	@Inject
	public TraitManagerImpl(final KVDataStoreManager kvDataStoreManager, KTransactionManager transactionManager) {
		Assertion.checkNotNull(kvDataStoreManager);
		Assertion.checkNotNull(transactionManager);
		//---------------------------------------------------------------------s
		this.kvDataStoreManager = kvDataStoreManager;
		this.transactionManager = transactionManager;
	}

	/** {@inheritDoc} */
	public <T extends Trait> Option<T> findTrait(Class<T> traitClass, String subjectId) {
		Assertion.checkNotNull(traitClass);
		//---------------------------------------------------------------------
		return doFind(subjectId, traitClass.getSimpleName(), traitClass);
	}

	/** {@inheritDoc} */
	public <T extends Trait> void putTrait(Class<T> traitClass, String subjectId, T trait) {
		Assertion.checkNotNull(traitClass);
		//---------------------------------------------------------------------
		doStore(subjectId, traitClass.getSimpleName(), trait);
	}

	/** {@inheritDoc} */
	public <T extends Trait> void deleteTrait(Class<T> traitClass, String subjectId) {
		doDelete(subjectId, traitClass.getSimpleName());
	}

	//=========================================================================
	//=========================================================================
	//=========================================================================

	private void doStore(String subjectId, String traitType, Trait trait) {
		Assertion.checkNotNull(trait);
		Assertion.checkArgNotEmpty(traitType);
		//---------------------------------------------------------------------
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			kvDataStoreManager.getDataStore().put(traitType + ":" + subjectId, trait);
			transaction.commit();
		}
	}

	private <C extends Trait> Option<C> doFind(String subjectId, String traitType, Class<C> clazz) {
		Assertion.checkNotNull(subjectId);
		Assertion.checkArgNotEmpty(traitType);
		//---------------------------------------------------------------------
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			return kvDataStoreManager.getDataStore().find(traitType + ":" + subjectId, clazz);
		}
	}

	private void doDelete(String subjectId, String traitType) {
		Assertion.checkNotNull(subjectId);
		Assertion.checkArgNotEmpty(traitType);
		//---------------------------------------------------------------------
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			kvDataStoreManager.getDataStore().delete(traitType + ":" + subjectId);
			transaction.commit();
		}
	}

}