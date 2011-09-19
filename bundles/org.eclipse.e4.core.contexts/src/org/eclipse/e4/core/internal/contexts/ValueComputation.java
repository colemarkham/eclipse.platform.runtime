/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.internal.contexts;

import java.util.Set;
import org.eclipse.e4.core.contexts.IContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.internal.contexts.EclipseContext.Scheduled;

public class ValueComputation extends Computation {

	final static private Object NotAValue = new Object();

	final private IContextFunction function;
	final private EclipseContext originatingContext;
	final private String name;

	private Object cachedValue = NotAValue;
	private boolean computing; // cycle detection

	public ValueComputation(String name, IEclipseContext originatingContext, IContextFunction computedValue) {
		this.originatingContext = (EclipseContext) originatingContext;
		this.function = computedValue;
		this.name = name;
		init();
	}

	public void handleInvalid(ContextChangeEvent event, Set<Scheduled> scheduled) {
		cachedValue = NotAValue;

		if (name.equals(event.getName()))
			invalidateComputation();

		int eventType = event.getEventType();
		originatingContext.invalidate(name, eventType == ContextChangeEvent.DISPOSE ? ContextChangeEvent.REMOVED : eventType, event.getOldValue(), scheduled);
	}

	public Object get() {
		if (!isValid())
			throw new IllegalArgumentException("Reusing invalidated computation"); //$NON-NLS-1$
		if (cachedValue != NotAValue)
			return cachedValue;
		if (this.computing)
			throw new RuntimeException("Cycle while computing value" + this.toString()); //$NON-NLS-1$

		originatingContext.pushComputation(this);
		computing = true;
		try {
			cachedValue = function.compute(originatingContext);
			validComputation = true;
		} finally {
			computing = false;
			originatingContext.popComputation(this);
		}
		return cachedValue;
	}

	public String toString() {
		if (function == null)
			return super.toString();
		return function.toString();
	}

	protected int calcHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((function == null) ? 0 : function.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((originatingContext == null) ? 0 : originatingContext.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ValueComputation other = (ValueComputation) obj;
		if (function == null) {
			if (other.function != null)
				return false;
		} else if (!function.equals(other.function))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (originatingContext == null) {
			if (other.originatingContext != null)
				return false;
		} else if (!originatingContext.equals(other.originatingContext))
			return false;
		return true;
	}

}