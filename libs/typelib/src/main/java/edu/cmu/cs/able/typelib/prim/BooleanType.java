package edu.cmu.cs.able.typelib.prim;

import java.util.Arrays;
import java.util.HashSet;

import edu.cmu.cs.able.typelib.type.DataType;

/**
 * Primitive type that represents a boolean value.
 */
public class BooleanType extends DataType {
	/**
	 * Data type name.
	 */
	public static final String NAME = "bool";
	
	/**
	 * Creates a new boolean type.
	 * @param any the any type
	 */
	BooleanType(AnyType any) {
		super(NAME, new HashSet<>(Arrays.asList(new DataType[] { any })));
	}

	@Override
	public boolean is_abstract() {
		return false;
	}
	
	/**
	 * Creates a new boolean value.
	 * @param value the boolean value
	 * @return the value
	 */
	public BooleanValue make(boolean value) {
		return new BooleanValue(value, this);
	}
}
