/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.expressions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.IEvaluationContext;

public class ResolveExpression extends CompositeExpression {

	private String fVariable;
	private Object[] fArgs;
	
	private static final String ATT_VARIABLE= "variable";  //$NON-NLS-1$
	private static final String ATT_ARGS= "args";  //$NON-NLS-1$
	
	public ResolveExpression(IConfigurationElement configElement) throws CoreException {
		fVariable= configElement.getAttribute(ATT_VARIABLE);
		Expressions.checkAttribute(ATT_VARIABLE, fVariable);
		fArgs= Expressions.getArguments(configElement, ATT_ARGS);
	}
	
	public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
		return evaluateAnd(new EvaluationContext(context, context.resolveVariable(fVariable, fArgs)));
	}
}