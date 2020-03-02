package org.sa.rainbow.stitch.tests

import org.sa.rainbow.stitch.tests.StitchInjectorProvider
import org.sa.rainbow.stitch.StitchRuntimeStandaloneModule

class StitchTestInjectorProvider extends StitchInjectorProvider {
	override createRuntimeModule() {
		return new StitchRuntimeStandaloneModule() {
			override bindClassLoaderToInstance() {
				StitchInjectorProvider.getClassLoader()
			}
		}
	}
}