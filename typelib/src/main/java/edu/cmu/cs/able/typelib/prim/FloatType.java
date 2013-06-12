package edu.cmu.cs.able.typelib.prim;

import java.util.Arrays;
import java.util.HashSet;

import edu.cmu.cs.able.typelib.type.DataType;

/**
 * Data type representing a float value.
 */
public class FloatType extends DataType {
	/**
	 * Data type name.
	 */
	public static final String NAME = "float";
	
	/**
	 * Creates a new boolean type.
	 * @param any the any type
	 */
	FloatType(AnyType any) {
		super(NAME, new HashSet<>(Arrays.asList(new DataType[] { any })));
	}

	@Override
	public boolean is_abstract() {
		return false;
	}
	
	/**
	 * Creates a new float value.
	 * @param value the float value
	 * @return the value
	 */
	public FloatValue make(float value) {
		return new FloatValue(value, this);
	}
}
