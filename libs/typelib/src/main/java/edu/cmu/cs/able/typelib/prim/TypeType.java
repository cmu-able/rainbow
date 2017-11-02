package edu.cmu.cs.able.typelib.prim;

import incubator.pval.Ensure;

import java.util.Arrays;
import java.util.HashSet;

import edu.cmu.cs.able.typelib.type.DataType;

/**
 * Data type whose values refer to other types.
 */
public class TypeType extends DataType {
	/**
	 * The data type's name.
	 */
	public static final String NAME = "type";
	
	/**
	 * Creates a new data type.
	 * @param any the any type which is the type's super type
	 */
	public TypeType(AnyType any) {
		super(NAME, new HashSet<>(Arrays.asList(new DataType[] { any })));
	}

	@Override
	public boolean is_abstract() {
		return false;
	}
	
	/**
	 * Creates a new value.
	 * @param t the type to refer to
	 * @return the type
	 */
	public TypeValue make(DataType t) {
		Ensure.not_null(t);
		return new TypeValue(t, this);
	}
}
