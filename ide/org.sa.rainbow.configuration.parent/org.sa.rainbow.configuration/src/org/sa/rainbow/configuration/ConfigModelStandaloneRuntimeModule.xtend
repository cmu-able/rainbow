package org.sa.rainbow.configuration

import com.google.inject.Binder
import com.google.inject.name.Names
import org.eclipse.xtext.common.types.access.CachingClasspathTypeProviderFactory
import org.eclipse.xtext.common.types.access.IJvmTypeProvider
import org.eclipse.xtext.common.types.xtext.AbstractTypeScopeProvider
import org.eclipse.xtext.common.types.xtext.ClasspathBasedTypeScopeProvider

class ConfigModelRuntimeStandaloneModule extends ConfigModelRuntimeModule {
	
	override configure(Binder binder) {
		super.configure(binder)
		configureJvmTypeProvider(binder)
	}
	
	
	def configureJvmTypeProvider(Binder binder) {
		binder.bind(AbstractTypeScopeProvider).annotatedWith(Names.named("jvmtypes")).to(ClasspathBasedTypeScopeProvider)
		binder.bind(IJvmTypeProvider.Factory).annotatedWith(Names.named("jvmtypes")).to(CachingClasspathTypeProviderFactory)
	}
}