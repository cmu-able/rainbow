package edu.cmu.cs.able.typelib.prim;

import java.util.Arrays;
import java.util.HashSet;

import edu.cmu.cs.able.typelib.type.DataType;

/**
 * Data type representing a double value.
 */
public class DoubleType extends DataType {
	/**
	 * Data type name.
	 */
	public static final String NAME = "double";
	
	/**
	 * Creates a new double type.
	 * @param any the any type
	 */
	DoubleType(AnyType any) {
		super(NAME, new HashSet<>(Arrays.asList(new DataType[] { any })));
	}

	@Override
	public boolean is_abstract() {
		return false;
	}
	
	/**
	 * Creates a new double value.
	 * @param value the double value
	 * @return the value
	 */
	public DoubleValue make(double value) {
		return new DoubleValue(value, this);
	}
}
