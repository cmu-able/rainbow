package incubator.scb.sdl;

import incubator.jcodegen.JavaType;
import incubator.pval.Ensure;

/**
 * SDL representation of a data type.
 */
public class SdlType {
	/**
	 * The data type name.
	 */
	private String m_name;
	
	/**
	 * Creates a new type.
	 * @param name the type name
	 */
	public SdlType(String name) {
		Ensure.not_null(name, "name == null");
		m_name = name;
	}
	
	/**
	 * Obtains the data type name.
	 * @return the data type name
	 */
	public String name() {
		return m_name;
	}
	
	/**
	 * Generates the {@link JavaType} that corresponds to this SDL type.
	 * @return the {@link JavaType}
	 */
	public JavaType generate_type() {
		return new JavaType(m_name);
	}
}
