package incubator.jcodegen;

/**
 * Java type of a class that contains a copy constructor.
 */
public class JavaClassWithCopyConstructorType extends JavaType {
	/**
	 * Creates a new type.
	 * @param name the type name
	 */
	public JavaClassWithCopyConstructorType(String name) {
		super(name);
	}
	
	@Override
	public String copy_expression(String variable) {
		return "(" + variable + " == null? null : new " + name() + "("
				+ variable + "))";
	}
}
