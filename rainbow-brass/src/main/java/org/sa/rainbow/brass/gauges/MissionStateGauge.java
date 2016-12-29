package org.sa.rainbow.brass.gauges;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.gauges.RegularPatternGauge;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by schmerl on 12/28/2016.
 */
public class MissionStateGauge extends RegularPatternGauge {
    private static final String NAME = "Mission State Gauge";
    protected static final String LOC = "LocationRecording";

    protected static final String LOC_PATTERN="topic: /amcl_pose/pose/pose/position.*x: (.*)\\n.*y: (.*)\\n.*z.*";

    protected String last_x;
    protected String last_y;



    /**
     * Main Constructor the Gauge that is hardwired to the Probe.
     *
     * @param id           the unique ID of the Gauge
     * @param beaconPeriod the liveness beacon period of the Gauge
     * @param gaugeDesc    the type-name description of the Gauge
     * @param modelDesc    the type-name description of the Model the Gauge updates
     * @param setupParams  the list of setup parameters with their values
     * @param mappings     the list of Gauge Value to Model Property mappings
     */
    public MissionStateGauge (String id, long beaconPeriod, TypedAttribute gaugeDesc, TypedAttribute modelDesc, List<TypedAttributeWithValue> setupParams, Map<String, IRainbowOperation> mappings) throws RainbowException {
        super (NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);
        addPattern (LOC, Pattern.compile (LOC_PATTERN, Pattern.DOTALL));
    }

    @Override
    protected void doMatch (String matchName, Matcher m) {
        if (LOC.equals (matchName)) {
            String x = m.group(1).trim();
            String y = m.group(2).trim();
            if (locationDifferent(x,y)) {
                IRainbowOperation op = m_commands.get ("location");
                Map<String, String> pMap = new HashMap<> ();
                pMap.put (op.getParameters ()[0], x);
                pMap.put (op.getParameters ()[1], y);
                issueCommand (op, pMap);
            }
        }
    }

    private boolean locationDifferent (String x, String y) {
        boolean different = !x.equals (last_x) || !y.equals (last_y);
        if (different) {
            last_x = x;
            last_y = y;
        }
        return different;
    }
}
