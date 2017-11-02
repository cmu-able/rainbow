package edu.cmu.cs.able.typelib.comp;

import org.junit.Test;

/**
 * Robustness tests for the map data type.
 */
@SuppressWarnings("javadoc")
public class MapDataTypeRbTest extends MapDataTypeTestFixture
		implements ComplexDataTypeAbstractRbTest {
	@Test(expected = AssertionError.class)
	@SuppressWarnings("unused")
	@Override
	public void create_with_null_inner_type() throws Exception {
		new MapDataType(null, m_type_1, m_scope.any());
	}
	
	@SuppressWarnings("unused")
	@Test(expected = AssertionError.class)
	public void create_with_null_value_inner_type() throws Exception {
		new MapDataType(m_type_1, null, m_scope.any());
	}

	@Test(expected = AssertionError.class)
	@Override
	@SuppressWarnings("unused")
	public void create_with_null_super_type() throws Exception {
		new MapDataType(m_type_2, m_type_1, null);
	}

	@Test(expected = AssertionError.class)
	@Override
	public void create_instance_with_nonconforming_inner_type()
			throws Exception {
		m_1_to_1.put(m_v1_t2, m_v1_t1);
	}
}
