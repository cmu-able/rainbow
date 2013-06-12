package edu.cmu.cs.able.typelib.prim;

import java.util.Arrays;
import java.util.HashSet;

import edu.cmu.cs.able.typelib.type.DataType;

/**
 * Primitive data type supporting 32-bit integer values.
 */
public class Int32Type extends DataType {
	/**
	 * The data type name.
	 */
	public static final String NAME = "int32";
	
	/**
	 * Creates a new 32-bit integer type.
	 * @param any the any type
	 */
	Int32Type(AnyType any) {
		super(NAME, new HashSet<>(Arrays.asList(new DataType[] { any })));
	}

	@Override
	public boolean is_abstract() {
		return false;
	}
	
	/**
	 * Creates a new value.
	 * @param value the Java value
	 * @return the value
	 */
	public Int32Value make(int value) {
		return new Int32Value(value, this);
	}
}
