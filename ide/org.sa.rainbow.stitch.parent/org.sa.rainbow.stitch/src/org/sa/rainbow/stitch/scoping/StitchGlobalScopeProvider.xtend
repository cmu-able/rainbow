package org.sa.rainbow.stitch.scoping

import com.google.common.base.Predicate
import com.google.inject.Inject
import com.google.inject.name.Named
import org.eclipse.emf.ecore.EReference
import org.eclipse.emf.ecore.impl.EReferenceImpl
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.common.types.TypesPackage
import org.eclipse.xtext.common.types.xtext.AbstractTypeScopeProvider
import org.eclipse.xtext.resource.IEObjectDescription
import org.eclipse.xtext.scoping.impl.ImportUriGlobalScopeProvider
import org.sa.rainbow.stitch.stitch.StitchPackage

class StitchGlobalScopeProvider extends ImportUriGlobalScopeProvider {
	@Inject
	@Named("jvmtypes")AbstractTypeScopeProvider typeScopeProvider;
	public 
	override getScope(Resource resource, EReference reference, Predicate<IEObjectDescription> filter) {
		super.getScope(resource, reference, filter)
		if (EcoreUtil2.isAssignableFrom(TypesPackage.Literals.JVM_TYPE, reference.EReferenceType)) {
			return typeScopeProvider.getScope(resource, reference, filter)
		}
		else if (reference == StitchPackage.Literals.ID_OR_METHOD_CALL__REF) { // THis is disabled in Stitch.xtext because idOrMethodCall is not a reference any longer
			val javaRef = EcoreUtil.copy(reference) as EReferenceImpl
			javaRef.EType = TypesPackage.eINSTANCE.jvmType
			val methodRef = EcoreUtil.copy(reference) as EReferenceImpl
			methodRef.EType = TypesPackage.eINSTANCE.jvmMember
			return new CombinedScope(super.getScope(resource, reference, filter), 
				typeScopeProvider.getScope(resource, javaRef, filter), 
				typeScopeProvider.getScope(resource, methodRef, filter)
			)
		}
//		else if (reference == StitchPackage.Literals.METHOD_CALL__METHOD) {
//			val javaRef = EcoreUtil.copy(reference) as EReferenceImpl
//			javaRef.EType = TypesPackage.eINSTANCE.jvmMember
//			
//			val methodRef = EcoreUtil.copy(reference) as EReferenceImpl
//			methodRef.EType = TypesPackage.eINSTANCE.jvmMember
//			return new CombinedScope(super.getScope(resource, reference, filter), 
//				typeScopeProvider.getScope(resource, javaRef, filter)
//			)
//		}
		return super.getScope(resource, reference, filter)
	}
	
}