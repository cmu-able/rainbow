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
package org.sa.rainbow.translator.znn.gauges;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.gauges.RegularPatternGauge;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CaptchaGauge extends RegularPatternGauge {
    public static final String    NAME       = "G - Captcha Enablement";

    private static final String   OFF        = "off";
    private static final String   ON         = "on";

    /** List of values reported by this Gauge */
    private static final String[] valueNames = { "enablement" };

    public CaptchaGauge (String id, long beaconPeriod, TypedAttribute gaugeDesc, TypedAttribute modelDesc,
            List<TypedAttributeWithValue> setupParams, Map<String, IRainbowOperation> mappings)
                    throws RainbowException {
        super (NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);
        addPattern (ON, Pattern.compile ("^on$"));
        addPattern (OFF, Pattern.compile ("^off$"));
    }

    @Override
    protected void doMatch (String matchName, Matcher m) {
        boolean captchaOn = ON.equals (matchName);
        IRainbowOperation cmd = m_commands.get (valueNames[0]);
        Map<String, String> pMap = new HashMap<> ();
        pMap.put (cmd.getParameters ()[0], Boolean.toString (captchaOn));
        if (captchaOn) {
            issueCommand (cmd, pMap);
        }
        else {
            List<IRainbowOperation> ops = new LinkedList<> ();
            List<Map<String, String>> params = new LinkedList<> ();

            ops.add (cmd);
            params.add (pMap);

            for (Entry<String, IRainbowOperation> entry : m_commands.entrySet ()) {
                if (entry.getKey ().startsWith ("clientMgmt(")) {
                    IRainbowOperation op = entry.getValue ();
                    pMap = new HashMap<> ();
                    pMap.put (op.getParameters ()[0], "0");
                    ops.add (op);
                    params.add (pMap);
                }
            }

            issueCommands (ops, params);
        }
    }

}
