package org.sa.rainbow.brass.confsynthesis;

import java.util.Properties;

public class PropertiesConfigurationSynthesizer {

	public static final String CONSTRAINTMODEL_PROPKEY = "alloy.basemodel";
    public static final String BASEMODEL_PROPKEY = "prism.basemodel";
    public static final String TEMPMODEL_PROPKEY = "prism.tempmodel";
    public static final String PROPS_PROPKEY = "prism.props";
    public static final String POLICY_PROPKEY = "prism.policy";
    
    public static final String MODELS_PATH = "/Users/jcamara/Dropbox/Documents/Work/projects/rainbow/deployments/rainbow-brass/brass-p2-cp3/alloy";
    
    public static final Properties DEFAULT = new Properties ();
    static {
        DEFAULT.setProperty (PropertiesConfigurationSynthesizer.CONSTRAINTMODEL_PROPKEY, MODELS_PATH+"ros-turtlebot.als");
        DEFAULT.setProperty (PropertiesConfigurationSynthesizer.BASEMODEL_PROPKEY, MODELS_PATH+"baseconfmodel.prism");
        DEFAULT.setProperty (PropertiesConfigurationSynthesizer.TEMPMODEL_PROPKEY, MODELS_PATH+"tempmodel.prism");
        DEFAULT.setProperty (PropertiesConfigurationSynthesizer.POLICY_PROPKEY, MODELS_PATH+"pol");
        DEFAULT.setProperty (PropertiesConfigurationSynthesizer.PROPS_PROPKEY, MODELS_PATH+"simpletest1.props");
    }
}
