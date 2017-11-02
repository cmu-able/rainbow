package edu.cmu.cs.able.typelib.prim;


/**
 * Data value representing a string value.
 */
public class StringValue extends JavaObjectDataValue<String> {
	/**
	 * Creates a new string value.
	 * @param value the value
	 * @param type the type
	 */
	protected StringValue(String value, StringType type) {
		super(value, type);
	}
	
	@Override
	public StringValue clone() throws CloneNotSupportedException {
		return new StringValue(value(), (StringType) type());
	}
}
