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
/**
 * Created November 1, 2006.
 */
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

/**
 * Gauge for consuming Apache Top's monitoring output.
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class FidelityGauge extends RegularPatternGauge {

    public static final String NAME = "G - Server Fidelity";

    /** List of values reported by this Gauge */
    private static final String[] valueNames = {
            "fidelity"
    };
    private static final String DEFAULT = "DEFAULT";

    /**
     * Main constructor.
     * 
     * @throws RainbowException
     */
    public FidelityGauge (String id, long beaconPeriod, TypedAttribute gaugeDesc, TypedAttribute modelDesc,
            List<TypedAttributeWithValue> setupParams, Map<String, IRainbowOperation> mappings)
                    throws RainbowException {

        super(NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);

        addPattern (DEFAULT, Pattern.compile ("\\[(.+)\\] (\\w+)"));
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.gauges.AbstractGauge#initProperty(java.lang.String, java.lang.Object)
     */
    @Override
    protected void doMatch (String matchName, Matcher m) {
        if (matchName == DEFAULT) {
            // acquire the recent CPU load data
//			String tstamp = m.group(1);
            String fidelity = m.group(2);

            // update server comp in model with requests per sec
            m_reportingPort.trace (getComponentType (), "Updating server prop using fidelity = " + fidelity);
            // ZNewsSys.s0.fidelity
            if (m_commands.containsKey (valueNames[0])) {
                // ZNewsSys.conn0.latency
                IRainbowOperation cmd = getCommand (valueNames[0]);
                Map<String, String> parameterMap = new HashMap<> ();
                String acmeFidelity = "5";
                switch (fidelity) {
                case "low":
                    acmeFidelity = "1";
                    break;
                case "text":
                    acmeFidelity = "3";
                    break;

                }
                parameterMap.put (cmd.getParameters ()[0], acmeFidelity);
                issueCommand (cmd, parameterMap);
            }
        }
    }

}
