package edu.cmu.cs.able.typelib.prim;

import org.junit.Before;
import org.junit.Test;

import auxtestlib.DefaultTCase;

/**
 * Tests the boolean type and its values.
 */
public class BooleanTest extends DefaultTCase {
	/**
	 * The primitive scope.
	 */
	private PrimitiveScope m_ps;
	
	/**
	 * The boolean type.
	 */
	private BooleanType m_bool;
	
	/**
	 * Prepares the test fixture.
	 */
	@Before
	public void set_up() {
		m_ps = new PrimitiveScope();
		m_bool = m_ps.bool();
	}
	
	/**
	 * Checks the properties of the boolean primitive type.
	 * @throws Exception test failed
	 */
	@Test
	public void boolean_properties() throws Exception {
		assertEquals(BooleanType.NAME, m_bool.name());
		assertFalse(m_bool.is_abstract());
		assertTrue(m_bool.sub_of(m_ps.any()));
	}
	
	/**
	 * Creates boolean values and obtains data from them.
	 * @throws Exception test failed
	 */
	@Test
	public void creating_getting_data() throws Exception {
		BooleanValue t1 = m_bool.make(true);
		BooleanValue t2 = m_bool.make(true);
		BooleanValue t3 = m_bool.make(false);
		
		assertTrue(t1.value());
		assertTrue(t2.value());
		assertFalse(t3.value());
	}
	
	/**
	 * Comparing boolean values for equality.
	 * @throws Exception test failed
	 */
	@Test
	public void comparing_equality() throws Exception {
		BooleanValue t1 = m_bool.make(true);
		BooleanValue t2 = m_bool.make(true);
		BooleanValue t3 = m_bool.make(false);
		
		assertTrue(t1.equals(t2));
		assertTrue(t2.equals(t1));
		assertFalse(t2.equals(t3));
		assertFalse(t3.equals(t1));
		
		assertFalse(t1.equals((Object) null));
		assertFalse(t1.equals(5));
		
		assertTrue(t1.hashCode() == t2.hashCode());
	}
	
	/**
	 * Obtains a string description from boolean values.
	 * @throws Exception test failed
	 */
	@Test
	public void obtaining_string_description() throws Exception {
		BooleanValue t1 = m_bool.make(true);
		BooleanValue t2 = m_bool.make(false);
		
		assertTrue(t1.toString().contains("true"));
		assertTrue(t2.toString().contains("false"));
	}
	
	/**
	 * Clones data values.
	 * @throws Exception test failed
	 */
	@Test
	public void cloning_values() throws Exception {
		BooleanValue x = m_bool.make(true);
		BooleanValue y = x.clone();
		assertEquals(x, y);
		assertNotSame(x, y);
	}
}
