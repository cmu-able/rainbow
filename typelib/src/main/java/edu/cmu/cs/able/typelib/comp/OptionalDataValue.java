package edu.cmu.cs.able.typelib.comp;

import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Data value containing a data value or <code>null</code>.
 */
public class OptionalDataValue extends DataValue {
	/**
	 * The data value or <code>null</code> if there is no value.
	 */
	private DataValue m_value;
	
	/**
	 * Creates a new value.
	 * @param type the type this value belongs to
	 * @param dv the data value, <code>null</code> if there is no data value
	 */
	OptionalDataValue(OptionalDataType type, DataValue dv) {
		super(type);
		m_value = dv;
	}
	
	/**
	 * Obtains the inner value, if any.
	 * @return the inner value or <code>null</code> if there is none
	 */
	public DataValue value() {
		return m_value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((m_value == null) ? 0 : m_value.hashCode());
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
		OptionalDataValue other = (OptionalDataValue) obj;
		if (m_value == null) {
			if (other.m_value != null)
				return false;
		} else if (!m_value.equals(other.m_value))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		if (m_value == null) {
			return "null[" + ((OptionalDataType) type()).inner_type() + "]";
		} else {
			return m_value.toString();
		}
	}
}
