package edu.cmu.cs.able.typelib.prim;

import org.junit.Before;
import org.junit.Test;

import auxtestlib.DefaultTCase;

/**
 * Tests the 64-bit integer type and its values.
 */
public class Int64Test extends DefaultTCase {
	/**
	 * The primitive scope.
	 */
	private PrimitiveScope m_ps;
	
	/**
	 * The data type.
	 */
	private Int64Type m_int64;
	
	/**
	 * Prepares the test fixture.
	 */
	@Before
	public void set_up() {
		m_ps = new PrimitiveScope();
		m_int64 = m_ps.int64();
	}
	
	/**
	 * Checks the properties of the primitive type.
	 * @throws Exception test failed
	 */
	@Test
	public void properties() throws Exception {
		assertEquals(Int64Type.NAME, m_int64.name());
		assertFalse(m_int64.is_abstract());
		assertTrue(m_int64.sub_of(m_ps.any()));
	}
	
	/**
	 * Creates values and obtains data from them.
	 * @throws Exception test failed
	 */
	@Test
	public void creating_getting_data() throws Exception {
		Int64Value v1 = m_int64.make(1);
		Int64Value v2 = m_int64.make(1);
		Int64Value v3 = m_int64.make(2);
		
		assertEquals(1, (long) v1.value());
		assertEquals(1, (long) v2.value());
		assertEquals(2, (long) v3.value());
	}
	
	/**
	 * Comparing values for equality.
	 * @throws Exception test failed
	 */
	@Test
	public void comparing_equality() throws Exception {
		Int64Value v1 = m_int64.make(1);
		Int64Value v2 = m_int64.make(1);
		Int64Value v3 = m_int64.make(2);
		
		assertTrue(v1.equals(v2));
		assertTrue(v2.equals(v1));
		assertFalse(v2.equals(v3));
		assertFalse(v3.equals(v1));
		
		assertFalse(v1.equals((Object) null));
		assertFalse(v1.equals("foo"));
		assertFalse(v1.equals(m_ps.bool().make(false)));
		
		assertTrue(v1.hashCode() == v2.hashCode());
	}
	
	/**
	 * Obtains a string description from values.
	 * @throws Exception test failed
	 */
	@Test
	public void obtaining_string_description() throws Exception {
		Int64Value v = m_int64.make(86);
		
		assertTrue(v.toString().contains("86"));
	}
}
