/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.parser;

/*An interface that contains static declarations for some basic information
 about the parser such as the number of rules in the grammar, the starting state, etc...*/
public interface ParserBasicInformation {

	int
		ERROR_SYMBOL      = 132,
	      MAX_NAME_LENGTH   = 41,
	      NUM_STATES        = 1301,

	      NT_OFFSET         = 132,
	      SCOPE_UBOUND      = 373,
	      SCOPE_SIZE        = 374,
	      LA_STATE_OFFSET   = 19214,
	      MAX_LA            = 1,
	      NUM_RULES         = 1020,
	      NUM_TERMINALS     = 132,
	      NUM_NON_TERMINALS = 440,
	      NUM_SYMBOLS       = 572,
	      START_STATE       = 1070,
	      EOFT_SYMBOL       = 70,
	      EOLT_SYMBOL       = 70,
	      ACCEPT_ACTION     = 19213,
	      ERROR_ACTION      = 19214;
}
