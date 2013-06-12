package edu.cmu.cs.able.typelib.prim;

import java.util.Arrays;
import java.util.HashSet;

import edu.cmu.cs.able.typelib.type.DataType;

/**
 * Data type representing a time interval as a number of microseconds.
 */
public class PeriodType extends DataType {
	/**
	 * Data type name.
	 */
	public static final String NAME = "period";
	
	/**
	 * Creates a new string type.
	 * @param any the any type
	 */
	PeriodType(AnyType any) {
		super(NAME, new HashSet<>(Arrays.asList(new DataType[] { any })));
	}

	@Override
	public boolean is_abstract() {
		return false;
	}
	
	/**
	 * Creates a new period value.
	 * @param value the period value (as a number of microseconds)
	 * @return the value
	 */
	public PeriodValue make(long value) {
		return new PeriodValue(value, this);
	}
}
