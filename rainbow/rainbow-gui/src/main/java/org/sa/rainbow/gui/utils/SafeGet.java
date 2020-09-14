package org.sa.rainbow.gui.utils;

public class SafeGet {
	public static String asString(Object s) {
		if (s instanceof String || s == null) return (String )s;
		return s.toString();
	}
	
	public static Double asDouble(Object d) {
		if (d instanceof Number) return ((Number )d).doubleValue();
		return null;
	}
}
