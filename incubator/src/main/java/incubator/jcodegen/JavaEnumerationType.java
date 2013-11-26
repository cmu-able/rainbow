package incubator.jcodegen;

/**
 * Subclass of type representing an enumeration.
 */
public class JavaEnumerationType extends JavaType {
	/**
	 * Creates a new enumeration.
	 * @param name the name of the enumeration
	 */
	public JavaEnumerationType(String name) {
		super(name);
	}
	
	@Override
	public boolean is_enumeration() {
		return true;
	}
}
