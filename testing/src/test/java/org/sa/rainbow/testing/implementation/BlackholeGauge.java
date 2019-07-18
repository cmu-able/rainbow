package org.sa.rainbow.testing.implementation;


import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.gauges.RegularPatternGauge;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reports the set of clients that are blacklisted, by setting the blackholed property of the load balancer
 *
 * <pre>
 *    BlackHoleGaugeT:
 *     commands:
 *       blockedIPs: "BlackholerT.setBlackholed({String})"
 *     setupParams:
 *       targetIP:
 *         type: String
 *         default: "localhost"
 *       beaconPeriod:
 *         type: long
 *         default: 30000
 *       javaClass:
 *         type: String
 *         default: "org.sa.rainbow.translator.znn.gauges.BlackholeGauge"
 *     configParams:
 *       targetProbeType:
 *         type: String
 *         default: ~
 * </pre>
 *
 * @author Bradley Schmerl: schmerl
 */
public class BlackholeGauge extends RegularPatternGauge {

    public static final String NAME = "G - Blackholed Clients";
    public static final String NONE = "NOBLACKHOLE";
    /**
     * List of values reported by this Gauge
     */
    private static final String[] commandNames = {"setBlackholed"};
    private static final String DEFAULT = "DEFAULT";
    public String lastReport = "";

    /**
     * Main constructor.
     *
     * @throws RainbowException
     */
    public BlackholeGauge(String id, long beaconPeriod, TypedAttribute gaugeDesc, TypedAttribute modelDesc,
                          List<TypedAttributeWithValue> setupParams, Map<String, IRainbowOperation> mappings) throws RainbowException {

        super(NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams,
                mappings);

        // match a list of IP addresses, comma separated
        addPattern(
                DEFAULT,
                Pattern.compile("((?:\\d{1,3}\\.){3}\\d{1,3})(?:,\\s*((?:\\d{1,3}\\.){3}\\d{1,3}))*"));
        addPattern(NONE, Pattern.compile("^none$"));
    }

    @Override
    protected void doMatch(String matchName, Matcher m) {
        if (Objects.equals(matchName, DEFAULT)) {

            // Send the list of attackers
            int c = m.groupCount();
            StringBuilder ips = new StringBuilder();
//			String[] ips = new String[c];
            for (int i = 1; i < c; i++) {
//				ips[i] = m.group(i + 1);
                ips.append(m.group(i));
                ips.append(",");
            }
            ips.deleteCharAt(ips.length() - 1);

            if (doReport(ips.toString())) {
                recordLastReport(ips.toString());
                IRainbowOperation cmd = m_commands.values().iterator().next();
                Map<String, String> pm = new HashMap<>();
                pm.put(cmd.getParameters()[0], ips.toString());
                issueCommand(cmd, pm);

            }
        } else if (matchName == NONE) {
            // The probe reported "none" meaning that there is nothing blackholed
            if (doReport("")) {
                recordLastReport("");
                IRainbowOperation cmd = m_commands.values().iterator().next();
                Map<String, String> pm = new HashMap<>();
                pm.put(cmd.getParameters()[0], "");
                issueCommand(cmd, pm);
            }
        }
    }

    private void recordLastReport(String report) {
        lastReport = report;
    }

    private boolean doReport(String report) {
//        boolean doReport;
//        synchronized (lastReport) {
//            doReport = !lastReport.equals(report);
//        }
//		return doReport;
        return true;
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