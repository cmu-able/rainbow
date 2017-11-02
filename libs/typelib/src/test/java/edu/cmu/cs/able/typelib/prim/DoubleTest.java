package edu.cmu.cs.able.typelib.prim;

import org.junit.Before;
import org.junit.Test;

import auxtestlib.DefaultTCase;

/**
 * Tests the double type and its values.
 */
public class DoubleTest extends DefaultTCase {
	/**
	 * The primitive scope.
	 */
	private PrimitiveScope m_ps;
	
	/**
	 * The data type.
	 */
	private DoubleType m_double;
	
	/**
	 * Prepares the test fixture.
	 */
	@Before
	public void set_up() {
		m_ps = new PrimitiveScope();
		m_double = m_ps.double_type();
	}
	
	/**
	 * Checks the properties of the primitive type.
	 * @throws Exception test failed
	 */
	@Test
	public void properties() throws Exception {
		assertEquals(DoubleType.NAME, m_double.name());
		assertFalse(m_double.is_abstract());
		assertTrue(m_double.sub_of(m_ps.any()));
	}
	
	/**
	 * Creates values and obtains data from them.
	 * @throws Exception test failed
	 */
	@Test
	public void creating_getting_data() throws Exception {
		DoubleValue v1 = m_double.make(0.5);
		DoubleValue v2 = m_double.make(-1);
		
		assertEquals(0.5, v1.value(), 0.0);
		assertEquals(-1, v2.value(), 0.0);
	}
	
	/**
	 * Comparing values for equality.
	 * @throws Exception test failed
	 */
	@Test
	public void comparing_equality() throws Exception {
		DoubleValue v1 = m_double.make(0.5);
		DoubleValue v2 = m_double.make(0.5);
		DoubleValue v3 = m_double.make(0.25);
		
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
		DoubleValue v = m_double.make(10.125);
		assertTrue(v.toString().contains("10.125"));
	}
	
	/**
	 * Clones data values.
	 * @throws Exception test failed
	 */
	@Test
	public void cloning_values() throws Exception {
		DoubleValue x = m_double.make(0.5);
		DoubleValue y = x.clone();
		assertEquals(x, y);
		assertNotSame(x, y);
	}
}
