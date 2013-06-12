package edu.cmu.cs.able.typelib.vtscope;

import incubator.pval.Ensure;
import edu.cmu.cs.able.typelib.scope.ScopedObject;
import edu.cmu.cs.able.typelib.type.DataType;

/**
 * A named type provides a name for a type in a typed scope.
 */
public class NamedType extends ScopedObject {
	/**
	 * The data type.
	 */
	private DataType m_type;
	
	/**
	 * Creates a new named type.
	 * @param name the name of the named type
	 * @param type the associated type
	 */
	public NamedType(String name, DataType type) {
		super(name);
		Ensure.notNull(type);
		m_type = type;
	}
	
	/**
	 * Obtains the type at this name.
	 * @return the type
	 */
	public DataType type() {
		return m_type;
	}
}
