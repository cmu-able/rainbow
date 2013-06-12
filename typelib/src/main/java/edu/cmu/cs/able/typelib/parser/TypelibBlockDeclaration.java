package edu.cmu.cs.able.typelib.parser;

import incubator.pval.Ensure;

/**
 * Class containing the information passed from the parser of a typelib block.
 */
class TypelibBlockDeclaration {
	/**
	 * The structure declared, if any.
	 */
	private StructureDeclaration m_sdel;
	
	/**
	 * The namespace declared, if any.
	 */
	private String m_nsdel;
	
	/**
	 * Creates a new declaration containing a structure declaration.
	 * @param sdel the structure declaration
	 */
	TypelibBlockDeclaration(StructureDeclaration sdel) {
		Ensure.not_null(sdel);
		m_sdel = sdel;
	}
	
	/**
	 * Creates a new declaration containing a namespace declaration.
	 * @param nsdel the namespace declaration
	 */
	TypelibBlockDeclaration(String nsdel) {
		Ensure.not_null(nsdel);
		m_nsdel = nsdel;
	}
	
	/**
	 * Obtains the declared structure.
	 * @return the structure or <code>null</code> if none
	 */
	StructureDeclaration structure_declaration() {
		return m_sdel;
	}
	
	/**
	 * Obtains the declared namespace.
	 * @return the namespace or <code>null</code> if none
	 */
	String namespace_declaration() {
		return m_nsdel;
	}
}
