package edu.cmu.cs.able.typelib.type;

import edu.cmu.cs.able.typelib.scope.Scope;

/**
 * Scope that contains data types. <code>DataTypeScope</code>s form the
 * hierarchy of data type containers.
 */
public class DataTypeScope extends Scope<DataType> {
	/**
	 * Creates a new root scope.
	 */
	public DataTypeScope() {
		super(null);
	}
	
	/**
	 * Creates a new scope.
	 * @param name the name of the scope
	 */
	public DataTypeScope(String name) {
		super(name);
	}
	
	@Override
	protected void check_add(DataType obj) {
		assert obj != null;
		
		DataTypeScope my_top_scope = top_scope(this);
		
		for (DataType d : obj.super_types()) {
			DataTypeScope parent_top_scope = top_scope(d.parent_dts());
			if (parent_top_scope != my_top_scope) {
				throw new IllegalStateException("Parent type '"
						+ d.name() + "' is not in the same type "
						+ "hierarchy.");
			}
		}
	}
	
	/**
	 * Obtains the top scope of a scope.
	 * @param s the scope
	 * @return the top scope
	 */
	private static DataTypeScope top_scope(DataTypeScope s) {
		DataTypeScope ts;
		for (ts = s; ts.parent() != null; ts = (DataTypeScope) ts.parent()) ;
		return ts;
	}
}
