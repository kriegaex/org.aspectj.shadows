/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.compiler.parser;

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;

public class SourceConstructorDeclaration extends ConstructorDeclaration {
	public int selectorSourceEnd;

	public SourceConstructorDeclaration(CompilationResult compilationResult){
		super(compilationResult);
	}
}