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
package org.sa.rainbow.core.gauges;


import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.translator.probes.IProbeIdentifier;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class implements the common methods for a Gauge that processes Probe
 * reports using one or more regular patterns.  Report strings are queued for
 * thread processing.  Probe beacon expiration isn't used by this type of Gauge.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public abstract class RegularPatternGauge extends AbstractGaugeWithProbes {
    public static String MOST_RECENT = "recentProbeValuesFirst";
    public static String MAX_UPDATES_PER_CYCLE = "maxUpdatesPerCycle";

    // This field needs to be synchronized because it is accessed by multiple threads
    protected Queue<String> m_lines = null;

    // The map of patterns that this gauge understands
    private Map<String,Pattern> m_patternMap = null;

    // Process the most recent probes
    private boolean m_mostRecentFirst = false;
    // The maximum number of probe values to process in one cycle.
    // If mostRecentFirst is false, then we just process the first N and keep the rest
    // If mostRecentFirst is true, then we process the last N and discsard the rest
    private int     m_updatesPerCycle = MAX_UPDATES_PER_SLEEP;

    /**
     * Main Constructor the Gauge that is hardwired to the Probe.
     * @param threadName  the name of the Gauge thread
     * @param id  the unique ID of the Gauge
     * @param beaconPeriod  the liveness beacon period of the Gauge
     * @param gaugeDesc  the type-name description of the Gauge
     * @param modelDesc  the type-name description of the Model the Gauge updates
     * @param setupParams  the list of setup parameters with their values
     * @param mappings  the list of Gauge Value to Model Property mappings
     */
    public RegularPatternGauge (String threadName, String id, long beaconPeriod, TypedAttribute gaugeDesc,
            TypedAttribute modelDesc, List<TypedAttributeWithValue> setupParams,
            Map<String, IRainbowOperation> mappings)
                    throws RainbowException {
        super (threadName, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);

        m_lines = new LinkedList<> ();
        m_patternMap = new HashMap<> ();

        m_mostRecentFirst = getSetupValue (MOST_RECENT, Boolean.class, false);
        m_updatesPerCycle = getSetupValue (MAX_UPDATES_PER_CYCLE, Integer.class, MAX_UPDATES_PER_SLEEP);
    }


    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.gauges.AbstractGauge#dispose()
     */
    @Override
    public void dispose () {
        synchronized (m_lines) {
            m_lines.clear ();
        }
        m_patternMap.clear();

        // null-out data members
        m_lines = null;
        m_patternMap = null;

        super.dispose();
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.gauges.AbstractGauge#probeReport(java.lang.String)
     */

    /**
     * This method is called when a probe reports. Takes the data and the probe and
     * adds the data to a queue for processing by the gauge, but only if the data
     * matches one of the patterns for this gauge. Otherwise it is discarded.
     * @param probe
     * @param data
     */
    @Override
    public void reportFromProbe (IProbeIdentifier probe, String data) {
        if (m_lines == null) return;
        synchronized (m_lines) {
            // Only offer if we have a match
            for (Map.Entry<String,Pattern> e : m_patternMap.entrySet()) {
                Matcher m = e.getValue ().matcher (data);
                if (m.matches()) {
                    m_lines.offer (data);
                    break;
                }
            }
        }

        super.reportFromProbe (probe, data);
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.gauges.AbstractGauge#runAction()
     */
    @Override
    protected void runAction() {
        // Pull probe values off the queue and process them
        Matcher m = null;
        String name = null;
        int cnt = Math.min (MAX_UPDATES_PER_SLEEP, m_updatesPerCycle);
        // We need to do some processing of the queue, so make a copy
        // so that the manipulation doesn't affect probe reporting
        Queue<String> lines = new LinkedList<String> ();
        synchronized (m_lines) {
            lines.addAll (m_lines);
            if (m_mostRecentFirst) m_lines.clear ();
            else {
                // delete the first cnt elements from the list
                int c = cnt;
                while (m_lines.size () > 0 && c-- > 0)  m_lines.poll ();
            }
        }

        if (m_mostRecentFirst) {
            // Flush the oldest values so that we're not keeping increasingly old values around
            while (lines.size () > cnt) {
                lines.poll ();
            }
        }

        while (lines.size() > 0 && cnt-- > 0) {
            String line = lines.poll();
            // process the line for stats
            //log("Got line: " + line);
            for (Map.Entry<String,Pattern> e : m_patternMap.entrySet()) {
                name = e.getKey();
                m = e.getValue().matcher(line);
                if (m.matches()) {
                    break;
                }
            }
            if (m != null && m.matches()) {
                doMatch(name, m);
            }
        }

        super.runAction();
    }

    protected void addPattern (String matchName, Pattern p) {
        m_patternMap.put(matchName.intern(), p);
    }

    protected abstract void doMatch (String matchName, Matcher m);

}
