package incubator.pval;

import org.apache.commons.lang.ObjectUtils;

/**
 * Class with static verification of parameter values.
 */
public class Ensure {
	/**
	 * Ensures none of the parameters is null.
	 * 
	 * @param values the values to check
	 * 
	 * @throws IllegalArgumentException if any of the parameters is
	 * <code>null</code>
	 */
	public static void notNull(Object... values) {
		notNull("Invalid null parameter", values);
	}
	
	/**
	 * Ensures that a value is not <code>null</code>.
	 * @param value the value
	 * @return the value tested, if not <code>null</code>
	 */
	public static <T> T not_null(T value) {
		return not_null(value, "Value is null.");
	}
	
	/**
	 * Ensures that a value is not <code>null</code>.
	 * @param value the value
	 * @param error_msg the error message to send if value is <code>null</code>
	 * @return the value tested, if not <code>null</code>
	 */
	public static <T> T not_null(T value, String error_msg) {
		Ensure.is_true(value != null, error_msg);
		return value;
	}
	
	/**
	 * Ensures that a value is <code>null</code>.
	 * @param value the value
	 */
	public static void is_null(Object value) {
		Ensure.is_true(value == null);
	}
	
	/**
	 * Ensures that a value is <code>null</code>.
	 * @param value the value
	 * @param error_message the error message to send if value is
	 * <code>null</code>
	 */
	public static void is_null(Object value, String error_message) {
		Ensure.is_true(value == null, error_message);
	}

	/**
	 * Ensures none of the parameters is null.
	 * 
	 * @param message the message to place in the exception if any of the
	 * parameters is null
	 * @param values the values to check
	 * 
	 * @throws IllegalArgumentException if any of the parameters is
	 * <code>null</code>
	 */
	public static void notNull(String message, Object... values) {
		/*
		 * There is a tricky corner case here: if we call
		 * notNull((String) null) then this method gets invoked instead of
		 * the previous.
		 */
		if (values.length == 0) {
			notNull((Object) message);
			return;
		}
		
		for (Object v : values) {
			if (v == null) {
				throw new IllegalArgumentException(message);
			}
		}
	}

	/**
	 * Ensures that two values are equal (using <code>equals</code>).
	 * @param expected the expected value (<code>null</code> accepted)
	 * @param value the value to test (<code>null</code> accepted)
	 */
	public static void equals(Object expected, Object value) {
		is_true(ObjectUtils.equals(expected, value));
	}

	/**
	 * Ensures all values are true.
	 * 
	 * @param values the values to check
	 * 
	 * @throws IllegalArgumentException if any of the values is
	 * <code>false</code>
	 */
	public static void isTrue(Boolean... values) {
		isTrue("Condition is false", values);
	}

	/**
	 * Ensures all values are true.
	 * 
	 * @param message the error message to throw
	 * @param values the values to check
	 * 
	 * @throws IllegalArgumentException if any of the values is
	 * <code>false</code>
	 */
	public static void isTrue(String message, Boolean... values) {
		for (boolean b : values) {
			if (!b) {
				throw new IllegalArgumentException(message);
			}
		}
	}
	
	/**
	 * Ensures that a value is <code>true</code>.
	 * @param value the value
	 */
	public static void is_true(boolean value) {
		is_true(value, "Condition is false");
	}
	
	/**
	 * Ensures that a value is <code>true</code>.
	 * @param value the value
	 * @param error_message the error message to use when the condition fails
	 */
	public static void is_true(boolean value, String error_message) {
		if (!value) {
			throw new AssertionError(error_message);
		}
	}
	
	/**
	 * Ensures that a value is <code>false</code>.
	 * @param value the value
	 */
	public static void is_false(boolean value) {
		is_false(value, "Condition is false");
	}
	
	/**
	 * Ensures that a value is <code>false</code>.
	 * @param value the value
	 * @param error_message the error message to use when the condition fails
	 */
	public static void is_false(boolean value, String error_message) {
		is_true(!value);
	}

	/**
	 * Checks that the some state condition is verified.
	 * 
	 * @param message the message to place in the exception if the condition is
	 * not verified
	 * @param checks the checks to perform
	 * 
	 * @throws IllegalStateException if any of the boolean values is
	 * <code>false</code>
	 */
	public static void stateCondition(String message, Boolean... checks) {
		for (boolean b : checks) {
			if (!b) {
				throw new IllegalStateException(message);
			}
		}
	}

