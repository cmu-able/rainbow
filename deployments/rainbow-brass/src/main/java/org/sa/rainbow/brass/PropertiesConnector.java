package org.sa.rainbow.brass;

import java.util.Properties;

public class PropertiesConnector {

    public static final String PRISM_ADV_EXPORT_PROPKEY = "prism.adv.export";
    public static final String PRISM_PARAMETERS_PROPKEY = "prism.parameters";
    public static final String PRISM_PROPERTIES_PROPKEY = "prism.properties";
    public static final String PRISM_MODEL_PROPKEY = "prism.model";
    public static final String PRISM_BIN_PROPKEY = "prism.bin";
    public static final String MAP_PROPKEY              = "customize.map.json";
    public static final String PRISM_OUTPUT_DIR_PROPKEY         = "prism.tmpdir";

    public static final Properties DEFAULT = new Properties ();

    static {
    	// To use standalone Prism generation (outside of Rainbow), set the environment variables below in your system
    	// e.g. .bashrc or .profile
    	String prismOutDir = System.getenv("PRISMOUTDIR"); // e.g., "/Users/jcamara/Dropbox/Documents/Work/Projects/BRASS/rainbow-prototype/trunk/rainbow-brass/prismtmp/";
    	String prismBin = System.getenv("PRISMBIN");// e.g., "/Applications/prism-4.3.beta-osx64/bin/prism";
    	
    	if (prismOutDir == null || prismBin == null)
    		System.out.println("Failed to initialize the default properties connector: environment variables not set");
    	
    	DEFAULT.setProperty (PRISM_BIN_PROPKEY, prismBin);
    	DEFAULT.setProperty (PRISM_OUTPUT_DIR_PROPKEY, prismOutDir);
        DEFAULT.setProperty (PRISM_MODEL_PROPKEY, prismOutDir + "prismtmp.prism");
        DEFAULT.setProperty (PRISM_PROPERTIES_PROPKEY, prismOutDir + "mapbotp2cp1.props");
        DEFAULT.setProperty (PRISM_PARAMETERS_PROPKEY, "INITIAL_BATTERY=30000,INITIAL_HEADING=1");
        DEFAULT.setProperty (PRISM_ADV_EXPORT_PROPKEY, prismOutDir + "botpolicy.adv");
        DEFAULT.setProperty (MAP_PROPKEY, prismOutDir + "map-p2cp1.json");
    }


}
