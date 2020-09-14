package incubator.jcodegen;

import incubator.pval.Ensure;

/**
 * Class representing a <em>java</em> data type. Data types are represented
 * by strings (how they are declared in <em>java</em>) and provide some
 * elementary code-generation features. 
 */
public class JavaType {
	/**
	 * The data type name.
	 */
	private String m_name;
	
	/**
	 * Creates a new type with the given name.
	 * @param name the type name
	 */
	public JavaType(String name) {
		Ensure.not_null(name, "name == null");
		m_name = name;
	}
	
	/**
	 * Obtains the data type name.
	 * @return the name
	 */
	public String name() {
		return m_name;
	}
	
	/**
	 * Is this type an enumeration?
	 * @return is this type an enumeration?
	 */
	public boolean is_enumeration() {
		return false;
	}
	
	/**
	 * Creates an expression that returns, if possible, a copy of a
	 * variable.
	 * @param variable the variable
	 * @return the expression
	 */
	public String copy_expression(String variable) {
		Ensure.not_null(variable);
		return variable;
	}
	
	/**
	 * Obtains the raw name of the class that represents this type (no
	 * generics).
	 * @return the raw class name
	 */
	public String raw_class_name() {
		return m_name;
	}
	
	/**
	 * Obtains the expression that evaluates to Class&lt;type&gt;. For most
	 * types, this is just the class name followed by <code>.class</code>.
	 * @return the expression
	 */
	public String class_expression() {
		return raw_class_name() + ".class";
	}
}
