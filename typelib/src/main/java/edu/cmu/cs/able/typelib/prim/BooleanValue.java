package edu.cmu.cs.able.typelib.prim;


/**
 * Data value representing a boolean value.
 */
public class BooleanValue extends JavaObjectDataValue<Boolean> {
	/**
	 * Creates a new boolean value.
	 * @param value the value
	 * @param type the type
	 */
	protected BooleanValue(boolean value, BooleanType type) {
		super(value, type);
	}
	
	@Override
	public BooleanValue clone() throws CloneNotSupportedException {
		return new BooleanValue(value(), (BooleanType) type());
	}
}
