package edu.cmu.cs.able.typelib.prim;

import java.util.Arrays;
import java.util.HashSet;

import edu.cmu.cs.able.typelib.type.DataType;

/**
 * Primitive data type supporting 16-bit integer values.
 */
public class Int16Type extends DataType {
	/**
	 * The data type name.
	 */
	public static final String NAME = "int16";
	
	/**
	 * Creates a new 16-bit integer type.
	 * @param any the any type
	 */
	Int16Type(AnyType any) {
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
	public Int16Value make(short value) {
		return new Int16Value(value, this);
	}
}
