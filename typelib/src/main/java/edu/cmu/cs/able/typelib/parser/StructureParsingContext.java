package edu.cmu.cs.able.typelib.parser;

import incubator.pval.Ensure;

/**
 * Context used during parsing the body of a structure.
 */
public class StructureParsingContext {
	/**
	 * The type parsing context.
	 */
	private TypelibParsingContext m_typelib_ctx;
	
	/**
	 * The structure declaration whose body we're parsing.
	 */
	private StructureDeclaration m_sdel;
	
	/**
	 * Creates a new parsing context.
	 * @param typelib_ctx the type parsing context (where the structure was
	 * declared)
	 * @param sdel the structure declaration
	 */
	public StructureParsingContext(TypelibParsingContext typelib_ctx,
			StructureDeclaration sdel) {
		Ensure.not_null(typelib_ctx);
		Ensure.not_null(sdel);
		m_typelib_ctx = typelib_ctx;
		m_sdel = sdel;
	}
	
	/**
	 * Obtains the type parsing context.
	 * @return the type parsing context
	 */
	public TypelibParsingContext typelib_ctx() {
		return m_typelib_ctx;
	}
	
	/**
	 * Obtains the structure declaration.
	 * @return the structure declaration
	 */
	public StructureDeclaration structure_declaration() {
		return m_sdel;
	}
}
