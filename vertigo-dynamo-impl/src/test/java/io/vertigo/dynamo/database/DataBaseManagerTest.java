package io.vertigo.dynamo.database;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.dynamo.database.DataBaseManager;
import io.vertigo.dynamo.database.connection.KConnection;
import io.vertigo.dynamo.database.statement.KCallableStatement;
import io.vertigo.dynamo.database.statement.KPreparedStatement;
import io.vertigo.dynamo.database.statement.QueryResult;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.Formatter;
import io.vertigo.dynamo.domain.metamodel.KDataType;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamock.domain.famille.Famille;
import io.vertigo.dynamox.domain.formatter.FormatterDefault;
import io.vertigo.kernel.Home;

import java.sql.SQLException;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author pchretien
* @version $Id: DataBaseManagerTest.java,v 1.3 2014/01/20 17:51:47 pchretien Exp $
 */
public class DataBaseManagerTest extends AbstractTestCaseJU4 {
	private static final String CAMPANULACEAE = "Campanulaceae";
	private static final String BALSAMINACEAE = "Balsaminaceae";
	private static final String AIZOACEAE = "Aizoaceae";
	@Inject
	private DataBaseManager dataBaseManager;

	@Override
	protected void doSetUp() throws Exception {
		//A chaque test on recrée la table famille
		final KConnection connection = dataBaseManager.getConnectionProvider().obtainConnection();
		execCallableStatement(connection, "create table famille(fam_id BIGINT , LIBELLE varchar(255));");
	}

	@Override
	protected void doTearDown() throws Exception {
		//A chaque fin de test on arrête la base.
		final KConnection connection = dataBaseManager.getConnectionProvider().obtainConnection();
		execCallableStatement(connection, "shutdown;");
	}

	@Test
	public void testConnection() throws Exception {
		final KConnection connection = dataBaseManager.getConnectionProvider().obtainConnection();
		Assert.assertNotNull(connection);
		connection.commit();
	}

	private void execCallableStatement(final KConnection connection, final String sql) throws SQLException {
		final KCallableStatement callableStatement = dataBaseManager.createCallableStatement(connection, sql);
		callableStatement.init();
		callableStatement.executeUpdate();
	}

	private void insert(final KConnection connection, final long key, final String libelle) throws SQLException {
		final String sql = "insert into famille values (?, ?)";
		final KCallableStatement callableStatement = dataBaseManager.createCallableStatement(connection, sql);
		try {
			callableStatement.registerParameter(0, KDataType.Long, KPreparedStatement.ParameterType.IN);
			callableStatement.registerParameter(1, KDataType.String, KPreparedStatement.ParameterType.IN);
			//-------
			callableStatement.init();
			//-------
			callableStatement.setValue(0, key);
			callableStatement.setValue(1, libelle);
			//-------
			callableStatement.executeUpdate();
		} finally {
			callableStatement.close();
		}
	}

	public void createDatas() throws Exception {
		final KConnection connection = dataBaseManager.getConnectionProvider().obtainConnection();
		try {
			execCallableStatement(connection, "insert into famille values (1, 'Aizoaceae')");
			//-----
			execCallableStatement(connection, "insert into famille values (2, 'Balsaminaceae')");
			//-----
			//On passe par une requête bindée
			insert(connection, 3, CAMPANULACEAE);
		} finally {
			connection.commit();
		}
	}

	@Test
	public void testCallableStatement() throws Exception {
		createDatas();
	}

	//On teste un preparestatement mappé sur un type statique (Class famille)
	@Test
	public void testSelectList() throws Exception {
		//On crée les données
		createDatas();
		//----
		final Domain domain = Home.getDefinitionSpace().resolve("DO_DT_FAMILLE_DTC", Domain.class);
		final QueryResult result = executeQuery(domain, "select * from famille");

		//On vérifie que l'on a bien les 3 familles
		Assert.assertEquals(3, result.getSQLRowCount());
		final DtList<Famille> familles = (DtList<Famille>) result.getValue();
		Assert.assertEquals(3, familles.size());

		for (final Famille famille : familles) {
			if (famille.getFamId() == 1) {
				Assert.assertEquals(AIZOACEAE, famille.getLibelle());
			} else if (famille.getFamId() == 2) {
				Assert.assertEquals(BALSAMINACEAE, famille.getLibelle());
			} else if (famille.getFamId() == 3) {
				Assert.assertEquals(CAMPANULACEAE, famille.getLibelle());
			} else {
				Assert.fail();
			}
		}
	}

