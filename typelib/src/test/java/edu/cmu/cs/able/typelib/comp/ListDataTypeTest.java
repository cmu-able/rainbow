package edu.cmu.cs.able.typelib.comp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import auxtestlib.DefaultTCase;
import edu.cmu.cs.able.typelib.TestDataType;
import edu.cmu.cs.able.typelib.TestDataValue;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Test case for the list data type.
 */
@SuppressWarnings("javadoc")
public class ListDataTypeTest extends DefaultTCase {
	/**
	 * Primitive data scope.
	 */
	private PrimitiveScope m_scope;
	
	/**
	 * Super data type used for testing.
	 */
	private TestDataType m_super_type;
	
	/**
	 * Set data type used for testing.
	 */
	private TestDataType m_type;
	
	/**
	 * Sub data type used for testing.
	 */
	private TestDataType m_sub_type;
	
	/**
	 * The list data type.
	 */
	private ListDataType m_list_type;
	
	/**
	 * A value used for testing.
	 */
	private TestDataValue m_v1;
	
	/**
	 * A value used for testing.
	 */
	private TestDataValue m_v2;
	
	/**
	 * A value used for testing whose value equals v2
	 */
	private TestDataValue m_v2_2;
	
	/**
	 * A value of the sub type used for testing.
	 */
	private TestDataValue m_vsub;
	
	/**
	 * A value of the super type used for testing.
	 */
	private TestDataValue m_vsuper;
	
	/**
	 * A List used for testing.
	 */
	private ListDataValue m_list;
	
	/**
	 * Sets up the test fixture.
	 */
	@Before
	public void set_up() {
		m_scope = new PrimitiveScope();
		m_super_type = new TestDataType("a", (Set<DataType>) null);
		m_scope.add(m_super_type);
		m_type = new TestDataType("b", m_super_type);
		m_scope.add(m_type);
		m_sub_type = new TestDataType("c", m_type);
		m_scope.add(m_sub_type);
		m_list_type = new ListDataType(m_type, m_scope.any());
		m_scope.add(m_list_type);
		m_v1 = new TestDataValue(m_type, 7);
		m_v2 = new TestDataValue(m_type, 9);
		m_v2_2 = new TestDataValue(m_type, 9);
		m_vsub = new TestDataValue(m_sub_type, 10);
		m_vsuper = new TestDataValue(m_super_type, -5);
		m_list = m_list_type.make();
	}
	
	/**
	 * Creates a list type and checks its properties.
	 * @throws Exception test failed
	 */
	@Test
	public void create_list_type_check_properties() throws Exception {
		assertEquals("list<b>", m_list_type.name());
		assertTrue(m_list_type.sub_of(m_scope.any()));
		assertFalse(m_list_type.is_abstract());
		assertEquals(m_type, m_list_type.inner_type());
	}
	
	/**
	 * Creates a lists and adds, gets and removes elements from the list.
	 * @throws Exception test failed
	 */
	@Test
	public void create_add_remove_check_elements() throws Exception {
		assertFalse(m_list.contains(m_v1));
		assertFalse(m_list.contains(m_v2));
		assertFalse(m_list.contains(m_v2_2));
		assertEquals(0, m_list.size());
		assertEquals(-1, m_list.index_of(m_v1));
		assertEquals(-1, m_list.index_of(m_v2));
		assertEquals(-1, m_list.index_of(m_v2_2));
		
		assertTrue(m_list.add(m_v1));
		assertEquals(m_v1, m_list.get(0));
		assertTrue(m_list.contains(m_v1));
		assertFalse(m_list.contains(m_v2));
		assertFalse(m_list.contains(m_v2_2));
		assertEquals(1, m_list.size());
		assertEquals(0, m_list.index_of(m_v1));
		assertEquals(-1, m_list.index_of(m_v2));
		assertEquals(-1, m_list.index_of(m_v2_2));
		
		assertTrue(m_list.add(m_v2));
		assertTrue(m_list.contains(m_v1));
		assertTrue(m_list.contains(m_v2));
		assertTrue(m_list.contains(m_v2_2));
		assertEquals(2, m_list.size());
		assertEquals(0, m_list.index_of(m_v1));
		assertEquals(1, m_list.index_of(m_v2));
		assertEquals(1, m_list.index_of(m_v2_2));
		
		assertTrue(m_list.add(m_v2_2));
		assertTrue(m_list.contains(m_v1));
		assertTrue(m_list.contains(m_v2));
		assertTrue(m_list.contains(m_v2_2));
		assertEquals(3, m_list.size());
		assertEquals(0, m_list.index_of(m_v1));
		assertEquals(1, m_list.index_of(m_v2));
		assertEquals(1, m_list.index_of(m_v2_2));
		
		m_list.remove(1);
		assertTrue(m_list.contains(m_v1));
		assertTrue(m_list.contains(m_v2));
		assertTrue(m_list.contains(m_v2_2));
		assertEquals(2, m_list.size());
		assertEquals(0, m_list.index_of(m_v1));
		assertEquals(1, m_list.index_of(m_v2));
		assertEquals(1, m_list.index_of(m_v2_2));
		
		m_list.remove(0);
		assertFalse(m_list.contains(m_v1));
		assertTrue(m_list.contains(m_v2));
		assertTrue(m_list.contains(m_v2_2));
		assertEquals(1, m_list.size());
		assertEquals(-1, m_list.index_of(m_v1));
		assertEquals(0, m_list.index_of(m_v2));
		assertEquals(0, m_list.index_of(m_v2_2));
	}
	
