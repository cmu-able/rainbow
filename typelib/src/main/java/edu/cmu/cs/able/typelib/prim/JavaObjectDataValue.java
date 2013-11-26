package edu.cmu.cs.able.typelib.prim;

import incubator.pval.Ensure;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * A java object data value is a data value backed up by an immutable Java
 * object.
 * @param <T> the type of the java object backing up this value; if the data
 * type backing this value has natural ordering, this type should implement
 * the <code>Comparable</code> interface
 */
public abstract class JavaObjectDataValue<T> extends DataValue {
	/**
	 * Obtains the value.
	 */
	private T m_value;
	
	/**
	 * Creates a new object.
	 * @param value the value to store.
	 * @param type the data type
	 */
	public JavaObjectDataValue(T value, DataType type) {
		super(type);
		Ensure.not_null(value, "value == null");
		m_value = value;
	}
	
	/**
	 * Obtains the Java value.
	 * @return the value
	 */
	public T value() {
		return m_value;
	}
	
	@Override
	public int hashCode() {
		return m_value.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof DataValue)) {
			return false;
		}
		
		DataValue dv = (DataValue) o;
		if (!type().is_instance(dv)) {
			return false;
		}
		
		@SuppressWarnings("unchecked")
		JavaObjectDataValue<T> jdv = (JavaObjectDataValue<T>) dv;
		return jdv.m_value.equals(m_value);
	}
	
	@Override
	public String toString() {
		return type().name() + "[" + m_value + "]";
	}
}
