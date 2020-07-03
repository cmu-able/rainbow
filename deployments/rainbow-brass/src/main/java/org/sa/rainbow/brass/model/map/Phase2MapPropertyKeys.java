package org.sa.rainbow.brass.model.map;

public interface Phase2MapPropertyKeys extends Phase1MapPropertyKeys {

	public static final String ILLUMINANCE = "illuminance";
	public static final String OBSTRUCTIONS = "obstructions";
	public static final String HITRATES = "hitrates";
	public static final String TIMES = "times";

	enum ObsLevel {LOW, MEDIUM, HIGH, NONE};
	
}