	/**
	 * Obtains all list elements.
	 * @throws Exception test failed
	 */
	@Test
	public void list_all_list_elements() throws Exception {
		assertEquals(0, m_list.all().size());
		
		assertTrue(m_list.add(m_v1));
		assertTrue(m_list.add(m_v2));
		assertTrue(m_list.add(m_v2_2));
		List<DataValue> all = m_list.all();
		assertEquals(3, all.size());
		assertTrue(all.contains(m_v1));
		assertTrue(all.contains(m_v2));
		assertTrue(all.contains(m_v2_2));
	}
	
	/**
	 * Removes all elements from the list.
	 * @throws Exception test failed
	 */
	@Test
	public void clear_all_elements() throws Exception {
		assertTrue(m_list.add(m_v1));
		assertTrue(m_list.add(m_v2));
		assertEquals(2, m_list.size());
		m_list.clear();
		assertEquals(0, m_list.size());
	}
	
	/**
	 * We can add elements of sub classes in the list.
	 * @throws Exception test failed
	 */
	@Test
	public void work_with_subclasses() throws Exception {
		assertTrue(m_list.add(m_vsub));
		assertEquals(1, m_list.size());
		assertTrue(m_list.contains(m_vsub));
	}
	
	/**
	 * Adding elements of the wrong type fails.
	 * @throws Exception test failed
	 */
	@Test
	public void add_wrong_types() throws Exception {
		try {
			m_list.add(m_scope.bool().make(true));
			fail();
		} catch (IllegalArgumentException e) {
			/*
			 * Expected.
			 */
		}
		
		try {
			m_list.add(m_vsuper);
			fail();
		} catch (IllegalArgumentException e) {
			/*
			 * Expected.
			 */
		}
	}
	
	/**
	 * Cannot create a list with invalid parameters.
	 * @throws Exception test failed
	 */
	@SuppressWarnings("unused")
	@Test
	public void create_invalid_parameters() throws Exception {
		try {
			new ListDataType(null, m_scope.any());
			fail();
		} catch (IllegalArgumentException e) {
			/*
			 * Expected.
			 */
		}
		
		try {
			new ListDataType(m_type, null);
			fail();
		} catch (AssertionError e) {
			/*
			 * Expected.
			 */
		}
	}
	
