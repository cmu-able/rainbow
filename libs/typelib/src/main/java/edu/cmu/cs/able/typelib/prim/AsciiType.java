package edu.cmu.cs.able.typelib.prim;

import java.util.Arrays;
import java.util.HashSet;

import edu.cmu.cs.able.typelib.type.DataType;

/**
 * Data type representing an ASCII value.
 */
public class AsciiType extends DataType {
	/**
	 * Data type name.
	 */
	public static final String NAME = "ascii";
	
	/**
	 * Creates a new string type.
	 * @param any the any type that is superset of this one
	 */
	AsciiType(AnyType any) {
		super(NAME, new HashSet<>(Arrays.asList(new DataType[] { any })));
	}

	@Override
	public boolean is_abstract() {
		return false;
	}

	/**
	 * Creates a new ascii value.
	 * @param value the ascii value
	 * @return the value
	 */
	public AsciiValue make(String value) {
		return new AsciiValue(value, this);
	}
}
