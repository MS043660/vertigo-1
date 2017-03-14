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
package io.vertigo.dynamo.impl.database.vendor.oracle;

import java.util.stream.Collectors;

import io.vertigo.dynamo.database.vendor.SqlDialect;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;

final class OracleDialect implements SqlDialect {
	/** {@inheritDoc} */
	@Override
	public String createInsertQuery(final DtDefinition dtDefinition, final String sequencePrefix, final String tableName) {
		final DtField idField = dtDefinition.getIdField().orElseThrow(() -> new IllegalStateException("no ID found"));
		return new StringBuilder()
				.append("begin insert into ").append(tableName).append(" (")
				.append(dtDefinition.getFields()
						.stream()
						.filter(DtField::isPersistent)
						.map(DtField::getName)
						.collect(Collectors.joining(", ")))
				.append(") values (")
				.append(dtDefinition.getFields()
						.stream()
						.filter(DtField::isPersistent)
						.map(dtField -> mapField(dtField, sequencePrefix, tableName))
						.collect(Collectors.joining(", ")))
				.append(") returning ").append(idField.getName())
				.append(" into %DTO.").append(idField.getName()).append("%;").append("end;")
				.toString();
	}

	private static String mapField(final DtField dtField, final String sequencePrefix, final String tableName) {
		if (dtField.getType() != DtField.FieldType.ID) {
			return " #DTO." + dtField.getName() + '#';
		}
		return getSequenceName(sequencePrefix, tableName) + ".nextval ";
	}

	/**
	 * Nom de la séquence utilisée lors des inserts
	 * @param dtDefinition Définition du DT mappé
	 * @return String Nom de la sequence
	 */
	private static String getSequenceName(final String sequencePrefix, final String tableName) {
		//oracle n'autorise pas de sequence de plus de 30 char.
		String sequenceName = sequencePrefix + tableName;
		if (sequenceName.length() > 30) {
			sequenceName = sequenceName.substring(0, 30);
		}
		return sequenceName;
	}

	/** {@inheritDoc} */
	@Override
	public void appendMaxRows(final StringBuilder request, final Integer maxRows) {
		request.append(" and rownum <= ").append(maxRows);
	}

	/** {@inheritDoc} */
	@Override
	public boolean generatedKeys() {
		return false;
	}
}