package org.sa.rainbow.brass.gauges;

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
    protected static final String CHARGE            = "BatteryCharge";
    protected static final String DEADLINE          = "Deadline";
    protected static final String CLOCK             = "Clock";
    protected static final String GROUNDPLANE       = "GroundPlane";
    protected static final String CALIBRATION_ERROR = "Calibration";

    protected static final String LOC_PATTERN         = "topic: /amcl_pose/pose/pose.*position.*\\n.*x: (.*)\\n.*y: (.*)\\n.*z.*\\norientation.*\\n.*x: (.*)\\n.*y: (.*)\\n.*z: (.*)\\n.*w: (.*)(.*)";
    protected static final String CHARGE_PATTERN      = "topic: /energy_monitor/voltage.*\\n.*data: (.*)\\n(.*)";
    protected static final String DEADLINE_PATTERN    = "topic: /notify_user.*\\n.*new_deadline: (.*)\\n.*user(.*)";
    protected static final String CLOCK_PATTERN       = "topic: /clock.*\\n.*secs: ([0-9]*).*nsecs: ([0-9]*)(.*)";
    private static final String   GROUND_PATTERN      = "topic: /calibration/ground_plane_error.*\\n.*data: \\[(.*)\\](.*)";
    private static final String   CALIBRATION_PATTERN = "topic: /calibration/calibration_error.*\n.*data: \\[(.*)\\](.*)";
    protected String              last_x;
    protected String              last_y;
    private String                last_w;
    private int                   last_voltage        = 0;
    private double                m_currentTime;
    private double                last_voltage_time;
    private double                last_charge         = 0;

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
        addPattern (CHARGE, Pattern.compile (CHARGE_PATTERN, Pattern.DOTALL));
        addPattern (DEADLINE, Pattern.compile (DEADLINE_PATTERN, Pattern.DOTALL));
        addPattern (CLOCK, Pattern.compile (CLOCK_PATTERN, Pattern.DOTALL));
        addPattern (GROUNDPLANE, Pattern.compile (GROUND_PATTERN, Pattern.DOTALL));
        addPattern (CALIBRATION_ERROR, Pattern.compile (CALIBRATION_PATTERN, Pattern.DOTALL));
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
            log("Setting location to " + x + ", " + y);
            if (locationDifferent (x, y, w)) {
                IRainbowOperation op = m_commands.get ("location");
                Map<String, String> pMap = new HashMap<> ();
                pMap.put (op.getParameters ()[0], x);
                pMap.put (op.getParameters ()[1], y);
                pMap.put (op.getParameters ()[2], w);
                issueCommand (op, pMap);
            }
        }
        else if (CHARGE.equals (matchName)) {
            int voltage = Integer.parseInt (group);

            double charge = voltageToChargeConsiderTime (voltage);

            if (chargeDifferent (charge)) {
//                double charge = voltage2Charge (voltage);
                IRainbowOperation op = m_commands.get ("charge");
                Map<String, String> pMap = new HashMap<> ();
                pMap.put (op.getParameters ()[0], Double.toString (charge));
                issueCommand (op, pMap);
            }
            restGroup = 2;
        }
        else if (DEADLINE.equals (matchName)) {
            String date = m.group (1).trim ();
            IRainbowOperation op = m_commands.get ("deadline");
            Map<String, String> pMap = new HashMap<> ();
            pMap.put (op.getParameters ()[0], date);
            issueCommand (op, pMap);
            restGroup = 2;
        }
        else if (CLOCK.equals (matchName)) {
            long secs = Long.parseLong (m.group (1).trim ());
            long nsecs = 0;
            if (!"".equals (m.group (2).trim ())) {
                nsecs = Long.parseLong (m.group (2).trim ());
            }
            double realSecs = secs + TimeUnit.MILLISECONDS.convert (nsecs, TimeUnit.NANOSECONDS) / 1000.0;
            m_currentTime = realSecs;
            IRainbowOperation op = m_commands.get ("clock");
            Map<String, String> pMap = new HashMap<> ();
            pMap.put (op.getParameters ()[0], Double.toString (realSecs));
            issueCommand (op, pMap);
            restGroup = 3;
        }
        else if (GROUNDPLANE.equals (matchName)) {
            String[] vals = m.group (1).split (",");
            if (!numberCheck (vals)) {
                log ("Got things that aren't numbers for ground plane " + vals.toString ());
                return;
            }
            IRainbowOperation op = m_commands.get ("ground-plane");
            Map<String, String> pMap = new HashMap<> ();
            pMap.put (op.getParameters ()[0], vals[0]);
            pMap.put (op.getParameters ()[1], vals[1]);
            issueCommand (op, pMap);
            restGroup = 2;
        }
        else if (CALIBRATION_ERROR.equals (matchName)) {
            String[] vals = m.group (1).split (",");
            if (!numberCheck (vals)) {
                log ("Got things that aren't numbers for calibration " + vals.toString ());
                return;
            }
            IRainbowOperation op = m_commands.get ("calibration");
            Map<String, String> pMap = new HashMap<> ();
            pMap.put (op.getParameters ()[0], vals[0]);
            pMap.put (op.getParameters ()[1], vals[1]);
            pMap.put (op.getParameters ()[2], vals[2]);
            pMap.put (op.getParameters ()[3], vals[3]);
            pMap.put (op.getParameters ()[4], vals[4]);
            pMap.put (op.getParameters ()[5], vals[5]);
            issueCommand (op, pMap);
            restGroup = 4;
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

    private boolean chargeDifferent (double charge) {
        if (Math.round (last_charge) != Math.round (charge)) {
            last_charge = charge;
            return true;
        }
        return false;
    }

    private boolean voltageDifferent (int voltage) {
        if (last_voltage != voltage) {
            last_voltage_time = m_currentTime;
            last_voltage = voltage;
            return true;
        }
        return false;
    }

    private double voltageToChargeConsiderTime (int voltage) {
        voltageDifferent (voltage);
        double estimatedCharge = PowerConverter.voltage2ChargeOpt (voltage)
                - BatteryPredictor.batteryConsumption (MapTranslator.ROBOT_HALF_SPEED_CONST,
                        MapTranslator.ROBOT_LOC_MODE_MED_CONST, m_currentTime - last_voltage_time);
        if (estimatedCharge < PowerConverter.voltage2ChargePess (voltage))
            return PowerConverter.voltage2ChargePess (voltage);
        return estimatedCharge;

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
