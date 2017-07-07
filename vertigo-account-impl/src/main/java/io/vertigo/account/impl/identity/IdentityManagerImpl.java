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
package io.vertigo.account.impl.identity;

import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.account.identity.AccountStore;
import io.vertigo.account.identity.IdentityManager;
import io.vertigo.account.identity.IdentityRealm;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.lang.Assertion;

/**
 * @author pchretien
 */
public final class IdentityManagerImpl implements IdentityManager {
	private final AccountStore accountStorePlugin;
	private final Optional<IdentityRealm> identityRealmPlugin;
	private final VFile defaultPhoto;

	/**
	 * Constructor.
	 * @param accountStorePlugin the account store plugin
	 * @param identityRealmPlugin the identity realm plugin
	 * @param fileManager the file manager
	 */
	@Inject
	public IdentityManagerImpl(
			final AccountStorePlugin accountStorePlugin,
			final Optional<IdentityRealmPlugin> identityRealmPlugin,
			final FileManager fileManager) {
		Assertion.checkNotNull(accountStorePlugin);
		Assertion.checkNotNull(identityRealmPlugin);
		Assertion.checkNotNull(fileManager);
		//-----
		this.accountStorePlugin = accountStorePlugin;
		this.identityRealmPlugin = Optional.ofNullable(identityRealmPlugin.orElse(null));//necessaire car Optional<IdentityRealmPlugin> n'est pas Optional<identityRealm> :)
		defaultPhoto = fileManager.createFile(
				"defaultPhoto.png",
				"image/png",
				IdentityManagerImpl.class.getResource("defaultPhoto.png"));
	}

	/** {@inheritDoc} */
	@Override
	public VFile getDefaultPhoto() {
		return defaultPhoto;
	}

	/** {@inheritDoc} */
	@Override
	public AccountStore getStore() {
		return accountStorePlugin;
	}

	/** {@inheritDoc} */
	@Override
	public Optional<IdentityRealm> getIdentityRealm() {
		return identityRealmPlugin;
	}

}
