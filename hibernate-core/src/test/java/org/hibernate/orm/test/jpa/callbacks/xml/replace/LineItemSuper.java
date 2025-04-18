/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.orm.test.jpa.callbacks.xml.replace;

import org.hibernate.orm.test.jpa.callbacks.xml.common.CallbackTarget;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;

/**
 * @author Steve Ebersole
 */
@MappedSuperclass
@EntityListeners({ ListenerA.class, ListenerB.class})
public class LineItemSuper extends CallbackTarget {
	private int quantity;

	public LineItemSuper() {
	}

	public LineItemSuper(int quantity) {
		this.quantity = quantity;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}
