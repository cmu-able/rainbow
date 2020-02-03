package org.sa.rainbow.configuration.scoping;

import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.common.types.TypesPackage;
import org.eclipse.xtext.common.types.xtext.AbstractTypeScopeProvider;
import org.eclipse.xtext.common.types.xtext.ClasspathBasedTypeScopeProvider;
import org.eclipse.xtext.common.types.xtext.TypesAwareDefaultGlobalScopeProvider;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.impl.ImportUriGlobalScopeProvider;

import com.google.common.base.Predicate;
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
		if (EcoreUtil2.isAssignableFrom(TypesPackage.Literals.JVM_TYPE, reference.getEReferenceType())) {
			IScope typeScope = typeScopeProvider.getScope(resource, reference, filter);
			return typeScope;
		} else {
			return super.getScope(resource, reference, filter);
		}
	}
}