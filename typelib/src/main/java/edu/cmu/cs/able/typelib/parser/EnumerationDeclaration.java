package edu.cmu.cs.able.typelib.parser;

import incubator.pval.Ensure;

import java.util.HashSet;
import java.util.Set;

/**
 * Declaration of an enumeration. This class is used to collect data from
 * the parser.
 */
class EnumerationDeclaration {
	/**
	 * Enumeration name.
	 */
	private String m_name;
	
	/**
	 * Enumeration value names in the enumeration.
	 */
	private Set<String> m_names;
	
	/**
	 * Creates a new enumeration declaration.
	 * @param name the enumeration name
	 */
	public EnumerationDeclaration(String name) {
		Ensure.not_null(name);
		m_name = name;
		m_names = new HashSet<>();
	}
	
	/**
	 * Obtains the enumeration name.
	 * @return the enumeration name
	 */
	public String name() {
		return m_name;
	}
	
	/**
	 * Adds a new value name to the enumeration.
	 * @param n the value name
	 */
	public void add(String n) {
		Ensure.not_null(n);
		m_names.add(n);
	}
	
	/**
	 * Obtains all names in the enumeration.
	 * @return all names
	 */
	public Set<String> names() {
		return new HashSet<>(m_names);
	}
}
