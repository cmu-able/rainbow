package org.sa.rainbow.translator.znn.gauges;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.gauges.RegularPatternGauge;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

public class AuthenticationEnablementGauge extends RegularPatternGauge {

    public static final String  NAME = "G - Authentication Enablement";

    private static final String OFF  = "off";
    private static final String ON   = "on";

    public AuthenticationEnablementGauge (String id, long beaconPeriod, TypedAttribute gaugeDesc,
            TypedAttribute modelDesc, List<TypedAttributeWithValue> setupParams, Map<String, IRainbowOperation> mappings)
                    throws RainbowException {
        super (NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);
        addPattern (ON, Pattern.compile ("^on$"));
        addPattern (OFF, Pattern.compile ("^off$"));
    }

    @Override
    protected void doMatch (String matchName, Matcher m) {
        boolean authenticationOff = OFF.equals (matchName);
        if (authenticationOff) {
            // Reset everything to unknown
            List<IRainbowOperation> ops = new LinkedList<> ();
            List<Map<String, String>> params = new LinkedList<> ();

            for (Entry<String, IRainbowOperation> entry : m_commands.entrySet ()) {
                if (entry.getKey ().startsWith ("clientMgmt(")) {
                    IRainbowOperation op = entry.getValue ();
                    Map<String, String> pMap = new HashMap<> ();
                    pMap.put (op.getParameters ()[0], "0");
                    ops.add (op);
                    params.add (pMap);
                }
            }

            issueCommands (ops, params);
        }
    }

    @Override
    protected void initProperty (String name, Object value) {
        // TODO Auto-generated method stub

    }

}
