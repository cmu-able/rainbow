package edu.cmu.cs.able.typelib.prim;


/**
 * Data value representing a 32-bit integer value.
 */
public class Int32Value extends JavaObjectDataValue<Integer> {
	/**
	 * Creates a new value.
	 * @param value the value
	 * @param type the type
	 */
	protected Int32Value(int value, Int32Type type) {
		super(value, type);
	}
	
	@Override
	public Int32Value clone() throws CloneNotSupportedException {
		return new Int32Value(value(), (Int32Type) type());
	}
}
