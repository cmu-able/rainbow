package org.sa.rainbow.brass.adaptation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * @author jcamara
 *
 */

public class PrismConnector {
    private static final String PRISM_ADV_EXPORT_PROPKEY = "prism.adv.export";

    private static final String PRISM_PARAMETERS_PROPKEY = "prism.parameters";

    private static final String PRISM_PROPERTIES_PROPKEY = "prism.properties";

    private static final String PRISM_MODEL_PROPKEY = "prism.model";

    private static final String PRISM_BIN_PROPKEY = "prism.bin";

    // TODO: Move the hardwired values into some configuration file
    private static final Properties DEFAULT = new Properties ();
    static {
        DEFAULT.setProperty (PRISM_BIN_PROPKEY, "/Applications/prism-4.3.beta-osx64/bin/prism");
        DEFAULT.setProperty (PRISM_MODEL_PROPKEY,
                "/Users/jcamara/Dropbox/Documents/Work/Projects/BRASS/rainbow-prototype/trunk/rainbow-brass/prismtmp/prismtmp.prism");
        DEFAULT.setProperty (PRISM_PROPERTIES_PROPKEY,
                "/Users/jcamara/Dropbox/Documents/Work/Projects/BRASS/rainbow-prototype/trunk/rainbow-brass/prismtmp/mapbot.props");
        DEFAULT.setProperty (PRISM_PARAMETERS_PROPKEY, "INITIAL_BATTERY=5000");
        DEFAULT.setProperty (PRISM_ADV_EXPORT_PROPKEY,
                "/Users/jcamara/Dropbox/Documents/Work/Projects/BRASS/rainbow-prototype/trunk/rainbow-brass/prismtmp/botpolicy.adv");
    }
    private static final boolean m_print_output = true;

    private String m_prismBin;
    private String m_prismModel;
    private String m_prismProperties;
    private String m_prismParameters;
    private String m_prismAdvExport;

    String convertToAbsolute (String filename) {
        if (filename.startsWith ("\"") && filename.endsWith ("\"") && filename.length () > 2) {
            filename = filename.substring (1, filename.length () - 1);
        }
        if (filename.startsWith ("~" + File.separator)) {
            filename = System.getProperty ("user.home") + filename.substring (1);
        }
        return new File (filename).getAbsolutePath ();
    }

    public PrismConnector (Properties props) {
        if (props == null) {
            props = DEFAULT;
        }
        m_prismBin = props.getProperty (PRISM_BIN_PROPKEY);
        m_prismModel = props.getProperty (PRISM_MODEL_PROPKEY);
        m_prismProperties = props.getProperty (PRISM_PROPERTIES_PROPKEY);
        m_prismParameters = props.getProperty (PRISM_PARAMETERS_PROPKEY);
        m_prismAdvExport = props.getProperty (PRISM_ADV_EXPORT_PROPKEY);

        // Convert to full paths
        m_prismBin = convertToAbsolute (m_prismBin);
        m_prismModel = convertToAbsolute (m_prismModel);
        m_prismProperties = convertToAbsolute (m_prismProperties);
        m_prismAdvExport = convertToAbsolute (m_prismAdvExport);
    }

    public String getPrismModelLocation () {
        return m_prismModel;
    }

    public String getPrismPolicyLocation () {
        return m_prismAdvExport;
    }

    public void invoke (int currentLocationId, int toLocationId) {
        String line;
        String locationParameterString = ",INITIAL_LOCATION=" + String.valueOf (currentLocationId) + ",TARGET_LOCATION="
                + String.valueOf (toLocationId);

        try {
            Process p = Runtime.getRuntime ()
                    .exec (m_prismBin + " " + m_prismModel + " " + m_prismProperties + " -prop 1 -ex -const "
                            + m_prismParameters + locationParameterString + " -exportadv " + m_prismAdvExport);
            if (m_print_output) {
                BufferedReader input = new BufferedReader (new InputStreamReader (p.getInputStream ()));
                while ((line = input.readLine ()) != null) {
                    System.out.println (line);
                }
                input.close ();
            }
            try{
            p.waitFor();
            } catch (InterruptedException e1){
            	e1.printStackTrace();
            }

        }
        catch (IOException e) {
            e.printStackTrace ();
        }
    }

    public static void main (String[] args) throws Exception {
        PrismConnector conn = new PrismConnector (DEFAULT);
        conn.invoke (8, 0); // Go from "ls" to "l1" in simplemap
    }
}
