package edu.cmu.cs.able.typelib.vtscope;

import edu.cmu.cs.able.typelib.scope.AmbiguousNameException;
import edu.cmu.cs.able.typelib.scope.Scope;
import edu.cmu.cs.able.typelib.type.DataType;

/**
 * <p>A typed scope provides a <em>type</em> for a valued scope: it contains
 * data types associated with names (note that these names are unrelated to the
 * data type's names themselves).</p>
 */
public class TypeScope extends Scope<NamedType> {
	/**
	 * Creates a new scope.
	 */
	public TypeScope() {
		super(null);
	}
	
	/**
	 * Obtains the type associated with a name, if any.
	 * @param name the name
	 * @return the type or <code>null</code>
	 * @throws AmbiguousNameException the data type name is ambiguous
	 */
	public DataType type(String name) throws AmbiguousNameException {
		NamedType nt = find(name);
		if (nt == null) {
			return null;
		}
		
		return nt.type();
	}
}
