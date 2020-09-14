/*
 * generated by Xtext 2.19.0
 */
package org.sa.rainbow.configuration.ide

import com.google.inject.Guice
import org.eclipse.xtext.util.Modules2
import org.sa.rainbow.configuration.RclRuntimeModule
import org.sa.rainbow.configuration.RclStandaloneSetup

/**
 * Initialization support for running Xtext languages as language servers.
 */
class RclIdeSetup extends RclStandaloneSetup {

	override createInjector() {
		Guice.createInjector(Modules2.mixin(new RclRuntimeModule, new RclIdeModule))
	}
	
}
