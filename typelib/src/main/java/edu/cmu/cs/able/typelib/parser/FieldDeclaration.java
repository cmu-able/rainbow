package edu.cmu.cs.able.typelib.parser;

import incubator.pval.Ensure;

/**
 * Declaration of a field in a structure. Used to collect information from the
 * parser.
 */
class FieldDeclaration {
	/**
	 * Field name.
	 */
	private String m_name;
	
	/**
	 * Field type name.
	 */
	private String m_type_name;
	
	/**
	 * Creates a new field declaration.
	 * @param name the field's name
	 * @param type_name the field's type name
	 */
	public FieldDeclaration(String name, String type_name) {
		Ensure.not_null(name);
		Ensure.not_null(type_name);
		
		m_name = name;
		m_type_name = type_name;
	}
	
	/**
	 * Obtains the field's name.
	 * @return the name
	 */
	public String name() {
		return m_name;
	}
	
	/**
	 * Obtains the field's type name.
	 * @return the type name
	 */
	public String type_name() {
		return m_type_name;
	}
}
