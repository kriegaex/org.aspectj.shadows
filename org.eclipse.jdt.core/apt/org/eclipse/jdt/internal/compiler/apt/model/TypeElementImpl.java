/*******************************************************************************
 * Copyright (c) 2005, 2020 IBM Corporation and others.
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
package org.eclipse.jdt.internal.compiler.apt.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

public class TypeElementImpl extends ElementImpl implements TypeElement {

	/**
	 * Compares Element instances possibly returned by
	 * {@link TypeElement#getEnclosedElements()} based on their source location, if available.
	 *
	 */
	private static final class SourceLocationComparator implements Comparator<Element> {
		private final IdentityHashMap<ElementImpl, Integer> sourceStartCache = new IdentityHashMap<>();

		@Override
		public int compare(Element o1, Element o2) {
			ElementImpl e1 = (ElementImpl) o1;
			ElementImpl e2 = (ElementImpl) o2;

			return getSourceStart(e1) - getSourceStart(e2);
		}

		private int getSourceStart(ElementImpl e) {
			Integer value = sourceStartCache.get(e);

			if (value == null) {
				value = determineSourceStart(e);
				sourceStartCache.put(e, value);
			}

			return value;
		}

		private int determineSourceStart(ElementImpl e) {
			switch(e.getKind()) {
				case ANNOTATION_TYPE :
				case INTERFACE :
				case CLASS :
				case ENUM :
				case RECORD :
					TypeElementImpl typeElementImpl = (TypeElementImpl) e;
					Binding typeBinding = typeElementImpl._binding;
					if (typeBinding instanceof SourceTypeBinding) {
						SourceTypeBinding sourceTypeBinding = (SourceTypeBinding) typeBinding;
						TypeDeclaration typeDeclaration = (TypeDeclaration) sourceTypeBinding.scope.referenceContext();
						return typeDeclaration.sourceStart;
					}
					break;
				case CONSTRUCTOR :
				case METHOD :
					ExecutableElementImpl executableElementImpl = (ExecutableElementImpl) e;
					Binding binding = executableElementImpl._binding;
					if (binding instanceof MethodBinding) {
						MethodBinding methodBinding = (MethodBinding) binding;
						return methodBinding.sourceStart();
					}
					break;
				case ENUM_CONSTANT :
				case FIELD :
				case RECORD_COMPONENT :
					VariableElementImpl variableElementImpl = (VariableElementImpl) e;
					binding = variableElementImpl._binding;
					if (binding instanceof FieldBinding) {
						FieldBinding fieldBinding = (FieldBinding) binding;
						FieldDeclaration fieldDeclaration = fieldBinding.sourceField();
						if (fieldDeclaration != null) {
							return fieldDeclaration.sourceStart;
						}
					}
					break;
				default:
					break;
			}

			return -1;
		}
	}

	private final ElementKind _kindHint;
	
	/**
	 * In general, clients should call {@link Factory#newDeclaredType(ReferenceBinding)} or
	 * {@link Factory#newElement(org.eclipse.jdt.internal.compiler.lookup.Binding)} to
	 * create new instances.
	 */
	TypeElementImpl(BaseProcessingEnvImpl env, ReferenceBinding binding, ElementKind kindHint) {
		super(env, binding);
		_kindHint = kindHint;
	}
	
	@Override
	public <R, P> R accept(ElementVisitor<R, P> v, P p)
	{
		return v.visitType(this, p);
	}

	@Override
	protected AnnotationBinding[] getAnnotationBindings()
	{
		return ((ReferenceBinding)_binding).getAnnotations();
	}

	@Override
	public List<? extends Element> getEnclosedElements() {
		ReferenceBinding binding = (ReferenceBinding)_binding;
		List<Element> enclosed = new ArrayList<>(binding.fieldCount() + binding.methods().length + binding.memberTypes().length);
		for (MethodBinding method : binding.methods()) {
			ExecutableElement executable = new ExecutableElementImpl(_env, method);
			enclosed.add(executable);
		}
		for (FieldBinding field : binding.fields()) {
			// TODO no field should be excluded according to the JLS
			if (!field.isSynthetic()) {
				 VariableElement variable = new VariableElementImpl(_env, field);
				 enclosed.add(variable);
			}
		}
		if (binding.isRecord() && binding instanceof SourceTypeBinding) {
			SourceTypeBinding sourceBinding = (SourceTypeBinding) binding;
			for (FieldBinding field : sourceBinding.getRecordComponents()) {
				RecordComponentElement rec = new RecordComponentElementImpl(_env, field);
				enclosed.add(rec);
			}
		}
		for (ReferenceBinding memberType : binding.memberTypes()) {
			TypeElement type = new TypeElementImpl(_env, memberType, null);
			enclosed.add(type);
		}

		Collections.sort(enclosed, new SourceLocationComparator());

		return Collections.unmodifiableList(enclosed);
	}

	@Override
    public List<? extends RecordComponentElement> getRecordComponents() {
		if (_binding instanceof SourceTypeBinding) {
			SourceTypeBinding binding = (SourceTypeBinding) _binding;
			List<RecordComponentElement> enclosed = new ArrayList<>();
			for (FieldBinding field : binding.fields()) {
				if (!field.isSynthetic()) {
					 RecordComponentElement variable = new RecordComponentElementImpl(_env, field);
					 enclosed.add(variable);
				}
			}
			Collections.sort(enclosed, new SourceLocationComparator());
			return Collections.unmodifiableList(enclosed);
		}
		// TODO: Add code for BinaryTypeBinding, which, as of now doesn't seem to contain components
		return Collections.emptyList();
    }

	@Override
	public Element getEnclosingElement() {
		ReferenceBinding binding = (ReferenceBinding)_binding;
		ReferenceBinding enclosingType = binding.enclosingType();
		if (null == enclosingType) {
			// this is a top level type; get its package
			return _env.getFactory().newPackageElement(binding.fPackage);
		}
		else {
			return _env.getFactory().newElement(binding.enclosingType());
		}
	}

	@Override
	public String getFileName() {
		char[] name = ((ReferenceBinding)_binding).getFileName();
		if (name == null)
			return null;
		return new String(name);
	}
	
	@Override
	public List<? extends TypeMirror> getInterfaces() {
		ReferenceBinding binding = (ReferenceBinding)_binding;
		if (null == binding.superInterfaces() || binding.superInterfaces().length == 0) {
			return Collections.emptyList();
		}
		List<TypeMirror> interfaces = new ArrayList<>(binding.superInterfaces().length);
		for (ReferenceBinding interfaceBinding : binding.superInterfaces()) {
			TypeMirror interfaceType = _env.getFactory().newTypeMirror(interfaceBinding);
			if (interfaceType.getKind() == TypeKind.ERROR) {
				if (this._env.getSourceVersion().compareTo(SourceVersion.RELEASE_6) > 0) {
					// for jdk 7 and above, add error types
					interfaces.add(interfaceType);
				}
			} else {
				interfaces.add(interfaceType);
			}
		}
		return Collections.unmodifiableList(interfaces);
	}

	@Override
	public ElementKind getKind() {
		if (null != _kindHint) {
			return _kindHint;
		}
		ReferenceBinding refBinding = (ReferenceBinding)_binding;
		// The order of these comparisons is important: e.g., enum is subset of class
		if (refBinding.isEnum()) {
			return ElementKind.ENUM;
		}
		else if (refBinding.isRecord()) {
			return ElementKind.RECORD;
		}
		else if (refBinding.isAnnotationType()) {
			return ElementKind.ANNOTATION_TYPE;
		}
		else if (refBinding.isInterface()) {
			return ElementKind.INTERFACE;
		}
		else if (refBinding.isClass()) {
			return ElementKind.CLASS;
		}
		else {
			throw new IllegalArgumentException("TypeElement " + new String(refBinding.shortReadableName()) +  //$NON-NLS-1$
					" has unexpected attributes " + refBinding.modifiers); //$NON-NLS-1$
		}
	}

	@Override
	public Set<Modifier> getModifiers()
	{
		ReferenceBinding refBinding = (ReferenceBinding)_binding;
		int modifiers = refBinding.modifiers;
		if (refBinding.isInterface() && refBinding.isNestedType()) {
			modifiers |= ClassFileConstants.AccStatic;
		}
		return Factory.getModifiers(modifiers, getKind(), refBinding.isBinaryBinding());
	}

	@Override
	public NestingKind getNestingKind() {
		ReferenceBinding refBinding = (ReferenceBinding)_binding;
		if (refBinding.isAnonymousType()) {
			return NestingKind.ANONYMOUS;
		} else if (refBinding.isLocalType()) {
			return NestingKind.LOCAL;
		} else if (refBinding.isMemberType()) {
			return NestingKind.MEMBER;
		}
		return NestingKind.TOP_LEVEL;
	}

	@Override
	PackageElement getPackage()
	{
		ReferenceBinding binding = (ReferenceBinding)_binding;
		return _env.getFactory().newPackageElement(binding.fPackage);
	}

	@Override
	public Name getQualifiedName() {
		ReferenceBinding binding = (ReferenceBinding)_binding;
		char[] qName;
		if (binding.isMemberType()) {
			qName = CharOperation.concatWith(binding.enclosingType().compoundName, binding.sourceName, '.');
			CharOperation.replace(qName, '$', '.');
		} else {
			qName = CharOperation.concatWith(binding.compoundName, '.');
		}
		return new NameImpl(qName);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.apt.model.ElementImpl#getSimpleName()
	 * @return last segment of name, e.g. for pa.pb.X.Y return Y.
	 */
	@Override
	public Name getSimpleName()
	{
		ReferenceBinding binding = (ReferenceBinding)_binding;
		return new NameImpl(binding.sourceName());
	}

	@Override
	public TypeMirror getSuperclass() {
		ReferenceBinding binding = (ReferenceBinding)_binding;
		ReferenceBinding superBinding = binding.superclass();
		if (null == superBinding || binding.isInterface()) {
			return _env.getFactory().getNoType(TypeKind.NONE);
		}
		// superclass of a type must be a DeclaredType
		return _env.getFactory().newTypeMirror(superBinding);
	}
	
	@Override
	public List<? extends TypeParameterElement> getTypeParameters() {
		ReferenceBinding binding = (ReferenceBinding)_binding;
		TypeVariableBinding[] variables = binding.typeVariables();
		if (variables.length == 0) {
			return Collections.emptyList();
		}
		List<TypeParameterElement> params = new ArrayList<>(variables.length); 
		for (TypeVariableBinding variable : variables) {
			params.add(_env.getFactory().newTypeParameterElement(variable, this));
		}
		return Collections.unmodifiableList(params);
	}

	@Override
	public boolean hides(Element hidden)
	{
		if (!(hidden instanceof TypeElementImpl)) {
			return false;
		}
		ReferenceBinding hiddenBinding = (ReferenceBinding)((TypeElementImpl)hidden)._binding;
		if (hiddenBinding.isPrivate()) {
			return false;
		}
		ReferenceBinding hiderBinding = (ReferenceBinding)_binding;
		if (TypeBinding.equalsEquals(hiddenBinding, hiderBinding)) {
			return false;
		}
		if (!hiddenBinding.isMemberType() || !hiderBinding.isMemberType()) {
			return false;
		}
		if (!CharOperation.equals(hiddenBinding.sourceName, hiderBinding.sourceName)) {
			return false;
		}
		return null != hiderBinding.enclosingType().findSuperTypeOriginatingFrom(hiddenBinding.enclosingType()); 
	}

	@Override
	public String toString() {
		ReferenceBinding binding = (ReferenceBinding) this._binding;
		char[] concatWith = CharOperation.concatWith(binding.compoundName, '.');
		if (binding.isNestedType()) {
			CharOperation.replace(concatWith, '$', '.');
			return new String(concatWith);
		}
		return new String(concatWith);

	}

}
