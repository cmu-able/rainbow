package org.sa.rainbow.configuration.scoping;
import org.eclipse.emf.ecore.EObject;
/*
Copyright 2020 Carnegie Mellon University

Permission is hereby granted, free of charge, to any person obtaining a copy of this 
software and associated documentation files (the "Software"), to deal in the Software 
without restriction, including without limitation the rights to use, copy, modify, merge,
 publish, distribute, sublicense, and/or sell copies of the Software, and to permit 
 persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all 
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
DEALINGS IN THE SOFTWARE.
 */
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.impl.EReferenceImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.common.types.TypesPackage;
import org.eclipse.xtext.common.types.xtext.AbstractTypeScopeProvider;
import org.eclipse.xtext.common.types.xtext.ClasspathBasedTypeScopeProvider;
import org.eclipse.xtext.common.types.xtext.TypesAwareDefaultGlobalScopeProvider;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.impl.ImportUriGlobalScopeProvider;
import org.sa.rainbow.configuration.rcl.RclPackage;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * A global scope provider that is both able to process the importURI attributes
 * to establish cross links with other EMF models, and to handle references to
 * JVM types. This class is a subclass of {@link ImportUriGlobalScopeProvider}
 * in order to get the importURI behavior and delegates references to JVM types
 * to an instance of AbstractTypeScopeProvider obtained through Guice.
 * 
 * @see TypesAwareDefaultGlobalScopeProvider where part of is getScope()
 *      function is duplicated here.
 * @see ClasspathBasedTypeScopeProvider
 * @see org.eclipse.xtext.common.types.xtext.ui.JdtBasedSimpleTypeScopeProvider
 *
 */
@SuppressWarnings("restriction")
public class RclGlobalScopeProvider extends ImportUriGlobalScopeProvider {

	class DoubleScope implements IScope {

		private IScope m_one;
		private IScope m_two;

		DoubleScope(IScope one, IScope two) {
			m_one = one;
			m_two = two;
			
		}
		
		@Override
		public IEObjectDescription getSingleElement(QualifiedName name) {
			
			IEObjectDescription se = m_one.getSingleElement(name);
			if (se == null) se= m_two.getSingleElement(name);
			return se;
		}

		@Override
		public Iterable<IEObjectDescription> getElements(QualifiedName name) {
			return Iterables.concat(m_one.getElements(name), m_two.getElements(name));
		}

		@Override
		public IEObjectDescription getSingleElement(EObject object) {
			IEObjectDescription se = m_one.getSingleElement(object);
			if (se == null) se = m_two.getSingleElement(object);
			return se;
		}

		@Override
		public Iterable<IEObjectDescription> getElements(EObject object) {
			return Iterables.concat(m_one.getElements(object), m_two.getElements(object));
		}

		@Override
		public Iterable<IEObjectDescription> getAllElements() {
			return Iterables.concat(m_one.getAllElements(), m_two.getAllElements());
		}
		
	}
	
	
	/*
	 * AbstractTypeScopeProvider is bound to
	 * org.eclipse.xtext.common.types.xtext.ClasspathBasedTypeScopeProvider in the
	 * "Standalone" setup and to
	 * org.eclipse.xtext.common.types.xtext.ui.JdtBasedSimpleTypeScopeProvider int
	 * the IU setup. In both cases, the class AbstractTypeScopeProvider which is the
	 * usual global scope provider when using the JVM types uses an
	 * AbstractTypeScopeProvider instance (through Guice) to handle all references
	 * to JVM types : we do the same here.
	 */
	@Inject
	@Named("jvmtypes")private AbstractTypeScopeProvider typeScopeProvider;
	

	@Override
	public IScope getScope(Resource resource, EReference reference, Predicate<IEObjectDescription> filter) {
		if (EcoreUtil2.isAssignableFrom(TypesPackage.Literals.JVM_TYPE,
				reference.getEReferenceType()) /*
												 * || reference.equals(RclPackage.Literals.TYPE__REF)
												 */) {
			IScope typeScope = typeScopeProvider.getScope(resource, reference, filter);
			return typeScope;
		} else if (reference.equals(RclPackage.Literals.TYPE__REF)) {
			EReferenceImpl javaReference = (EReferenceImpl )EcoreUtil.copy(reference);
			javaReference.setEType(TypesPackage.eINSTANCE.getJvmType());
			return new DoubleScope(super.getScope(resource, reference, filter), typeScopeProvider.getScope(resource, javaReference, filter));
		}
	    else {
			return super.getScope(resource, reference, filter);
		}
	}
	
	
	
}