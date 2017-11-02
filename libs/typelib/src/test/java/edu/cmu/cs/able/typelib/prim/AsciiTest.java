package edu.cmu.cs.able.typelib.prim;

import org.junit.Before;
import org.junit.Test;

import auxtestlib.DefaultTCase;

/**
 * Tests the ascii type and its values.
 */
public class AsciiTest extends DefaultTCase {
	/**
	 * The primitive scope.
	 */
	private PrimitiveScope m_ps;
	
	/**
	 * The data type.
	 */
	private AsciiType m_ascii;
	
	/**
	 * Prepares the test fixture.
	 */
	@Before
	public void set_up() {
		m_ps = new PrimitiveScope();
		m_ascii = m_ps.ascii();
	}
	
	/**
	 * Checks the properties of the primitive type.
	 * @throws Exception test failed
	 */
	@Test
	public void properties() throws Exception {
		assertEquals(AsciiType.NAME, m_ascii.name());
		assertFalse(m_ascii.is_abstract());
		assertTrue(m_ascii.sub_of(m_ps.any()));
	}
	
	/**
	 * Creates values and obtains data from them.
	 * @throws Exception test failed
	 */
	@Test
	public void creating_getting_data() throws Exception {
		AsciiValue v1 = m_ascii.make("Afb");
		AsciiValue v2 = m_ascii.make("foo");
		
		assertEquals("Afb", v1.value());
		assertEquals("foo", v2.value());
	}
	
	/**
	 * Creates values with invalid data.
	 * @throws Exception test failed
	 */
	@Test
	public void create_invalid_data() throws Exception {
		try {
			m_ascii.make(null);
			fail();
		} catch (IllegalArgumentException e) {
			/*
			 * Expected.
			 */
		}
		
		try {
			m_ascii.make("A\u00ddx");
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
		AsciiValue v1 = m_ascii.make("foo");
		AsciiValue v2 = m_ascii.make("foo");
		AsciiValue v3 = m_ascii.make("bar");
		
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
		AsciiValue v = m_ascii.make("foo");
		assertTrue(v.toString().contains("foo"));
	}
	
	/**
	 * Clones data values.
	 * @throws Exception test failed
	 */
	@Test
	public void cloning_values() throws Exception {
		AsciiValue x = m_ascii.make("foo");
		AsciiValue y = x.clone();
		assertEquals(x, y);
		assertNotSame(x, y);
	}
}
