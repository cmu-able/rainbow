/**
 * Created January 18, 2007.
 */
package org.sa.rainbow.gauges;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

/**
 * This Class captures the information in a Gauge Type description.
 * The captured information can be used to create Gauge Instance descriptions.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class GaugeTypeDescription {

    protected String m_typeName = null;
    protected String m_typeComment = null;
    /** Stores, by type name, a hash of type-name pairs. */
    protected Map<String, TypedAttribute>          m_commandSignatures = null;
    /** Stores, by name, a hash of type-name and any default value of the setup parameters. */
    protected Map<String, TypedAttributeWithValue> m_setupParams    = null;
    /** Stores, by name, a hash of the type-name and any default value of the configuration parameters. */
    protected Map<String, TypedAttributeWithValue> m_configParams   = null;

    /**
     * Main Constructor.
     */
    public GaugeTypeDescription (String gaugeType, String typeComment) {
        m_typeName = gaugeType;
        m_typeComment = typeComment;
        m_commandSignatures = new HashMap<String, TypedAttribute> ();
        m_setupParams = new HashMap<String, TypedAttributeWithValue> ();
        m_configParams = new HashMap<String, TypedAttributeWithValue> ();
    }

    public GaugeInstanceDescription makeInstance (String gaugeName, String instComment) {
        // create a Gauge Instance description using type, name, and comments
        GaugeInstanceDescription inst = new GaugeInstanceDescription(m_typeName, gaugeName, m_typeComment, instComment);
        // transfer the set of reported values, setup params, and config params
        for (TypedAttribute valPair : commandSignatures ()) {
            inst.addCommandSignature ((TypedAttribute )valPair.clone ());
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

    public void addCommandSignature (TypedAttribute pair) {
        m_commandSignatures.put (pair.getName (), pair);
    }

    public TypedAttribute findCommandSignature (String name) {
        return m_commandSignatures.get(name);
    }

    @SuppressWarnings("unchecked")
    public List<TypedAttribute> commandSignatures () {
        List<TypedAttribute> valueList = new ArrayList<TypedAttribute> (m_commandSignatures.values ());
        Collections.sort(valueList);
        return valueList;
    }

    public void addSetupParam (TypedAttributeWithValue triple) {
        m_setupParams.put (triple.getName (), triple);
    }

    public TypedAttributeWithValue findSetupParam (String name) {
        return m_setupParams.get(name);
    }

    @SuppressWarnings("unchecked")
    public List<TypedAttributeWithValue> setupParams () {
        List<TypedAttributeWithValue> paramList = new ArrayList<TypedAttributeWithValue> (m_setupParams.values ());
        Collections.sort(paramList);
        return paramList;
    }

    public void addConfigParam (TypedAttributeWithValue triple) {
        m_configParams.put (triple.getName (), triple);
    }

    public TypedAttributeWithValue findConfigParam (String name) {
        return m_configParams.get(name);
    }

    @SuppressWarnings("unchecked")
    public List<TypedAttributeWithValue> configParams () {
        List<TypedAttributeWithValue> paramList = new ArrayList<TypedAttributeWithValue> (m_configParams.values ());
        Collections.sort(paramList);
        return paramList;
    }

}
