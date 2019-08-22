package org.sa.rainbow.core.ports.guava;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.event.IRainbowMessage;

import edu.cmu.cs.able.typelib.prim.StringValue;
import edu.cmu.cs.able.typelib.type.DataValue;

public class GuavaRainbowMessage implements IRainbowMessage {
	private static final Logger LOGGER = Logger.getLogger(GuavaRainbowMessage.class);
	private static final String PROP_PREFIX = "__PROP_";
	private static final int PROP_PREFIX_LENGTH = PROP_PREFIX.length();
	private Map<String, Object> m_properties;

	public GuavaRainbowMessage() {
		m_properties = new HashMap<>();
	}

	public GuavaRainbowMessage(Map<String, Object> m) {
		m_properties = new HashMap<>();
		m_properties.putAll(m);
	}

	public GuavaRainbowMessage(GuavaRainbowMessage m) {
		m_properties = new HashMap<>();
		m_properties.putAll(m.m_properties);
	}

	@Override
	public List<String> getPropertyNames() {
		return Collections.unmodifiableList(new ArrayList<String>(m_properties.keySet()));
	}

	@Override
	public Object getProperty(String id) {
		return m_properties.get(id);
	}

	@Override
	public void setProperty(String id, Object prop) {
		m_properties.put(id, prop);
	}

	public void removeProperty(String id) {
		m_properties.remove(id);
	}

	public boolean hasProperty(String target) {
		return m_properties.containsKey(target);
	}

	public void fillProperties(Properties p) {
		p.forEach((k, v) -> {
			m_properties.put(PROP_PREFIX + ((String) k), v);
		});
	}

	public Properties pulloutProperties() {
		Properties p = new Properties();
		for (Iterator<Entry<String,Object>> iterator = m_properties.entrySet().iterator(); iterator.hasNext();) {
			Entry<String,Object> type = (Entry<String,Object>) iterator.next();
			if (type.getKey().startsWith(PROP_PREFIX)) {
				if (type.getValue() instanceof String) {
					p.setProperty(type.getKey().substring (PROP_PREFIX_LENGTH), (String )type.getValue());
					iterator.remove();
				}
				else {
					LOGGER.warn("The property key " + type.getKey() + " is not a String as expected"
							+ type.getValue().getClass().toString());
				}
			}
			
		}
		return p;
	}

}
