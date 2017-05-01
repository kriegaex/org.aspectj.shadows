/*******************************************************************************
 * Copyright (c) 2016, 2017 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.JavaModelException;

public class BinaryModule extends AbstractModule {
	public BinaryModule(JavaElement parent, String name) {
		super(parent, name);
	}
	/*
	 * @see IParent#getChildren()
	 */
	public IJavaElement[] getChildren() throws JavaModelException {
		ClassFile cf = (ClassFile) this.parent;
		ClassFileInfo cfi = (ClassFileInfo) cf.getElementInfo();
		return cfi.binaryChildren;
	}
	@Override
	public int getFlags() throws JavaModelException {
		ModuleDescriptionInfo info = (ModuleDescriptionInfo) getElementInfo();
		return info.getModifiers();
	}
	public String getKey(boolean forceOpen) throws JavaModelException {
		return getKey(this, forceOpen);
	}
	public String toString(String lineDelimiter) {
		StringBuffer buffer = new StringBuffer();
		try {
			toStringContent(buffer, lineDelimiter);
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return buffer.toString();
	}
	protected void toStringContent(StringBuffer buffer, String lineDelimiter) throws JavaModelException {
		IModuleDescription.IPackageExport[] exports = getExportedPackages();
		IModuleDescription.IModuleReference[] requires = getRequiredModules();
		buffer.append("module "); //$NON-NLS-1$
		buffer.append(this.name).append(' ');
		buffer.append('{').append(lineDelimiter);
		if (exports != null) {
			for(int i = 0; i < exports.length; i++) {
				buffer.append("\texports "); //$NON-NLS-1$
				buffer.append(exports[i].toString());
				buffer.append(lineDelimiter);
			}
		}
		buffer.append(lineDelimiter);
		if (requires != null) {
			for(int i = 0; i < requires.length; i++) {
				buffer.append("\trequires "); //$NON-NLS-1$
				if (requires[i].isPublic()) {
					buffer.append(" public "); //$NON-NLS-1$
				}
				buffer.append(requires[i].getElementName());
				buffer.append(';').append(lineDelimiter);
			}
		}
		buffer.append(lineDelimiter).append('}').toString();
	}
}
