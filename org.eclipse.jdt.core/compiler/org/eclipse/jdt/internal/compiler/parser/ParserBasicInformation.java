/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.parser;

/*An interface that contains static declarations for some basic information
 about the parser such as the number of rules in the grammar, the starting state, etc...*/
public interface ParserBasicInformation {

	public final static int
    ERROR_SYMBOL      = 139,
    MAX_NAME_LENGTH   = 41,
    NUM_STATES        = 1286,

    NT_OFFSET         = 139,
    SCOPE_UBOUND      = 395,
    SCOPE_SIZE        = 396,
    LA_STATE_OFFSET   = 20569,
    MAX_LA            = 1,
    NUM_RULES         = 1104,
    NUM_TERMINALS     = 139,
    NUM_NON_TERMINALS = 482,
    NUM_SYMBOLS       = 621,
    START_STATE       = 1150,
    EOFT_SYMBOL       = 100,
    EOLT_SYMBOL       = 100,
    ACCEPT_ACTION     = 20568,
    ERROR_ACTION      = 20569;

}
