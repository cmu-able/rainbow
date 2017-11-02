package edu.cmu.cs.able.typelib.comp;

import java.util.Map;

import org.junit.Test;

import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Equivalence class tests for the map data type.
 */
@SuppressWarnings("javadoc")
public class MapDataTypeEqTest extends MapDataTypeTestFixture
		implements ComplexDataTypeAbstractEqTest {
	@Test
	@Override
	public void check_data_type_properties() throws Exception {
		assertEquals("map<int32,string>", m_int32_to_string_type.name());
		assertEquals(1, m_int32_to_string_type.super_types().size());
		assertTrue(m_int32_to_string_type.super_types().contains(m_scope.any()));
		assertFalse(m_int32_to_string_type.is_abstract());
		assertSame(m_scope.int32(), m_int32_to_string_type.key_type());
		assertSame(m_scope.string(), m_int32_to_string_type.value_type());
	}

	@Test
	@Override
	public void subclasses_can_be_used_in_inner_type() throws Exception {
		m_1_to_1.put(m_vsub, m_v1_t1);
		m_1_to_1.put(m_v2_t1, m_vsub);
		
		assertEquals(2, m_1_to_1.size());
		assertTrue(m_1_to_1.contains(m_vsub));
		assertTrue(m_1_to_1.contains(m_v2_t1));
		assertEquals(m_v1_t1, m_1_to_1.get(m_vsub));
		assertEquals(m_vsub, m_1_to_1.get(m_v2_t1));
		m_1_to_1.remove(m_vsub);
		assertEquals(1, m_1_to_1.size());
	}

	@Test
	@Override
	public void compare_equal_data_values_equals_and_hash_code()
			throws Exception {
		MapDataValue x = m_1_to_1_type.make();
		assertTrue(x.equals(m_1_to_1));
		assertEquals(x.hashCode(), m_1_to_1.hashCode());
		
		m_1_to_1.put(m_v1_t1, m_v2_t1);
		x.put(m_v1_t1, m_v2_t1);
		assertTrue(x.equals(m_1_to_1));
		assertTrue(x.hashCode() == m_1_to_1.hashCode());
	}

	@Test
	@Override
	public void compare_different_data_values_equals_and_hash_code()
			throws Exception {
		MapDataValue x = m_1_to_1_type.make();
		
		m_1_to_1.put(m_v1_t1, m_v2_t1);
		assertFalse(x.equals(m_1_to_1));
		assertFalse(x.hashCode() == m_1_to_1.hashCode());
		
		x.put(m_v1_t1, m_v1_t1);
		assertFalse(x.equals(m_1_to_1));
		assertFalse(x.hashCode() == m_1_to_1.hashCode());
	}

	@Test
	@Override
	public void compare_with_complex_with_different_inner_type()
			throws Exception {
		assertFalse(m_1_to_1.equals(m_int32_to_string_type.make()));
	}

	@Test
	@Override
	public void compare_value_to_null() throws Exception {
		assertFalse(m_1_to_1.equals(null));
	}

	@Test
	@Override
	public void compare_value_to_different_java_type() throws Exception {
		assertFalse(m_1_to_1.equals(new Integer(3)));
	}

	@Test
	@Override
	public void convert_value_to_string() throws Exception {
		assertEquals("<>", m_1_to_1.toString());
		
		m_1_to_1.put(m_v1_t1, m_v2_t1);
		assertEquals("<" + m_v1_t1.toString() + "=" + m_v2_t1 + ">",
				m_1_to_1.toString());
		
		m_1_to_1.put(m_v2_t1, m_vsub);
		assertEquals("<" + m_v1_t1.toString() + "=" + m_v2_t1.toString() + ","
				+ m_v2_t1.toString() + "=" + m_vsub.toString() + ">",
				m_1_to_1.toString());
	}

	@Test
	@Override
	public void read_inner_value_from_complex_type_value() throws Exception {
		m_1_to_1.put(m_v1_t1, m_v2_t1);
		assertEquals(m_v2_t1, m_1_to_1.get(m_v1_t1));
	}

	@Test
	@Override
	public void obtaining_existing_data_type() throws Exception {
		assertSame(m_1_to_1_type, MapDataType.map_of(m_type_1, m_type_1,
				m_scope));
	}

	@Test
	@Override
	public void obtaining_new_data_type() throws Exception {
		MapDataType x = MapDataType.map_of(m_type_2, m_type_2, m_scope);
		assertNotNull(x);
		assertEquals(m_type_2, x.key_type());
		assertEquals(m_type_2, x.value_type());
		assertSame(x, MapDataType.map_of(m_type_2, m_type_2, m_scope));
	}
	
	@Test
	public void snapshotting_map() throws Exception {
		Map<DataValue, DataValue> m = m_1_to_1.all();
		assertEquals(0, m.size());
		
		m_1_to_1.put(m_v1_t1, m_v2_t1);
		m = m_1_to_1.all();
		assertEquals(1, m.size());
		assertTrue(m.containsKey(m_v1_t1));
		assertEquals(m_v2_t1, m.get(m_v1_t1));
	}
	
	@Test
	public void clearing_map() throws Exception {
		m_1_to_1.put(m_v1_t1, m_v2_t1);
		m_1_to_1.clear();
		assertEquals(0, m_1_to_1.size());
	}

	@Test
	@Override
	public void compare_to_itself() throws Exception {
		assertTrue(m_1_to_1.equals(m_1_to_1));
	}

	@Test
	@Override
	public void cloning_values() throws Exception {
		m_1_to_1.put(m_v1_t1, m_v2_t1);
		MapDataValue mv = m_1_to_1.clone();
		assertNotNull(mv);
		assertNotSame(mv, m_1_to_1);
		assertEquals(m_1_to_1, mv);
		
		Map<DataValue, DataValue> c = mv.all();
		assertEquals(1, c.size());
		assertEquals(m_v1_t1, c.keySet().iterator().next());
		assertNotSame(m_v1_t1, c.keySet().iterator().next());
		assertEquals(m_v2_t1, c.values().iterator().next());
		assertNotSame(m_v2_t1, c.values().iterator().next());
	}
}
