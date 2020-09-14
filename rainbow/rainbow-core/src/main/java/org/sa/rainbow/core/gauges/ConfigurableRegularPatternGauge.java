package org.sa.rainbow.core.gauges;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

public abstract class ConfigurableRegularPatternGauge extends RegularPatternGauge {

	public ConfigurableRegularPatternGauge(String threadName, String id, long beaconPeriod, TypedAttribute gaugeDesc,
			TypedAttribute modelDesc, List<TypedAttributeWithValue> setupParams,
			Map<String, IRainbowOperation> mappings) throws RainbowException {
		super(threadName, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);
		loadPatterns();
	}
	
	protected abstract void loadPatterns();
	
	protected void fillParametersFromGroups(Map<String,String> parameters, Matcher m) {
		Pattern p = Pattern.compile("\\$<(.*)>$");
		for (Map.Entry<String,String> e : parameters.entrySet()) {
			Matcher matcher = p.matcher(e.getKey());
			if 	(matcher.matches()) {
				try {
					String value = m.group(matcher.group(1));
					e.setValue(value);
				}
				catch (IllegalArgumentException ex) {}
			}
		}
	}	
	@Override
	protected void doMatch(String matchName, Matcher m) {
		IRainbowOperation command = getCommand(matchName);
		if (command != null) {
			Map<String, String> parameters = getParameters(command);
			fillParametersFromGroups(parameters, m);
			issueCommand(command, parameters);
		}
	}
	
	
	

}
