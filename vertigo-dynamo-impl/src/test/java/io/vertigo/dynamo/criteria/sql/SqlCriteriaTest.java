/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2018, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.criteria.sql;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.dynamo.criteria.AbstractCriteriaTest;
import io.vertigo.dynamo.criteria.Criteria;
import io.vertigo.dynamo.criteria.data.movies.Movie2;
import io.vertigo.dynamo.criteria.data.movies.Movie2DataBase;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.store.datastore.SqlUtil;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.util.ListBuilder;

/**
 *
 */
@RunWith(JUnitPlatform.class)
public final class SqlCriteriaTest extends AbstractCriteriaTest {
	@Inject
	protected StoreManager storeManager;
	@Inject
	protected FileManager fileManager;
	@Inject
	protected VTransactionManager transactionManager;
	@Inject
	protected TaskManager taskManager;

	private DtDefinition dtDefinitionMovie;

	@Override
	protected void doSetUp() throws Exception {
		dtDefinitionMovie = DtObjectUtil.findDtDefinition(Movie2.class);
		initMainStore();
	}

	private void initMainStore() {
		//A chaque test on recrée la table famille
		SqlUtil.execRequests(
				transactionManager,
				taskManager,
				getCreateMovies(),
				"TK_INIT_MAIN",
				Optional.empty());

		final Movie2DataBase movie2DataBase = new Movie2DataBase();
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			movie2DataBase.getAllMovies().forEach(movie2 -> storeManager.getDataStore().create(movie2));
			transaction.commit();
		}
	}

	protected final List<String> getCreateMovies() {
		return new ListBuilder<String>()
				.add(" create table movie_2(id BIGINT , TITLE varchar(50), YEAR INT);")
				.add(" create sequence SEQ_MOVIE_2 start with 1 increment by 1;")
				.build();
	}

	@Override
	public void assertCriteria(final long expected, final Criteria<Movie2> criteria) {
		try (VTransactionWritable tx = transactionManager.createCurrentTransaction()) {
			final long count = storeManager.getDataStore().find(dtDefinitionMovie, criteria).size();
			Assert.assertEquals(expected, count);
		}
	}

}
