package edu.cmu.cs.able.typelib.parser;

import incubator.pval.Ensure;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.cs.able.typelib.prim.Int32Type;
import edu.cmu.cs.able.typelib.scope.HierarchicalName;

/**
 * <p>Name of a data type which is a simple name: the name of the data type
 * corresponds to the name of the type to be found in the scope. Simple
 * data types are, for example, primitive data types such as
 * {@link Int32Type}. If a data type is not found by this class, it cannot be
 * created.</p>
 * <p>The data type name can be hierarchical with multiple parts in the name
 * path. It can also be either an absolute name or a relative name. The
 * structure of a name, as defined in this class, matches closely the
 * definition in {@link HierarchicalName}.</p>
 */
class DataTypeSimpleName extends DataTypeName {
	/**
	 * Is the type name absolute?
	 */
	private boolean m_absolute;
	
	/**
	 * List of names in the type.
	 */
	private List<String> m_names;
	
	/**
	 * Creates a new data type name.
	 */
	public DataTypeSimpleName() {
		m_absolute = false;
		m_names = new ArrayList<>();
	}
	
	/**
	 * Marks the data type name as absolute.
	 */
	public void mark_absolute() {
		m_absolute = true;
	}
	
	/**
	 * Adds a new name to the data type name.
	 * @param n the name to add
	 */
	public void add(String n) {
		Ensure.not_null(n);
		m_names.add(n);
	}
	
	@Override
	protected HierarchicalName absolute_name() {
		return new HierarchicalName(m_absolute, m_names);
	}
}
