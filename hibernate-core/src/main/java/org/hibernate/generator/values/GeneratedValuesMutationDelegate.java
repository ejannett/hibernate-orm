/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.generator.values;

import java.sql.PreparedStatement;

import org.hibernate.engine.jdbc.mutation.JdbcValueBindings;
import org.hibernate.engine.jdbc.mutation.group.PreparedStatementDetails;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.EventType;
import org.hibernate.jdbc.Expectation;
import org.hibernate.sql.model.ast.builder.TableMutationBuilder;
import org.hibernate.sql.results.jdbc.spi.JdbcValuesMappingProducer;

/**
 * Each implementation defines a strategy for retrieving values
 * {@linkplain org.hibernate.generator.OnExecutionGenerator generated by
 * the database} after execution of a mutation statement.
 * <p>
 * An implementation controls:
 * <ul>
 * <li>building the SQL mutation statement, and
 * <li>retrieving the generated values using JDBC.
 * </ul>
 *
 * @author Marco Belladelli
 * @see org.hibernate.generator.OnExecutionGenerator
 * @since 6.5
 */
public interface GeneratedValuesMutationDelegate {
	/**
	 * Create a {@link TableMutationBuilder} instance used to build table mutations for this delegate.
	 */
	TableMutationBuilder<?> createTableMutationBuilder(Expectation expectation, SessionFactoryImplementor sessionFactory);

	/**
	 * Create a {@link PreparedStatement} from the provided {@code sql} based on the delegate needs.
	 */
	PreparedStatement prepareStatement(String sql, SharedSessionContractImplementor session);

	/**
	 * Perform the {@code mutation} and extract the database-generated values.
	 *
	 * @see #createTableMutationBuilder
	 */
	GeneratedValues performMutation(
			PreparedStatementDetails statementDetails,
			JdbcValueBindings valueBindings,
			Object entity,
			SharedSessionContractImplementor session);

	/**
	 * Returns the timing this generated values delegate handles.
	 */
	EventType getTiming();

	/**
	 * Returns {@code true} when this delegate supports retrieving arbitrary generated values,
	 * or {@code false} when it only supports identifiers.
	 */
	boolean supportsArbitraryValues();

	/**
	 * Returns {@code true} when this delegate supports retrieving the {@link org.hibernate.annotations.RowId} value.
	 */
	boolean supportsRowId();

	/**
	 * Retrieve the {@linkplain JdbcValuesMappingProducer mapping producer} used to read the generated values.
	 */
	JdbcValuesMappingProducer getGeneratedValuesMappingProducer();
}
