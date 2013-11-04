/**
 * Created March 18, 2007.
 */
package org.sa.rainbow.translator.znn.gauges;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.gauges.RegularPatternGauge;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.translator.znn.probes.PingRTTProbe;

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

        m_history = new LinkedList<Double> ();

        addPattern (DEFAULT, Pattern.compile ("\\[(.+)\\]\\s+(.+?):([0-9.]+)[/]([0-9.]+)[/]([0-9.]+)"));
    }


    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.gauges.AbstractGauge#initProperty(java.lang.String, java.lang.Object)
     */
    @Override
    protected void initProperty (String name, Object value) {
        if (!valueNames[0].equals(name) || !(value instanceof String)) return;

        // store model property value of "load" as initial value in cumulation
        double val = Double.parseDouble((String )value);
        m_cumulation = val;
        m_history.offer(val);
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
                IRainbowOperation cmd = m_commands.get (valueNames[0]);
                Map<String, String> parameterMap = new HashMap<> ();
                parameterMap.put (cmd.getParameters ()[0], Double.toString (latency));
                issueCommand (cmd, parameterMap);
            }
        }
    }

}
