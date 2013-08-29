package edu.cmu.cs.able.typelib.prim;


/**
 * Data value representing a 64-bit integer value.
 */
public class Int64Value extends JavaObjectDataValue<Long> {
	/**
	 * Creates a new value.
	 * @param value the value
	 * @param type the type
	 */
	protected Int64Value(long value, Int64Type type) {
		super(value, type);
	}
	
	@Override
	public Int64Value clone() throws CloneNotSupportedException {
		return new Int64Value(value(), (Int64Type) type());
	}
}
