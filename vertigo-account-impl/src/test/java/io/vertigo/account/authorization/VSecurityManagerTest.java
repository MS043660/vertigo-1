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
package io.vertigo.account.authorization;

import java.util.Locale;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.account.authorization.SecurityNames.Permissions;
import io.vertigo.account.authorization.SecurityNames.RecordOperations;
import io.vertigo.account.authorization.SecurityNames.RecordPermissions;
import io.vertigo.account.authorization.metamodel.Permission;
import io.vertigo.account.authorization.metamodel.PermissionName;
import io.vertigo.account.authorization.metamodel.Role;
import io.vertigo.account.authorization.model.Record;
import io.vertigo.account.data.TestUserSession;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.persona.security.UserSession;
import io.vertigo.persona.security.VSecurityManager;

/**
 * @author pchretien
 */
public final class VSecurityManagerTest extends AbstractTestCaseJU4 {

	private static final long DEFAULT_REG_ID = 1L;
	private static final long DEFAULT_DEP_ID = 2L;
	private static final long DEFAULT_COM_ID = 3L;
	private static final long DEFAULT_UTI_ID = 1000L;
	private static final long DEFAULT_TYPE_ID = 10L;
	private static final double DEFAULT_MONTANT_MAX = 100d;

	private long currentDosId = 1;

	@Inject
	private VSecurityManager securityManager;

	@Inject
	private AuthorizationManager accessControlManager;

	@Test
	public void testCreateUserSession() {
		final UserSession userSession = securityManager.createUserSession();
		Assert.assertEquals(Locale.FRANCE, userSession.getLocale());
		Assert.assertEquals(TestUserSession.class, userSession.getClass());
	}

	@Test
	public void testInitCurrentUserSession() {
		final UserSession userSession = securityManager.createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			Assert.assertTrue(securityManager.getCurrentUserSession().isPresent());
			Assert.assertEquals(userSession, securityManager.getCurrentUserSession().get());
		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	public void testAuthenticate() {
		final UserSession userSession = securityManager.createUserSession();
		Assert.assertFalse(userSession.isAuthenticated());
		userSession.authenticate();
	}

	@Test
	public void testNoUserSession() {
		final Optional<UserSession> userSession = securityManager.getCurrentUserSession();
		Assert.assertFalse(userSession.isPresent());
	}

	@Test
	public void testResetUserSession() {
		final UserSession userSession = securityManager.createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			Assert.assertTrue(securityManager.getCurrentUserSession().isPresent());
			//
			accessControlManager.obtainUserPermissions().clearSecurityKeys();
			accessControlManager.obtainUserPermissions().clearPermissions();
			accessControlManager.obtainUserPermissions().clearRoles();
		} finally {
			securityManager.stopCurrentUserSession();
		}
		Assert.assertFalse(securityManager.getCurrentUserSession().isPresent());
	}

	@Test
	public void testRole() {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		final Role admin = definitionSpace.resolve("R_ADMIN", Role.class);
		Assert.assertTrue("R_ADMIN".equals(admin.getName()));
		final Role secretary = definitionSpace.resolve("R_SECRETARY", Role.class);
		Assert.assertTrue("R_SECRETARY".equals(secretary.getName()));
	}

	@Test
	public void testAccess() {
		//TODO
	}

	@Test
	public void testNotAuthorized() {
		//TODO
	}

	@Test
	public void testToString() {
		final Permission admUsr = getPermission(Permissions.PRM_ADMUSR);
		admUsr.toString();
		final Permission admPro = getPermission(Permissions.PRM_ADMPRO);
		admPro.toString();
		/*Pour la couverture de code, et 35min de dette technique.... */
	}

