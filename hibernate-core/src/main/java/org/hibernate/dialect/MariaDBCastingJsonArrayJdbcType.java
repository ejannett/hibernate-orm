/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.dialect;

import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.JsonArrayJdbcType;

/**
 * @author Christian Beikov
 */
public class MariaDBCastingJsonArrayJdbcType extends JsonArrayJdbcType {

	public MariaDBCastingJsonArrayJdbcType(JdbcType elementJdbcType) {
		super( elementJdbcType );
	}

	@Override
	public void appendWriteExpression(
			String writeExpression,
			SqlAppender appender,
			Dialect dialect) {
		appender.append( "json_extract(" );
		appender.append( writeExpression );
		appender.append( ",'$')" );
	}
}
