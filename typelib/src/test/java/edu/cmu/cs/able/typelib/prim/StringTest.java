package edu.cmu.cs.able.typelib.prim;

import org.junit.Before;
import org.junit.Test;

import auxtestlib.DefaultTCase;

/**
 * Tests the string type and its values.
 */
public class StringTest extends DefaultTCase {
	/**
	 * The primitive scope.
	 */
	private PrimitiveScope m_ps;
	
	/**
	 * The data type.
	 */
	private StringType m_string;
	
	/**
	 * Prepares the test fixture.
	 */
	@Before
	public void set_up() {
		m_ps = new PrimitiveScope();
		m_string = m_ps.string();
	}
	
	/**
	 * Checks the properties of the primitive type.
	 * @throws Exception test failed
	 */
	@Test
	public void properties() throws Exception {
		assertEquals(StringType.NAME, m_string.name());
		assertFalse(m_string.is_abstract());
		assertTrue(m_string.sub_of(m_ps.any()));
	}
	
	/**
	 * Creates values and obtains data from them.
	 * @throws Exception test failed
	 */
	@Test
	public void creating_getting_data() throws Exception {
		StringValue v1 = m_string.make("A\u00ddb");
		StringValue v2 = m_string.make("foo");
		
		assertEquals("A\u00ddb", v1.value());
		assertEquals("foo", v2.value());
	}
	
	/**
	 * Creates values with invalid data.
	 * @throws Exception test failed
	 */
	@Test
	public void create_invalid_data() throws Exception {
		try {
			m_string.make(null);
			fail();
		} catch (IllegalArgumentException e) {
			/*
			 * Expected.
			 */
		}
	}
	
	/**
	 * Comparing values for equality.
	 * @throws Exception test failed
	 */
	@Test
	public void comparing_equality() throws Exception {
		StringValue v1 = m_string.make("foo");
		StringValue v2 = m_string.make("foo");
		StringValue v3 = m_string.make("bar");
		
		assertTrue(v1.equals(v2));
		assertTrue(v2.equals(v1));
		assertFalse(v2.equals(v3));
		assertFalse(v3.equals(v1));
		
		assertFalse(v1.equals((Object) null));
		assertFalse(v1.equals(15));
		assertFalse(v1.equals(m_ps.bool().make(false)));
		
		assertTrue(v1.hashCode() == v2.hashCode());
	}
	
	/**
	 * Obtains a string description from values.
	 * @throws Exception test failed
	 */
	@Test
	public void obtaining_string_description() throws Exception {
		StringValue v = m_string.make("foo");
		assertTrue(v.toString().contains("foo"));
	}
}
