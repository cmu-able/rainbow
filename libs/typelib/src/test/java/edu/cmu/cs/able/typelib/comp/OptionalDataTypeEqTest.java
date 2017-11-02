package edu.cmu.cs.able.typelib.comp;

import org.junit.Test;

/**
 * Test suite for the optional type type.
 */
@SuppressWarnings("javadoc")
public class OptionalDataTypeEqTest extends OptionalDataTypeTestFixture
		implements ComplexDataTypeAbstractEqTest {
	@Override
	@Test
	public void check_data_type_properties() throws Exception {
		assertEquals("b?", m_opt_1.name());
		assertTrue(m_opt_1.sub_of(m_opt_any));
		assertFalse(m_opt_1.is_abstract());
		assertEquals(m_type_1, m_opt_1.inner_type());
	}
	
	@Override
	@Test
	public void subclasses_can_be_used_in_inner_type() throws Exception {
		OptionalDataValue v = m_opt_1.make(m_vsub);
		assertEquals(m_vsub, v.value());
	}

	@Override
	@Test
	public void compare_equal_data_values_equals_and_hash_code()
			throws Exception {
		OptionalDataValue o1 = m_opt_1.make(m_v2_t1);
		OptionalDataValue o2 = m_opt_1.make(m_v2_2_t1);
		
		assertEquals(o1, o2);
		assertEquals(m_null_1, m_opt_1.make(null));
		assertEquals(o1.hashCode(), o2.hashCode());
	}

	@Override
	@Test
	public void compare_different_data_values_equals_and_hash_code()
			throws Exception {
		OptionalDataValue o1 = m_opt_1.make(m_v1_t1);
		OptionalDataValue o2 = m_opt_1.make(m_v2_t1);
		
		assertFalse(o1.equals(o2));
		assertFalse(o1.equals(m_null_1));
		assertFalse(m_null_1.equals(o1));
		assertFalse(o1.hashCode() == o2.hashCode());
	}

	@Override
	@Test
	public void compare_with_complex_with_different_inner_type()
			throws Exception {
		OptionalDataValue o1 = m_opt_1.make(m_v1_t1);
		OptionalDataValue o2 = m_opt_2.make(m_v1_t2);
		
		assertFalse(m_v1_t1.equals(m_v1_t2));
		assertFalse(o1.equals(o2));
	}

	@Override
	@Test
	public void compare_value_to_null() throws Exception {
		OptionalDataValue o1 = m_opt_1.make(m_v1_t1);
		assertFalse(o1.equals((Object) null));
		assertFalse(m_null_1.equals((Object) null));
	}

	@Override
	@Test
	public void compare_value_to_different_java_type() throws Exception {
		OptionalDataValue o1 = m_opt_1.make(m_v1_t1);
		assertFalse(o1.equals(3));
		assertFalse(m_null_1.equals(4));
	}

	@Override
	@Test
	public void convert_value_to_string() throws Exception {
		OptionalDataValue o1 = m_opt_1.make(m_v1_t1);
		assertEquals(m_v1_t1.toString(), o1.toString());
		
		assertEquals("null[" + m_type_1.toString() + "]", m_null_1.toString());
	}
	
	@Test
	public void create_with_value_inside() throws Exception {
		OptionalDataValue o1 = m_opt_1.make(m_v1_t1);
		assertEquals(m_v1_t1, o1.value());
	}
	
	@Test
	public void create_null_optional_value() throws Exception {
		OptionalDataValue o1 = m_opt_1.make(null);
		assertNull(o1.value());
	}
	
	@Test
	public void assignable_value_to_optional_any() throws Exception {
		OptionalDataValue o1 = m_opt_1.make(m_v1_t1);
		assertTrue(m_opt_any.is_instance(o1));
	}

	@Override
	@Test
	public void read_inner_value_from_complex_type_value() throws Exception {
		assertEquals(m_v1_t1, m_opt_1.make(m_v1_t1).value());
		assertNull(m_null_1.value());
	}
	
	@Override
	@Test
	public void obtaining_existing_data_type() throws Exception {
		OptionalDataType found = OptionalDataType.optional_of(m_type_1);
		assertNotNull(found);
		assertSame(found, m_opt_1);
	}
	
	@Override
	@Test
	public void obtaining_new_data_type() throws Exception {
		OptionalDataType found = OptionalDataType.optional_of(m_sub_type_1);
		assertNotNull(found);
		assertNotSame(found, m_opt_1);
		assertNotSame(found, m_opt_2);
		assertEquals(m_sub_type_1, found.inner_type());
		assertSame(found, OptionalDataType.optional_of(m_sub_type_1));
	}

	@Test
	@Override
	public void compare_to_itself() throws Exception {
		OptionalDataValue o1 = m_opt_1.make(m_v2_t1);
		assertTrue(o1.equals(o1));
	}
	
	@Test
	public void compare_optional_with_non_optional_same_value()
			throws Exception {
		assertFalse(m_opt_1.make(m_v1_t1).equals(m_v1_t1));
	}
	
	@Test
	public void compare_nulls_different_types() throws Exception {
		assertEquals(m_null_1, m_null_2);
		assertEquals(m_null_1.hashCode(), m_null_2.hashCode());
	}
	
	@Test
	public void optional_value_of_sub_type() throws Exception {
		OptionalDataType op_sub_1 = OptionalDataType.optional_of(m_sub_type_1);
		OptionalDataValue v = m_opt_1.make(m_vsub);
		assertNotSame(op_sub_1, m_opt_1);
		assertSame(op_sub_1, v.type());
	}

	@Override
	@Test
	public void cloning_values() throws Exception {
		OptionalDataValue ov1 = m_opt_1.make(m_v1_t1);
		OptionalDataValue ov2 = ov1.clone();
		
		assertEquals(ov1, ov2);
		assertNotSame(ov1, ov2);
		
		assertEquals(ov1.value(), ov2.value());
		assertNotSame(ov1.value(), ov2.value());
	}
	
	@Test
	public void cloning_null() throws Exception {
		OptionalDataValue null_c = m_null_1.clone();
		assertEquals(null_c, m_null_1);
	}
}
