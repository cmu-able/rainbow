package org.sa.rainbow.brass.gauges.p2_cp3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.gauges.RegularPatternGauge;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

public class ClockGauge extends RegularPatternGauge {


	private static final String   NAME              = "Clock Gauge";
    protected static final String CLOCK             = "Clock";
    protected static final String CLOCK_PATTERN       = "topic: /clock.*\\n.*secs: ([0-9]*).*nsecs: ([0-9]*)(.*)";

    private double                m_currentTime;

    
    public ClockGauge(String id, long beaconPeriod, TypedAttribute gaugeDesc,
			TypedAttribute modelDesc, List<TypedAttributeWithValue> setupParams,
			Map<String, IRainbowOperation> mappings) throws RainbowException {
		super(NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);
		addPattern(CLOCK, Pattern.compile(CLOCK_PATTERN, Pattern.DOTALL));
    }
    
	@Override
	protected void doMatch(String matchName, Matcher m) {
        int restGroup = 0;

        if (CLOCK.equals(matchName)) {
        	long secs = Long.parseLong (m.group (1).trim ());
            long nsecs = 0;
            if (!"".equals (m.group (2).trim ())) {
                nsecs = Long.parseLong (m.group (2).trim ());
            }
            double realSecs = secs + TimeUnit.MILLISECONDS.convert (nsecs, TimeUnit.NANOSECONDS) / 1000.0;
            m_currentTime = realSecs;
            IRainbowOperation op = m_commands.get ("clock");
            Map<String, String> pMap = new HashMap<> ();
            pMap.put (op.getParameters ()[0], Double.toString (realSecs));
            issueCommand (op, pMap);
            restGroup = 3;
        }
        
        if (m.groupCount () == restGroup) {
            String rest = m.group (restGroup);
            if (!rest.isEmpty () && rest.indexOf ("topic:") != -1) {
                log ("Gauge had more: " + rest.substring (rest.indexOf ("topic:")));
            }
        }
	}

}