	//On teste un preparestatement mappé sur un type statique (Class famille)
	@Test
	public void testSelectObject() throws Exception {
		//On crée les données
		createDatas();
		//----
		final Domain domain = Home.getDefinitionSpace().resolve("DO_DT_FAMILLE_DTO", Domain.class);
		final QueryResult result = executeQuery(domain, "select * from famille where fam_id=1");
		Assert.assertEquals(1, result.getSQLRowCount());
		final Famille famille = (Famille) result.getValue();
		Assert.assertEquals(AIZOACEAE, famille.getLibelle());
	}

	private QueryResult executeQuery(final Domain domain, final String sql) throws SQLException, Exception {
		final KConnection connection = dataBaseManager.getConnectionProvider().obtainConnection();
		try {
			final KPreparedStatement preparedStatement = dataBaseManager.createPreparedStatement(connection, sql, false);
			try {
				preparedStatement.init();
				return preparedStatement.executeQuery(domain);
			} finally {
				preparedStatement.close();
			}
		} finally {
			connection.commit();
		}
	}

	//On teste un preparestatement mappé sur un type statique (Class famille)
	@Test
	public void testSelectPrimitive() throws Exception {
		//On crée les données
		createDatas();
		//----
		final Domain domain = new Domain("DO_INTEGER", KDataType.Integer, new FormatterDefault("FMT_INTEGER"));
		final QueryResult result = executeQuery(domain, "select count(*) from famille");
		Assert.assertEquals(1, result.getSQLRowCount());
		Assert.assertEquals(3, result.getValue());
	}

	//On teste un preparestatement mappé sur un type statique (Class famille)
	@Test
	public void testSelectPrimitive2() throws Exception {
		//On crée les données
		createDatas();
		//----
		final Domain domain = new Domain("DO_LIB", KDataType.String, new FormatterDefault("FMT_INTEGER"));
		final QueryResult result = executeQuery(domain, "select libelle from famille where fam_id=1");
		Assert.assertEquals(1, result.getSQLRowCount());
		Assert.assertEquals(AIZOACEAE, result.getValue());
	}

	//On teste un preparestatement mappé sur un type dynamique DTList.
	@Test
	public void testDynSelect() throws Exception {
		//On crée les données
		createDatas();
		//----
		final Domain domain = new Domain("DO_TEST", KDataType.DtList, Home.getDefinitionSpace().resolve(Formatter.FMT_DEFAULT, Formatter.class));
		final QueryResult result = executeQuery(domain, "select * from famille");
		Assert.assertEquals(3, result.getSQLRowCount());
		final DtList<DtObject> familles = (DtList<DtObject>) result.getValue();
		Assert.assertEquals(3, familles.size());

		for (final DtObject famille : familles) {
			if (getValue(famille, "FAM_ID").equals(1L)) {
				Assert.assertEquals(AIZOACEAE, getValue(famille, "LIBELLE"));
			} else if (getValue(famille, "FAM_ID").equals(2L)) {
				Assert.assertEquals(BALSAMINACEAE, getValue(famille, "LIBELLE"));
			} else if (getValue(famille, "FAM_ID").equals(3L)) {
				Assert.assertEquals(CAMPANULACEAE, getValue(famille, "LIBELLE"));
			} else {
				//System.out.println("result >>>" + famille);
				Assert.fail();
			}
		}
	}

	private static Object getValue(final DtObject dto, final String fieldName) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		final DtField dtField = dtDefinition.getField(fieldName);
		return dtField.getDataAccessor().getValue(dto);
	}
}