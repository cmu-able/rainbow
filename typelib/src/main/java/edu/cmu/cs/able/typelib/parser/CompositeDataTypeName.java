package edu.cmu.cs.able.typelib.parser;

import incubator.pval.Ensure;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.cs.able.typelib.comp.SetDataType;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.scope.HierarchicalName;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataTypeScope;

/**
 * <p>Represents the name of a composite data type. A composite data type is
 * built from other data types the <em>inner</em> types. An examples of
 * a composite data types is a {@link SetDataType}. This class contains a
 * common structure for all composite data types. Composite data
 * types can be dynamically created.</p>
 * <p>Because composite data types depend on other data types, this class
 * provides a method to add dependencies: {@link #add(DataTypeName)}. 
 * Dependencies are also just names which may themselves be composite in
 * turn. When creating the composite data type, if the data type is not found,
 * we need to first find (or create) all dependencies. This is known as
 * <em>resolution</em>. Resolution will be performed automatically by this
 * class.</p>
 */
public abstract class CompositeDataTypeName extends DataTypeName {
	/**
	 * All names of dependency data types.
	 */
	private List<DataTypeName> m_names;
	
	/**
	 * All resolved data types. This list will be <code>null</code> if the
	 * data type has not yet been resolved.
	 */
	private List<DataType> m_resolved;
	
	/**
	 * Creates a new composite data type name.
	 */
	public CompositeDataTypeName() {
		m_names = new ArrayList<>();
		m_resolved = null;
	}
	
	/**
	 * Adds a new data type name as a dependency. If the composite data type
	 * was already resolved, it will no longer be resolved (the new data type
	 * name needs resolution first).
	 * @param n the data type name
	 */
	protected void add(DataTypeName n) {
		Ensure.not_null(n);
		m_resolved = null;
		m_names.add(n);
	}
	
	/**
	 * Resolves the composite data type.
	 * @param scope the scope where data types should be searched in.
	 * @param pscope the primitive type scope
	 * @return <code>false</code> if failed to resolve any of the data types,
	 * <code>true</code> if resolution succeeded
	 * @see #find_in_scope(DataTypeScope, PrimitiveScope)
	 */
	private boolean resolve(DataTypeScope scope, PrimitiveScope pscope) {
		Ensure.not_null(scope);
		Ensure.not_null(pscope);
		List<DataType> resolved = new ArrayList<>();
		for (DataTypeName n : m_names) {
			DataType t = n.find_in_scope(scope, pscope);
			if (t == null) {
				return false;
			}
			
			resolved.add(t);
		}
		
		m_resolved = resolved;
		return true;
	}
	
	/**
	 * Obtains all inner types. This method can only be called after
	 * resolution has been done.
	 * @return all data types corresponding to all names added to the
	 * composite data type, in the order by which they were added
	 * @see #find_in_scope(DataTypeScope, PrimitiveScope)
	 */
	protected List<DataType> inner_types() {
		Ensure.not_null(m_resolved);
		return new ArrayList<>(m_resolved);
	}
	
	/**
	 * Obtains the name (without hierarchy) of the composite data type.
	 * @return the type's name
	 */
	protected abstract String composite_name();
	
	/**
	 * Obtains the scope in which the data type should be created. This
	 * method is only called after resolution has been made.
	 * @return the scope
	 */
	protected abstract DataTypeScope scope();
	
	/**
	 * Creates the composite data type. This method is only called after
	 * resolution has been made.
	 * @param pscope the primitive scope
	 * @return the created data type
	 */
	protected abstract DataType create_data_type(PrimitiveScope pscope);
	
	@Override
	protected HierarchicalName absolute_name() {
		DataTypeScope s = scope();
		String cn = composite_name();
		
		HierarchicalName shn = s.absolute_hname();
		if (shn == null) {
			return new HierarchicalName(true, cn);
		} else {
			return shn.push(cn);
		}
	}
	
	@Override
	public DataType find_in_scope(DataTypeScope scope, PrimitiveScope pscope) {
		Ensure.not_null(scope);
		Ensure.not_null(pscope);
		
		if (!resolve(scope, pscope)) {
			return null;
		}
		
		DataType dt = super.find_in_scope(scope, pscope);
		if (dt == null) {
			dt = create_data_type(pscope);
		}
		
		return dt;
	}
}
