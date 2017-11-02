package edu.cmu.cs.able.typelib.comp;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

/**
 * Robustness tests for the optional data type.
 */
public class OptionalDataTypeRbTest extends OptionalDataTypeTestFixture
		implements ComplexDataTypeAbstractRbTest {
	@Override
	@Test(expected = IllegalArgumentException.class)
	@SuppressWarnings("unused")
	public void create_with_null_inner_type() throws Exception {
		new OptionalDataType(null, new HashSet<>(
				Arrays.asList(m_opt_any)));
	}

	@Override
	@Test(expected = AssertionError.class)
	@SuppressWarnings("unused")
	public void create_with_null_super_type() throws Exception {
		new OptionalDataType(m_type_1, null);
	}

	@Override
	@Test(expected = IllegalArgumentException.class)
	public void create_instance_with_nonconforming_inner_type()
			throws Exception {
		m_opt_1.make(m_v2_t2);
	}
}
