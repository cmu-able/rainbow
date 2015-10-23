/**
 * Created September 6, 2006
 */
package org.sa.rainbow.core.models;


import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

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
 * Defines a utility function to store id, name, and encapsulate domain and
 * range values.  A convenience method returns the exact, nearest, or closest-
 * approximation range value given a domain value.
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class UtilityFunction {


    private String m_id = null;

    private String m_label = null;

    private String m_mapping = null;

    private String m_desc = null;

    private SortedMap<Double,Double> m_values = null;

    /**
     * Main Constructor
     */
    public UtilityFunction (String id, String label, String mapping, String desc, Map<?, ?> values) {
        m_id = id;
        m_label = label;
        m_mapping = mapping;
        m_desc = desc;
        m_values = new TreeMap<> ();

        // store values into value map as doubles
        for (Object k : values.keySet()) {
            Object v = values.get(k);
            if (k instanceof Number && v instanceof Number) {
                m_values.put(((Number )k).doubleValue(), ((Number )v).doubleValue());
            }
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */

    @Override
    public Object clone() throws CloneNotSupportedException {
        return new UtilityFunction (
                m_id,
                m_label,
                m_mapping,
                m_desc,
                m_values);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */

    @Override
    public String toString() {
        return "Util: " + m_id + " (" + m_label + " : " + m_mapping + ") " + m_values;
    }


    public String id () {
        return m_id;
    }


    public String label () {
        return m_label;
    }


    public String mapping () {
        return m_mapping;
    }


    public String description () {
        return m_desc;
    }


    public Map<Double,Double> values () {
        return m_values;
    }

    /**
     * Returns an exact value if one exists for the supplied x.  Otherwise,
     * linearly extrapolate from known range and return value, using this
     * slope-based formula, a and b are immediately adjacent values of x,
     * where a &lt; x &lt; b:<p>
     *   <code>f(x) = f(a) + ( (f(b)-f(a))/(b-a) * (x-a) )</code>
     * If x falls below the lowest domain value x_L, then return f(x_L).
     * If x falls above the highest domain value x_H, then return f(x_H).
     * @param x  supplied x for which to compute utility value
     * @return exact or estimated value
     */
    public double f (double x) {
        double rv = 0.0;
        if (m_values.containsKey(x)) {
            rv = m_values.get(x);
        } else if (x < m_values.firstKey()) {
            rv = m_values.get(m_values.firstKey());
        } else if (x > m_values.lastKey()) {
            rv = m_values.get(m_values.lastKey());
        } else {  // x falls between two known values, extrapolte
            double a = m_values.headMap(x).lastKey();
            double b = m_values.tailMap(x).firstKey();
            double f_a = m_values.get(a);
            double f_b = m_values.get(b);
            rv = f_a + ((f_b-f_a)/(b-a))*(x-a);
        }
        return rv;
    }

}