	/**
	 * Cannot add, remove, get or check existence with the wrong parameters.
	 * @throws Exception test failed
	 */
	@Test
	public void add_remove_check_invalid_parameters() throws Exception {
		try {
			m_list.add(null);
			fail();
		} catch (IllegalArgumentException e) {
			/*
			 * Expected.
			 */
		}
		
		try {
			m_list.add(m_scope.bool().make(true));
			fail();
		} catch (IllegalArgumentException e) {
			/*
			 * Expected.
			 */
		}
		
		try {
			m_list.remove(-1);
			fail();
		} catch (IllegalArgumentException e) {
			/*
			 * Expected.
			 */
		}
		
		try {
			m_list.remove(1);
			fail();
		} catch (IllegalArgumentException e) {
			/*
			 * Expected.
			 */
		}
		
		try {
			m_list.get(-1);
			fail();
		} catch (IllegalArgumentException e) {
			/*
			 * Expected.
			 */
		}
		
		try {
			m_list.get(0);
			fail();
		} catch (IllegalArgumentException e) {
			/*
			 * Expected.
			 */
		}
		
		try {
			m_list.contains(null);
			fail();
		} catch (IllegalArgumentException e) {
			/*
			 * Expected.
			 */
		}
		
		try {
			m_list.contains(m_scope.bool().make(true));
			fail();
		} catch (IllegalArgumentException e) {
			/*
			 * Expected.
			 */
		}
		
		try {
			m_list.index_of(null);
			fail();
		} catch (IllegalArgumentException e) {
			/*
			 * Expected.
			 */
		}
		
		try {
			m_list.index_of(m_scope.bool().make(true));
			fail();
		} catch (IllegalArgumentException e) {
			/*
			 * Expected.
			 */
		}
	}
	
	/**
	 * Compares lists for equality (also checks hash code).
	 * @throws Exception test failed
	 */
	@Test
	public void list_comparison() throws Exception {
		ListDataValue v = m_list_type.make();
		assertTrue(v.equals(m_list));
		assertTrue(v.hashCode() == m_list.hashCode());
		
		ListDataValue sup_v = new ListDataType(m_super_type,
				m_scope.any()).make();
		assertFalse(sup_v.equals(m_list));
		
		assertTrue(m_list.add(m_v1));
		assertFalse(v.equals(m_list));
		assertFalse(v.hashCode() == m_list.hashCode());
		
		assertTrue(m_list.add(m_v2));
		assertTrue(v.add(m_v2));
		assertFalse(v.equals(m_list));
		assertFalse(v.hashCode() == m_list.hashCode());
		
		assertTrue(v.add(m_v1));
		assertFalse(v.equals(m_list));
		assertFalse(v.hashCode() == m_list.hashCode());
		
		v.remove(0);
		v.remove(0);
		assertTrue(v.add(m_v1));
		assertTrue(v.add(m_v2));
		assertTrue(v.equals(m_list));
		assertTrue(v.hashCode() == m_list.hashCode());
		
		assertFalse(m_list.equals((Object) null));
		assertFalse(m_list.equals(3));
		assertTrue(m_list.equals(m_list));
	}
	
	/**
	 * Checks that lists are correctly converted to strings.
	 * @throws Exception test failed
	 */
	@Test
	public void list_string() throws Exception {
		assertTrue(m_list.add(m_v1));
		String str = m_list.toString();
		assertTrue(str.contains("" + m_v1.m_val));
	}
	
	@Test
	public void obtaining_existing_data_type() throws Exception {
		ListDataType t = ListDataType.list_of(m_type, m_scope);
		assertSame(m_list_type, t);
	}
	
	@Test
	public void obtaining_new_data_type() throws Exception {
		ListDataType t1 = ListDataType.list_of(m_scope.int8(), m_scope);
		assertSame(t1, ListDataType.list_of(m_scope.int8(), m_scope));
	}
	
	@Test
	public void set_collection_contents() throws Exception {
		List<DataValue> to_set = new ArrayList<>();
		to_set.add(m_v1);
		to_set.add(m_v2);
		to_set.add(m_v1);
		m_list.set_contents(to_set);
		assertEquals(3, m_list.size());
		assertEquals(m_v1, m_list.get(0));
		assertEquals(m_v2, m_list.get(1));
		assertEquals(m_v1, m_list.get(2));
	}
	
	@Test
	public void take_snapshot() throws Exception {
		m_list.add(m_v1);
		m_list.add(m_v2);
		m_list.add(m_v1);
		
		List<DataValue> snapshot = m_list.snapshot();
		assertEquals(3, snapshot.size());
		assertEquals(m_v1, snapshot.get(0));
		assertEquals(m_v2, snapshot.get(1));
		assertEquals(m_v1, snapshot.get(2));
	}
}
