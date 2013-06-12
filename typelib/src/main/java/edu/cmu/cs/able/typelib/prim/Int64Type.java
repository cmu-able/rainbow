package edu.cmu.cs.able.typelib.prim;

import java.util.Arrays;
import java.util.HashSet;

import edu.cmu.cs.able.typelib.type.DataType;

/**
 * Primitive data type supporting 64-bit integer values.
 */
public class Int64Type extends DataType {
	/**
	 * The data type name.
	 */
	public static final String NAME = "int64";
	
	/**
	 * Creates a new 64-bit integer type.
	 * @param any the any type
	 */
	Int64Type(AnyType any) {
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
	public Int64Value make(long value) {
		return new Int64Value(value, this);
	}
}
