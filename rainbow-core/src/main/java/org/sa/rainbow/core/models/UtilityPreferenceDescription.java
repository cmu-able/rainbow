/*
 * The MIT License
 *
 * Copyright 2014 CMU ABLE Group.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
        utilities = new HashMap<> ();
        weights = new HashMap<> ();
        attributeVectors = new HashMap<> ();
        utilityFunctions = new HashMap<> ();
    }

    public void addAttributes (String label, UtilityAttributes atts) {
        utilities.put (label, atts);
        utilityFunctions.put (label, new UtilityFunction (label, atts.label, atts.mapping, atts.desc, atts.values));
    }


    public Map<String, UtilityFunction> getUtilityFunctions () {
        return Collections.unmodifiableMap (utilityFunctions);
    }


}
