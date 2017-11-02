package edu.cmu.cs.able.typelib.type;

import java.util.HashMap;
import java.util.Map;

import incubator.pval.Ensure;

/**
 * <p>A value represents an instance of a type (see {@link DataType}). There
 * will be an instance of this class (or subclasses) for each value whose
 * type is an instance of {@link DataType}. Data values aren't usually created
 * directly but through the data types using factory methods.</p>
 * <p>Values which represent instances of subclasses of a type must be
 * instances of subclasses of the type's value class to ensure casting in Java
 * works.</p>
 * <p>Values may have attached objects. Attached objects can be used as an 
 * extension mechanism. Each attached object is attached with a key, which
 * has to be a string, but whose meaning is not defined by this class.</p>
 * <p>The data value should implement the standard <code>equals</code>,
 * <code>hashCode</code> and <code>toString</code> methods (the last one is
 * optional but recommended).
 */
public abstract class DataValue {
	/**
	 * The value's type.
	 */
	private DataType m_type;
	
	/**
	 * Value's attachments.
	 */
	private Map<String, Object> m_attachments;
	
	/**
	 * Creates a new value.
	 * @param type the type this value belongs to
	 */
	protected DataValue(DataType type) {
		Ensure.not_null(type, "type == null");
		m_type = type;
		m_attachments = null;
	}
	
	/**
	 * Obtains the data type of this value.
	 * @return the data type
	 */
	public DataType type() {
		return m_type;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_type == null) ? 0 : m_type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataValue other = (DataValue) obj;
		if (m_type == null) {
			if (other.m_type != null)
				return false;
		} else if (!m_type.equals(other.m_type))
			return false;
		return true;
	}
	
	/**
	 * Sets or removes the object to be attached with the given key. If an
	 * object already exists with the given key, it will be removed
	 * @param key the key to attach the object
	 * @param value the object to attach; if <code>null</code> removes an
	 * attachment
	 */
	public void set_attach(String key, Object value) {
		Ensure.not_null(key);
		if (value == null) {
			if (m_attachments != null) {
				m_attachments.remove(key);
			}
		} else {
			if (m_attachments == null) {
				m_attachments = new HashMap<>();
			}
			
			m_attachments.put(key,  value);
		}
	}
	
	/**
	 * Obtains the object attached with a key.
	 * @param key the key
	 * @return the attached object
	 */
	public Object get_attach(String key) {
		Ensure.not_null(key);
		if (m_attachments == null) {
			return null;
		}
		
		return m_attachments.get(key);
	}
	
	@Override
	public abstract DataValue clone() throws CloneNotSupportedException;
}
