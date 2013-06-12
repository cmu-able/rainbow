package edu.cmu.cs.able.typelib.prim;

/**
 * Data value representing a float value.
 */
public class FloatValue extends JavaObjectDataValue<Float> {
	/**
	 * Creates a new float value.
	 * @param value the value
	 * @param type the type
	 */
	protected FloatValue(float value, FloatType type) {
		super(value, type);
	}
}
