package edu.cmu.cs.able.typelib.comp;

import org.junit.Test;

/**
 * Structure of equivalence class tests for complex data types.
 */
public interface ComplexDataTypeAbstractEqTest {
	/**
	 * Checks if the information the data type provides about itself is
	 * correct. Type typically includes checking: the name, the super
	 * class, whether it is abstract and its inner type.
	 * @throws Exception test failed
	 */
	@Test
	void check_data_type_properties() throws Exception;
	
	/**
	 * Values of sub types of the data type can be used with the data
	 * type.
	 * @throws Exception test failed
	 */
	@Test
	void subclasses_can_be_used_in_inner_type() throws Exception;
	
	/**
	 * Compares equal data values of the same data type.
	 * @throws Exception test failed
	 */
	@Test
	void compare_equal_data_values_equals_and_hash_code() throws Exception;
	
	/**
	 * Compares a value to itself.
	 * @throws Exception test failed
	 */
	@Test
	void compare_to_itself() throws Exception;
	
	/**
	 * Compares different data values of the same data type.
	 * @throws Exception test failed
	 */
	@Test
	void compare_different_data_values_equals_and_hash_code() throws Exception;
	
	/**
	 * Compares a data value to another data value of the same complex
	 * data type but that has a different inner data type.
	 * @throws Exception test failed
	 */
	@Test
	void compare_with_complex_with_different_inner_type() throws Exception;
	
	/**
	 * Compares a data value to <code>null</code>.
	 * @throws Exception test failed
	 */
	@Test
	void compare_value_to_null() throws Exception;
	
	/**
	 * Compares a data value to a different java type.
	 * @throws Exception test failed
	 */
	@Test
	void compare_value_to_different_java_type() throws Exception;
	
	/**
	 * Converts the data value to a string. 
	 * @throws Exception test failed
	 */
	@Test
	void convert_value_to_string() throws Exception;
	
	/**
	 * Reads the inner value in the complex type.
	 * @throws Exception test failed
	 */
	@Test
	void read_inner_value_from_complex_type_value() throws Exception;
	
	/**
	 * Finds an already existing complex data type given its sub data types.
	 * @throws Exception test failed
	 */
	@Test
	public void obtaining_existing_data_type() throws Exception;
	
	/**
	 * Creates a complex data type given its sub data types. After creating the
	 * data type, finding the complex data type finds the created one.
	 * @throws Exception test failed
	 */
	@Test
	public void obtaining_new_data_type() throws Exception;
	
	/**
	 * Clones data values.
	 * @throws Exception test failed
	 */
	@Test
	public void cloning_values() throws Exception;
}
