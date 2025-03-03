/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.orm.test.annotations.fetchprofile;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * @author Emmanuel Bernard
 */
@Entity
@Table(name="Order_Country")
public class Country {
	@Id @GeneratedValue
	public Integer getId() { return id; }
	public void setId(Integer id) { this.id = id; }
	private Integer id;

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	private String name;
}
