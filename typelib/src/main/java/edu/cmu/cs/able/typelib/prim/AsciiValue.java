package edu.cmu.cs.able.typelib.prim;

import incubator.pval.Ensure;
import edu.cmu.cs.able.typelib.AsciiEncoding;

/**
 * Data value representing an ASCII value.
 */
public class AsciiValue extends JavaObjectDataValue<String> {
	/**
	 * Creates a new ASCII value.
	 * @param value the value which must be an ASCII string
	 * @param type the type
	 */
	protected AsciiValue(String value, AsciiType type) {
		super(value, type);
		Ensure.is_true(AsciiEncoding.is_ascii(value), "String is not an "
				+ "ASCII string");
	}
	
	@Override
	public AsciiValue clone() throws CloneNotSupportedException {
		return new AsciiValue(value(), (AsciiType) type());
	}
}
