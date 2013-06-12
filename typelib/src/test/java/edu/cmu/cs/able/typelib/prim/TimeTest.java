package edu.cmu.cs.able.typelib.prim;

import org.junit.Before;
import org.junit.Test;

import auxtestlib.DefaultTCase;

/**
 * Tests the time type and its values.
 */
public class TimeTest extends DefaultTCase {
	/**
	 * The primitive scope.
	 */
	private PrimitiveScope m_ps;
	
	/**
	 * The data type.
	 */
	private TimeType m_time;
	
	/**
	 * Prepares the test fixture.
	 */
	@Before
	public void set_up() {
		m_ps = new PrimitiveScope();
		m_time = m_ps.time();
	}
	
	/**
	 * Checks the properties of the primitive type.
	 * @throws Exception test failed
	 */
	@Test
	public void properties() throws Exception {
		assertEquals(TimeType.NAME, m_time.name());
		assertFalse(m_time.is_abstract());
		assertTrue(m_time.sub_of(m_ps.any()));
	}
	
	/**
	 * Creates values and obtains data from them.
	 * @throws Exception test failed
	 */
	@Test
	public void creating_getting_data() throws Exception {
		TimeValue v1 = m_time.make(500);
		assertEquals(500, (long) v1.value());
	}
	
	/**
	 * Creates values with invalid data.
	 * @throws Exception test failed
	 */
	@Test
	public void create_invalid_data() throws Exception {
		try {
			m_time.make(-1);
			fail();
		} catch (AssertionError e) {
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
		TimeValue v1 = m_time.make(100);
		TimeValue v2 = m_time.make(100);
		TimeValue v3 = m_time.make(500);
		
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
		TimeValue v = m_time.make(150);
		assertTrue(v.toString().contains("150"));
	}
}
