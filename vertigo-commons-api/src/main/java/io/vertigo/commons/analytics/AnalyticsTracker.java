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
package io.vertigo.commons.analytics;

/**
 * Collect tracker.
 * @author pchretien, npiedeloup
 */
public interface AnalyticsTracker extends AutoCloseable {

	/**
	 * Incrémente une mesure (set si pas présente).
	 * @param measureType Type de mesure
	 * @param value Incrément de la mesure
	 */
	AnalyticsTracker incMeasure(final String measureType, final double value);

	/**
	* Affecte une valeur fixe à la mesure.
	* A utiliser pour les exceptions par exemple (et toute donnée ne s'ajoutant pas).
	* @param measureType Type de mesure
	* @param value valeur de la mesure
	*/
	AnalyticsTracker setMeasure(final String measureType, final double value);

	/**
	 * Affecte une valeur fixe à une meta-donnée.
	 *
	 * @param metaDataName Nom de la meta-donnée
	 * @param value Valeur de la meta-donnée
	 */
	AnalyticsTracker addMetaData(final String metaDataName, final String value);

	/**
	 * Mark this tracker as succeeded.
	 */
	AnalyticsTracker markAsSucceeded();

	/** {@inheritDoc} */
	@Override
	void close();
}
