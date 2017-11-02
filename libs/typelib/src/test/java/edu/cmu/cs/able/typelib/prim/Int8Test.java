package edu.cmu.cs.able.typelib.prim;

import org.junit.Before;
import org.junit.Test;

import auxtestlib.DefaultTCase;

/**
 * Tests the 8-bit integer type and its values.
 */
public class Int8Test extends DefaultTCase {
	/**
	 * The primitive scope.
	 */
	private PrimitiveScope m_ps;
	
	/**
	 * The data type.
	 */
	private Int8Type m_int8;
	
	/**
	 * Prepares the test fixture.
	 */
	@Before
	public void set_up() {
		m_ps = new PrimitiveScope();
		m_int8 = m_ps.int8();
	}
	
	/**
	 * Checks the properties of the primitive type.
	 * @throws Exception test failed
	 */
	@Test
	public void properties() throws Exception {
		assertEquals(Int8Type.NAME, m_int8.name());
		assertFalse(m_int8.is_abstract());
		assertTrue(m_int8.sub_of(m_ps.any()));
	}
	
	/**
	 * Creates values and obtains data from them.
	 * @throws Exception test failed
	 */
	@Test
	public void creating_getting_data() throws Exception {
		Int8Value v1 = m_int8.make((byte) 1);
		Int8Value v2 = m_int8.make((byte) 1);
		Int8Value v3 = m_int8.make((byte) 2);
		
		assertEquals(1, (byte) v1.value());
		assertEquals(1, (byte) v2.value());
		assertEquals(2, (byte) v3.value());
	}
	
	/**
	 * Comparing values for equality.
	 * @throws Exception test failed
	 */
	@Test
	public void comparing_equality() throws Exception {
		Int8Value v1 = m_int8.make((byte) 1);
		Int8Value v2 = m_int8.make((byte) 1);
		Int8Value v3 = m_int8.make((byte) 2);
		
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
		Int8Value v = m_int8.make((byte) 86);
		
		assertTrue(v.toString().contains("86"));
	}
	
	/**
	 * Clones data values.
	 * @throws Exception test failed
	 */
	@Test
	public void cloning_values() throws Exception {
		Int8Value x = m_int8.make((byte) 3);
		Int8Value y = x.clone();
		assertEquals(x, y);
		assertNotSame(x, y);
	}
}
