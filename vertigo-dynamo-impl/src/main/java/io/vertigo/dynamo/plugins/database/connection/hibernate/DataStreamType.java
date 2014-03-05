package io.vertigo.dynamo.plugins.database.connection.hibernate;

import io.vertigo.dynamo.domain.metamodel.DataStream;
import io.vertigo.dynamo.impl.database.vendor.core.DataStreamMappingUtil;

import java.io.IOException;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;

/**
 * Custom hibernate UserType for DataStream.
 * DataStream map to only one Blob column.
 * @author npiedeloup
 */
public final class DataStreamType implements UserType {

	private static int[] SQL_TYPES = new int[] { Types.BLOB };

	/** {@inheritDoc} */
	public int[] sqlTypes() {
		return SQL_TYPES;
	}

	/** {@inheritDoc} */
	public Class returnedClass() {
		return DataStream.class;
	}

	/** {@inheritDoc} */
	public boolean equals(final Object x, final Object y) {
		if (x == y) {
			return true;
		}
		if (null == x || null == y) {
			return false;
		}
		return x.equals(y);
	}

	/** {@inheritDoc} */
	public int hashCode(final Object x) throws HibernateException {
		if (x != null) {
			return x.hashCode();
		}
		return 0;
	}

	/** {@inheritDoc} */
	public Object nullSafeGet(final ResultSet rs, final String[] names, final SessionImplementor session, final Object owner) throws HibernateException, SQLException {
		//Cf io.vertigo.dynamo.impl.database.vendor.core.SQLMappingImpl
		final String columnName = names[0];
		final int index = rs.findColumn(columnName);
		final DataStream value = DataStreamMappingUtil.getDataStream(rs, index);
		return value;
	}

	/** {@inheritDoc} */
	public void nullSafeSet(final PreparedStatement statement, final Object value, final int index, final SessionImplementor session) throws HibernateException, SQLException {
		if (value == null) {
			statement.setNull(index, sqlTypes()[0]);
		} else {
			//Cf io.vertigo.dynamo.impl.database.vendor.core.SQLMappingImpl
			try {
				final DataStream dataStream = (DataStream) value;
				statement.setBinaryStream(index, dataStream.createInputStream(), (int) dataStream.getLength()); //attention le setBinaryStream avec une longueur de fichier en long N'EST PAS implémentée dans de nombreux drivers !!
			} catch (final IOException e) {
				final SQLException sqlException = new SQLException("Erreur d'ecriture du flux");
				sqlException.initCause(e);
				throw sqlException;
			}
		}
	}

	/** {@inheritDoc} */
	public boolean isMutable() {
		return false;
	}

	/** {@inheritDoc} */
	public Object assemble(final Serializable cached, final Object owner) {
		return cached;
	}

	/** {@inheritDoc} */
	public Object deepCopy(final Object value) {
		return value;
	}

	/** {@inheritDoc} */
	public Serializable disassemble(final Object value) {
		return (Serializable) value;
	}

	/** {@inheritDoc} */
	public Object replace(final Object original, final Object target, final Object owner) {
		return original;
	}

}