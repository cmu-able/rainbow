package org.sa.rainbow.translator.znn.gauges;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.gauges.RegularPatternGauge;
import org.sa.rainbow.core.models.commands.IRainbowModelCommandRepresentation;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

public class ThrottleGauge extends RegularPatternGauge {

    public static final String    NAME         = "G - Throttled Clients";
    public static final String    NONE         = "NOTHROTTLE";

    public String                 lastReport   = "";

    private static final String[] commandNames = { "setThrottled" };
    private static final String   DEFAULT      = "DEFAULT";

    public ThrottleGauge (String id, long beaconPeriod, TypedAttribute gaugeDesc, TypedAttribute modelDesc,
            List<TypedAttributeWithValue> setupParams, List<IRainbowModelCommandRepresentation> mappings)
                    throws RainbowException {
        super (NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);

        addPattern (DEFAULT, Pattern.compile ("((?:\\d{1,3}\\.){3}\\d{1,3})(?:,\\s*((?:\\d{1,3}\\.){3}\\d{1,3}))*"));
        addPattern (NONE, Pattern.compile ("^none$"));
    }

    @Override
    protected void doMatch (String matchName, Matcher m) {
        if (matchName == DEFAULT) {

            // Send the list of attackers
            int c = m.groupCount ();
            StringBuffer ips = new StringBuffer ();
//          String[] ips = new String[c];
            for (int i = 0; i < c; i++) {
//              ips[i] = m.group(i + 1);
                ips.append (m.group (i + 1));
                ips.append (",");
            }
            ips.deleteCharAt (ips.length () - 1);

            if (doReport (ips.toString ())) {
                recordLastReport (ips.toString ());
                IRainbowModelCommandRepresentation cmd = m_commands.values ().iterator ().next ();
                Map<String, String> pm = new HashMap<String, String> ();
                pm.put (cmd.getParameters ()[0], ips.toString ());
                issueCommand (cmd, pm);

            }
        }
        else if (matchName == NONE) {
            // The probe reported "none" meaning that there is nothing blackholed
            if (doReport ("")) {
                recordLastReport ("");
                IRainbowModelCommandRepresentation cmd = m_commands.values ().iterator ().next ();
                Map<String, String> pm = new HashMap<String, String> ();
                pm.put (cmd.getParameters ()[0], "");
                issueCommand (cmd, pm);
            }
        }
    }

    private void recordLastReport (String report) {
        synchronized (lastReport) {
            lastReport = report;
        }
    }

    private boolean doReport (String report) {
        boolean doReport;
        synchronized (lastReport) {
            doReport = !lastReport.equals (report);
        }
//      return doReport;
        return true;
    }

    @Override
    protected void initProperty (String name, Object value) {
        // TODO Auto-generated method stub

    }

}
