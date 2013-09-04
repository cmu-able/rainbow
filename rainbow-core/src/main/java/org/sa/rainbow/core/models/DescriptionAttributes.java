/**
 * Creatd May 14, 2007.
 */
package org.sa.rainbow.core.models;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Stores common elements of attributes for a description file, particularly
 * for Probe and Effector descriptions.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class DescriptionAttributes implements Comparable<DescriptionAttributes> {

    private static final String INFO_SUFFIX = "Info";

    /** The name of the element */
    public String name = null;
    /** The target location, e.g., hostname, of the element */
    public String location = null;
    /** The String name of the "kind" for this element */
    public String kindName = null;
    /** Key'd map of info attributes, not sorted. */
    public Map<String,String> info = null;
    /** Key'd map of String arrays, sorted by natural order of key. */
    public Map<String,String[]> arrays = null;

    /**
     * Default Constructor.
     */
    public DescriptionAttributes () {
        info = new HashMap<String,String>();
        arrays = new TreeMap<String,String[]>();
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo (DescriptionAttributes o) {
        return name.compareTo(o.name);
    }

    public String infoPropName () {
        return kindName + INFO_SUFFIX;
    }

}
