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

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.gauges.RegularPatternGauge;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gauge for consuming CPU load monitoring output.
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class CpuLoadGauge extends RegularPatternGauge {

    public static final String NAME = "G - CPU Load";
    /** Sample window to compute an average load */
    public static final int AVG_SAMPLE_WINDOW = 10;

    /** List of values reported by this Gauge */
    private static final String[] valueNames = {
            "load"
    };
    private static final String DEFAULT = "DEFAULT";

    private Queue<Double> m_history = null;
    private double m_cumulation = 0;

    /**
     * Main constructor.
     * 
     * @throws RainbowException
     */
    public CpuLoadGauge (String id, long beaconPeriod, TypedAttribute gaugeDesc, TypedAttribute modelDesc,
            List<TypedAttributeWithValue> setupParams, Map<String, IRainbowOperation> mappings)
                    throws RainbowException {

        super(NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);

        m_history = new LinkedList<> ();

        addPattern(DEFAULT, Pattern.compile("\\[(.+)\\]\\s+([0-9.]+)\\s+([0-9.]+)\\s+([0-9.]+)\\s+([0-9.]+)(\\s+([0-9.]+))?"));

        Double initialLoad = getSetupValue ("initialLoad", Double.class);
        if (initialLoad != null) {
            m_cumulation = initialLoad;
            m_history.offer (initialLoad);
        }
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.gauges.RegularPatternGauge#doMatch(java.lang.String, java.util.regex.Matcher)
     */
    @Override
    protected void doMatch (String matchName, Matcher m) {
        if (matchName == DEFAULT) {
            // acquire the recent CPU load data
//			String tstamp = m.group(1);
            double userT = Double.parseDouble(m.group(2));
            double niceT = Double.parseDouble(m.group(3));
            double sysT = Double.parseDouble(m.group(4));
            double idleT = Double.parseDouble(m.group(5));
            double iowaitT = 0.0;
            if (m.group(7) != null) {  // optional iowait element is present
                iowaitT = Double.parseDouble(m.group(7));
            }
            double tLoad = userT + niceT + sysT + iowaitT;
            // add value to cumulation and enqueue
            m_cumulation += tLoad;
            m_history.offer(tLoad);
            if (m_history.size() > AVG_SAMPLE_WINDOW) {
                // if queue size reached window size, then
                //   dequeue and delete oldest value and report average
                m_cumulation -= m_history.poll();
            }
            tLoad = m_cumulation / m_history.size();
            if (idleT < 1.0) {
                // update server comp in model with requests per sec
                m_reportingPort.trace (getComponentType (), "Updating server prop using load = " + tLoad);
                // ZNewsSys.s0.load
                IRainbowOperation cmd = getCommand (valueNames[0]);
                Map<String, String> pMap = new HashMap<> ();
                pMap.put (cmd.getParameters ()[0], Double.toString (tLoad));
                issueCommand (cmd, pMap);
            }
        }
    }

    @Override
    protected void runAction () {
        super.runAction ();
    }
}
