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

import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.util.Beacon;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This Class captures the information in a Gauge Instance description, though
 * instantiated first from the Gauge Type description.  This description helps
 * to track a Gauge from creation through termination.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class GaugeInstanceDescription extends GaugeTypeDescription {

    public enum State {
        UNINITIALIZED, CREATING, CREATED, CONFIGURING,
        /** Although a Gauge can technically be configured, but not yet alive,
         *  for simplicity, we'll consider configured to be alive.  Pausing
         *  a Gauge is separately handled from these lifecycle states. */
        ALIVE,
        TERMINATED 
    }

    private static final String LOCATION_PARAM_NAME = "targetIP";


    private String m_instName = null;

    private String m_instComment = null;

    private TypedAttribute                        m_modelDesc         = null;

    private Map<String, OperationRepresentation> m_mappings          = null;


    private String m_id = null;
    private State m_state = State.UNINITIALIZED;

    private Beacon m_beacon = null;

    /**
     * Main Constructor.
     */
    public GaugeInstanceDescription (String gaugeType, String gaugeName,
                                     String typeComment, String instComment) {

        super(gaugeType, typeComment);
        m_instName = gaugeName;
        m_instComment = instComment == null ? "" : instComment;
        m_mappings = new HashMap<> ();
        TypedAttributeWithValue tav = findSetupParam (IGauge.SETUP_BEACON_PERIOD);
        if (tav != null) {
            String beaconPeriod = (String )tav.getValue ();
            if (beaconPeriod != null) {
                m_beacon = new Beacon (Long.valueOf (beaconPeriod));
            }
        }
        if (m_beacon == null) {
            m_beacon = new Beacon ();
        }
    }


    public String gaugeName () {
        return m_instName;
    }


    public String instanceComment () {
        return m_instComment;
    }


    public TypedAttribute modelDesc () {
        return m_modelDesc;
    }

    public void setModelDesc (TypedAttribute modelDesc) {
        m_modelDesc = modelDesc;
        Collection<OperationRepresentation> commands = m_commandSignatures.values ();
        for (OperationRepresentation command : commands) {
            command.setModel (modelDesc);
        }
    }

    @Override
    public void addCommandSignature (String key, OperationRepresentation commandRep) {
        if (m_modelDesc != null) {
            commandRep.setModel (m_modelDesc);
        }
        super.addCommandSignature (key, commandRep);
    }

    public void addCommand (String key, OperationRepresentation operation) {
        m_mappings.put (key, operation);
    }

    public IRainbowOperation findMapping (String name) {
        return m_mappings.get(name);
    }


    public Map<String, OperationRepresentation> mappings () {
        return m_mappings;
    }


    public String id () {
        return m_id;
    }
    public void setID (String id) {
        m_id = id;
    }

    /**
     * Assuming the location info is always set via the setup parameter "targetIP",
     * this method returns the value of that location.
     * @return String  the string indicating the location of the target host
     */

    private String location () {
        String location = null;
        TypedAttributeWithValue loc = findSetupParam (LOCATION_PARAM_NAME);
        if (loc != null) {
            location = (String )loc.getValue ();
        }
        return location;
    }


    public Beacon beacon () {
        return m_beacon;
    }

    public State state () {
        return m_state;
    }
    public void setState (State newState) {
        m_state = newState;
    }
    public boolean notYetCreated () {
        return m_state.compareTo(State.CREATING) < 0;
    }
    public boolean notYetConfigured () {
        return m_state.compareTo(State.CREATED) >= 0
                && m_state.compareTo(State.CONFIGURING) < 0;
    }
    public boolean isAlive () {
        return m_state == State.ALIVE;
    }


    public static String genID (GaugeInstanceDescription gd) {
        return gd.gaugeName () + ":" + gd.gaugeType () + "@" + gd.location ();
    }

}
