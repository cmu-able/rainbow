package org.sa.rainbow.brass;

import java.util.Properties;

public class PropertiesConnector {

    public static final String PRISM_ADV_EXPORT_PROPKEY = "prism.adv.export";
    public static final String PRISM_PARAMETERS_PROPKEY = "prism.parameters";
    public static final String PRISM_PROPERTIES_PROPKEY = "prism.properties";
    public static final String PRISM_MODEL_PROPKEY = "prism.model";
    public static final String PRISM_BIN_PROPKEY = "prism.bin";
    public static final String MAP_PROPKEY              = "customize.map.json";
    public static final String PRISM_OUTPUT_DIR         = "prism.tmpdir";
    // TODO: Move the hardwired values into some configuration file
    public static final Properties DEFAULT = new Properties ();

    static {
        PropertiesConnector.DEFAULT.setProperty (PropertiesConnector.PRISM_BIN_PROPKEY,
                "/Applications/prism-4.3.beta-osx64/bin/prism");
        PropertiesConnector.DEFAULT.setProperty (PropertiesConnector.PRISM_MODEL_PROPKEY,
                "/Users/jcamara/Dropbox/Documents/Work/Projects/BRASS/rainbow-prototype/trunk/rainbow-brass/prismtmp/prismtmp.prism");
        PropertiesConnector.DEFAULT.setProperty (PropertiesConnector.PRISM_PROPERTIES_PROPKEY,
                "/Users/jcamara/Dropbox/Documents/Work/Projects/BRASS/rainbow-prototype/trunk/rainbow-brass/prismtmp/mapbot.props");
        PropertiesConnector.DEFAULT.setProperty (PropertiesConnector.PRISM_PARAMETERS_PROPKEY,
                "INITIAL_BATTERY=30000,INITIAL_HEADING=1");
        PropertiesConnector.DEFAULT.setProperty (PropertiesConnector.PRISM_ADV_EXPORT_PROPKEY,
                "/Users/jcamara/Dropbox/Documents/Work/Projects/BRASS/rainbow-prototype/trunk/rainbow-brass/prismtmp/botpolicy.adv");
        DEFAULT.setProperty (MAP_PROPKEY,
                "/Users/jcamara/Dropbox/Documents/Work/Projects/BRASS/rainbow-prototype/trunk/rainbow-brass/prismtmp/map.json");
        DEFAULT.setProperty (PRISM_OUTPUT_DIR,
                "/Users/jcamara/Dropbox/Documents/Work/Projects/BRASS/rainbow-prototype/trunk/rainbow-brass/prismtmp/");
    }


}
