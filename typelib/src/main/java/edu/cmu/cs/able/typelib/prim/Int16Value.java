package edu.cmu.cs.able.typelib.prim;

/**
 * Data value representing a 16-bit integer value.
 */
public class Int16Value extends JavaObjectDataValue<Short> {
	/**
	 * Creates a new value.
	 * @param value the value
	 * @param type the type
	 */
	protected Int16Value(short value, Int16Type type) {
		super(value, type);
	}
}
