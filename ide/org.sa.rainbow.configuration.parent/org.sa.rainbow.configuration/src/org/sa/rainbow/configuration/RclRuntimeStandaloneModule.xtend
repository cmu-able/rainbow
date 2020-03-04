package org.sa.rainbow.configuration
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
import com.google.inject.Binder
import com.google.inject.name.Names
import org.eclipse.emf.ecore.EPackage
import org.eclipse.xtext.common.types.access.CachingClasspathTypeProviderFactory
import org.eclipse.xtext.common.types.access.IJvmTypeProvider
import org.eclipse.xtext.common.types.xtext.AbstractTypeScopeProvider
import org.eclipse.xtext.common.types.xtext.ClasspathBasedTypeScopeProvider
import org.sa.rainbow.stitch.stitch.StitchPackage

class RclRuntimeStandaloneModule extends RclRuntimeModule {
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