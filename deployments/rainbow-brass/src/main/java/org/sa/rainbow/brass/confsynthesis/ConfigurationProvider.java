package org.sa.rainbow.brass.confsynthesis;

import java.util.HashMap;
import java.util.List;

public interface ConfigurationProvider {
	void populate();
	HashMap<String,Configuration> getConfigurations();	
	Double getReconfigurationTime(String sourceConfiguration, String targetConfiguration);
	List<String> getReconfigurationPath(String sourceConfiguration, String targetConfiguration);
	Boolean containsConfiguration(String id);
	String translateId (String id);
}
