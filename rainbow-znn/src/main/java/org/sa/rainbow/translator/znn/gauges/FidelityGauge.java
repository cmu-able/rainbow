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
import org.sa.rainbow.core.models.commands.IRainbowModelCommandRepresentation;
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
            List<TypedAttributeWithValue> setupParams, List<IRainbowModelCommandRepresentation> mappings)
                    throws RainbowException {

        super(NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);

        addPattern(DEFAULT, Pattern.compile("\\[(.+)\\] (\\d+)"));
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
            // acquire the recent CPU load data
//			String tstamp = m.group(1);
            int fidelity = Integer.parseInt(m.group(2));

            // update server comp in model with requests per sec
            m_reportingPort.trace (getComponentType (), "Updating server prop using fidelity = " + fidelity);
            // ZNewsSys.s0.fidelity
            if (m_commands.containsKey (valueNames[0])) {
                // ZNewsSys.conn0.latency
                IRainbowModelCommandRepresentation cmd = m_commands.get (valueNames[0]);
                Map<String, String> parameterMap = new HashMap<> ();
                parameterMap.put (cmd.getParameters ()[0], Integer.toString (fidelity));
                issueCommand (cmd, parameterMap);
            }
        }
    }

}
