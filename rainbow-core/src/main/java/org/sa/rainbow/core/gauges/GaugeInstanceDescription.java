/**
 * Created January 18, 2007.
 */
package org.sa.rainbow.core.gauges;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sa.rainbow.core.models.commands.IRainbowModelCommandRepresentation;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.util.Beacon;

/**
 * This Class captures the information in a Gauge Instance description, though
 * instantiated first from the Gauge Type description.  This description helps
 * to track a Gauge from creation through termination.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class GaugeInstanceDescription extends GaugeTypeDescription {

    public static enum State {
        UNINITIALIZED, CREATING, CREATED, CONFIGURING,
        /** Although a Gauge can technically be configured, but not yet alive,
         *  for simplicity, we'll consider configured to be alive.  Pausing
         *  a Gauge is separately handled from these lifecycle states. */
        ALIVE,
        TERMINATED 
    }

    public static final String LOCATION_PARAM_NAME = "targetIP";

    private String m_instName = null;
    private String m_instComment = null;
    private TypedAttribute                        m_modelDesc         = null;
    private Map<String, IRainbowModelCommandRepresentation> m_mappings          = null;

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
        m_instComment = instComment;
        m_mappings = new HashMap<> ();
        m_beacon = new Beacon();
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
    }

    public void addCommand (IRainbowModelCommandRepresentation mapping) {
        m_mappings.put (mapping.getCommandName (), mapping);
    }

    public IRainbowModelCommandRepresentation findMapping (String name) {
        return m_mappings.get(name);
    }

    @SuppressWarnings("unchecked")
    public List<IRainbowModelCommandRepresentation> mappings () {
        List<IRainbowModelCommandRepresentation> mapList = new ArrayList<IRainbowModelCommandRepresentation> (
                m_mappings.values ());
        return mapList;
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
    public String location () {
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

}
