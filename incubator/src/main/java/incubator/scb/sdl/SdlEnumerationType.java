package incubator.scb.sdl;

import incubator.jcodegen.JavaEnumerationType;
import incubator.jcodegen.JavaType;

/**
 * SDL representation of an enumeration data type.
 */
public class SdlEnumerationType extends SdlType {
	/**
	 * Creates a new type.
	 * @param name the type name
	 */
	public SdlEnumerationType(String name) {
		super(name);
	}
	
	@Override
	public JavaType generate_type() {
		return new JavaEnumerationType(name());
	}
}
