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
package io.vertigo.dynamo.impl.persistence.datastore;

import io.vertigo.commons.cache.CacheManager;
import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.impl.persistence.datastore.cache.CacheDataStoreConfiguration;
import io.vertigo.dynamo.impl.persistence.datastore.logical.LogicalDataStoreConfiguration;
import io.vertigo.dynamo.persistence.PersistenceManager;
import io.vertigo.dynamo.persistence.datastore.BrokerConfiguration;
import io.vertigo.dynamo.persistence.datastore.DataStore;
import io.vertigo.lang.Assertion;

/**
 * Implémentation Standard du StoreProvider.
 *
 * @author pchretien
 */
public final class BrokerConfigurationImpl implements BrokerConfiguration {
	private final CacheDataStoreConfiguration cacheStoreConfiguration;
	private final LogicalDataStoreConfiguration logicalDataStoreConfiguration;

	/**
	 * Constructeur.
	 *
	 * @param cacheManager Manager de gestion du cache
	 */
	public BrokerConfigurationImpl(final CacheManager cacheManager, final PersistenceManager persistenceManager, final CollectionsManager collectionsManager) {
		Assertion.checkNotNull(cacheManager);
		Assertion.checkNotNull(persistenceManager);
		Assertion.checkNotNull(collectionsManager);
		//-----
		cacheStoreConfiguration = new CacheDataStoreConfiguration(cacheManager);
		logicalDataStoreConfiguration = new LogicalDataStoreConfiguration(persistenceManager, collectionsManager);
	}

	/**
	 * Enregistre si un DT peut être mis en cache et la façon de charger les données.
	 * @param dtDefinition Définition de DT
	 * @param timeToLiveInSeconds Durée de vie du cache
	 * @param isReloadedByList Si ce type d'objet doit être chargé de façon ensembliste ou non
	 */
	@Override
	public void registerCacheable(final DtDefinition dtDefinition, final long timeToLiveInSeconds, final boolean isReloadedByList) {
		Assertion.checkNotNull(dtDefinition);
		//-----
		cacheStoreConfiguration.registerCacheable(dtDefinition, timeToLiveInSeconds, isReloadedByList);
	}

	public CacheDataStoreConfiguration getCacheStoreConfiguration() {
		return cacheStoreConfiguration;
	}

	public LogicalDataStoreConfiguration getLogicalStoreConfiguration() {
		return logicalDataStoreConfiguration;
	}

	@Override
	public void register(final DtDefinition dtDefinition, final DataStore specificStore) {
		logicalDataStoreConfiguration.register(dtDefinition, specificStore);
	}
}