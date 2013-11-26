package incubator.jcodegen;

import incubator.pval.Ensure;

/**
 * Represents a field in a class.
 */
public class JavaField {
	/**
	 * The field data type.
	 */
	private JavaType m_type;
	
	/**
	 * The field name.
	 */
	private String m_name;
	
	/**
	 * Creates a new field.
	 * @param name the field name
	 * @param type the field type
	 */
	public JavaField(String name, JavaType type) {
		Ensure.not_null(name, "name == null");
		Ensure.not_null(type, "type == null");
		
		m_type = type;
		m_name = name;
	}
	
	/**
	 * Obtains the field type.
	 * @return the field type
	 */
	public JavaType type() {
		return m_type;
	}
	
	/**
	 * Obtains the field name.
	 * @return the field name
	 */
	public String name() {
		return m_name;
	}
	
	/**
	 * Generates the code with this field declaration.
	 * @return the code
	 */
	public String generate() {
		return "private " + m_type.name() + " " + m_name + ";\n";
	}
}
