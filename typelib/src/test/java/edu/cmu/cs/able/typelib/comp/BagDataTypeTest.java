package edu.cmu.cs.able.typelib.comp;

import java.util.ArrayList;
import java.util.Collection;
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
 * Test case for the bag data type.
 */
@SuppressWarnings("javadoc")
public class BagDataTypeTest extends DefaultTCase {
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
	 * The bag data type.
	 */
	private BagDataType m_bag_type;
	
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
	 * A bag used for testing.
	 */
	private BagDataValue m_bag;
	
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
		m_bag_type = new BagDataType(m_type, m_scope.any());
		m_scope.add(m_bag_type);
		m_v1 = new TestDataValue(m_type, 7);
		m_v2 = new TestDataValue(m_type, 9);
		m_v2_2 = new TestDataValue(m_type, 9);
		m_vsub = new TestDataValue(m_sub_type, 10);
		m_vsuper = new TestDataValue(m_super_type, -5);
		m_bag = m_bag_type.make();
	}
	
	/**
	 * Creates a bag type and checks its properties.
	 * @throws Exception test failed
	 */
	@Test
	public void create_bag_type_check_properties() throws Exception {
		assertEquals("bag<b>", m_bag_type.name());
		assertTrue(m_bag_type.sub_of(m_scope.any()));
		assertFalse(m_bag_type.is_abstract());
		assertEquals(m_type, m_bag_type.inner_type());
	}
	
	/**
	 * Creates a bag and adds and removes elements from the bag checking
	 * whether they are there or not.
	 * @throws Exception test failed
	 */
	@Test
	public void create_add_remove_check_elements() throws Exception {
		assertFalse(m_bag.contains(m_v1));
		assertFalse(m_bag.contains(m_v2));
		assertFalse(m_bag.contains(m_v2_2));
		assertEquals(0, m_bag.size());
		assertEquals(0, m_bag.count(m_v1));
		assertEquals(0, m_bag.count(m_v2));
		
		assertTrue(m_bag.add(m_v1));
		assertTrue(m_bag.contains(m_v1));
		assertFalse(m_bag.contains(m_v2));
		assertFalse(m_bag.contains(m_v2_2));
		assertEquals(1, m_bag.size());
		assertEquals(1, m_bag.count(m_v1));
		assertEquals(0, m_bag.count(m_v2));
		
		assertTrue(m_bag.add(m_v2));
		assertTrue(m_bag.contains(m_v1));
		assertTrue(m_bag.contains(m_v2));
		assertTrue(m_bag.contains(m_v2_2));
		assertEquals(2, m_bag.size());
		assertEquals(1, m_bag.count(m_v1));
		assertEquals(1, m_bag.count(m_v2));
		
		assertTrue(m_bag.add(m_v2_2));
		assertTrue(m_bag.contains(m_v1));
		assertTrue(m_bag.contains(m_v2));
		assertTrue(m_bag.contains(m_v2_2));
		assertEquals(3, m_bag.size());
		assertEquals(1, m_bag.count(m_v1));
		assertEquals(2, m_bag.count(m_v2));
		
		assertTrue(m_bag.remove(m_v1));
		assertFalse(m_bag.contains(m_v1));
		assertTrue(m_bag.contains(m_v2));
		assertTrue(m_bag.contains(m_v2_2));
		assertEquals(2, m_bag.size());
		assertEquals(0, m_bag.count(m_v1));
		assertEquals(2, m_bag.count(m_v2));
		
		assertFalse(m_bag.remove(m_v1));
		assertFalse(m_bag.contains(m_v1));
		assertTrue(m_bag.contains(m_v2));
		assertTrue(m_bag.contains(m_v2_2));
		assertEquals(2, m_bag.size());
		assertEquals(0, m_bag.count(m_v1));
		assertEquals(2, m_bag.count(m_v2));
		
		assertTrue(m_bag.remove(m_v2));
		assertFalse(m_bag.contains(m_v1));
		assertTrue(m_bag.contains(m_v2));
		assertTrue(m_bag.contains(m_v2_2));
		assertEquals(1, m_bag.size());
		assertEquals(0, m_bag.count(m_v1));
		assertEquals(1, m_bag.count(m_v2));
	}
	
	/**
	 * Obtains all bag elements.
	 * @throws Exception test failed
	 */
	@Test
	public void list_all_bag_elements() throws Exception {
		assertEquals(0, m_bag.all().size());
		
		assertTrue(m_bag.add(m_v1));
		assertTrue(m_bag.add(m_v2));
		assertTrue(m_bag.add(m_v2_2));
		Collection<DataValue> all = m_bag.all();
		assertEquals(3, all.size());
		assertTrue(all.contains(m_v1));
		assertTrue(all.contains(m_v2));
		assertTrue(all.contains(m_v2_2));
	}
	
	/**
	 * Removes all elements from the bag.
	 * @throws Exception test failed
	 */
	@Test
	public void clear_all_elements() throws Exception {
		assertTrue(m_bag.add(m_v1));
		assertTrue(m_bag.add(m_v2));
		assertEquals(2, m_bag.size());
		m_bag.clear();
		assertEquals(0, m_bag.size());
	}
	
	/**
	 * We can add elements of sub classes in the bag.
	 * @throws Exception test failed
	 */
	@Test
	public void work_with_subclasses() throws Exception {
		assertTrue(m_bag.add(m_vsub));
		assertEquals(1, m_bag.size());
		assertTrue(m_bag.contains(m_vsub));
	}
	
	/**
	 * Adding elements of the wrong type fails.
	 * @throws Exception test failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void add_wrong_types() throws Exception {
		assertTrue(m_bag.add(m_scope.bool().make(true)));
	}
	
	/**
	 * Adding a super type of a the bag type fails.
	 * @throws Exception test failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void add_super_type_fails() throws Exception {
		assertTrue(m_bag.add(m_vsuper));
	}
	
	/**
	 * Cannot create a bag without inner type.
	 * @throws Exception test failed
	 */
	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void create_without_inner_type() throws Exception {
		new BagDataType(null, m_scope.any());
	}
	
	/**
	 * Cannot create a bag without super type.
	 * @throws Exception test failed
	 */
	@SuppressWarnings("unused")
	@Test(expected = AssertionError.class)
	public void create_without_super_type() throws Exception {
		new BagDataType(m_type, null);
	}
	
	/**
	 * Cannot add <code>null</code> to a bag.
	 * @throws Exception test failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void add_null_fails() throws Exception {
		m_bag.add(null);
	}
	
	
	/**
	 * Cannot remove <code>null</code> from a bag.
	 * @throws Exception test failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void remove_null_fails() throws Exception {
		m_bag.remove(null);
	}
	
	/**
	 * Cannot remove an object with the wrong type from the bag.
	 * @throws Exception test failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void remove_wrong_type_fails() throws Exception {
		m_bag.remove(m_scope.bool().make(true));
	}
	
	/**
	 * Cannot check whether a bag contains null.
	 * @throws Exception test failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void contains_null_fails() throws Exception {
		m_bag.contains(null);
	}
	
	/**
	 * Cannot check whether an object of the wrong type null.
	 * @throws Exception test failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void contains_wrong_type_fails() throws Exception {
		m_bag.contains(m_scope.bool().make(true));
	}
	
	/**
	 * Cannot count null.
	 * @throws Exception test failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void count_null_fails() throws Exception {
		m_bag.count(null);
	}
	
	/**
	 * Cannot count an object of the wrong type
	 * @throws Exception test failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void count_wrong_type_fails() throws Exception {
		m_bag.count(m_scope.bool().make(true));
	}
	
	/**
	 * Compares bags for equality (also checks hash code).
	 * @throws Exception test failed
	 */
	@Test
	public void bag_comparison() throws Exception {
		BagDataValue v = m_bag_type.make();
		assertTrue(v.equals(m_bag));
		assertTrue(v.hashCode() == m_bag.hashCode());
		
		BagDataValue sup_v = new BagDataType(m_super_type,
				m_scope.any()).make();
		assertFalse(sup_v.equals(m_bag));
		
		assertTrue(m_bag.add(m_v1));
		assertFalse(v.equals(m_bag));
		assertFalse(v.hashCode() == m_bag.hashCode());
		
		assertTrue(m_bag.add(m_v2));
		v.add(m_v2);
		assertFalse(v.equals(m_bag));
		assertFalse(v.hashCode() == m_bag.hashCode());
		
		assertTrue(v.add(m_v1));
		assertTrue(v.equals(m_bag));
		assertTrue(v.hashCode() == m_bag.hashCode());
		
		assertTrue(v.add(m_v1));
		assertFalse(v.equals(m_bag));
		assertFalse(v.hashCode() == m_bag.hashCode());
		
		assertTrue(m_bag.add(m_v1));
		assertTrue(v.equals(m_bag));
		assertTrue(v.hashCode() == m_bag.hashCode());
		
		assertFalse(m_bag.equals((Object) null));
		assertFalse(m_bag.equals(3));
		assertTrue(m_bag.equals(m_bag));
	}
	
	/**
	 * Checks that bags are correctly converted to strings.
	 * @throws Exception test failed
	 */
	@Test
	public void set_string() throws Exception {
		assertTrue(m_bag.add(m_v1));
		String str = m_bag.toString();
		assertTrue(str.contains("" + m_v1.m_val));
	}
	
	@Test
	public void obtaining_existing_data_type() throws Exception {
		BagDataType t = BagDataType.bag_of(m_type, m_scope);
		assertSame(m_bag_type, t);
	}
	
	@Test
	public void obtaining_new_data_type() throws Exception {
		BagDataType t1 = BagDataType.bag_of(m_scope.int8(), m_scope);
		assertSame(t1, BagDataType.bag_of(m_scope.int8(), m_scope));
	}
	
	@Test
	public void set_collection_contents() throws Exception {
		List<DataValue> to_set = new ArrayList<>();
		to_set.add(m_v1);
		to_set.add(m_v2);
		to_set.add(m_v1);
		m_bag.set_contents(to_set);
		assertEquals(3, m_bag.size());
		assertEquals(2, m_bag.count(m_v1));
		assertEquals(1, m_bag.count(m_v2));
	}
	
	@Test
	public void take_snapshot() throws Exception {
		m_bag.add(m_v1);
		m_bag.add(m_v2);
		m_bag.add(m_v1);
		
		List<DataValue> snapshot = m_bag.snapshot();
		assertEquals(3, snapshot.size());
		assertTrue(snapshot.contains(m_v1));
		assertTrue(snapshot.contains(m_v2));
		int idx = snapshot.indexOf(m_v1);
		int idx_2 = snapshot.lastIndexOf(m_v1);
		assertTrue(idx != idx_2);
	}
}
