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
package io.vertigo.studio.plugins.mda.authorization;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import io.vertigo.account.authorization.metamodel.Permission;
import io.vertigo.account.authorization.metamodel.Role;
import io.vertigo.account.authorization.metamodel.SecuredEntity;
import io.vertigo.app.Home;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.impl.mda.GeneratorPlugin;
import io.vertigo.studio.mda.MdaResultBuilder;
import io.vertigo.studio.plugins.mda.FileGenerator;
import io.vertigo.studio.plugins.mda.FileGeneratorConfig;
import io.vertigo.util.MapBuilder;

/**
 * Generation des objets relatifs au module Securite.
 *
 * @author pchretien
 */
public final class AuthorizationGeneratorPlugin implements GeneratorPlugin {

	private final String targetSubDir;

	/**
	 * Constructeur.
	 * @param targetSubDir Repertoire de generation des fichiers de ce plugin
	 */
	@Inject
	public AuthorizationGeneratorPlugin(@Named("targetSubDir") final String targetSubDir) {
		//-----
		this.targetSubDir = targetSubDir;
	}

	/** {@inheritDoc} */
	@Override
	public void generate(
			final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder) {
		Assertion.checkNotNull(fileGeneratorConfig);
		Assertion.checkNotNull(mdaResultBuilder);
		//-----
		generateRoles(targetSubDir, fileGeneratorConfig, mdaResultBuilder);

		generatePermissions(targetSubDir, fileGeneratorConfig, mdaResultBuilder);

		generateOperations(targetSubDir, fileGeneratorConfig, mdaResultBuilder);
	}

	private static Collection<Role> getRoles() {
		return Home.getApp().getDefinitionSpace().getAll(Role.class);
	}

	private static List<Permission> getGlobalPermissions() {
		final Collection<Permission> permissions = Home.getApp().getDefinitionSpace().getAll(Permission.class);
		return permissions.stream()
				.filter(o -> !o.getEntityDefinition().isPresent())
				.collect(Collectors.toList());
	}

	private static Collection<SecuredEntity> getSecuredEntities() {
		return Home.getApp().getDefinitionSpace().getAll(SecuredEntity.class);
	}

	private static void generateRoles(final String targetSubDir, final FileGeneratorConfig fileGeneratorConfig, final MdaResultBuilder mdaResultBuilder) {
		generateDictionnary("Roles", targetSubDir, fileGeneratorConfig, mdaResultBuilder, getRoles());
	}

	private static void generatePermissions(final String targetSubDir, final FileGeneratorConfig fileGeneratorConfig, final MdaResultBuilder mdaResultBuilder) {
		generateDictionnary("Permissions", targetSubDir, fileGeneratorConfig, mdaResultBuilder, getGlobalPermissions());
	}

	private static void generateOperations(final String targetSubDir, final FileGeneratorConfig fileGeneratorConfig, final MdaResultBuilder mdaResultBuilder) {
		generateDictionnary("SecuredEntities", targetSubDir, fileGeneratorConfig, mdaResultBuilder, getSecuredEntities());
	}

	private static void generateDictionnary(
			final String objectName,
			final String targetSubDir,
			final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder,
			final Collection<?> values) {
		Assertion.checkArgNotEmpty(objectName);
		Assertion.checkArgument(Character.isUpperCase(objectName.charAt(0)) && !objectName.contains("_"), "Object name ({0}) should be in camelcase and starts with UpperCase", objectName);
		Assertion.checkArgument(objectName.charAt(objectName.length() - 1) == 's', "Object name ({0}) should ends with 's'", objectName);
		//----
		if (!values.isEmpty()) {
			final String lowerCaseObjectName = objectName.toLowerCase(Locale.ROOT);
			final Map<String, Object> model = new MapBuilder<String, Object>()
					.put(lowerCaseObjectName, values)
					.put("classSimpleName", objectName)
					.put("packageName", fileGeneratorConfig.getProjectPackageName() + ".authorization")
					.build();

			FileGenerator.builder(fileGeneratorConfig)
					.withModel(model)
					.withFileName(objectName + ".java")
					.withGenSubDir(targetSubDir)
					.withPackageName(fileGeneratorConfig.getProjectPackageName() + ".authorization")
					.withTemplateName("authorization/template/" + lowerCaseObjectName + ".ftl")
					.build()
					.generateFile(mdaResultBuilder);

		}
	}
}
