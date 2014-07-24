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
package io.vertigo.studio.tools;

import io.vertigo.kernel.AppBuilder;
import io.vertigo.kernel.AppBuilder.App;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.util.ClassUtil;
import io.vertigo.studio.tools.generate.GenerateGoal;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Génération des fichiers Java et SQL à patrir de fichiers template freemarker.
 * 
 * @author dchallas, pchretien
 */
public final class NameSpace2Java {

	private NameSpace2Java() {
		super();
	}

	/**
	 * Lancement du générateur de classes Java.
	 * à partir des déclarations (ksp, oom..)
	 * @param args Le premier argument [0] précise le nom du fichier properties de paramétrage
	 */
	public static void main(final String[] args) {
		if (!(args.length == 1)) {
			throw new IllegalArgumentException("Usage : java io.vertigo.studio.tools.NameSpace2Java \"<<pathToParams.properties>>\" ");
		}
		// ---------------------------------------------------------------------
		try (App app = new AppBuilder() //
				.withSilence(true)//
				.withEnvParams(NameSpace2Java.class, args[0])//
				.start()) {
			final List<Class<? extends Goal>> goalClazzList = new ArrayList<>();
			//-------------------------------
			goalClazzList.add(GenerateGoal.class);
			//		goalClazzList.add(ReportingGoal.class);
			//-------------------------------
			process(app.getEnvParams(), goalClazzList);
		} finally {
			Home.stop();
		}
	}

	private static void process(final Properties properties, final List<Class<? extends Goal>> goalClazzList) {
		for (final Class<? extends Goal> goalClazz : goalClazzList) {
			final Goal goal = ClassUtil.newInstance(goalClazz);
			goal.process(properties);
		}
	}
}
