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
 * Test case for the set data type.
 */
@SuppressWarnings("javadoc")
public class SetDataTypeTest extends DefaultTCase {
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
	 * The set data type.
	 */
	private SetDataType m_set_type;
	
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
	 * A set used for testing.
	 */
	private SetDataValue m_set;
	
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
		m_set_type = new SetDataType(m_type, m_scope.any());
		m_scope.add(m_set_type);
		m_v1 = new TestDataValue(m_type, 7);
		m_v2 = new TestDataValue(m_type, 9);
		m_v2_2 = new TestDataValue(m_type, 9);
		m_vsub = new TestDataValue(m_sub_type, 10);
		m_vsuper = new TestDataValue(m_super_type, -5);
		m_set = m_set_type.make();
	}
	
	/**
	 * Creates a set type and checks its properties.
	 * @throws Exception test failed
	 */
	@Test
	public void create_set_type_check_properties() throws Exception {
		assertEquals("set<b>", m_set_type.name());
		assertTrue(m_set_type.sub_of(m_scope.any()));
		assertFalse(m_set_type.is_abstract());
		assertEquals(m_type, m_set_type.inner_type());
	}
	
	/**
	 * Creates a set and adds and removes elements from the set checking
	 * whether they are there or not.
	 * @throws Exception test failed
	 */
	@Test
	public void create_add_remove_check_elements() throws Exception {
		assertFalse(m_set.contains(m_v1));
		assertFalse(m_set.contains(m_v2));
		assertFalse(m_set.contains(m_v2_2));
		assertEquals(0, m_set.size());
		
		assertTrue(m_set.add(m_v1));
		assertTrue(m_set.contains(m_v1));
		assertFalse(m_set.contains(m_v2));
		assertFalse(m_set.contains(m_v2_2));
		assertEquals(1, m_set.size());
		
		assertTrue(m_set.add(m_v2));
		assertTrue(m_set.contains(m_v1));
		assertTrue(m_set.contains(m_v2));
		assertTrue(m_set.contains(m_v2_2));
		assertEquals(2, m_set.size());
		
		assertFalse(m_set.add(m_v2_2));
		assertTrue(m_set.contains(m_v1));
		assertTrue(m_set.contains(m_v2));
		assertTrue(m_set.contains(m_v2_2));
		assertEquals(2, m_set.size());
		
		assertTrue(m_set.remove(m_v1));
		assertFalse(m_set.contains(m_v1));
		assertTrue(m_set.contains(m_v2));
		assertTrue(m_set.contains(m_v2_2));
		assertEquals(1, m_set.size());
		
		assertFalse(m_set.remove(m_v1));
		assertFalse(m_set.contains(m_v1));
		assertTrue(m_set.contains(m_v2));
		assertTrue(m_set.contains(m_v2_2));
		assertEquals(1, m_set.size());
	}
	
	/**
	 * Obtains all set elements.
	 * @throws Exception test failed
	 */
	@Test
	public void list_all_set_elements() throws Exception {
		assertEquals(0, m_set.all().size());
		
		m_set.add(m_v1);
		m_set.add(m_v2);
		m_set.add(m_v2_2);
		Set<DataValue> all = m_set.all();
		assertEquals(2, all.size());
		assertTrue(all.contains(m_v1));
		assertTrue(all.contains(m_v2));
		assertTrue(all.contains(m_v2_2));
	}
	
	/**
	 * Removes all elements from the set.
	 * @throws Exception test failed
	 */
	@Test
	public void clear_all_elements() throws Exception {
		m_set.add(m_v1);
		m_set.add(m_v2);
		assertEquals(2, m_set.size());
		m_set.clear();
		assertEquals(0, m_set.size());
	}
	
	/**
	 * We can add elements of sub classes in the set.
	 * @throws Exception test failed
	 */
	@Test
	public void work_with_subclasses() throws Exception {
		m_set.add(m_vsub);
		assertEquals(1, m_set.size());
		assertTrue(m_set.contains(m_vsub));
	}
	
	/**
	 * Adding elements of the wrong type fails.
	 * @throws Exception test failed
	 */
	@Test
	public void add_wrong_types() throws Exception {
		try {
			m_set.add(m_scope.bool().make(true));
			fail();
		} catch (IllegalArgumentException e) {
			/*
			 * Expected.
			 */
		}
		
		try {
			m_set.add(m_vsuper);
			fail();
		} catch (IllegalArgumentException e) {
			/*
			 * Expected.
			 */
		}
	}
	
	/**
	 * Cannot create a set with invalid parameters.
	 * @throws Exception test failed
	 */
	@Test
	@SuppressWarnings("unused")
	public void create_invalid_parameters() throws Exception {
		try {
			new SetDataType(null, m_scope.any());
			fail();
		} catch (IllegalArgumentException e) {
			/*
			 * Expected.
			 */
		}
		
		try {
			new SetDataType(m_type, null);
			fail();
		} catch (AssertionError e) {
			/*
			 * Expected.
			 */
		}
	}
	
	/**
	 * Cannot add, remove or check existence with the wrong parameters.
	 * @throws Exception test failed
	 */
	@Test
	public void add_remove_check_invalid_parameters() throws Exception {
		try {
			m_set.add(null);
			fail();
		} catch (IllegalArgumentException e) {
			/*
			 * Expected.
			 */
		}
		
		try {
			m_set.add(m_scope.bool().make(true));
			fail();
		} catch (IllegalArgumentException e) {
			/*
			 * Expected.
			 */
		}
		
		try {
			m_set.remove(null);
			fail();
		} catch (IllegalArgumentException e) {
			/*
			 * Expected.
			 */
		}
		
		try {
			m_set.remove(m_scope.bool().make(true));
			fail();
		} catch (IllegalArgumentException e) {
			/*
			 * Expected.
			 */
		}
		
		try {
			m_set.contains(null);
			fail();
		} catch (IllegalArgumentException e) {
			/*
			 * Expected.
			 */
		}
		
		try {
			m_set.contains(m_scope.bool().make(true));
			fail();
		} catch (IllegalArgumentException e) {
			/*
			 * Expected.
			 */
		}
	}
	
	/**
	 * Compares sets for equality (also checks hash code).
	 * @throws Exception test failed
	 */
	@Test
	public void set_comparison() throws Exception {
		SetDataValue v = m_set_type.make();
		assertTrue(v.equals(m_set));
		assertTrue(v.hashCode() == m_set.hashCode());
		
		SetDataValue sup_v = new SetDataType(m_super_type,
				m_scope.any()).make();
		assertFalse(sup_v.equals(m_set));
		
		m_set.add(m_v1);
		assertFalse(v.equals(m_set));
		assertFalse(v.hashCode() == m_set.hashCode());
		
		m_set.add(m_v2);
		v.add(m_v2);
		assertFalse(v.equals(m_set));
		assertFalse(v.hashCode() == m_set.hashCode());
		
		v.add(m_v1);
		assertTrue(v.equals(m_set));
		assertTrue(v.hashCode() == m_set.hashCode());
		
		assertFalse(m_set.equals((Object) null));
		assertFalse(m_set.equals(3));
		assertTrue(m_set.equals(m_set));
	}
	
	/**
	 * Checks that sets are correctly converted to strings.
	 * @throws Exception test failed
	 */
	@Test
	public void set_string() throws Exception {
		m_set.add(m_v1);
		String str = m_set.toString();
		assertTrue(str.contains("" + m_v1.m_val));
	}
	
	@Test
	public void obtaining_existing_data_type() throws Exception {
		SetDataType t = SetDataType.set_of(m_type, m_scope);
		assertSame(m_set_type, t);
	}
	
	@Test
	public void obtaining_new_data_type() throws Exception {
		SetDataType t1 = SetDataType.set_of(m_scope.int8(), m_scope);
		assertSame(t1, SetDataType.set_of(m_scope.int8(), m_scope));
	}
	
	@Test
	public void set_collection_contents() throws Exception {
		List<DataValue> to_set = new ArrayList<>();
		to_set.add(m_v1);
		to_set.add(m_v2);
		to_set.add(m_v1);
		m_set.set_contents(to_set);
		assertEquals(2, m_set.size());
		assertTrue(m_set.contains(m_v1));
		assertTrue(m_set.contains(m_v2));
	}
	
	@Test
	public void take_snapshot() throws Exception {
		m_set.add(m_v1);
		m_set.add(m_v2);
		m_set.add(m_v1);
		
		List<DataValue> snapshot = m_set.snapshot();
		assertEquals(2, snapshot.size());
		assertTrue(snapshot.contains(m_v1));
		assertTrue(snapshot.contains(m_v2));
	}
}
