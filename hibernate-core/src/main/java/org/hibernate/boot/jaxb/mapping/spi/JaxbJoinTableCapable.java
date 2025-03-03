/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.boot.jaxb.mapping.spi;

/**
 * @author Steve Ebersole
 */
public interface JaxbJoinTableCapable {
	JaxbJoinTableImpl getJoinTable();
	void setJoinTable(JaxbJoinTableImpl value);
}
