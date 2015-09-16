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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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


    @Nullable
    protected Queue<String> m_lines = null;
    @Nullable
    private Map<String,Pattern> m_patternMap = null;

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
    }


    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.gauges.AbstractGauge#dispose()
     */
    @Override
    public void dispose () {
        m_lines.clear();
        m_patternMap.clear();

        // null-out data members
        m_lines = null;
        m_patternMap = null;

        super.dispose();
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.gauges.AbstractGauge#probeReport(java.lang.String)
     */
    @Override
    public void reportFromProbe (IProbeIdentifier probe, String data) {
        if (m_lines == null) return;

        m_lines.offer(data);

        super.reportFromProbe (probe, data);
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.gauges.AbstractGauge#runAction()
     */
    @Override
    protected void runAction() {
        Matcher m = null;
        String name = null;
        int cnt = MAX_UPDATES_PER_SLEEP;
        while (m_lines.size() > 0 && cnt-- > 0) {
            String line = m_lines.poll();
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

    protected void addPattern (@NotNull String matchName, Pattern p) {
        m_patternMap.put(matchName.intern(), p);
    }

    protected abstract void doMatch (String matchName, Matcher m);

}
