package edu.cmu.cs.able.typelib.parser;

import incubator.pval.Ensure;
import edu.cmu.cs.able.typelib.comp.SetDataType;
import edu.cmu.cs.able.typelib.prim.Int32Type;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.scope.AmbiguousNameException;
import edu.cmu.cs.able.typelib.scope.HierarchicalName;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataTypeScope;

/**
 * Contains a name of a data type. This is an abstract class as there are
 * several ways of defining the name of a data type. This class (and the
 * rest of its hierarchy) is used during parsing of a data type name.
 */
public abstract class DataTypeName {
	/**
	 * Constructor.
	 */
	public DataTypeName() {
	}
	
	/**
	 * Obtains the absolute hierarchical name of the data type.
	 * @return the hierarchical name
	 */
	protected abstract HierarchicalName absolute_name();
	
	/**
	 * Finds the data type in a scope, creating it if necessary (and possible).
	 * Not all data types can be created dynamically. Some data types
	 * like {@link SetDataType} can be created dynamically but some other
	 * data types like {@link Int32Type} cannot. Finding a data type may
	 * be a recursive task as, for example, finding a set will require that
	 * the inner type of the set be found.
	 * @param scope the scope where creation should occur; this is merely
	 * indicative as some data types have predefined rules on which scopes
	 * they are created; for example, the {@link SetDataType} is created in
	 * the same scope as its inner type
	 * @param pscope the primitive type scope (which should be the same as
	 * <code>scope</code> or a parent of)
	 * @return the data type found or created; <code>null</code> if the data
	 * type was not found and could not be created
	 */
	public DataType find_in_scope(DataTypeScope scope, PrimitiveScope pscope) {
		Ensure.not_null(scope);
		Ensure.not_null(pscope);
		
		HierarchicalName hn = absolute_name();
		DataType dt = null;
		try {
			dt = scope.find(hn);
		} catch (AmbiguousNameException e) {
			/*
			 * If name is ambiguous it means it was not found.
			 */
		}
		
		return dt;
	}
}
