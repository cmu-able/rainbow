package edu.cmu.cs.able.typelib.prim;


/**
 * Data value representing a period (time interval) value in microseconds.
 */
public class PeriodValue extends JavaObjectDataValue<Long> {
	/**
	 * Creates a new period value.
	 * @param value the number of microseconds in the period
	 * @param type the type
	 */
	protected PeriodValue(long value, PeriodType type) {
		super(value, type);
	}
}
