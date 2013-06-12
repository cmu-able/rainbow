package edu.cmu.cs.able.typelib.parser;

import incubator.pval.Ensure;

import java.util.HashSet;
import java.util.Set;

import edu.cmu.cs.able.typelib.struct.FieldDescription;

/**
 * Declaration of a structure header. This class is used to collect data from
 * the parser.
 */
class StructureDeclaration {
	/**
	 * Structure name.
	 */
	private String m_name;
	
	/**
	 * Fields in the structure.
	 */
	private Set<FieldDescription> m_fields;
	
	/**
	 * Structure parents.
	 */
	private Set<String> m_parents;
	
	/**
	 * Is the structure abstract?
	 */
	private boolean m_abstract;
	
	/**
	 * Creates a new structure declaration.
	 * @param name the structure name
	 */
	public StructureDeclaration(String name) {
		Ensure.not_null(name);
		m_name = name;
		m_fields = new HashSet<>();
		m_parents = new HashSet<>();
		m_abstract = false;
	}
	
	/**
	 * Obtains the structure's name.
	 * @return the structure's name
	 */
	public String name() {
		return m_name;
	}
	
	/**
	 * Adds a new field to the structure declaration.
	 * @param f the field
	 */
	public void add(FieldDescription f) {
		Ensure.not_null(f);
		m_fields.add(f);
	}
	
	/**
	 * Adds a new parent to the structure declaration.
	 * @param p the name of the parent to add
	 */
	public void add_parent(String p) {
		Ensure.not_null(p);
		m_parents.add(p);
	}
	
	/**
	 * Obtains all fields in the structure declaration.
	 * @return all fields
	 */
	public Set<FieldDescription> fields() {
		return new HashSet<>(m_fields);
	}
	
	/**
	 * Obtains all parents of the structure.
	 * @return all parents
	 */
	public Set<String> parents() {
		return new HashSet<>(m_parents);
	}
	
	/**
	 * Marks the structure as being abstract.
	 */
	public void make_abstract() {
		m_abstract = true;
	}
	
	/**
	 * Checks whether the structure is abstract.
	 * @return is the structure abstract?
	 */
	public boolean is_abstract() {
		return m_abstract;
	}
}
