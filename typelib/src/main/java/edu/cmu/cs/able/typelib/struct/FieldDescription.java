package edu.cmu.cs.able.typelib.struct;

import edu.cmu.cs.able.typelib.type.DataType;
import incubator.pval.Ensure;

/**
 * Description of a field in a structure.
 */
public class FieldDescription {
	/**
	 * The field name.
	 */
	private String m_name;
	
	/**
	 * The field's data type.
	 */
	private DataType m_type;
	
	/**
	 * Creates a new field description.
	 * @param name the field name
	 * @param type the field type
	 */
	public FieldDescription(String name, DataType type) {
		Ensure.not_null(name, "name == null");
		Ensure.not_null(type, "type == null");
		m_name = name;
		m_type = type;
	}
	
	/**
	 * Obtains the field's name.
	 * @return the field's name
	 */
	public String name() {
		return m_name;
	}
	
	/**
	 * Obtains the field's type.
	 * @return the field's type
	 */
	public DataType type() {
		return m_type;
	}
}
