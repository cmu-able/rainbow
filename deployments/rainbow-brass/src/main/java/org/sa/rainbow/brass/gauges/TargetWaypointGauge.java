package org.sa.rainbow.brass.gauges;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.gauges.RegularPatternGauge;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

public class TargetWaypointGauge extends RegularPatternGauge {

    private static final String NAME           = "TargetWaypointGauge";
    private static final String TARGET         = "target";
    private static final String TARGET_PATTERN = ".*target.*:.*[\\\"']([^\\\"']*)[\\\"'].*";

    public TargetWaypointGauge (String id, long beaconPeriod, TypedAttribute gaugeDesc, TypedAttribute modelDesc,
            List<TypedAttributeWithValue> setupParams, Map<String, IRainbowOperation> mappings)
                    throws RainbowException {
        super (NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);
        addPattern (TARGET, Pattern.compile (TARGET_PATTERN, Pattern.DOTALL));
    }

    @Override
    protected void doMatch (String matchName, Matcher m) {
        if (TARGET.equals (matchName)) {
            String waypoint = m.group (1).trim ();
            IRainbowOperation op = m_commands.get ("target");
            Map<String, String> pMap = new HashMap<> ();
            pMap.put (op.getParameters ()[0], waypoint);
            issueCommand (op, pMap);
        }
    }

}
