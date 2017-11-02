package edu.cmu.cs.able.typelib.comp;

import org.junit.Test;

/**
 * Abstract robustness tests for complex data types.
 */
public interface ComplexDataTypeAbstractRbTest {
	/**
	 * Creates a complex type with no inner type.
	 * @throws Exception test failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void create_with_null_inner_type() throws Exception;
	
	/**
	 * Creates a complex type with no super type.
	 * @throws Exception test failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void create_with_null_super_type() throws Exception;
	
	/**
	 * Creates a complex value with an inner data value which has an invalid
	 * type.
	 * @throws Exception test failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void create_instance_with_nonconforming_inner_type()
			throws Exception;
}
