package org.sa.rainbow.core.models;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class holds utility preference information parsed from the description file (usu. Yaml).
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class UtilityPreferenceDescription {

    public static class UtilityAttributes {
        public String              label   = null;
        public String              mapping = null;
        public String              desc    = null;
        public Map<Number, Number> values  = null; // gets assigned during parsing
    }

    protected Map<String, UtilityAttributes> utilities        = null;

    public Map<String, UtilityAttributes> getUtilities () {
        return utilities;
    }

    public Map<String, Map<String, Double>> weights          = null;
    public Map<String, Map<String, Object>> attributeVectors = null;
    public ModelReference                   associatedModel  = null;
    private Map<String, UtilityFunction>    utilityFunctions = null;

    /**
     * Default Constructor.
     */
    public UtilityPreferenceDescription () {
        utilities = new HashMap<String, UtilityAttributes> ();
        weights = new HashMap<String, Map<String, Double>> ();
        attributeVectors = new HashMap<String, Map<String, Object>> ();
        utilityFunctions = new HashMap<String, UtilityFunction> ();
    }

    public void addAttributes (String label, UtilityAttributes atts) {
        utilities.put (label, atts);
        utilityFunctions.put (label, new UtilityFunction (label, atts.label, atts.mapping, atts.desc, atts.values));
    }

    public Map<String, UtilityFunction> getUtilityFunctions () {
        return Collections.unmodifiableMap (utilityFunctions);
    }


}
