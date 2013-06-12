package edu.cmu.cs.able.typelib.comp;

import incubator.pval.Ensure;

import java.util.HashMap;
import java.util.Map;

import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Data value containing a map.
 */
public class MapDataValue extends DataValue {
	/**
	 * Map data.
	 */
	private Map<DataValue, DataValue> m_data;
	
	/**
	 * Creates a new map.
	 * @param type the type
	 */
	MapDataValue(MapDataType type) {
		super(type);
		Ensure.not_null(type);
		m_data = new HashMap<>();
	}
	
	/**
	 * Obtains all values in the map.
	 * @return all values
	 */
	public Map<DataValue, DataValue> all() {
		return new HashMap<>(m_data);
	}
	
	/**
	 * Obtains the number of elements in the map.
	 * @return the number of elements
	 */
	public int size() {
		return m_data.size();
	}
	
	/**
	 * Defines an element in the map. Removes any existing element with the
	 * same key.
	 * @param k the key
	 * @param v the value
	 */
	public void put(DataValue k, DataValue v) {
		Ensure.not_null(k);
		Ensure.not_null(v);
		Ensure.is_true(((MapDataType) type()).key_type().is_instance(k));
		Ensure.is_true(((MapDataType) type()).value_type().is_instance(v));
		m_data.put(k, v);
	}
	
	/**
	 * Checks if the map contains an element.
	 * @param k the key of the element to check
	 * @return does the map contain the element?
	 */
	public boolean contains(DataValue k) {
		Ensure.not_null(k);
		Ensure.is_true(((MapDataType) type()).key_type().is_instance(k));
		return m_data.containsKey(k);
	}
	
	/**
	 * Obtains the value associated with a key.
	 * @param k the key
	 * @return the value associated or <code>null</code> if none
	 */
	public DataValue get(DataValue k) {
		Ensure.not_null(k);
		Ensure.is_true(((MapDataType) type()).key_type().is_instance(k));
		return m_data.get(k);
	}
	
	/**
	 * Removes an entry with the given key, if any exists. 
	 * @param k the key
	 */
	public void remove(DataValue k) {
		Ensure.not_null(k);
		Ensure.is_true(((MapDataType) type()).key_type().is_instance(k));
		m_data.remove(k);
	}
	
	/**
	 * Removes all elements from the map.
	 */
	public void clear() {
		m_data.clear();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((m_data == null) ? 0 : m_data.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MapDataValue other = (MapDataValue) obj;
		if (m_data == null) {
			if (other.m_data != null)
				return false;
		} else if (!m_data.equals(other.m_data))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('<');
		boolean first = true;
		for (DataValue k : m_data.keySet()) {
			if (!first) {
				sb.append(',');
			} else {
				first = false;
			}
			
			sb.append(k);
			sb.append("=");
			sb.append(m_data.get(k));
		}
		
		sb.append('>');
		
		return sb.toString();
	}
}