	/**
	 * Checks that the some state condition is verified.
	 * 
	 * @param checks the checks to perform
	 * 
	 * @throws IllegalStateException if any of the boolean values is
	 * <code>false</code>
	 */
	public static void stateCondition(Boolean... checks) {
		for (boolean b : checks) {
			if (!b) {
				throw new IllegalStateException(
						"State condition check failed");
			}
		}
	}
	
	/**
	 * Ensures that <em>v1</em> is greater than <em>v2</em>.
	 * @param v1 the first value
	 * @param v2 the second value
	 */
	public static void greater(long v1, long v2) {
		Ensure.greater(v1, v2, "Condition is false");
	}
	
	/**
	 * Ensures that <em>v1</em> is greater than <em>v2</em>.
	 * @param v1 the first value
	 * @param v2 the second value
	 * @param error_message the error message to use when the condition fails
	 */
	public static void greater(long v1, long v2, String error_message) {
		Ensure.is_true(v1 > v2, error_message);
	}
	
	/**
	 * Ensures that <em>v1</em> is greater than or equal to <em>v2</em>.
	 * @param v1 the first value
	 * @param v2 the second value
	 */
	public static void greater_equal(long v1, long v2) {
		Ensure.greater_equal(v1, v2, "Condition is false");
	}
	
	/**
	 * Ensures that <em>v1</em> is greater than or equal to <em>v2</em>.
	 * @param v1 the first value
	 * @param v2 the second value
	 * @param error_message the error message to use when the condition fails
	 */
	public static void greater_equal(long v1, long v2, String error_message) {
		Ensure.is_true(v1 >= v2, error_message);
	}
	
	/**
	 * Ensures that <em>v1</em> is less than <em>v2</em>.
	 * @param v1 the first value
	 * @param v2 the second value
	 */
	public static void less(long v1, long v2) {
		Ensure.less(v1, v2, "Condition is false");
	}
	
	/**
	 * Ensures that <em>v1</em> is less than <em>v2</em>.
	 * @param v1 the first value
	 * @param v2 the second value
	 * @param error_message the error message to use when the condition fails
	 */
	public static void less(long v1, long v2, String error_message) {
		Ensure.is_true(v1 < v2, error_message);
	}
	
	/**
	 * Ensures that <em>v1</em> is less than or equal to <em>v2</em>.
	 * @param v1 the first value
	 * @param v2 the second value
	 */
	public static void less_equal(long v1, long v2) {
		Ensure.less_equal(v1, v2, "Condition is false");
	}
	
	/**
	 * Ensures that <em>v1</em> is less than or equal to <em>v2</em>.
	 * @param v1 the first value
	 * @param v2 the second value
	 * @param error_message the error message to use when the condition fails
	 */
	public static void less_equal(long v1, long v2, String error_message) {
		Ensure.is_true(v1 <= v2, error_message);
	}
	
	/**
	 * Ensures that <em>o1</em> is the same object as <em>o2</em>.
	 * @param o1 the first object
	 * @param o2 the second object
	 */
	public static void same(Object o1, Object o2) {
		Ensure.is_true(o1 == o2);
	}
	
	/**
	 * Ensures that <em>o1</em> is not the same object as <em>o2</em>.
	 * @param o1 the first object
	 * @param o2 the second object
	 */
	public static void not_same(Object o1, Object o2) {
		Ensure.is_true(o1 != o2);
	}
	
	/**
	 * Ensures that an object is an instance of a class.
	 * @param obj the object
	 * @param cls the class
	 */
	public static void is_instance(Object obj, Class<?> cls) {
		Ensure.is_true(cls.isInstance(obj));
	}
	
	/**
	 * Ensures that this instruction is never reached.
	 */
	public static void unreachable() {
		unreachable("Unreachable code reached.");
	}
	
	/**
	 * Ensures that this instruction is never reached.
	 * @param error_message an optional error message
	 */
	public static void unreachable(String error_message) {
		is_true(false, error_message);
	}
	
	/**
	 * Ensures that the given throwable is never thrown. This method will
	 * always throw an <code>AssertionError</code>.
	 * @param t the throwable
	 */
	public static void never_thrown(Throwable t) {
		throw new AssertionError("Exception should never be thrown.", t);
	}
}
