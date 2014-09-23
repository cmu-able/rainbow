/**
 * Created January 18, 2007.
 */
package org.sa.rainbow.core.gauges;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.sa.rainbow.core.util.Pair;
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
    protected Map<String, OperationRepresentation>   m_commandSignatures = null;
    /** Stores, by name, a hash of type-name and any default value of the setup parameters. */
    protected Map<String, TypedAttributeWithValue> m_setupParams    = null;
    /** Stores, by name, a hash of the type-name and any default value of the configuration parameters. */
    protected Map<String, TypedAttributeWithValue> m_configParams   = null;

    /**
     * Main Constructor.
     */
    public GaugeTypeDescription (String gaugeType, String typeComment) {
        m_typeName = gaugeType;
        m_typeComment = typeComment == null ? "" : typeComment;
        m_commandSignatures = new HashMap<String, OperationRepresentation> ();
        m_setupParams = new HashMap<String, TypedAttributeWithValue> ();
        m_configParams = new HashMap<String, TypedAttributeWithValue> ();
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

    public void addCommandSignature (String key, OperationRepresentation commandRep) {
        m_commandSignatures.put (key, commandRep);
    }

    public OperationRepresentation findCommandSignature (String name) {
        return m_commandSignatures.get(name);
    }

    public List<Pair<String, OperationRepresentation>> commandSignatures () {
        List<Pair<String, OperationRepresentation>> valueList = new ArrayList<> ();
        for (Entry<String, OperationRepresentation> pair : m_commandSignatures.entrySet ()) {
            valueList.add (new Pair<String, OperationRepresentation> (pair.getKey (), pair.getValue ()));
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

    public List<TypedAttributeWithValue> configParams () {
        List<TypedAttributeWithValue> paramList = new ArrayList<TypedAttributeWithValue> (m_configParams.values ());
        Collections.sort(paramList);
        return paramList;
    }

}
