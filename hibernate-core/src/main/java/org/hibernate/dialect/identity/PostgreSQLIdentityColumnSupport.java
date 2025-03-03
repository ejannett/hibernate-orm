/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.dialect.identity;

import static org.hibernate.internal.util.StringHelper.unquote;

/**
 * @author Andrea Boriero
 */
public class PostgreSQLIdentityColumnSupport extends IdentityColumnSupportImpl {

	public static final PostgreSQLIdentityColumnSupport INSTANCE = new PostgreSQLIdentityColumnSupport();
	@Override
	public boolean supportsIdentityColumns() {
		return true;
	}

	@Override
	public String getIdentitySelectString(String table, String column, int type) {
		return "select currval('" + unquote(table) + '_' + unquote(column) + "_seq')";
	}

	@Override
	public String getIdentityColumnString(int type) {
		return "generated by default as identity";
	}
}
