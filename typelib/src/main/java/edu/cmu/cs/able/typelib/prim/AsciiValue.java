package edu.cmu.cs.able.typelib.prim;

import edu.cmu.cs.able.typelib.AsciiEncoding;
import incubator.pval.Ensure;

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
		Ensure.isTrue(AsciiEncoding.is_ascii(value));
	}
}
