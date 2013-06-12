package edu.cmu.cs.able.typelib.prim;

import edu.cmu.cs.able.typelib.type.DataType;

/**
 * The <code>any</code> data type is the top most data type for all data types.
 * It is similar to the Java concept of <code>Object</code>.
 */
public class AnyType extends DataType {
	/**
	 * Data type name.
	 */
	public static final String NAME = "any";
	
	/**
	 * Constructor.
	 */
	AnyType() {
		super(NAME, null);
	}

	@Override
	public boolean is_abstract() {
		return true;
	}
}
