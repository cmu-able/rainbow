package edu.cmu.cs.able.typelib.type;

import org.junit.Test;

import auxtestlib.DefaultTCase;

/**
 * Abstract test case for testing data types.
 */
public abstract class AbstractDataTypeTestCase extends DefaultTCase {
	/**
	 * Creates a type and checks that it reports correctly whether it is
	 * abstract or not.
	 * @throws Exception test failed
	 */
	@Test
	public abstract void create_check_is_abstract() throws Exception;
	
	/**
	 * Creates a type and checks that it reports correctly what its super types
	 * are.
	 * @throws Exception test failed
	 */
	@Test
	public abstract void create_check_super_types() throws Exception;
	
	/**
	 * Creates values from a type.
	 * @throws Exception test failed
	 */
	@Test
	public abstract void create_values() throws Exception;
	
	/**
	 * Comparing values for equality (also checks the hash code method).
	 * @throws Exception test failed
	 */
	@Test
	public abstract void compare_equals_and_hash_code() throws Exception;
	
	/**
	 * Creates a type and checks its string description.
	 * @throws Exception test failed
	 */
	@Test
	public abstract void create_check_type_string_description()
			throws Exception;
	
	/**
	 * Creates a type and checks its value description.
	 * @throws Exception test failed
	 */
	@Test
	public abstract void create_check_value_string_description()
			throws Exception;
}
