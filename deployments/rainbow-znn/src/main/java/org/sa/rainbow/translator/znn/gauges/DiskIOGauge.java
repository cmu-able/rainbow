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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gauge for aggregating Disk IO probe monitoring output.  This gauge keeps a
 * rolling historical window of size AVG_SAMPLE_WINDOW to compute average.
 * <p>
 * When there are multiple readings (e.g., multiple disks) per cycle, we
 * combine those readings into a single reading.  Assuming that the readings
 * are simultaneous, then the number CPU ticks that elapsed would be equivalent,
 * allowing us to simply add the reading values across the disks (since the
 * factions essentially have the same denominators).
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class DiskIOGauge extends RegularPatternGauge {

    public static final String NAME = "G - Disk IO";
    /** Sample window to compute an average load */
    public static final int AVG_SAMPLE_WINDOW = 10;

    /** List of values reported by this Gauge */
    private static final String[] valueNames = {
            "transferRate",
            "readRate",
            "writeRate",
            "readSize",
            "writeSize"
    };
    private static final String DEFAULT = "DEFAULT";

    private Queue<Double[]> m_history = null;
    private Double[] m_cumulations = null;
    private SimpleDateFormat m_dateFormat = null;
    /** The lastTstamp and lastReading should be set together */
    private Date m_lastTstamp = null;
    private Double[] m_lastReading = null;

    /**
     * Main constructor.
     * 
     * @throws RainbowException
     */
    public DiskIOGauge (String id, long beaconPeriod, TypedAttribute gaugeDesc, TypedAttribute modelDesc,
            List<TypedAttributeWithValue> setupParams, Map<String, IRainbowOperation> mappings)
                    throws RainbowException {

        super(NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);

        m_history = new LinkedList<> ();
        m_cumulations = new Double[valueNames.length];
        for (int i=0; i < valueNames.length; ++i) {
            m_cumulations[i] = 0.0;
        }
        m_dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy");

        addPattern(DEFAULT, Pattern.compile("\\[(.+)\\]\\s+(\\w+)\\s+([0-9.]+)\\s+([0-9.]+)\\s+([0-9.]+)\\s+([0-9.]+)\\s+([0-9.]+)"));

        for (int i = 0; i < valueNames.length; ++i) {
            Double val = getSetupValue (valueNames[i], Double.class);
            if (val != null) {
                if (m_cumulations == null) {
                    m_cumulations = new Double[valueNames.length];
                }
                m_cumulations[i] = val;
                Double[] values = m_history.peek ();
                if (values == null) {
                    values = new Double[valueNames.length];
                    m_history.offer (values);
                }
                values[i] = val;
            }
        }
    }


    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.gauges.RegularPatternGauge#doMatch(java.lang.String, java.util.regex.Matcher)
     */
    @Override
    protected void doMatch (String matchName, Matcher m) {
        if (matchName == DEFAULT) {
            // acquire the recent disk IO data and add values to cumulation
            Date tstamp = null;
            try {
                tstamp = m_dateFormat.parse(m.group(1));
            } catch (ParseException e) {
                m_reportingPort.error (getComponentType (), id () + ": Date parsing error!", e);
            }
//			String devName = m.group(2);
            Double[] values = new Double[valueNames.length];
            for (int i=0; i < valueNames.length; ++i) {
                values[i] = Double.parseDouble(m.group(i+3));
            }

            if (m_lastTstamp != null && tstamp != null) {  // determine timestamp difference
                long diff = tstamp.getTime() - m_lastTstamp.getTime();
                if (diff <= 1000) {  // within 1 second, treat as one reading
                    // merge results with previous reading, by addition of values
                    for (int i=0; i < values.length; ++i) {
                        m_lastReading[i] += values[i];
                    }
                } else {
                    // add lastReading to cumulation and enqueue lastReading
                    for (int i=0; i < valueNames.length; ++i) {
                        m_cumulations[i] += m_lastReading[i];
                    }
                    m_history.offer(m_lastReading);
                    // save new array of values as lastReading
                    m_lastReading = values;
                    if (m_history.size() > AVG_SAMPLE_WINDOW) {
                        // if queue size reached window size, then dequeue and deduct oldest values
                        // NOTE:  "values" reused below for oldest values
                        values = m_history.poll();
                        for (int i=0; i < valueNames.length; ++i) {
                            m_cumulations[i] -= values[i];
                        }
                    }
                }
                m_lastTstamp = tstamp;  // store new timestamp
            } else {
                // store first-time new values to lastReading
                m_lastReading = values;
                if (tstamp != null) {
                    m_lastTstamp = tstamp;  // store first timestamp
                }
            }

            // compute average
            int size = m_history.size();
            if (size > 0) {
                // NOTE:  "values" declared anew to compute average values
                values = new Double[valueNames.length];
                for (int i=0; i < valueNames.length; ++i) {
                    values[i] = m_cumulations[i] / size;
                }

                // update server comp in model with requests per sec
                m_reportingPort.trace (getComponentType (), "Updating server prop using values = " + values);
                for (int i=0; i < valueNames.length; ++i) {
                    if (m_commands.containsKey (valueNames[i])) {
                        // ZNewsSys.s0.<vName>, if "vName" exists in mapping
                        IRainbowOperation cmd = getCommand (valueNames[i]);
                        Map<String, String> pMap = new HashMap<> ();
                        pMap.put (cmd.getParameters ()[0], Double.toString (values[i]));
                        issueCommand (cmd, pMap);
                    }
                }
            }
        }
    }

}
