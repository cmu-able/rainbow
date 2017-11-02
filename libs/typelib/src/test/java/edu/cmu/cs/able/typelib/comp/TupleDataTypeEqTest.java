package edu.cmu.cs.able.typelib.comp;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import edu.cmu.cs.able.typelib.TestDataType;
import edu.cmu.cs.able.typelib.TestDataValue;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataTypeScope;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Equivalence class tests for the tuple data type.
 */
@SuppressWarnings("javadoc")
public class TupleDataTypeEqTest extends TupleDataTypeTestFixture
		implements ComplexDataTypeAbstractEqTest {

	@Test
	@Override
	public void check_data_type_properties() throws Exception {
		assertFalse(m_test_tuple_type.is_abstract());
		assertEquals("tuple<" + m_type_1.name() + ">",
				m_test_tuple_type.name());
		assertEquals(m_scope, m_test_tuple_type.parent());
		assertEquals(m_scope, m_test_tuple_type.parent_dts());
		assertTrue(m_test_tuple_type.sub_of(m_scope.any()));
		List<DataType> inners = m_test_tuple_type.inner_types();
		assertEquals(1, inners.size());
		assertEquals(m_type_1, inners.get(0));
		
		assertFalse(m_test_112_tuple_type.is_abstract());
		assertEquals("tuple<" + m_type_1.name() + "," + m_type_1.name()
				+ "," + m_type_2.name() + ">", m_test_112_tuple_type.name());
		assertEquals(m_scope, m_test_112_tuple_type.parent());
		assertEquals(m_scope, m_test_112_tuple_type.parent_dts());
		assertTrue(m_test_112_tuple_type.sub_of(m_scope.any()));
		inners = m_test_112_tuple_type.inner_types();
		assertEquals(3, inners.size());
		assertEquals(m_type_1, inners.get(0));
		assertEquals(m_type_1, inners.get(1));
		assertEquals(m_type_2, inners.get(2));
	}

	@Test
	@Override
	public void subclasses_can_be_used_in_inner_type() throws Exception {
		m_test_tuple_type.make(Arrays.asList((DataValue) m_vsub));
	}

	@Test
	@Override
	public void compare_equal_data_values_equals_and_hash_code()
			throws Exception {
		TupleDataValue tv1, tv2;
		tv1 = m_test_tuple_type.make(Arrays.asList((DataValue) m_v2_t1));
		tv2 = m_test_tuple_type.make(Arrays.asList((DataValue) m_v2_2_t1));
		assertNotNull(tv1);
		assertNotNull(tv2);
		assertEquals(tv1, tv2);
		assertEquals(tv1.hashCode(), tv2.hashCode());
	}

	@Test
	@Override
	public void compare_different_data_values_equals_and_hash_code()
			throws Exception {
		TupleDataValue tv2;
		tv2 = m_test_tuple_type.make(Arrays.asList((DataValue) m_v2_t1));
		assertNotNull(m_tv1);
		assertNotNull(tv2);
		assertFalse(m_tv1.equals(tv2));
		assertFalse(m_tv1.hashCode() == tv2.hashCode());
	}

	@Test
	@Override
	public void compare_with_complex_with_different_inner_type()
			throws Exception {
		TupleDataValue tv2;
		tv2 = m_test_112_tuple_type.make(Arrays.asList((DataValue) m_v1_t1,
				m_v2_t1, m_v1_t2));
		assertNotNull(m_tv1);
		assertNotNull(tv2);
		assertFalse(m_tv1.equals(tv2));
	}

	@Test
	@Override
	public void compare_value_to_null() throws Exception {
		assertFalse(m_tv1.equals(null));
	}

	@Test
	@Override
	public void compare_value_to_different_java_type() throws Exception {
		assertFalse(m_tv1.equals("foo"));
	}

	@Test
	@Override
	public void convert_value_to_string() throws Exception {
		String s = m_tv1.toString();
		assertEquals("<" + m_v1_t1.toString() + ">", s);
	}

	@Test
	@Override
	public void read_inner_value_from_complex_type_value() throws Exception {
		List<DataValue> lv = m_tv1.data();
		assertEquals(1, lv.size());
		assertEquals(m_v1_t1, lv.get(0));
	}
	
	@Test
	@Override
	public void obtaining_existing_data_type() throws Exception {
		TupleDataType found = TupleDataType.tuple_of(Arrays.asList(
				(DataType) m_type_1), m_scope);
		assertNotNull(found);
		assertSame(found, m_test_tuple_type);
		
		found = TupleDataType.tuple_of(Arrays.asList(
				(DataType) m_type_1, m_type_1, m_type_2), m_scope);
		assertNotNull(found);
		assertSame(found, m_test_112_tuple_type);
	}
	
	@Test
	@Override
	public void obtaining_new_data_type() throws Exception {
		TupleDataType found = TupleDataType.tuple_of(Arrays.asList(
				(DataType) m_type_2, m_type_2), m_scope);
		assertNotNull(found);
		assertSame(found, TupleDataType.tuple_of(Arrays.asList(
				(DataType) m_type_2, m_type_2), m_scope));
	}
	
	@Test
	public void obtaining_new_data_type_from_types_in_multiple_scopes()
			throws Exception {
		DataTypeScope ss = new DataTypeScope("sub_scope");
		m_scope.add(ss);
		TestDataType type_in_sub_scope = new TestDataType("in_ss");
		ss.add(type_in_sub_scope);
		
		TestDataValue ss_v = new TestDataValue(type_in_sub_scope, 0);
		
		TupleDataType t = TupleDataType.tuple_of(Arrays.asList(
				(DataType) type_in_sub_scope, m_type_1), m_scope);
		assertNotNull(t);
		
		assertNotNull(t.make(Arrays.asList((DataValue) ss_v, m_v1_t1)));
	}

	@Test
	@Override
	public void compare_to_itself() throws Exception {
		assertTrue(m_tv1.equals(m_tv1));
	}

	@Test
	@Override
	public void cloning_values() throws Exception {
		TupleDataValue tv = m_tv1.clone();
		assertNotNull(tv);
		assertNotSame(tv, m_tv1);
		assertEquals(m_tv1, tv);
		
		List<DataValue> c = tv.data();
		assertEquals(1, c.size());
		assertEquals(m_v1_t1, c.get(0));
		assertNotSame(m_v1_t1, c.get(0));
	}
}
