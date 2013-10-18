package org.sa.rainbow.core.models;

import java.util.HashMap;
import java.util.Map;

/**
 * This class holds utility preference information parsed from the description
 * file (usu. Yaml).
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class UtilityPreferenceDescription {

    public static class UtilityAttributes {
        public String label = null;
        public String mapping = null;
        public String desc = null;
        public Map<Number,Number> values = null;  // gets assigned during parsing
    }

    public Map<String,UtilityAttributes> utilities = null;
    public Map<String,Map<String,Double>> weights = null;
    public Map<String,Map<String,Object>> attributeVectors = null;

    /**
     * Default Constructor.
     */
    public UtilityPreferenceDescription() {
        utilities = new HashMap<String,UtilityAttributes>();
        weights = new HashMap<String,Map<String,Double>>();
        attributeVectors = new HashMap<String,Map<String,Object>>();
    }

}
