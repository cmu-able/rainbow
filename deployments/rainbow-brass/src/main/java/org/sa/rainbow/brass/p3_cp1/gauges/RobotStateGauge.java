package org.sa.rainbow.brass.p3_cp1.gauges;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.gauges.RegularPatternGauge;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

public class RobotStateGauge extends RegularPatternGauge {
	private static final String NAME = "Robot State Gauge";
	protected static final String CHARGE = "BatteryCharge";
	protected static final String SPEED = "Speed";
	protected static final String CONFIG = "Config";

	protected static final String CHARGE_PATTERN = "topic: /mobile_base/commands/charge_level.*\\n.*data: (.*)";
	protected static final String CONFIG_PATTERN = "cp1 configuration: (.*)";
	private double last_charge = 0;
	private String last_config = "";
	
	public RobotStateGauge(String id, long beaconPeriod, TypedAttribute gaugeDesc, TypedAttribute modelDesc,
			List<TypedAttributeWithValue> setupParams, Map<String, IRainbowOperation> mappings)
			throws RainbowException {
		super(NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);
		addPattern(CHARGE, Pattern.compile(CHARGE_PATTERN, Pattern.DOTALL));
		addPattern(CONFIG, Pattern.compile(CONFIG_PATTERN));
	}

	@Override
	protected void doMatch(String matchName, Matcher m) {
		if (CHARGE.equals (matchName)) {
            double charge = Double.parseDouble(m.group(1).trim());

            if (chargeDifferent (charge)) {
                IRainbowOperation op = m_commands.get ("charge");
                Map<String, String> pMap = new HashMap<> ();
                pMap.put (op.getParameters ()[0], Double.toString (charge));
                issueCommand (op, pMap);
            }
		}
		else if (CONFIG.equals(matchName)) {
			String config = m.group(1).trim();
			
			if (!last_config.equals(config)) {
				last_config = config;
				IRainbowOperation op = m_commands.get("config");
				Map<String,String> pMap = new HashMap<>();
				pMap.put(op.getParameters()[0], config);
				issueCommand(op, pMap);
			}
		}
	}

	private boolean chargeDifferent (double charge) {
        if (Math.round (last_charge) != Math.round (charge)) {
            last_charge = charge;
            return true;
        }
        return false;
    }
	
	protected boolean shouldProcess() {
		return !isRainbowAdapting();
	};
}
