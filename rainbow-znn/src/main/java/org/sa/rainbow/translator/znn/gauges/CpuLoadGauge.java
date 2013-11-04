/**
 * Created November 1, 2006.
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
            List<TypedAttributeWithValue> setupParams, List<IRainbowOperation> mappings)
                    throws RainbowException {

        super(NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);

        m_history = new LinkedList<Double>();

        addPattern(DEFAULT, Pattern.compile("\\[(.+)\\]\\s+([0-9.]+)\\s+([0-9.]+)\\s+([0-9.]+)\\s+([0-9.]+)(\\s+([0-9.]+))?"));
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
                IRainbowOperation cmd = m_commands.get (valueNames[0]);
                Map<String, String> pMap = new HashMap<String, String> ();
                pMap.put (cmd.getParameters ()[0], Double.toString (tLoad));
                issueCommand (cmd, pMap);
            }
        }
    }

}
