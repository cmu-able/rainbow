package edu.cmu.cs.able.typelib.comp;

import incubator.pval.Ensure;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Data value containing a set of other values.
 */
public class ListDataValue extends CollectionDataValue {
	/**
	 * The set data.
	 */
	private List<DataValue> m_data;
	
	/**
	 * Creates a new, empty list.
	 * @param type the type
	 */
	ListDataValue(ListDataType type) {
		super(type);
		m_data = new ArrayList<>();
	}
	
	/**
	 * Obtains all values in the list.
	 * @return all values
	 */
	public List<DataValue> all() {
		return new ArrayList<>(m_data);
	}
	
	@Override
	public List<DataValue> snapshot() {
		return new ArrayList<>(m_data);
	}
	
	@Override
	public boolean add(DataValue dv) {
		Ensure.notNull(dv);
		Ensure.isTrue(((ListDataType) type()).inner_type().is_instance(dv));
		m_data.add(dv);
		return true;
	}
	
	/**
	 * Checks whether this list contains an element.
	 * @param dv the element to check
	 * @return does the list contain the element?
	 */
	public boolean contains(DataValue dv) {
		return index_of(dv) >= 0;
	}
	
	/**
	 * Obtains the first index of an element. 
	 * @param dv the element to check
	 * @return the index or <code>-1</code> if none
	 */
	public int index_of(DataValue dv) {
		Ensure.notNull(dv);
		Ensure.isTrue(((ListDataType) type()).inner_type().is_instance(dv));
		return m_data.indexOf(dv);
	}
	
	/**
	 * Obtains the size of the list.
	 * @return the size
	 */
	public int size() {
		return m_data.size();
	}
	
	/**
	 * Removes an element from the list.
	 * @param idx the index to remove
	 * @return the removed element
	 */
	public DataValue remove(int idx) {
		Ensure.isTrue(idx >= 0);
		Ensure.isTrue(idx < m_data.size());
		return m_data.remove(idx);
	}
	
	/**
	 * Obtains a value from the list.
	 * @param idx the index
	 * @return the value at the given index
	 */
	public DataValue get(int idx) {
		Ensure.isTrue(idx >= 0);
		Ensure.isTrue(idx < m_data.size());
		return m_data.get(idx);
	}
	
	/**
	 * Removes all elements from the list.
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
		ListDataValue other = (ListDataValue) obj;
		if (m_data == null) {
			if (other.m_data != null)
				return false;
		} else if (!m_data.equals(other.m_data))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "[" + m_data + "]";
	}
}
