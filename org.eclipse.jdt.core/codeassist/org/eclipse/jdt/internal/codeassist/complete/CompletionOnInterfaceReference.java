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
package org.eclipse.jdt.internal.codeassist.complete;

public class CompletionOnInterfaceReference extends CompletionOnSingleTypeReference {
	public CompletionOnInterfaceReference(char[] source, long pos) {
		super(source, pos);
	}
	public StringBuffer printExpression(int indent, StringBuffer output) {
		return output.append("<CompleteOnInterface:").append(token).append('>');//$NON-NLS-1$
	}
}
