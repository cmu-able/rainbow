package org.sa.rainbow.brass.model.map;

import java.util.HashMap;
import java.util.Map;

public class EnvMapArc {
	public String m_source, m_target;


	private final Map<String, Object> m_properties = new HashMap<> ();

	public EnvMapArc(String source, String target, double distance, boolean enabled) {
		super();
		this.m_source = source;
		this.m_target = target;
		m_properties.put(Phase1MapPropertyKeys.DISTANCE, distance);
		m_properties.put(Phase1MapPropertyKeys.ENABLEMENT, enabled);

	}

	public String getSource() {
		return m_source;
	}

	public void setSource(String m_source) {
		this.m_source = m_source;
	}

	public String getTarget() {
		return m_target;
	}

	public void setTarget(String m_target) {
		this.m_target = m_target;
	}

	public double getDistance() {
		return (Double )m_properties.get(Phase1MapPropertyKeys.DISTANCE);
	}

	public void setDistance(double distance) {
		m_properties.put(Phase1MapPropertyKeys.DISTANCE, distance);
	}

	public boolean isEnabled() {
		return (Boolean )m_properties.get(Phase1MapPropertyKeys.ENABLEMENT);
	}

	public void setEnabled(boolean enabled) {
		m_properties.put(Phase1MapPropertyKeys.ENABLEMENT, enabled);

	}

	public boolean includesNode(String node) {
		return (m_source.equals(node) || m_target.equals(node));
	}

	public boolean includesNodes(String nodea, String nodeb) {
		return (includesNode(nodea) && includesNode(nodeb));
	}

	public void setProperty(String key, Object value) {
		m_properties.put(key, value);
	}

	public Object getProperty(String key) {
		return m_properties.get(key);
	}

}