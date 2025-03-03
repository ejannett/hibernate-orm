/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.orm.test.mapping.embeddable.strategy.instantiator.embeddable;

import org.hibernate.metamodel.spi.EmbeddableInstantiator;
import org.hibernate.metamodel.spi.ValueAccess;

/**
 * @author Steve Ebersole
 */
public class NameInstantiator implements EmbeddableInstantiator {
	@Override
	public Object instantiate(ValueAccess valueAccess) {
		// alphabetical
		final String first = valueAccess.getValue( 0, String.class );
		final String last = valueAccess.getValue( 1, String.class );
		return new Name( first, last );
	}

	@Override
	public boolean isInstance(Object object) {
		return object instanceof Name;
	}

	@Override
	public boolean isSameClass(Object object) {
		return object.getClass().equals( Name.class );
	}
}
