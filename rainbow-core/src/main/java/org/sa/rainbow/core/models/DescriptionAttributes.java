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
    
    private String location = null;
    /** The String name of the "kind" for this element */
    
    private String kindName = null;
    /** Key'd map of info attributes, not sorted. */
    
    private Map<String,String> info = null;
    /** Key'd map of String arrays, sorted by natural order of key. */
    
    private Map<String,String[]> arrays = null;

    /**
     * Default Constructor.
     */
    public DescriptionAttributes () {
        setInfo (new HashMap<String,String>());
        setArrays (new TreeMap<String,String[]>());
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo (DescriptionAttributes o) {
        return name.compareTo(o.name);
    }


    public String infoPropName () {
        return getKindName() + INFO_SUFFIX;
    }


    public String getLocation () {
        return location;
    }

    public void setLocation (String location) {
        this.location = location;
    }


    public String getKindName () {
        return kindName;
    }

    public void setKindName (String kindName) {
        this.kindName = kindName;
    }


    public Map<String,String> getInfo () {
        return info;
    }

    public void putInfo (String key, String val) {
        info.put (key, val);
    }

    public void setInfo (Map<String,String> info) {
        this.info = info;
    }


    public Map<String,String[]> getArrays () {
        return arrays;
    }

    public void setArrays (Map<String,String[]> arrays) {
        this.arrays = arrays;
    }

    public void putArray (String arrayKey, String[] valArray) {
        arrays.put (arrayKey, valArray);
    }

}
