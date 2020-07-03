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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThrottleGauge extends RegularPatternGauge {

    public static final String    NAME         = "G - Throttled Clients";
    public static final String    NONE         = "NOTHROTTLE";

    public String                 lastReport   = "";

    private static final String[] commandNames = { "setThrottled" };
    private static final String   DEFAULT      = "DEFAULT";

    public ThrottleGauge (String id, long beaconPeriod, TypedAttribute gaugeDesc, TypedAttribute modelDesc,
            List<TypedAttributeWithValue> setupParams, Map<String, IRainbowOperation> mappings)
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
            StringBuilder ips = new StringBuilder ();
//          String[] ips = new String[c];
            for (int i = 0; i < c; i++) {
//              ips[i] = m.group(i + 1);
                ips.append (m.group (i + 1));
                ips.append (",");
            }
            ips.deleteCharAt (ips.length () - 1);

            if (doReport (ips.toString ())) {
                recordLastReport (ips.toString ());
                IRainbowOperation cmd = m_commands.values ().iterator ().next ();
                Map<String, String> pm = new HashMap<> ();
                pm.put (cmd.getParameters ()[0], ips.toString ());
                issueCommand (cmd, pm);

            }
        }
        else if (matchName == NONE) {
            // The probe reported "none" meaning that there is nothing blackholed
            if (doReport ("")) {
                recordLastReport ("");
                IRainbowOperation cmd = m_commands.values ().iterator ().next ();
                Map<String, String> pm = new HashMap<> ();
                pm.put (cmd.getParameters ()[0], "");
                issueCommand (cmd, pm);
            }
        }
    }

    private void recordLastReport (String report) {
            lastReport = report;
    }

    private boolean doReport (String report) {
        boolean doReport;
        synchronized (lastReport) {
            doReport = !lastReport.equals (report);
        }
//      return doReport;
        return true;
    }

}
