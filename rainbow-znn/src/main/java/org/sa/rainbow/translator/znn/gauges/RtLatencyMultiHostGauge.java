/**
 * Created April 9, 2007.
 */
package org.sa.rainbow.translator.znn.gauges;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.gauges.RegularPatternGauge;
import org.sa.rainbow.core.models.commands.IRainbowModelCommandRepresentation;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.translator.znn.probes.PingRTTProbe;

/**
 * Gauge for computing roundtrip latency of N KB data using Ping RTT data
 * (PingRTTProbe) between Gauge's host and a list of target hosts.
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class RtLatencyMultiHostGauge extends RegularPatternGauge {

    public static final String NAME = "G - Latency";
    /** Sample window to compute an average latency */
    public static final int AVG_SAMPLE_WINDOW = 5;
    /** Standard Ping request size */
    public static final int PING_SIZE = PingRTTProbe.PING_REQ_SIZE;
    /** The estimated size of data we'll use to compute roundtrip latency */
    public static final int LATENCY_DATA_SIZE = 1024;  // one KB

    /** List of values reported by this Gauge */
    private static final String[] valueNames = {
        "latency(*)"
    };
    private static final String DEFAULT = "DEFAULT";

    private Map<String,Queue<Double>> m_historyMap = null;
    private Map<String,Double> m_cumulationMap = null;

    /**
     * Main constructor.
     * 
     * @throws RainbowException
     */
    public RtLatencyMultiHostGauge (String threadName, String id, long beaconPeriod, TypedAttribute gaugeDesc,
            TypedAttribute modelDesc, List<TypedAttributeWithValue> setupParams,
            List<IRainbowModelCommandRepresentation> mappings) throws RainbowException {

        super(NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);

        m_historyMap = new HashMap<String,Queue<Double>>();
        m_cumulationMap = new HashMap<String,Double>();

        addPattern(DEFAULT, Pattern.compile("\\[(.+)\\]\\s+(.+?):([0-9.]+)[/]([0-9.]+)[/]([0-9.]+)"));
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.gauges.AbstractGauge#initProperty(java.lang.String, java.lang.Object)
     */
    @Override
    protected void initProperty (String name, Object value) {
        // no prop to init, do nothing
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.gauges.RegularPatternGauge#doMatch(java.lang.String, java.util.regex.Matcher)
     */
    @Override
    protected void doMatch (String matchName, Matcher m) {
        if (matchName == DEFAULT) {
            // acquire the next set of ping RTT data, we care for the average
//			String tstamp = m.group(1);
            String host = m.group(2);
            if (host.equals("")) return;
//			double msMin = Double.parseDouble(m.group(3));
            double msAvg = Double.parseDouble(m.group(4));
//			double msMax = Double.parseDouble(m.group(5));
            double bwBPS = PING_SIZE /*B*/ * 1000 /*ms/s*/ / msAvg /*ms*/;
            double latency = LATENCY_DATA_SIZE / bwBPS;

            // setup data struct for host if new
            if (! m_historyMap.containsKey(host)) {
                m_historyMap.put(host, new LinkedList<Double>());
                m_cumulationMap.put(host, 0.0);
            }
            Queue<Double> history = m_historyMap.get(host);
            double cumulation = m_cumulationMap.get(host);
            // add value to cumulation and enqueue
            cumulation += latency;
            history.offer(latency);
            if (history.size() > AVG_SAMPLE_WINDOW) {
                // if queue size reached window size, then
                //   dequeue and delete oldest value and report average
                cumulation -= history.poll();
            }
            m_cumulationMap.put(host, cumulation);  // store updated cumulation
            latency = cumulation / history.size();
            m_reportingPort.trace (getComponentType (),
                    id () + ": " + cumulation + ", hist" + Arrays.toString (history.toArray ()));

            // update connection in model with latency in seconds
            for (String valueName : valueNames) {
                // massage value name for mapping purposes
                valueName = valueName.replace("*", host);
                if (m_commands.containsKey (valueName)) {
                    // ZNewsSys.conn0.latency
                    IRainbowModelCommandRepresentation cmd = m_commands.get (valueName);
                    Map<String, String> parameterMap = new HashMap<> ();
                    parameterMap.put (cmd.getParameters ()[0], Double.toString (latency));
                    issueCommand (cmd, parameterMap);
                }
            }
        }
    }

}
