package edu.cmu.cs.able.typelib.prim;

import incubator.pval.Ensure;

import java.util.Arrays;
import java.util.HashSet;

import edu.cmu.cs.able.typelib.type.DataType;

/**
 * Data type representing a time value (number of microseconds since the epoch).
 */
public class TimeType extends DataType {
	/**
	 * Data type name.
	 */
	public static final String NAME = "time";
	
	/**
	 * Creates a new string type.
	 * @param any the any type
	 */
	TimeType(AnyType any) {
		super(NAME, new HashSet<>(Arrays.asList(new DataType[] { any })));
	}

	@Override
	public boolean is_abstract() {
		return false;
	}
	
	/**
	 * Creates a new time value.
	 * @param value the time value (number of microseconds since the epoch)
	 * @return the value
	 */
	public TimeValue make(long value) {
		Ensure.is_true(value >= 0);
		return new TimeValue(value, this);
	}
}
