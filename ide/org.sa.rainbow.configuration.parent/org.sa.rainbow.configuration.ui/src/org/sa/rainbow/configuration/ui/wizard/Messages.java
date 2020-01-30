package org.sa.rainbow.configuration.ui.wizard;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.sa.rainbow.configuration.ui.wizard.messages"; //$NON-NLS-1$
	
	public static String HelloWorldProject_Label;
	public static String HelloWorldProject_Description;
	public static String RainbowProject_Label;
	public static String RainbowProject_Description;
	public static String RainbowProjectWithTargetDefinition_Label;
	public static String RainbowProjectWithTargetDefinition_Description;
	
	static {
	// initialize resource bundle
	NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
