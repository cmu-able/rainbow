package edu.cmu.cs.able.typelib.parser;

import incubator.pval.Ensure;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.type.DataTypeScope;

/**
 * Parsing context of typelib code.
 */
public class TypelibParsingContext {
	/**
	 * The primitive scope.
	 */
	private PrimitiveScope m_pscope;
	
	/**
	 * The current scope.
	 */
	private DataTypeScope m_scope;
	
	/**
	 * Creates a new context.
	 * @param pscope the primitive scope
	 * @param scope the current scope
	 */
	public TypelibParsingContext(PrimitiveScope pscope, DataTypeScope scope) {
		Ensure.not_null(pscope);
		Ensure.not_null(scope);
		
		m_pscope = pscope;
		m_scope = scope;
	}
	
	/**
	 * Obtains the primitive scope.
	 * @return the scope
	 */
	public PrimitiveScope primitive_scope() {
		return m_pscope;
	}
	
	/**
	 * Obtains the data type scope.
	 * @return the scope
	 */
	public DataTypeScope scope() {
		return m_scope;
	}
}
