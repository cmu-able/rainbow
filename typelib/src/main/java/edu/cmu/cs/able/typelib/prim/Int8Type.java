package edu.cmu.cs.able.typelib.prim;

import java.util.Arrays;
import java.util.HashSet;

import edu.cmu.cs.able.typelib.type.DataType;

/**
 * Primitive data type supporting 8-bit integer values.
 */
public class Int8Type extends DataType {
	/**
	 * The data type name.
	 */
	public static final String NAME = "int8";
	
	/**
	 * Creates a new 8-bit integer type.
	 * @param any the any data type
	 */
	Int8Type(AnyType any) {
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
	public Int8Value make(byte value) {
		return new Int8Value(value, this);
	}
}
