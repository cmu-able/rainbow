package edu.cmu.cs.able.typelib.parser;

import edu.cmu.cs.able.typelib.parser.TypelibParsingContext;
import incubator.pval.Ensure;

/**
 * Context used during parsing the body of an enumeration.
 */
public class EnumerationParsingContext {
	/**
	 * The type parsing context.
	 */
	private TypelibParsingContext m_typelib_ctx;
	
	/**
	 * The enumeration declaration whose body we're parsing.
	 */
	private EnumerationDeclaration m_edel;
	
	/**
	 * Creates a new parsing context.
	 * @param typelib_ctx the type parsing context (where the enumeration was
	 * declared)
	 * @param edel the enumeration declaration
	 */
	public EnumerationParsingContext(TypelibParsingContext typelib_ctx,
			EnumerationDeclaration edel) {
		Ensure.not_null(typelib_ctx);
		Ensure.not_null(edel);
		m_typelib_ctx = typelib_ctx;
		m_edel = edel;
	}
	
	/**
	 * Obtains the type parsing context.
	 * @return the type parsing context
	 */
	public TypelibParsingContext typelib_ctx() {
		return m_typelib_ctx;
	}
	
	/**
	 * Obtains the enumeration declaration.
	 * @return the enumeration declaration
	 */
	public EnumerationDeclaration enumeration_declaration() {
		return m_edel;
	}
}
