package edu.cmu.cs.able.typelib.comp;

import incubator.pval.Ensure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Data value containing a set of other values.
 */
public class SetDataValue extends CollectionDataValue {
	/**
	 * The set data.
	 */
	private Set<DataValue> m_data;
	
	/**
	 * Creates a new, empty set.
	 * @param type the type
	 */
	SetDataValue(SetDataType type) {
		super(type);
		m_data = new HashSet<>();
	}
	
	@Override
	public List<DataValue> snapshot() {
		return new ArrayList<>(m_data);
	}
	
	/**
	 * Obtains all values in the set.
	 * @return all values
	 */
	public Set<DataValue> all() {
		return new HashSet<>(m_data);
	}
	
	@Override
	public boolean add(DataValue dv) {
		Ensure.notNull(dv);
		Ensure.isTrue(((SetDataType) type()).inner_type().is_instance(dv));
		return m_data.add(dv);
	}
	
	/**
	 * Removes a data value from the set.
	 * @param dv the data value
	 * @return was the value removed? (returns <code>false</code> if the set
	 * did not contain the specified element)
	 */
	public boolean remove(DataValue dv) {
		Ensure.notNull(dv);
		Ensure.isTrue(((SetDataType) type()).inner_type().is_instance(dv));
		return m_data.remove(dv);
	}
	
	/**
	 * Does the set contains the given element?
	 * @param dv the element
	 * @return does the set contain the give element?
	 */
	public boolean contains(DataValue dv) {
		Ensure.notNull(dv);
		Ensure.isTrue(((SetDataType) type()).inner_type().is_instance(dv));
		return m_data.contains(dv);
	}
	
	/**
	 * Obtains the size of the set.
	 * @return the size of the set
	 */
	public int size() {
		return m_data.size();
	}
	
	/**
	 * Removes all elements from the set.
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
		SetDataValue other = (SetDataValue) obj;
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
	public SetDataValue clone() throws CloneNotSupportedException {
		SetDataValue sv = new SetDataValue((SetDataType) type());
		for (DataValue v : snapshot()) {
			sv.add(v.clone());
		}
		
		return sv;
	}
}
