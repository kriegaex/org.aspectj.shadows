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
package org.eclipse.jdt.internal.codeassist.impl;

import java.util.Map;

import org.eclipse.jdt.core.compiler.CharOperation;

public class AssistOptions {
	/**
	 * Option IDs
	 */
	public static final String OPTION_PerformVisibilityCheck =
		"org.eclipse.jdt.core.codeComplete.visibilityCheck"; 	//$NON-NLS-1$
	public static final String OPTION_ForceImplicitQualification =
		"org.eclipse.jdt.core.codeComplete.forceImplicitQualification"; 	//$NON-NLS-1$
	public static final String OPTION_FieldPrefixes =
		"org.eclipse.jdt.core.codeComplete.fieldPrefixes"; 	//$NON-NLS-1$
	public static final String OPTION_StaticFieldPrefixes =
		"org.eclipse.jdt.core.codeComplete.staticFieldPrefixes"; 	//$NON-NLS-1$
	public static final String OPTION_LocalPrefixes =
		"org.eclipse.jdt.core.codeComplete.localPrefixes"; 	//$NON-NLS-1$
	public static final String OPTION_ArgumentPrefixes =
		"org.eclipse.jdt.core.codeComplete.argumentPrefixes"; 	//$NON-NLS-1$
	public static final String OPTION_FieldSuffixes =
		"org.eclipse.jdt.core.codeComplete.fieldSuffixes"; 	//$NON-NLS-1$
	public static final String OPTION_StaticFieldSuffixes =
		"org.eclipse.jdt.core.codeComplete.staticFieldSuffixes"; 	//$NON-NLS-1$
	public static final String OPTION_LocalSuffixes =
		"org.eclipse.jdt.core.codeComplete.localSuffixes"; 	//$NON-NLS-1$
	public static final String OPTION_ArgumentSuffixes =
		"org.eclipse.jdt.core.codeComplete.argumentSuffixes"; 	//$NON-NLS-1$
	public static final String ENABLED = "enabled"; //$NON-NLS-1$
	public static final String DISABLED = "disabled"; //$NON-NLS-1$

	public boolean checkVisibility = false;
	public boolean forceImplicitQualification = false;
	public char[][] fieldPrefixes = null;
	public char[][] staticFieldPrefixes = null;
	public char[][] localPrefixes = null;
	public char[][] argumentPrefixes = null;
	public char[][] fieldSuffixes = null;
	public char[][] staticFieldSuffixes = null;
	public char[][] localSuffixes = null;
	public char[][] argumentSuffixes = null;

	/** 
	 * Initializing the assist options with default settings
	 */
	public AssistOptions() {
		// Initializing the assist options with default settings
	}

	/** 
	 * Initializing the assist options with external settings
	 */
	public AssistOptions(Map settings) {
		if (settings == null)
			return;

		set(settings);
	}
	public void set(Map optionsMap) {

		Object optionValue;
		if ((optionValue = optionsMap.get(OPTION_PerformVisibilityCheck)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.checkVisibility = true;
			} else if (DISABLED.equals(optionValue)) {
				this.checkVisibility = false;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_ForceImplicitQualification)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.forceImplicitQualification = true;
			} else if (DISABLED.equals(optionValue)) {
				this.forceImplicitQualification = false;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_FieldPrefixes)) != null) {
			if (optionValue instanceof String) {
				String stringValue = (String) optionValue;
				if (stringValue.length() > 0){
					this.fieldPrefixes = CharOperation.splitAndTrimOn(',', stringValue.toCharArray());
				} else {
					this.fieldPrefixes = null;
				}
			}
		}
		if ((optionValue = optionsMap.get(OPTION_StaticFieldPrefixes)) != null) {
			if (optionValue instanceof String) {
				String stringValue = (String) optionValue;
				if (stringValue.length() > 0){
					this.staticFieldPrefixes = CharOperation.splitAndTrimOn(',', stringValue.toCharArray());
				} else {
					this.staticFieldPrefixes = null;
				}
			}
		}
		if ((optionValue = optionsMap.get(OPTION_LocalPrefixes)) != null) {
			if (optionValue instanceof String) {
				String stringValue = (String) optionValue;
				if (stringValue.length() > 0){
					this.localPrefixes = CharOperation.splitAndTrimOn(',', stringValue.toCharArray());
				} else {
					this.localPrefixes = null;
				}
			}
		}
		if ((optionValue = optionsMap.get(OPTION_ArgumentPrefixes)) != null) {
			if (optionValue instanceof String) {
				String stringValue = (String) optionValue;
				if (stringValue.length() > 0){
					this.argumentPrefixes = CharOperation.splitAndTrimOn(',', stringValue.toCharArray());
				} else {
					this.argumentPrefixes = null;
				}
			}
		}
		if ((optionValue = optionsMap.get(OPTION_FieldSuffixes)) != null) {
			if (optionValue instanceof String) {
				String stringValue = (String) optionValue;
				if (stringValue.length() > 0){
					this.fieldSuffixes = CharOperation.splitAndTrimOn(',', stringValue.toCharArray());
				} else {
					this.fieldSuffixes = null;
				}
			}
		}
		if ((optionValue = optionsMap.get(OPTION_StaticFieldSuffixes)) != null) {
			if (optionValue instanceof String) {
				String stringValue = (String) optionValue;
				if (stringValue.length() > 0){
					this.staticFieldSuffixes = CharOperation.splitAndTrimOn(',', stringValue.toCharArray());
				} else {
					this.staticFieldSuffixes = null;
				}
			}
		}
		if ((optionValue = optionsMap.get(OPTION_LocalSuffixes)) != null) {
			if (optionValue instanceof String) {
				String stringValue = (String) optionValue;
				if (stringValue.length() > 0){
					this.localSuffixes = CharOperation.splitAndTrimOn(',', stringValue.toCharArray());
				} else {
					this.localSuffixes = null;
				}
			}
		}
		if ((optionValue = optionsMap.get(OPTION_ArgumentSuffixes)) != null) {
			if (optionValue instanceof String) {
				String stringValue = (String) optionValue;
				if (stringValue.length() > 0){
					this.argumentSuffixes = CharOperation.splitAndTrimOn(',', stringValue.toCharArray());
				} else {
					this.argumentSuffixes = null;
				}
			}
		}
	}
}