	@Test
	public void testAuthorized() {
		final Permission admUsr = getPermission(Permissions.PRM_ADMUSR);
		final Permission admPro = getPermission(Permissions.PRM_ADMPRO);

		final UserSession userSession = securityManager.<TestUserSession> createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			accessControlManager.obtainUserPermissions().withSecurityKeys("utiId", DEFAULT_UTI_ID)
					.withSecurityKeys("typId", DEFAULT_TYPE_ID)
					.withSecurityKeys("montantMax", DEFAULT_MONTANT_MAX)
					.addPermission(admUsr)
					.addPermission(admPro);

			Assert.assertTrue(accessControlManager.hasPermission(Permissions.PRM_ADMUSR));
			Assert.assertTrue(accessControlManager.hasPermission(Permissions.PRM_ADMPRO));
			Assert.assertFalse(accessControlManager.hasPermission(Permissions.PRM_ADMAPP));
		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testAuthorizedOnEntity() {

		final Record record = createRecord();

		final Record recordTooExpensive = createRecord();
		recordTooExpensive.setAmount(10000d);

		final Record recordOtherUser = createRecord();
		recordOtherUser.setUtiIdOwner(2000L);

		final Record recordOtherUserAndTooExpensive = createRecord();
		recordOtherUserAndTooExpensive.setUtiIdOwner(2000L);
		recordOtherUserAndTooExpensive.setAmount(10000d);

		final Permission recordRead = getPermission(RecordPermissions.PRM_RECORD$READ);
		final UserSession userSession = securityManager.<TestUserSession> createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			accessControlManager.obtainUserPermissions().withSecurityKeys("utiId", DEFAULT_UTI_ID)
					.withSecurityKeys("typId", DEFAULT_TYPE_ID)
					.withSecurityKeys("montantMax", DEFAULT_MONTANT_MAX)
					.addPermission(recordRead);

			final boolean canReadRecord = accessControlManager.hasPermission(RecordPermissions.PRM_RECORD$READ);
			Assert.assertTrue(canReadRecord);

			//read -> MONTANT<=${montantMax} or UTI_ID_OWNER=${utiId}
			Assert.assertTrue(accessControlManager.isAuthorized(record, RecordOperations.READ));
			Assert.assertTrue(accessControlManager.isAuthorized(recordTooExpensive, RecordOperations.READ));
			Assert.assertTrue(accessControlManager.isAuthorized(recordOtherUser, RecordOperations.READ));
			Assert.assertFalse(accessControlManager.isAuthorized(recordOtherUserAndTooExpensive, RecordOperations.READ));

		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testAuthorizedOnEntityGrant() {
		final Record record = createRecord();

		final Record recordTooExpensive = createRecord();
		recordTooExpensive.setAmount(10000d);

		final Record recordOtherUser = createRecord();
		recordOtherUser.setUtiIdOwner(2000L);

		final Record recordOtherUserAndTooExpensive = createRecord();
		recordOtherUserAndTooExpensive.setUtiIdOwner(2000L);
		recordOtherUserAndTooExpensive.setAmount(10000d);

		final Record recordArchivedNotWriteable = createRecord();
		recordArchivedNotWriteable.setEtaCd("ARC");

		final Permission recordCreate = getPermission(RecordPermissions.PRM_RECORD$CREATE);
		final UserSession userSession = securityManager.<TestUserSession> createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			accessControlManager.obtainUserPermissions()
					.withSecurityKeys("utiId", DEFAULT_UTI_ID)
					.withSecurityKeys("typId", DEFAULT_TYPE_ID)
					.withSecurityKeys("montantMax", DEFAULT_MONTANT_MAX)
					.addPermission(recordCreate);

			final boolean canCreateRecord = accessControlManager.hasPermission(RecordPermissions.PRM_RECORD$CREATE);
			Assert.assertTrue(canCreateRecord);

			//read -> MONTANT<=${montantMax} or UTI_ID_OWNER=${utiId}
			Assert.assertTrue(accessControlManager.isAuthorized(record, RecordOperations.READ));
			Assert.assertTrue(accessControlManager.isAuthorized(recordTooExpensive, RecordOperations.READ));
			Assert.assertTrue(accessControlManager.isAuthorized(recordOtherUser, RecordOperations.READ));
			Assert.assertFalse(accessControlManager.isAuthorized(recordOtherUserAndTooExpensive, RecordOperations.READ));
			Assert.assertTrue(accessControlManager.isAuthorized(recordArchivedNotWriteable, RecordOperations.READ));

			//create -> TYP_ID=${typId} and MONTANT<=${montantMax}
			Assert.assertTrue(accessControlManager.isAuthorized(record, RecordOperations.CREATE));
			Assert.assertFalse(accessControlManager.isAuthorized(recordTooExpensive, RecordOperations.CREATE));
			Assert.assertTrue(accessControlManager.isAuthorized(recordOtherUser, RecordOperations.CREATE));
			Assert.assertFalse(accessControlManager.isAuthorized(recordOtherUserAndTooExpensive, RecordOperations.CREATE));
			Assert.assertTrue(accessControlManager.isAuthorized(recordArchivedNotWriteable, RecordOperations.CREATE));

		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testAuthorizedOnEntityOverride() {
		final Record record = createRecord();

		final Record recordTooExpensive = createRecord();
		recordTooExpensive.setAmount(10000d);

		final Record recordOtherUser = createRecord();
		recordOtherUser.setUtiIdOwner(2000L);

		final Record recordOtherUserAndTooExpensive = createRecord();
		recordOtherUserAndTooExpensive.setUtiIdOwner(2000L);
		recordOtherUserAndTooExpensive.setAmount(10000d);

		final Permission recordRead = getPermission(RecordPermissions.PRM_RECORD$READ_HP);
		final UserSession userSession = securityManager.<TestUserSession> createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			accessControlManager.obtainUserPermissions()
					.withSecurityKeys("utiId", DEFAULT_UTI_ID)
					.withSecurityKeys("typId", DEFAULT_TYPE_ID)
					.withSecurityKeys("montantMax", DEFAULT_MONTANT_MAX)
					.addPermission(recordRead);

			final boolean canReadRecord = accessControlManager.hasPermission(RecordPermissions.PRM_RECORD$READ_HP);
			Assert.assertTrue(canReadRecord);

			//read -> MONTANT<=${montantMax} or UTI_ID_OWNER=${utiId}
			Assert.assertTrue(accessControlManager.isAuthorized(record, RecordOperations.READ));
			Assert.assertTrue(accessControlManager.isAuthorized(recordTooExpensive, RecordOperations.READ));
			Assert.assertTrue(accessControlManager.isAuthorized(recordOtherUser, RecordOperations.READ));
			Assert.assertTrue(accessControlManager.isAuthorized(recordOtherUserAndTooExpensive, RecordOperations.READ));

		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testAuthorizedOnEntityEnumAxes() {
		final Record record = createRecord();

		final Record recordTooExpensive = createRecord();
		recordTooExpensive.setAmount(10000d);

		final Record recordOtherUser = createRecord();
		recordOtherUser.setUtiIdOwner(2000L);

		final Record recordOtherUserAndTooExpensive = createRecord();
		recordOtherUserAndTooExpensive.setUtiIdOwner(2000L);
		recordOtherUserAndTooExpensive.setAmount(10000d);

		final Record recordArchivedNotWriteable = createRecord();
		recordArchivedNotWriteable.setEtaCd("ARC");

		final Permission recordWrite = getPermission(RecordPermissions.PRM_RECORD$WRITE);
		final UserSession userSession = securityManager.<TestUserSession> createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			accessControlManager.obtainUserPermissions()
					.withSecurityKeys("utiId", DEFAULT_UTI_ID)
					.withSecurityKeys("typId", DEFAULT_TYPE_ID)
					.withSecurityKeys("montantMax", DEFAULT_MONTANT_MAX)
					.addPermission(recordWrite);

			final boolean canReadRecord = accessControlManager.hasPermission(RecordPermissions.PRM_RECORD$WRITE);
			Assert.assertTrue(canReadRecord);

			//read -> MONTANT<=${montantMax} or UTI_ID_OWNER=${utiId}
			Assert.assertTrue(accessControlManager.isAuthorized(record, RecordOperations.READ));
			Assert.assertTrue(accessControlManager.isAuthorized(recordTooExpensive, RecordOperations.READ));
			Assert.assertTrue(accessControlManager.isAuthorized(recordOtherUser, RecordOperations.READ));
			Assert.assertFalse(accessControlManager.isAuthorized(recordOtherUserAndTooExpensive, RecordOperations.READ));
			Assert.assertTrue(accessControlManager.isAuthorized(recordArchivedNotWriteable, RecordOperations.READ));

			//write -> (UTI_ID_OWNER=${utiId} and ETA_CD<ARC) or (TYP_ID=${typId} and MONTANT<=${montantMax} and ETA_CD<ARC)
			Assert.assertTrue(accessControlManager.isAuthorized(record, RecordOperations.WRITE));
			Assert.assertTrue(accessControlManager.isAuthorized(recordTooExpensive, RecordOperations.WRITE));
			Assert.assertTrue(accessControlManager.isAuthorized(recordOtherUser, RecordOperations.WRITE));
			Assert.assertFalse(accessControlManager.isAuthorized(recordOtherUserAndTooExpensive, RecordOperations.WRITE));
			Assert.assertFalse(accessControlManager.isAuthorized(recordArchivedNotWriteable, RecordOperations.WRITE));

		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testAuthorizedOnEntityTreeAxes() {
		final Record record = createRecord();
		record.setEtaCd("PUB");

		final Record recordOtherType = createRecord();
		recordOtherType.setEtaCd("PUB");
		recordOtherType.setTypId(11L);

		final Record recordOtherEtat = createRecord();
		recordOtherEtat.setEtaCd("CRE");

		final Record recordOtherUser = createRecord();
		recordOtherUser.setEtaCd("PUB");
		recordOtherUser.setUtiIdOwner(2000L);

		final Record recordOtherUserAndTooExpensive = createRecord();
		recordOtherUserAndTooExpensive.setEtaCd("PUB");
		recordOtherUserAndTooExpensive.setUtiIdOwner(2000L);
		recordOtherUserAndTooExpensive.setAmount(10000d);

		final Record recordOtherCommune = createRecord();
		recordOtherCommune.setEtaCd("PUB");
		recordOtherCommune.setComId(3L);

		final Record recordDepartement = createRecord();
		recordDepartement.setEtaCd("PUB");
		recordDepartement.setComId(null);

		final Record recordOtherDepartement = createRecord();
		recordOtherDepartement.setEtaCd("PUB");
		recordOtherDepartement.setDepId(10L);
		recordOtherDepartement.setComId(null);

		final Record recordRegion = createRecord();
		recordRegion.setEtaCd("PUB");
		recordRegion.setDepId(null);
		recordRegion.setComId(null);

		final Record recordNational = createRecord();
		recordNational.setEtaCd("PUB");
		recordNational.setRegId(null);
		recordNational.setDepId(null);
		recordNational.setComId(null);

		final Permission recordNotify = getPermission(RecordPermissions.PRM_RECORD$NOTIFY);
		final UserSession userSession = securityManager.<TestUserSession> createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			accessControlManager.obtainUserPermissions()
					.withSecurityKeys("utiId", DEFAULT_UTI_ID)
					.withSecurityKeys("typId", DEFAULT_TYPE_ID)
					.withSecurityKeys("montantMax", DEFAULT_MONTANT_MAX)
					.withSecurityKeys("geo", new Long[] { DEFAULT_REG_ID, DEFAULT_DEP_ID, null }) //droit sur tout un département
					.addPermission(recordNotify);

			Assert.assertTrue(accessControlManager.hasPermission(RecordPermissions.PRM_RECORD$NOTIFY));

			//grant read -> MONTANT<=${montantMax} or UTI_ID_OWNER=${utiId}
			Assert.assertTrue(accessControlManager.isAuthorized(record, RecordOperations.READ));
			Assert.assertTrue(accessControlManager.isAuthorized(recordOtherUser, RecordOperations.READ));
			Assert.assertFalse(accessControlManager.isAuthorized(recordOtherUserAndTooExpensive, RecordOperations.READ));

			//notify -> TYP_ID=${typId} and ETA_CD=PUB and GEO<=${geo}
			Assert.assertTrue(accessControlManager.isAuthorized(record, RecordOperations.NOTIFY));
			Assert.assertFalse(accessControlManager.isAuthorized(recordOtherType, RecordOperations.NOTIFY));
			Assert.assertFalse(accessControlManager.isAuthorized(recordOtherEtat, RecordOperations.NOTIFY));
			Assert.assertTrue(accessControlManager.isAuthorized(recordOtherUser, RecordOperations.NOTIFY));
			Assert.assertTrue(accessControlManager.isAuthorized(recordOtherUserAndTooExpensive, RecordOperations.NOTIFY));
			Assert.assertTrue(accessControlManager.isAuthorized(recordOtherCommune, RecordOperations.NOTIFY));
			Assert.assertTrue(accessControlManager.isAuthorized(recordDepartement, RecordOperations.NOTIFY));
			Assert.assertFalse(accessControlManager.isAuthorized(recordOtherDepartement, RecordOperations.NOTIFY));
			Assert.assertFalse(accessControlManager.isAuthorized(recordRegion, RecordOperations.NOTIFY));
			Assert.assertFalse(accessControlManager.isAuthorized(recordNational, RecordOperations.NOTIFY));

			//override write -> TYP_ID=${typId} and ETA_CD=PUB and GEO<=${geo}
			//default write don't apply : (UTI_ID_OWNER=${utiId} and ETA_CD<ARC) or (TYP_ID=${typId} and MONTANT<=${montantMax} and ETA_CD<ARC)
			Assert.assertTrue(accessControlManager.isAuthorized(record, RecordOperations.WRITE));
			Assert.assertFalse(accessControlManager.isAuthorized(recordOtherType, RecordOperations.WRITE));
			Assert.assertFalse(accessControlManager.isAuthorized(recordOtherEtat, RecordOperations.WRITE));
			Assert.assertTrue(accessControlManager.isAuthorized(recordOtherUser, RecordOperations.WRITE));
			Assert.assertTrue(accessControlManager.isAuthorized(recordOtherUserAndTooExpensive, RecordOperations.WRITE));
			Assert.assertTrue(accessControlManager.isAuthorized(recordOtherCommune, RecordOperations.WRITE));
			Assert.assertTrue(accessControlManager.isAuthorized(recordDepartement, RecordOperations.WRITE));
			Assert.assertFalse(accessControlManager.isAuthorized(recordOtherDepartement, RecordOperations.WRITE));
			Assert.assertFalse(accessControlManager.isAuthorized(recordRegion, RecordOperations.WRITE));
			Assert.assertFalse(accessControlManager.isAuthorized(recordNational, RecordOperations.WRITE));

		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testNoWriterRole() {
		//TODO
	}

	private Record createRecord() {
		final Record record = new Record();
		record.setDosId(++currentDosId);
		record.setRegId(DEFAULT_REG_ID);
		record.setDepId(DEFAULT_DEP_ID);
		record.setComId(DEFAULT_COM_ID);
		record.setTypId(DEFAULT_TYPE_ID);
		record.setTitle("Record de test #" + currentDosId);
		record.setAmount(DEFAULT_MONTANT_MAX);
		record.setUtiIdOwner(DEFAULT_UTI_ID);
		record.setEtaCd("CRE");
		return record;
	}

	private Permission getPermission(final PermissionName permissionName) {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		return definitionSpace.resolve(permissionName.name(), Permission.class);
	}

}