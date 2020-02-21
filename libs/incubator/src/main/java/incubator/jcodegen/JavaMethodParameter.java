package incubator.jcodegen;

import incubator.pval.Ensure;

/**
 * Class representing a parameter in a <em>java</em> method.
 */
public class JavaMethodParameter {
	/**
	 * Parameter name.
	 */
	private String m_name;
	
	/**
	 * Parameter type.
	 */
	private JavaType m_type;
	
	/**
	 * Creates a new parameter.
	 * @param name the parameter name
	 * @param type the parameter type
	 */
	public JavaMethodParameter(String name, JavaType type) {
		Ensure.not_null(name, "name == null");
		Ensure.not_null(type, "type == null");
		
		m_name = name;
		m_type = type;
	}
	
	/**
	 * Obtains the parameter name.
	 * @return the parameter name
	 */
	public String name() {
		return m_name;
	}
	
	/**
	 * Obtains the parameter type.
	 * @return the parameter type
	 */
	public JavaType type() {
		return m_type;
	}
}
