package incubator.jcodegen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import incubator.pval.Ensure;

/**
 * Class representing a method in a <em>java</em> class.
 */
public class JavaMethod {
	/**
	 * Method name.
	 */
	private String m_name;
	
	/**
	 * Method return type.
	 */
	private JavaType m_type;
	
	/**
	 * The method parameters.
	 */
	private List<JavaMethodParameter> m_parameters;
	
	/**
	 * Method contents.
	 */
	private StringBuilder m_contents;
	
	/**
	 * Is the method a static method?
	 */
	private boolean m_static;
	
	/**
	 * Method protection level.
	 */
	private ProtectionLevel m_protection;
	
	/**
	 * Creates a new method.
	 * @param name the method name
	 * @param type the method type (may be <code>null</code> if there is
	 * no return type)
	 */
	public JavaMethod(String name, JavaType type) {
		Ensure.not_null(name, "name == null");
		
		m_name = name;
		m_type = type;
		m_parameters = new ArrayList<>();
		m_contents = new StringBuilder();
		m_static = false;
		m_protection = ProtectionLevel.PUBLIC;
	}
	
	/**
	 * Obtains the method name.
	 * @return the method name
	 */
	public String name() {
		return m_name;
	}
	
	/**
	 * Obtains the method type.
	 * @return the method type
	 */
	public JavaType type() {
		return m_type;
	}
	
	/**
	 * Creates a new method parameter.
	 * @param name the parameter name
	 * @param type the parameter type
	 * @return the created parameter
	 */
	public JavaMethodParameter make_parameter(String name, JavaType type) {
		Ensure.not_null(name, "name == null");
		Ensure.not_null(type, "type == null");
		
		JavaMethodParameter jmp = new JavaMethodParameter(name, type);
		m_parameters.add(jmp);
		return jmp;
	}
	
	/**
	 * Obtains all parameters in the method.
	 * @return all parameters
	 */
	public List<JavaMethodParameter> parameters() {
		return new ArrayList<>(m_parameters);
	}
	
	/**
	 * Appends contents to the method contents.
	 * @param text the text to append
	 */
	public void append_contents(String text) {
		Ensure.not_null(text);
		m_contents.append(text);
	}
	
	/**
	 * Appends contents to the method contents before a line that matches
	 * a given pattern.
	 * @param text the text to append
	 * @param pattern the pattern
	 * @return were the contents added?
	 */
	public boolean append_contents_before(String text, String pattern) {
		Ensure.not_null(text, "text == null");
		Ensure.not_null(pattern, "pattern == null");
		
		String[] lines = StringUtils.splitByWholeSeparatorPreserveAllTokens(
				m_contents.toString(), "\n");
		int idx = 0;
		for (; idx < lines.length; idx++) {
			if (Pattern.matches(pattern, lines[idx])) {
				break;
			}
		}
		
		if (idx == lines.length) {
			return false;
		}
		
		List<String> lines_l = Arrays.asList(lines);
		m_contents = new StringBuilder();
		m_contents.append(StringUtils.join(lines_l.subList(0, idx), '\n'));
		m_contents.append("\n");
		m_contents.append(text);
		m_contents.append(StringUtils.join(lines_l.subList(idx,
				lines_l.size()), '\n'));
		
		return true;
	}
	
	/**
	 * Marks the method as being static.
	 */
	public void set_static() {
		m_static = true;
	}
	
	/**
	 * Obtains whether the method is static.
	 * @return is the method static?
	 */
	public boolean is_static() {
		return m_static;
	}
	
	/**
	 * Sets the method's protection level.
	 * @param l the method's protection level
	 */
	public void protection(ProtectionLevel l) {
		Ensure.not_null(l, "l == null");
		m_protection = l;
	}
	
	/**
	 * Generates the code of this method.
	 * @return the code
	 */
	public String generate() {
		StringBuilder bldr = new StringBuilder();
		bldr.append(m_protection.keyword() + " ");
		if (m_static) {
			bldr.append("static ");
		}
		
		if (m_type != null) {
			bldr.append(m_type.name() + " ");
		}
		
		bldr.append(m_name + "(");
		
		boolean first = true;
		for (JavaMethodParameter jmp : m_parameters) {
			if (first) {
				first = false;
			} else {
				bldr.append(",");
			}
			bldr.append(jmp.type().name() + " " + jmp.name());
		}
		bldr.append(") {\n");
		bldr.append(m_contents.toString());
		bldr.append("}\n");
		return bldr.toString();
	}
}
