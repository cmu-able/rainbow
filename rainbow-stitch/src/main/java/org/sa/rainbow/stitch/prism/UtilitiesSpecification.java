/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.sa.rainbow.stitch.prism;

import java.util.Map;
import java.util.TreeMap;

/**
 * Utilities description object.
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public class UtilitiesSpecification {
    public Map<String, UtilityDescription> utilities;
    public Map<String, Map<String,Double>> weights;
    public Map<String, TreeMap<String,Integer>> vectors;
}
