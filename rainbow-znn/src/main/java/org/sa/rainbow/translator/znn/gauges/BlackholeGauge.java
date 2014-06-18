package org.sa.rainbow.translator.znn.gauges;

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

public class BlackholeGauge extends RegularPatternGauge {

    public static final String NAME = "G - Blackholed Clients";
    public static final String NONE = "NOBLACKHOLE";

    public String                 lastReport = "";

    /** List of values reported by this Gauge */
    private static final String[] commandNames = { "setBlackholed" };

    private static final String DEFAULT = "DEFAULT";

    /**
     * Main constructor.
     * @throws RainbowException 
     */
    public BlackholeGauge(String id, long beaconPeriod, TypedAttribute gaugeDesc, TypedAttribute modelDesc,
            List<TypedAttributeWithValue> setupParams, Map<String, IRainbowOperation> mappings) throws RainbowException {

        super(NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams,
                mappings);

        // match a list of IP addresses
        // TODO: How to match empty reports? (I.e., nothing is blackholed)
        addPattern(
                DEFAULT,
                Pattern.compile("((?:\\d{1,3}\\.){3}\\d{1,3})(?:,\\s*((?:\\d{1,3}\\.){3}\\d{1,3}))*"));
        addPattern(NONE, Pattern.compile("^none$"));
    }

    @Override
    protected void doMatch(String matchName, Matcher m) {
        if (matchName == DEFAULT) {

            // Send the list of attackers
            int c = m.groupCount();
            StringBuffer ips = new StringBuffer ();
//			String[] ips = new String[c];
            for (int i = 1; i < c; i++) {
//				ips[i] = m.group(i + 1);
                ips.append (m.group (i));
                ips.append (",");
            }
            ips.deleteCharAt (ips.length () - 1);

            if (doReport (ips.toString ())) {
                recordLastReport (ips.toString ());
                IRainbowOperation cmd = m_commands.values ().iterator ().next ();
                Map<String, String> pm = new HashMap<String, String> ();
                pm.put (cmd.getParameters ()[0], ips.toString ());
                issueCommand (cmd, pm);

            }
        } else if (matchName == NONE) {
            // The probe reported "none" meaning that there is nothing blackholed
            if (doReport ("")) {
                recordLastReport ("");
                IRainbowOperation cmd = m_commands.values ().iterator ().next ();
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
            doReport = !lastReport.equals(report);
        }
//		return doReport;
        return true;
    }

    @Override
    protected void initProperty(String name, Object value) {
        // TODO Auto-generated method stub

    }


//    // This is solely for testing remote model access
//    @Override
//    protected void runAction () {
//        if (m_modelPort == null) {
//            try {
//                m_modelPort = RainbowPortFactory.createModelsManagerRequirerPort ();
//                IModelInstance<IAcmeSystem> inst = m_modelPort.<IAcmeSystem> getModelInstance ("Acme", "ZNewsSys");
//                IAcmeSystem sys = inst.getModelInstance ();
//                System.out.println ();
//            }
//            catch (RainbowConnectionException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace ();
//            }
//        }
//        super.runAction ();
//    }

}
