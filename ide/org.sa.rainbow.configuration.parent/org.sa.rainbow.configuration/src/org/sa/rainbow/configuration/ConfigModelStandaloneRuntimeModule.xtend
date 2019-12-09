package org.sa.rainbow.configuration

import com.google.inject.Binder
import com.google.inject.name.Names
import org.eclipse.emf.ecore.EPackage
import org.eclipse.xtext.common.types.access.CachingClasspathTypeProviderFactory
import org.eclipse.xtext.common.types.access.IJvmTypeProvider
import org.eclipse.xtext.common.types.xtext.AbstractTypeScopeProvider
import org.eclipse.xtext.common.types.xtext.ClasspathBasedTypeScopeProvider
import org.sa.rainbow.stitch.stitch.StitchPackage

class ConfigModelRuntimeStandaloneModule extends ConfigModelRuntimeModule {
	
	override configure(Binder binder) {
		super.configure(binder)
		configureJvmTypeProvider(binder)
				if (!EPackage.Registry.INSTANCE.containsKey("http://www.sa.org/rainbow/stitch/Stitch")) {
			EPackage.Registry.INSTANCE.put("http://www.sa.org/rainbow/stitch/Stitch", StitchPackage.eINSTANCE)
		}
	
	}
	
	
	def configureJvmTypeProvider(Binder binder) {
		binder.requestStaticInjection(ConfigAttributeConstants)
		
		binder.bind(AbstractTypeScopeProvider).annotatedWith(Names.named("jvmtypes")).to(ClasspathBasedTypeScopeProvider)
		binder.bind(IJvmTypeProvider.Factory).annotatedWith(Names.named("jvmtypes")).to(CachingClasspathTypeProviderFactory)
	}
}