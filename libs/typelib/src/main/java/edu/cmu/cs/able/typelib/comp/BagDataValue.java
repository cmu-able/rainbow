package edu.cmu.cs.able.typelib.comp;

import incubator.pval.Ensure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Data value containing a bag of other values.
 */
public class BagDataValue extends CollectionDataValue {
	/**
	 * The bag data: maps values to their counts in the bag.
	 */
	private Map<DataValue, Integer> m_data;
	
	/**
	 * Creates a new, empty bag.
	 * @param type the type
	 */
	BagDataValue(BagDataType type) {
		super(type);
		m_data = new HashMap<>();
	}
	
	/**
	 * Obtains all values in the bag.
	 * @return all values
	 */
	public Collection<DataValue> all() {
		ArrayList<DataValue> v = new ArrayList<>();
		for (Map.Entry<DataValue, Integer> e : m_data.entrySet()) {
			for (int i = 0; i < e.getValue(); i++) {
				v.add(e.getKey());
			}
		}
		
		return v;
	}
	
	@Override
	public List<DataValue> snapshot() {
		return new ArrayList<>(all());
	}
	
	@Override
	public boolean add(DataValue dv) {
		Ensure.not_null(dv, "dv == null");
		Ensure.is_true(((BagDataType) type()).inner_type().is_instance(dv),
				"Value does not match bag type");
		
		Integer count = m_data.get(dv);
		if (count == null) {
			m_data.put(dv, 1);
		} else {
			m_data.put(dv, count + 1);
		}
		
		return true;
	}
	
	/**
	 * Removes a value from the bag.
	 * @param dv the value to remove
	 * @return was the value removed?
	 */
	public boolean remove(DataValue dv) {
		Ensure.not_null(dv, "dv == null");
		Ensure.is_true(((BagDataType) type()).inner_type().is_instance(dv),
				"Value does not match bag type");
		
		Integer count = m_data.get(dv);
		if (count == null) {
			return false;
		} else if (count == 1) {
			m_data.remove(dv);
		} else {
			m_data.put(dv, count - 1);
		}
		
		return true;
	}
	
	/**
	 * Checks if the bag contains a value.
	 * @param dv the value to check
	 * @return does it contain the value?
	 */
	public boolean contains(DataValue dv) {
		return count(dv) > 0;
	}
	
	/**
	 * Obtains how may instances of a value are in the bag.
	 * @param dv the value to check
	 * @return the number of instances; <code>0</code> means the bag does not
	 * contains the value
	 */
	public int count(DataValue dv) {
		Ensure.not_null(dv, "dv == null");
		Ensure.is_true(((BagDataType) type()).inner_type().is_instance(dv),
				"Value does not match bag type");
		
		Integer c = m_data.get(dv);
		if (c == null) {
			return 0;
		}
		
		return c;
	}
	
	/**
	 * Obtains the number of elements in the bag.
	 * @return the number of elements in the bag
	 */
	public int size() {
		int cnt = 0;
		
		for (Integer i : m_data.values()) {
			cnt += i;
		}
		
		return cnt;
	}
	
	/**
	 * Removes all elements from the bag.
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
		BagDataValue other = (BagDataValue) obj;
		if (m_data == null) {
			if (other.m_data != null)
				return false;
		} else if (!m_data.equals(other.m_data))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "{" + m_data + "}";
	}
	
	@Override
	public BagDataValue clone() throws CloneNotSupportedException{
		BagDataValue cl = new BagDataValue((BagDataType) type());
		for (DataValue v : snapshot()) {
			cl.add(v.clone());
		}
		
		return cl;
	}
}
