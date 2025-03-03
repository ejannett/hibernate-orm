/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.annotations;

import org.hibernate.jdbc.Expectation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies a custom SQL DML statement to be used in place of the default SQL generated by
 * Hibernate when an entire collection is deleted from the database.
 *
 * @author László Benke
 */
@Target({TYPE, FIELD, METHOD})
@Retention(RUNTIME)
public @interface SQLDeleteAll {
	/**
	 * Procedure name or SQL {@code DELETE} statement.
	 */
	String sql();

	/**
	 * Is the statement callable (aka a {@link java.sql.CallableStatement})?
	 */
	boolean callable() default false;

	/**
	 * An {@link Expectation} class used to verify that the operation was successful.
	 *
	 * @see Expectation.None
	 * @see Expectation.RowCount
	 * @see Expectation.OutParameter
	 */
	Class<? extends Expectation> verify() default Expectation.class;

	/**
	 * A {@link ResultCheckStyle} used to verify that the operation was successful.
	 *
	 * @deprecated use {@link #verify()} with an {@link Expectation} class
	 */
	@Deprecated(since = "6.5", forRemoval = true)
	ResultCheckStyle check() default ResultCheckStyle.NONE;

	/**
	 * The name of the table affected. Never required.
	 *
	 * @return the name of the secondary table
	 *
	 * @since 6.2
	 */
	String table() default "";
}
