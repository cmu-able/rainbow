package incubator.jcodegen;

import incubator.pval.Ensure;

/**
 * Java type representing a set of other values.
 */
public class JavaSetType extends JavaType {
	/**
	 * The inner java type.
	 */
	private JavaType m_inner;
	
	/**
	 * Creates a new type.
	 * @param inner the type of the set contents
	 */
	public JavaSetType(JavaType inner) {
		super("java.util.Set<"
				+ Ensure.not_null(inner, "inner == null").name() + ">");
		m_inner = inner;
	}
	
	@Override
	public String copy_expression(String variable) {
		Ensure.not_null(variable, "variable == null");
		return "new java.util.HashSet<>(" + variable + ")";
	}
	
	@Override
	public String raw_class_name() {
		return "java.util.Set";
	}
	
	@Override
	public String class_expression() {
		return "(Class<java.util.Set<" + m_inner.name() + ">>) (Object) "
				+ "java.util.Set.class";
	}
}
