package edu.cmu.cs.able.typelib.prim;

import java.util.Arrays;
import java.util.HashSet;

import edu.cmu.cs.able.typelib.type.DataType;

/**
 * Data type representing a string value.
 */
public class StringType extends DataType {
	/**
	 * Data type name.
	 */
	public static final String NAME = "string";
	
	/**
	 * Creates a new string type.
	 * @param any the any type
	 */
	StringType(AnyType any) {
		super(NAME, new HashSet<>(Arrays.asList(new DataType[] { any })));
	}

	@Override
	public boolean is_abstract() {
		return false;
	}
	
	/**
	 * Creates a new string value.
	 * @param value the string value
	 * @return the value
	 */
	public StringValue make(String value) {
		return new StringValue(value, this);
	}
}
