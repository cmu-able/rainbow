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

/**
 * Created by schmerl on 12/27/2016.
 */
public class InstructionGraphGauge extends RegularPatternGauge {
    public static final    String NAME = "Task Progress Gauge";
    protected static final String IG   = "IGProgress";
    protected static final String NEWIG = "NewIG";
    private static final String   FINISHEDIG = "IGFinished";

    protected static final String INSTRUCTION_PATTERN = "topic: /ig_action_server/feedback/feedback/sequence[^\\d]*(\\d+)\\.\\d+:([^:]*):(.*)\\\".*";
    protected static final String NEWIG_PATTERN = "topic: /ig_action_server/feedback/feedback/sequence.*Received new " +
            "valid IG: (.*)";
    protected static final String FINISHIG_PATTERN    = "topic: /ig_action_server/feedback/feedback/sequence.*Finished!.*";


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
    public InstructionGraphGauge (String id, long beaconPeriod, TypedAttribute gaugeDesc, TypedAttribute modelDesc,
            List<TypedAttributeWithValue> setupParams, Map<String, IRainbowOperation> mappings)
                    throws RainbowException {
        super (NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);
        addPattern (IG, Pattern.compile (INSTRUCTION_PATTERN, Pattern.DOTALL));
        addPattern (NEWIG, Pattern.compile (NEWIG_PATTERN, Pattern.DOTALL));
//        addPattern (FINISHEDIG, Pattern.compile (FINISHIG_PATTERN, Pattern.DOTALL));
    }

    @Override
    protected void doMatch (String matchName, Matcher m) {
        if (IG.equals (matchName)) {
            String node = m.group (1).split ("\\.")[0];
            String status = m.group (3).trim();
            System.out.println("Reporting " + m.group(2) + ": " + status);

            if ("START".equals (status)) {
                IRainbowOperation operation = m_commands.get ("current-instruction");
                Map<String,String> pap = new HashMap<> ();
                pap.put (operation.getParameters ()[0], node);
                pap.put (operation.getParameters ()[1], "START");
                issueCommand (operation, pap);
            }
            else if ("SUCCESS".equals (status)) {
                IRainbowOperation operation = m_commands.get ("current-instruction");
                Map<String, String> pap = new HashMap<> ();
                pap.put (operation.getParameters ()[0], node);
                pap.put (operation.getParameters ()[1], "SUCCESS");
                issueCommand (operation, pap);
            }
            else if (status.startsWith ("FAILED")) {
                IRainbowOperation operation = m_commands.get ("current-failed");
                Map<String,String> pap = new HashMap<> ();
                pap.put (operation.getParameters ()[0], node);
                issueCommand (operation, pap);
//                IRainbowOperation failedOp = m_commands.get ("finished-ig");
//                Map<String, String> finishP = new HashMap<> ();
//                finishP.put (failedOp.getParameters ()[0], "false");
//                ArrayList<Map<String, String>> ps = new ArrayList<Map<String, String>> (2);
//                ps.add (pap);
//                ps.add (finishP);
//                issueCommands (Arrays.asList (new IRainbowOperation[] { operation, failedOp }), ps);

            }
        }
        else if (NEWIG.equals (matchName)) {
        	System.out.println("Reporting new IG " + m.group(1).trim());
            String ig = m.group(1).trim ();
            IRainbowOperation op = m_commands.get ("new-ig");
            Map<String, String> pMap = new HashMap<> ();
            pMap.put (op.getParameters ()[0],ig);
            issueCommand(op, pMap);
        }
        else if (FINISHEDIG.equals (matchName)) {
        	System.out.println("Reporting finisehd instruction graph");
            IRainbowOperation op = m_commands.get ("finished-ig");
            Map<String, String> pMap = new HashMap<> ();
            pMap.put (op.getParameters ()[0], "true");
            issueCommand (op, pMap);
        }
    }
}
