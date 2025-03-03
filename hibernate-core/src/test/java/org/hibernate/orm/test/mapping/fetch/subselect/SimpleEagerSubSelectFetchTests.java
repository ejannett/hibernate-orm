/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.orm.test.mapping.fetch.subselect;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import org.hibernate.testing.jdbc.SQLStatementInspector;
import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Steve Ebersole
 */
@DomainModel(annotatedClasses = {
		SimpleEagerSubSelectFetchTests.Owner.class,
		SimpleEagerSubSelectFetchTests.Thing.class
})
@SessionFactory( useCollectingStatementInspector = true )
public class SimpleEagerSubSelectFetchTests {

	@Test
	public void smokeTest(SessionFactoryScope scope) {
		final SQLStatementInspector statementInspector = scope.getCollectingStatementInspector();
		statementInspector.clear();

		scope.inTransaction( (session) -> {
			final List<Owner> misspelled = session.createQuery( "from Owner o where o.name like 'Onwer%'", Owner.class ).list();
			assertThat( misspelled ).hasSize( 2 );

			final Owner firstResult = misspelled.get( 0 );
			final Owner secondResult = misspelled.get( 1 );

			// make sure we got the right owners
			assertThat( firstResult.getId() ).isLessThanOrEqualTo( 2 );
			assertThat( secondResult.getId() ).isLessThanOrEqualTo( 2 );

			// check that the one-to-many was loaded
			assertThat( Hibernate.isInitialized( firstResult.getThings() ) ).isTrue();
			assertThat( Hibernate.isInitialized( secondResult.getThings() ) ).isTrue();

			// the initial query + the "subselect" select
			assertThat( statementInspector.getSqlQueries() ).hasSize( 2 );
		} );
	}

	@Test
	public void baselineJoinFetchTest(SessionFactoryScope scope) {
		final SQLStatementInspector statementInspector = scope.getCollectingStatementInspector();
		statementInspector.clear();

		scope.inTransaction( (session) -> {
			final List<Owner> misspelled = session.createQuery( "from Owner o left join fetch o.things where o.name like 'Onwer%'", Owner.class ).list();
			assertThat( misspelled ).hasSize( 2 );

			final Owner firstResult = misspelled.get( 0 );
			final Owner secondResult = misspelled.get( 1 );

			// make sure we got the right owners
			assertThat( firstResult.getId() ).isLessThanOrEqualTo( 2 );
			assertThat( secondResult.getId() ).isLessThanOrEqualTo( 2 );

			// both one-to-many should be initialized
			assertThat( Hibernate.isInitialized( firstResult.getThings() ) ).isTrue();
			assertThat( Hibernate.isInitialized( secondResult.getThings() ) ).isTrue();

			assertThat( firstResult.getThings().size() + secondResult.getThings().size() ).isEqualTo( 3 );

			// the initial query with join
			assertThat( statementInspector.getSqlQueries() ).hasSize( 1 );
			assertThat( statementInspector.getNumberOfJoins( 0 ) ).isEqualTo( 1 );
		} );
	}

	@BeforeEach
	public void prepareTestData(SessionFactoryScope scope) {
		scope.inTransaction( (session) -> {
			final Owner o1 = new Owner( 1, "Onwer 1" );
			final Owner o2 = new Owner( 2, "Onwer 2" );
			final Owner o3 = new Owner( 3, "Owner 3" );

			final Thing thing1 = new Thing( 1, "first", o1 );
			final Thing thing2 = new Thing( 2, "second", o1 );
			final Thing thing3 = new Thing( 3, "third", o1 );
			final Thing thing4 = new Thing( 4, "fourth", o3 );
			final Thing thing5 = new Thing( 5, "fifth", o3 );

			session.persist( o1 );
			session.persist( o2 );
			session.persist( o3 );

			session.persist( thing1 );
			session.persist( thing2 );
			session.persist( thing3 );
			session.persist( thing4 );
			session.persist( thing5 );
		} );
	}

	@AfterEach
	public void dropTestData(SessionFactoryScope scope) {
		scope.inTransaction( (session) -> {
			session.createQuery( "delete Thing" ).executeUpdate();
			session.createQuery( "delete Owner" ).executeUpdate();
		} );
	}

	@Entity(name = "Owner")
	@Table(name = "t_sub_fetch_owner")
	public static class Owner {
		@Id
		private Integer id;
		private String name;

		@OneToMany(mappedBy = "owner", fetch = FetchType.EAGER)
		@Fetch(FetchMode.SUBSELECT)
		private Set<Thing> things = new HashSet<>();

		private Owner() {
		}

		public Owner(Integer id, String name) {
			this.id = id;
			this.name = name;
		}

		public Integer getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Set<Thing> getThings() {
			return things;
		}

		public void setThings(Set<Thing> things) {
			this.things = things;
		}
	}

	@Entity(name = "Thing")
	@Table(name = "t_sub_fetch_thing")
	public static class Thing {
		@Id
		private Integer id;
		private String name;

		@ManyToOne
		private Owner owner;

		private Thing() {
		}

		public Thing(Integer id, String name, Owner owner) {
			this.id = id;
			this.name = name;
			this.owner = owner;
			owner.getThings().add( this );
		}

		public Integer getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Owner getOwner() {
			return owner;
		}

		public void setOwner(Owner owner) {
			this.owner = owner;
		}
	}
}
