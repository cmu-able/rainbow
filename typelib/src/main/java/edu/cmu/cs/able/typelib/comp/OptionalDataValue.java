package edu.cmu.cs.able.typelib.comp;

import org.apache.commons.lang.ObjectUtils;

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
		if (m_value == null) {
			return 0;
		} else {
			return m_value.hashCode();
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (obj == null) {
			return false;
		}
		
		if (!(obj instanceof OptionalDataValue)) {
			return false;
		}
		
		OptionalDataValue oobj = (OptionalDataValue) obj;
		return ObjectUtils.equals(m_value, oobj.m_value);
	}
	
	@Override
	public String toString() {
		if (m_value == null) {
			return "null[" + ((OptionalDataType) type()).inner_type() + "]";
		} else {
			return m_value.toString();
		}
	}
	
	@Override
	public OptionalDataValue clone() throws CloneNotSupportedException {
		return new OptionalDataValue((OptionalDataType) type(),
				m_value == null? null : m_value.clone());
	}
}
