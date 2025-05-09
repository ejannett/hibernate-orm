/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.orm.test.mapping.collections;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.SQLDeleteAll;
import org.hibernate.annotations.SQLUpdate;

import org.hibernate.testing.jdbc.SQLStatementInspector;
import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.Jira;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Marco Belladelli
 */
@DomainModel( annotatedClasses = {
		ElementCollectionCustomSqlMutationsTest.Project.class,
		ElementCollectionCustomSqlMutationsTest.User.class,
} )
@SessionFactory
@Jira( "https://hibernate.atlassian.net/browse/HHH-17170" )
public class ElementCollectionCustomSqlMutationsTest {
	@BeforeAll
	public void setUp(SessionFactoryScope scope) {
		scope.inTransaction( session -> {
			final User u1 = new User( "user1" );
			final User u2 = new User( "user2" );
			final Project p1 = new Project( "p1" );
			p1.getMembers().add( u1 );
			p1.getMembers().add( u2 );
			final Project p2 = new Project( "p2" );
			p2.getMembers().add( u1 );
			p2.getOrderedUsers().add( u2 );
			p2.getOrderedUsers().add( u1 );
			session.persist( p1 );
			session.persist( p2 );
		} );
	}

	@Test
	public void testSQLDelete(SessionFactoryScope scope) {
		final SQLStatementInspector inspector = scope.getCollectingStatementInspector();
		scope.inTransaction( session -> {
			final Project project = session.find( Project.class, "p1" );
			project.getMembers().remove( project.getMembers().iterator().next() );
			inspector.clear();
		} );
		// element collection always deletes all records and re-inserts the ones remaining
		// the @SQLDelete annotation is ignored
		assertThat( inspector.getSqlQueries() ).hasSize( 2 );
		assertThat( inspector.getSqlQueries().get( 0 ) ).contains( "1=1" );
		scope.inTransaction( session -> assertThat(
				session.find( Project.class, "p1" ).getMembers()
		).hasSize( 1 ) );
	}

	@Test
	public void testSQLDeleteAll(SessionFactoryScope scope) {
		final SQLStatementInspector inspector = scope.getCollectingStatementInspector();
		scope.inTransaction( session -> {
			final Project project = session.find( Project.class, "p2" );
			project.getMembers().remove( project.getMembers().iterator().next() );
			inspector.clear();
		} );
		assertThat( inspector.getSqlQueries() ).hasSize( 1 );
		assertThat( inspector.getSqlQueries().get( 0 ) ).contains( "1=1" );
		scope.inTransaction( session -> assertThat(
				session.find( Project.class, "p2" ).getMembers()
		).isEmpty() );
	}

	@Test
	public void testSQLUpdate(SessionFactoryScope scope) {
		final SQLStatementInspector inspector = scope.getCollectingStatementInspector();
		scope.inTransaction( session -> {
			final Project project = session.find( Project.class, "p2" );
			assertThat( project.getOrderedUsers().stream().map( User::getName ) ).containsExactly( "user2", "user1" );
			project.getOrderedUsers().sort( Comparator.comparing( User::getName ) );
			inspector.clear();
		} );
		assertThat( inspector.getSqlQueries() ).hasSize( 2 );
		assertThat( inspector.getSqlQueries().get( 0 ) ).contains( "3=3" );
		assertThat( inspector.getSqlQueries().get( 1 ) ).contains( "3=3" );
		scope.inTransaction( session -> {
			final Project project = session.find( Project.class, "p2" );
			assertThat( project.getOrderedUsers().stream().map( User::getName ) ).containsExactly( "user1", "user2" );
		} );
	}

	@Entity( name = "Project" )
	@Table( name = "t_project" )
	public static class Project {
		@Id
		private String name;

		@ElementCollection
		@CollectionTable( name = "project_users", joinColumns = { @JoinColumn( name = "project_id" ) } )
		@SQLDeleteAll( sql = "delete from project_users where project_id = ? and 1=1" )
		private Set<User> members = new HashSet<>();

		@ElementCollection
		@CollectionTable( name = "ordered_users", joinColumns = { @JoinColumn( name = "project_id" ) } )
		@OrderColumn( name = "order_col" )
		@SQLUpdate( sql = "update ordered_users set name = ? where project_id = ? and order_col = ? and 3=3" )
		private List<User> orderedUsers = new ArrayList<>();

		public Project() {
		}

		public Project(String name) {
			this.name = name;
		}

		public Set<User> getMembers() {
			return members;
		}

		public List<User> getOrderedUsers() {
			return orderedUsers;
		}
	}

	@Embeddable
	public static class User {
		private String name;

		public User() {
		}

		public User(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}
}
