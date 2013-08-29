package edu.cmu.cs.able.typelib.prim;

import org.junit.Before;
import org.junit.Test;

import auxtestlib.DefaultTCase;

/**
 * Tests the float type and its values.
 */
public class FloatTest extends DefaultTCase {
	/**
	 * The primitive scope.
	 */
	private PrimitiveScope m_ps;
	
	/**
	 * The data type.
	 */
	private FloatType m_float;
	
	/**
	 * Prepares the test fixture.
	 */
	@Before
	public void set_up() {
		m_ps = new PrimitiveScope();
		m_float = m_ps.float_type();
	}
	
	/**
	 * Checks the properties of the primitive type.
	 * @throws Exception test failed
	 */
	@Test
	public void properties() throws Exception {
		assertEquals(FloatType.NAME, m_float.name());
		assertFalse(m_float.is_abstract());
		assertTrue(m_float.sub_of(m_ps.any()));
	}
	
	/**
	 * Creates values and obtains data from them.
	 * @throws Exception test failed
	 */
	@Test
	public void creating_getting_data() throws Exception {
		FloatValue v1 = m_float.make(0.5f);
		FloatValue v2 = m_float.make(-1f);
		
		assertEquals(0.5f, v1.value(), 0f);
		assertEquals(-1f, v2.value(), 0f);
	}
	
	/**
	 * Comparing values for equality.
	 * @throws Exception test failed
	 */
	@Test
	public void comparing_equality() throws Exception {
		FloatValue v1 = m_float.make(0.5f);
		FloatValue v2 = m_float.make(0.5f);
		FloatValue v3 = m_float.make(0.25f);
		
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
		FloatValue v = m_float.make(10.125f);
		assertTrue(v.toString().contains("10.125"));
	}
	
	/**
	 * Clones data values.
	 * @throws Exception test failed
	 */
	@Test
	public void cloning_values() throws Exception {
		FloatValue x = m_float.make(0.5f);
		FloatValue y = x.clone();
		assertEquals(x, y);
		assertNotSame(x, y);
	}
}
