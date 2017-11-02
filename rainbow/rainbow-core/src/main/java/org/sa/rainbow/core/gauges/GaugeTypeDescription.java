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
 * Created January 18, 2007.
 */
package org.sa.rainbow.core.gauges;

import org.sa.rainbow.core.util.Pair;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

import java.util.*;
import java.util.Map.Entry;

/**
 * This Class captures the information in a Gauge Type description.
 * The captured information can be used to create Gauge Instance descriptions.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class GaugeTypeDescription {

    private String m_typeName = null;
    private String m_typeComment = null;
    /** Stores, by type name, a hash of type-name pairs. */
    Map<String, OperationRepresentation> m_commandSignatures = null;
    /** Stores, by name, a hash of type-name and any default value of the setup parameters. */
    private Map<String, TypedAttributeWithValue> m_setupParams = null;
    /** Stores, by name, a hash of the type-name and any default value of the configuration parameters. */
    private Map<String, TypedAttributeWithValue> m_configParams = null;

    /**
     * Main Constructor.
     */
    public GaugeTypeDescription (String gaugeType, String typeComment) {
        m_typeName = gaugeType;
        m_typeComment = typeComment == null ? "" : typeComment;
        m_commandSignatures = new HashMap<> ();
        m_setupParams = new HashMap<> ();
        m_configParams = new HashMap<> ();
    }


    public GaugeInstanceDescription makeInstance (String gaugeName, String instComment) {
        // create a Gauge Instance description using type, name, and comments
        GaugeInstanceDescription inst = new GaugeInstanceDescription(m_typeName, gaugeName, m_typeComment, instComment);
        // transfer the set of reported values, setup params, and config params
        for (Pair<String, OperationRepresentation> valPair : commandSignatures ()) {
            try {
                inst.addCommandSignature (valPair.firstValue (), valPair.secondValue ().clone ());
            }
            catch (CloneNotSupportedException e) {
            }
        }
        for (TypedAttributeWithValue param : setupParams ()) {
            inst.addSetupParam ((TypedAttributeWithValue )param.clone ());
        }
        for (TypedAttributeWithValue param : configParams ()) {
            inst.addConfigParam ((TypedAttributeWithValue )param.clone ());
        }
        return inst;
    }


    public String gaugeType () {
        return m_typeName;
    }


    public String typeComment () {
        return m_typeComment;
    }

    public void addCommandSignature (String key, String commandPattern) {
        OperationRepresentation rep = OperationRepresentation.parseCommandSignature (commandPattern);
        if (rep != null) {
            addCommandSignature (key, rep);
        }
    }

    void addCommandSignature (String key, OperationRepresentation commandRep) {
        m_commandSignatures.put (key, commandRep);
    }

    public OperationRepresentation findCommandSignature (String name) {
        return m_commandSignatures.get(name);
    }


    public List<Pair<String, OperationRepresentation>> commandSignatures () {
        List<Pair<String, OperationRepresentation>> valueList = new ArrayList<> ();
        for (Entry<String, OperationRepresentation> pair : m_commandSignatures.entrySet ()) {
            valueList.add (new Pair<> (pair.getKey (), pair.getValue ()));
        }
        Collections.sort(valueList);
        return valueList;
    }

    public void addSetupParam (TypedAttributeWithValue triple) {
        m_setupParams.put (triple.getName (), triple);
    }

    public TypedAttributeWithValue findSetupParam (String name) {
        return m_setupParams.get(name);
    }


    public List<TypedAttributeWithValue> setupParams () {
        List<TypedAttributeWithValue> paramList = new ArrayList<> (m_setupParams.values ());
        Collections.sort(paramList);
        return paramList;
    }

    public void addConfigParam (TypedAttributeWithValue triple) {
        m_configParams.put (triple.getName (), triple);
    }

    public TypedAttributeWithValue findConfigParam (String name) {
        return m_configParams.get(name);
    }


    public List<TypedAttributeWithValue> configParams () {
        List<TypedAttributeWithValue> paramList = new ArrayList<> (m_configParams.values ());
        Collections.sort(paramList);
        return paramList;
    }

}
