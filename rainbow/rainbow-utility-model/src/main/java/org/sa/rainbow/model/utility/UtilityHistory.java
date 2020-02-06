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
package org.sa.rainbow.model.utility;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.sa.rainbow.core.models.ModelReference;

public class UtilityHistory {

    private Map<String, SortedMap<Long, Double>> m_utilityHistory;
    private ModelReference          m_model;

    public UtilityHistory (ModelReference model) {
        m_model = model;
        m_utilityHistory = new HashMap<> ();
    }

    UtilityHistory (Map<String, SortedMap<Long, Double>> history, ModelReference ref) {
        this (ref);
        for (Entry<String, SortedMap<Long, Double>> entry : history.entrySet ()) {
            SortedMap<Long, Double> m = new TreeMap<> ();
            m.putAll (entry.getValue ());
            m_utilityHistory.put (entry.getKey (), m);
        }
    }

    public UtilityHistory copy () {
        UtilityHistory c = new UtilityHistory (m_model);
        c.m_utilityHistory = new HashMap<> ();
        for (String key : getUtilityKeys ()) {
            SortedMap<Long, Double> m = new TreeMap<> ();
            c.m_utilityHistory.put (key, m);
            for (Entry<Long, Double> e : m_utilityHistory.get (key).entrySet ()) {
                m.put (e.getKey (), e.getValue ());
            }
        }
        return c;
    }

    public void add (String utility, long timestamp, double utilityValue) {
        SortedMap<Long, Double> values = m_utilityHistory.get (utility);
        if (values == null) {
            values = new TreeMap<> ();
            m_utilityHistory.put (utility, values);
        }
        values.put (timestamp, utilityValue);
    }

    public void add (String utility, double utilityValue) {
        add (utility, new Date ().getTime (), utilityValue);
    }

    public SortedMap<Long, Double> getUtilityHistory (String utility) {
        SortedMap<Long, Double> history = m_utilityHistory.get (utility);
        if (history != null)
            return Collections.unmodifiableSortedMap (history);
        else
            return new TreeMap<> ();
    }

    public Collection<String> getUtilityKeys () {
        return m_utilityHistory.keySet ();
    }

    public ModelReference getModelReference () {
        return m_model;
    }

    public void forget (long timestampRecorded) {
        m_utilityHistory.remove (timestampRecorded);
    }
}
