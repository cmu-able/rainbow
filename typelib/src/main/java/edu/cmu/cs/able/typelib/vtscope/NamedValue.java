package edu.cmu.cs.able.typelib.vtscope;

import incubator.pval.Ensure;
import edu.cmu.cs.able.typelib.scope.ScopedObject;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * A named value is a value in a named scope which corresponds to a type
 * in a named type.
 */
public class NamedValue extends ScopedObject {
	/**
	 * The data value.
	 */
	private DataValue m_value;
	
	/**
	 * Creates a new named value.
	 * @param name the name
	 * @param value the value
	 */
	public NamedValue(String name, DataValue value) {
		super(name);
		Ensure.notNull(value);
		
		m_value = value;
	}
	
	/**
	 * Obtains the value associated with this named value.
	 * @return the value
	 */
	public DataValue value() {
		return m_value;
	}
}
