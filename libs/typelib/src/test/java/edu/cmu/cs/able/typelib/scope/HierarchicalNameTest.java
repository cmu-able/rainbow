package edu.cmu.cs.able.typelib.scope;

import java.util.List;

import org.junit.Test;

import auxtestlib.DefaultTCase;

/**
 * Test suite for hierarchical names.
 */
@SuppressWarnings("javadoc")
public class HierarchicalNameTest extends DefaultTCase {
	/**
	 * Creates a long hierarchical name and extracts some parts of it.
	 * @throws Exception test failed
	 */
	@Test
	public void make_long_name_and_extract_parts() throws Exception {
		HierarchicalName hn = new HierarchicalName(false, "x", "y");
		HierarchicalName hnp = hn.push("z");
		
		HierarchicalName hn2 = new HierarchicalName(false, "x", "y", "z");
		assertFalse(hn.equals(hn2));
		assertEquals(hnp, hn2);
		assertFalse(hn.hashCode() == hn2.hashCode());
		assertEquals(hnp.hashCode(), hn2.hashCode());
		
		assertFalse(hn2.leaf());
		
		HierarchicalName hn3 = new HierarchicalName(false, "z");
		assertEquals(hn3, hn2.pop_first().pop_first());
		assertFalse(hn.leaf());
		assertTrue(hn.pop_first().leaf());
		
		assertEquals("x", hnp.peek());
		assertNull(hnp.pop_first().pop_first().pop_first());
	}
	
	/**
	 * Absolute and relative hierarchical names should compare as false
	 * @throws Exception test failed
	 */
	@Test
	public void comparison_absolute_with_relative() throws Exception {
		HierarchicalName hn1 = new HierarchicalName(true, "a");
		HierarchicalName hn2 = new HierarchicalName(false, "a");
		assertFalse(hn1.equals(hn2));
		assertFalse(hn1.hashCode() == hn2.hashCode());
		
		assertTrue(hn1.absolute());
		assertFalse(hn2.absolute());
		
		assertEquals(hn1, hn2.make_absolute());
	}
	
	/**
	 * Creating empty hierarchical names is not allowed.
	 * @throws Exception test failed
	 */
	@SuppressWarnings("unused")
	@Test(expected = AssertionError.class)
	public void create_empty_name() throws Exception {
		new HierarchicalName(false);
	}
	
	/**
	 * Cannot make an absolute name absolute.
	 * @throws Exception test failed
	 */
	@Test(expected = AssertionError.class)
	public void making_absolute_absolute() throws Exception {
		HierarchicalName hn = new HierarchicalName(true, "foo");
		hn.make_absolute();
	}
	
	/**
	 * Checks hierarchical name transformation to string.
	 * @throws Exception test failed
	 */
	@Test
	public void hierarchical_name_as_string() throws Exception {
		HierarchicalName hn1 = new HierarchicalName(true, "a");
		assertEquals("::a", hn1.toString());
		
		HierarchicalName hn2 = new HierarchicalName(true, "a", "b");
		assertEquals("::a::b", hn2.toString());
		
		HierarchicalName hn3 = new HierarchicalName(false, "a");
		assertEquals("a", hn3.toString());
		
		HierarchicalName hn4 = new HierarchicalName(false, "a", "b");
		assertEquals("a::b", hn4.toString());
	}
	
	@SuppressWarnings("unused")
	@Test(expected = AssertionError.class)
	public void cannot_create_absolute_with_null_name_list() throws Exception {
		new HierarchicalName(true, (List<String>) null);
	}
	
	@SuppressWarnings("unused")
	@Test(expected = AssertionError.class)
	public void cannot_create_relativewith_null_name_list() throws Exception {
		new HierarchicalName(false, (List<String>) null);
	}
	
	@SuppressWarnings("unused")
	@Test(expected = AssertionError.class)
	public void cannot_create_absolute_with_null_part() throws Exception {
		new HierarchicalName(true, null, "foo");
	}
	
	@SuppressWarnings("unused")
	@Test(expected = AssertionError.class)
	public void cannot_create_relative_with_null_part() throws Exception {
		new HierarchicalName(true, "foo", null);
	}
	
	/**
	 * Compares the hierarchical object to objects which are not hierarchical
	 * objects.
	 * @throws Exception test failed
	 */
	@Test
	public void compare_to_invalid() throws Exception {
		HierarchicalName hn = new HierarchicalName(false, "foo");
		assertFalse(hn.equals((Object) null));
		assertFalse(hn.equals(23));
	}
	
	@SuppressWarnings("unused")
	@Test(expected = AssertionError.class)
	public void cannot_have_absolute_empty_hierachical_names()
			throws Exception{
		new HierarchicalName(true);
	}
	
	@SuppressWarnings("unused")
	@Test(expected = AssertionError.class)
	public void cannot_have_relative_empty_hierachical_names()
			throws Exception{
		new HierarchicalName(false);
	}
	
	/**
	 * Poping an absolute hierarchical name makes a relative one.
	 * @throws Exception test failed
	 */
	@Test
	public void poping_absolute_makes_relative() throws Exception {
		HierarchicalName hn = new HierarchicalName(true, "x", "y");
		assertTrue(hn.absolute());
		
		hn = hn.pop_first();
		assertFalse(hn.absolute());
	}		
}
