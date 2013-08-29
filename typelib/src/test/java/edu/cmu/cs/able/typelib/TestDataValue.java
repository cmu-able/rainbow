package edu.cmu.cs.able.typelib;

import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Data value used for testing.
 */
public class TestDataValue extends DataValue {
	/**
	 * The value.
	 */
	public long m_val;
	
	/**
	 * Creates a new data value.
	 * @param t the value
	 * @param v the value
	 */
	public TestDataValue(DataType t, long v) {
		super(t);
		m_val = v;
	}
	
	@Override
	public int hashCode() {
		return (int) m_val;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof TestDataValue)) {
			return false;
		}
		
		TestDataValue v = (TestDataValue) o;
		if (!v.type().equals(type())) {
			return false;
		}
		
		return ((TestDataValue) o).m_val == m_val;
	}

	@Override
	public TestDataValue clone() throws CloneNotSupportedException {
		return new TestDataValue(type(), m_val);
	}
}
