package org.sa.rainbow.translator.znn.gauges;

/*
 * The MIT License
 *
 * Copyright 2014 CMU ABLE Group.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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

/**
 * Sets whether authentication has been enabled in ZNN. This information is reported by an authentication probe. The
 * gauge issues the appropriate model command.
 * 
 * <p>
 * The command that is issued on the model is named "clientMgmt" and is mapped to the setAuthenticationResponse command.
 * 
 * <p>
 * The gauge type is as follows:
 * 
 * <pre>
 *  AuthenticationEnablementGaugeT:
 *     commands:
 *       clientMgmt: AuthenticationHandlerT.setAuthenticationResponse (int)
 *     setupParams:
 *       targetIP:
 *         type: String
 *         default: "localhost"
 *       beaconPeriod:
 *         type: long
 *         default: 5000
 *       javaClass:
 *         type: String
 *         default: "org.sa.rainbow.translator.znn.gauges.AuthenticationEnablementGauge"
 *     configParams:
 *       targetProbeType:
 *         type: String
 *         default: ~
 * </pre>
 * 
 * @author Bradley Schmerl: schmerl
 *
 */
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

}
