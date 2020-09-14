package org.sa.rainbow.configuration.ui;

public class ConfigurationActivator extends org.sa.rainbow.configuration.ui.internal.ConfigurationActivator {

	public ConfigurationActivator() {
		super();
	}
	
	/*
	 * @Override protected Injector createInjector(String language) { Injector
	 * injector = super.createInjector(language); if
	 * (!EPackage.Registry.INSTANCE.containsKey(
	 * "http://www.sa.org/rainbow/stitch/Stitch")) {
	 * EPackage.Registry.INSTANCE.put("http://www.sa.org/rainbow/stitch/Stitch",
	 * StitchPackage.eINSTANCE); } IResourceFactory resourceFactory =
	 * injector.getInstance(IResourceFactory.class); IResourceServiceProvider
	 * serviceProvider = injector.getInstance(IResourceServiceProvider.class);
	 * Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap ().put("s",
	 * resourceFactory);
	 * IResourceServiceProvider.Registry.INSTANCE.getExtensionToFactoryMap().put(
	 * "s", serviceProvider); return injector; }
	 */

}
