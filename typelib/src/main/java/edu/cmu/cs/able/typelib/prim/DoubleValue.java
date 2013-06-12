package edu.cmu.cs.able.typelib.prim;

/**
 * Data value representing a double value.
 */
public class DoubleValue extends JavaObjectDataValue<Double> {
	/**
	 * Creates a new double value.
	 * @param value the value
	 * @param type the type
	 */
	protected DoubleValue(double value, DoubleType type) {
		super(value, type);
	}
}
