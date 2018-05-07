package org.sa.rainbow.brass.gauges.p2_cp3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sa.rainbow.brass.model.map.BatteryPredictor;
import org.sa.rainbow.brass.model.map.MapTranslator;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.gauges.RegularPatternGauge;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

/**
 * Created by schmerl on 12/28/2016.
 */
public class MissionStateGauge extends RegularPatternGauge {
    private static final String   NAME              = "Mission State Gauge";
    protected static final String LOC               = "LocationRecording";
    protected static final String DEADLINE          = "Deadline";
    protected static final String RECONFIGURING = "Reconfig";

    protected static final String LOC_PATTERN         = "topic: /tf/transforms\\[0\\]/transform.*\\ntranslation.*\\n.*x: (.*)\\n.*y: (.*)\\n.*z.*\\nrotation.*\\n.*x: (.*)\\n.*y: (.*)\\n.*z: (.*)\\n.*w: (.*)(.*)";
    protected static final String DEADLINE_PATTERN    = "topic: /notify_user.*\\n.*new_deadline: (.*)\\n.*user(.*)";
    protected static final String RECONFIG_PATTERN    = "topic: /ig_interpreter/reconfiguring.*\n.*data:(.*)";
    protected String              last_x;
    protected String              last_y;
    private String                last_w;

    /**
     * Main Constructor the Gauge that is hardwired to the Probe.
     *
     * @param id
     *            the unique ID of the Gauge
     * @param beaconPeriod
     *            the liveness beacon period of the Gauge
     * @param gaugeDesc
     *            the type-name description of the Gauge
     * @param modelDesc
     *            the type-name description of the Model the Gauge updates
     * @param setupParams
     *            the list of setup parameters with their values
     * @param mappings
     *            the list of Gauge Value to Model Property mappings
     */
    public MissionStateGauge (String id, long beaconPeriod, TypedAttribute gaugeDesc, TypedAttribute modelDesc,
            List<TypedAttributeWithValue> setupParams, Map<String, IRainbowOperation> mappings)
                    throws RainbowException {
        super (NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);
        addPattern (LOC, Pattern.compile (LOC_PATTERN, Pattern.DOTALL));
        addPattern (DEADLINE, Pattern.compile (DEADLINE_PATTERN, Pattern.DOTALL));
        addPattern (RECONFIGURING, Pattern.compile(RECONFIG_PATTERN, Pattern.DOTALL));
    }

    @Override
    protected void doMatch (String matchName, Matcher m) {
        String group = m.group (1);
        int restGroup = 0;
        if (LOC.equals (matchName)) {
            String x = group.trim ();
            String y = m.group (2).trim ();

            String a = m.group (3).trim ();
            String b = m.group (4).trim ();
            String c = m.group (5).trim ();
            String d = m.group (6).trim ();
            restGroup = 7;

            String w = yawFromQuarternion (a, b, c, d);

            if (locationDifferent (x, y, w)) {
                IRainbowOperation op = m_commands.get ("location");
                Map<String, String> pMap = new HashMap<> ();
                pMap.put (op.getParameters ()[0], x);
                pMap.put (op.getParameters ()[1], y);
                pMap.put (op.getParameters ()[2], w);
                issueCommand (op, pMap);
            }
        }
        else if (DEADLINE.equals (matchName)) {
            String date = m.group (1).trim ();
            IRainbowOperation op = m_commands.get ("deadline");
            Map<String, String> pMap = new HashMap<> ();
            pMap.put (op.getParameters ()[0], date);
            issueCommand (op, pMap);
            restGroup = 2;
        }
        else if (RECONFIGURING.equals(matchName)) {
        	String mode = m.group(1).trim().toLowerCase();
        	  IRainbowOperation op = m_commands.get ("reconfiguring");
              Map<String, String> pMap = new HashMap<> ();
              pMap.put (op.getParameters ()[0], mode);
              issueCommand (op, pMap);
              restGroup = 2;
        	
        }
        if (m.groupCount () == restGroup) {
            String rest = m.group (restGroup);
            if (!rest.isEmpty () && rest.indexOf ("topic:") != -1) {
                log ("Gauge had more: " + rest.substring (rest.indexOf ("topic:")));
            }
        }
    }

    private boolean numberCheck (String[] vals) {
        for (String s : vals) {
            try {
                Double.parseDouble (s);
            }
            catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    // Converstino to yaw from http://www.chrobotics.com/library/understanding-quaternions
    private String yawFromQuarternion (String a, String b, String c, String d) {
        double A = Double.parseDouble (a);
        double B = Double.parseDouble (b);
        double C = Double.parseDouble (c);
        double D = Double.parseDouble (d);

        double w = Math.atan ((2 * (A * B + C * D)) / (A * A - B * B - C * C + D * D));
        return Double.toString (w);

    }

    private boolean locationDifferent (String x, String y, String w) {
        boolean different = !x.equals (last_x) || !y.equals (last_y) || !w.equals (last_w);
        if (different) {
            last_x = x;
            last_y = y;
            last_w = w;
        }
        return different;
    }
}
