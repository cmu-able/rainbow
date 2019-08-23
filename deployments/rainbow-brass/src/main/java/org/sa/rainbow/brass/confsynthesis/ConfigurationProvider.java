package org.sa.rainbow.brass.confsynthesis;

import java.util.HashMap;
import java.util.List;

import org.sa.rainbow.core.error.RainbowException;

public interface ConfigurationProvider {
	void populate() throws RainbowException;
	HashMap<String,Configuration> getConfigurations();	
	HashMap<String,Configuration> getLegalTargetConfigurations();	
	HashMap<String,List<String>> getLegalReconfigurationsFrom(String fromConfiguration);
	Double getReconfigurationTime(String sourceConfiguration, String targetConfiguration);
	List<String> getReconfigurationPath(String sourceConfiguration, String targetConfiguration);
	Boolean containsConfiguration(String id);
	String translateId (String id);
}
