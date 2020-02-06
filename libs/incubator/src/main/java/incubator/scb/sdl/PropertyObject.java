package incubator.scb.sdl;

import incubator.pval.Ensure;

import java.util.HashMap;
import java.util.Map;

/**
 * Object that can contain properties.
 */
public class PropertyObject {
	/**
	 * The properties.
	 */
	private Map<String, Object> m_properties;

	/**
	 * Creates a new property object.
	 */
	public PropertyObject() {
		m_properties = new HashMap<>();
	}
	
	/**
	 * Obtains the value of a property.
	 * @param clazz the expected type of the property
	 * @param name the property name
	 * @return the property value, <code>null</code> if it doesn't exist or
	 * doesn't have the specified type
	 * @param <T> the expected type of object
	 */
	public <T> T property(Class<T> clazz, String name) {
		Ensure.not_null(name, "name == null");
		Ensure.not_null(clazz, "clazz == null");
		Object obj = m_properties.get(name);
		if (obj == null || !clazz.isInstance(obj)) {
			return null;
		} else {
			return clazz.cast(obj);
		}
	}
	
	/**
	 * Sets the value of a property.
	 * @param name the property name
	 * @param value the property value; if <code>null</code> the property is
	 * reset
	 */
	public void property(String name, Object value) {
		Ensure.not_null(name, "name == null");
		if (value == null) {
			m_properties.remove(name);
		} else {
			m_properties.put(name, value);
		}
	}
}
