/*
 * generated by Xtext 2.19.0
 */
package org.acme


/**
 * Initialization support for running Xtext languages without Equinox extension registry.
 */
class AcmeStandaloneSetup extends AcmeStandaloneSetupGenerated {

	def static void doSetup() {
		new AcmeStandaloneSetup().createInjectorAndDoEMFRegistration()
	}
}