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
 * Created March 18, 2007.
 */
package org.sa.rainbow.translator.znn.gauges;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.gauges.RegularPatternGauge;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.translator.znn.probes.PingRTTProbe;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gauge for computing roundtrip latency of N KB data using Ping RTT data (PingRTTProbe).
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class RtLatencyGauge extends RegularPatternGauge {

    public static final String    NAME              = "G - Latency";
    /** Sample window to compute an average latency */
    public static final int AVG_SAMPLE_WINDOW = 5;
    /** Standard Ping request size */
    public static final int PING_SIZE = PingRTTProbe.PING_REQ_SIZE;
    /** The estimated size of data we'll use to compute roundtrip latency */
    public static final int LATENCY_DATA_SIZE = 1024;  // one KB

    /** List of values reported by this Gauge */
    private static final String[] valueNames = {
            "latency"
    };
    private static final String DEFAULT = "DEFAULT";

    private Queue<Double> m_history = null;
    private double m_cumulation = 0;

    /**
     * Main constructor.
     */
    public RtLatencyGauge (String id, long beaconPeriod, TypedAttribute gaugeDesc, TypedAttribute modelDesc,
            List<TypedAttributeWithValue> setupParams, Map<String, IRainbowOperation> mappings)
                    throws RainbowException {
        super (NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);

        m_history = new LinkedList<> ();

        addPattern (DEFAULT, Pattern.compile ("\\[(.+)\\]\\s+(.+?):([0-9.]+)[/]([0-9.]+)[/]([0-9.]+)"));

        Double initalLatency = getSetupValue (valueNames[0], Double.class);
        if (initalLatency != null) {
            m_cumulation = initalLatency;
            m_history.offer (initalLatency);
        }
    }


    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.gauges.RegularPatternGauge#doMatch(java.lang.String, java.util.regex.Matcher)
     */
    @Override
    protected void doMatch (String matchName, Matcher m) {
        if (matchName == DEFAULT) {
            // acquire the next set of ping RTT data, we care for the average
//			String tstamp = m.group(1);
//			String host = m.group(2);
//			double msMin = Double.parseDouble(m.group(3));
            double msAvg = Double.parseDouble(m.group(4));
//			double msMax = Double.parseDouble(m.group(5));
            double bwBPS = PING_SIZE /*B*/ * 1000 /*ms/s*/ / msAvg /*ms*/;
            double latency = LATENCY_DATA_SIZE / bwBPS;
            // add value to cumulation and enqueue
            m_cumulation += latency;
            m_history.offer(latency);
            if (m_history.size() > AVG_SAMPLE_WINDOW) {
                // if queue size reached window size, then
                //   dequeue and delete oldest value and report average
                m_cumulation -= m_history.poll();
            }
            latency = m_cumulation / m_history.size();
            // update connection in model with latency in seconds
            if (m_commands.containsKey (valueNames[0])) {
                // ZNewsSys.conn0.latency
                IRainbowOperation cmd = getCommand (valueNames[0]);
                Map<String, String> parameterMap = new HashMap<> ();
                parameterMap.put (cmd.getParameters ()[0], Double.toString (latency));
                issueCommand (cmd, parameterMap);
            }
        }
    }

}
