package edu.cmu.cs.able.typelib.vtscope;

import incubator.pval.Ensure;
import edu.cmu.cs.able.typelib.scope.AmbiguousNameException;
import edu.cmu.cs.able.typelib.scope.Scope;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * A scope of named values. A named scope is always associated with a typed
 * scope providing the type requirements to the named scope.
 */
public class ValueScope extends Scope<NamedValue> {
	/**
	 * The typed scope.
	 */
	private TypeScope m_types;
	
	/**
	 * Creates a new named scope.
	 * @param types the typed scope this named scope is linked to; any values
	 * added to the scope must conform to the data types in the scope
	 */
	public ValueScope(TypeScope types) {
		super(null);
		Ensure.notNull(types);
		m_types = types;
	}
	
	/**
	 * Obtains the typed scope associated with this named scope.
	 * @return the typed scope
	 */
	public TypeScope typed_scope() {
		return m_types;
	}
	
	/**
	 * Obtains the type with the given name. This is equivalent to get the
	 * typed scope, the named type and then the type.
	 * @param name the name of the type
	 * @return the type or <code>null</code> if none
	 * @throws AmbiguousNameException the name is ambiguous
	 */
	public DataType type(String name) throws AmbiguousNameException {
		Ensure.notNull(name);
		return m_types.type(name);
	}
	
	/**
	 * Obtains the value with the given name.
	 * @param name the name of the type
	 * @return the value or <code>null</code> if none
	 * @throws AmbiguousNameException the name is ambiguous
	 */
	public DataValue value(String name) throws AmbiguousNameException {
		Ensure.notNull(name);
		NamedValue nv = find(name);
		if (nv == null) {
			return null;
		}
		
		return nv.value();
	}
	
	@Override
	protected void check_add(NamedValue v) {
		assert v != null;
		String name = v.name();
		
		DataType type = null;
		try {
			type = type(name);
		} catch (AmbiguousNameException e) {
			throw new IllegalStateException("Name '" + name + "' is ambiguous "
					+ "in scope.");
		}
		
		if (type == null) {
			throw new IllegalStateException("No type with name '" + name
					+ "' in scope.");
		}
		
		if (!type.is_instance(v.value())) {
			throw new IllegalArgumentException("Type mismatch between value "
					+ "and named type at name '" + name + "'.");
		}
	}
}
