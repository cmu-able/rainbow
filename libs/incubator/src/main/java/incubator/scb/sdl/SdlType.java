package incubator.scb.sdl;

import incubator.jcodegen.JavaField;
import incubator.jcodegen.JavaMethod;
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
	
	/**
	 * Generates an expression that compares two fields, yielding
	 * <code>true</code> if the values are equal and <code>false</code> if
	 * the values are not equal.
	 * @param f1 field to compare
	 * @param f2 field to compare
	 * @return the comparison expression
	 */
	public String generate_comparison(JavaField f1, JavaField f2) {
		Ensure.not_null(f1, "f1 == null");
		Ensure.not_null(f2, "f2 == null");
		
		return "java.util.Objects.equals(" + f1.name() + ", " + f2.name() + ")";
	}
	
	/**
	 * Creates an expression that returns, if possible, a copy of a
	 * variable.
	 * @param variable the variable
	 * @return the expression
	 */
	public String copy_expression(String variable) {
		Ensure.not_null(variable, "variable == null");
		return generate_type().copy_expression(variable);
	}

	/**
	 * Generates an expression that adds deltas for a field given two
	 * objects.
	 * @param old_v the variable with the old object
	 * @param new_v the variable with the new object
	 * @param field_method method used to obtain the field value from the
	 * object 
	 * @param delta_var the variable with the list that receives the deltas
	 * @return the statements
	 */
	public String generate_delta_assign(String old_v, String new_v,
			JavaMethod field_method, String delta_var) {
		Ensure.not_null(old_v, "old_v == null");
		Ensure.not_null(new_v, "new_v == null");
		Ensure.not_null(field_method, "field_method == null");
		Ensure.not_null(delta_var, "delta_var == null");
		
		return delta_var + ".add(new incubator.scb.delta.ScbFieldDelta(this, "
				+ "old, " + field_method.name() + "(), " + field_method.name()
				+ "().get(" + old_v + "), " + field_method.name()
				+ "().get(" + new_v + ")));";
				
	}
	
}
