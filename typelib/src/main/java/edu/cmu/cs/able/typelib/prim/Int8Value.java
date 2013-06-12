package edu.cmu.cs.able.typelib.prim;

/**
 * Data value representing an 8-bit integer value.
 */
public class Int8Value extends JavaObjectDataValue<Byte> {
	/**
	 * Creates a new value.
	 * @param value the value
	 * @param type the type
	 */
	protected Int8Value(byte value, Int8Type type) {
		super(value, type);
	}
}
