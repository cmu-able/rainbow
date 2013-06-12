package edu.cmu.cs.able.typelib.prim;

import org.junit.Before;
import org.junit.Test;

import edu.cmu.cs.able.typelib.type.AbstractDataTypeTestCase;

/**
 * Tests the type type and its values.
 */
public class TypeTest extends AbstractDataTypeTestCase {
	/**
	 * The primitive scope.
	 */
	private PrimitiveScope m_ps;
	
	/**
	 * The boolean type.
	 */
	private TypeType m_type;
	
	/**
	 * Prepares the test fixture.
	 */
	@Before
	public void set_up() {
		m_ps = new PrimitiveScope();
		m_type = m_ps.type();
	}

	@Override
	@Test
	public void create_check_is_abstract() throws Exception {
		assertFalse(m_type.is_abstract());
	}

	@Override
	@Test
	public void create_check_super_types() throws Exception {
		assertEquals(1, m_type.super_types().size());
		assertTrue(m_type.super_types().contains(m_ps.any()));
	}

	@Override
	@Test
	public void create_values() throws Exception {
		TypeValue v = m_type.make(m_ps.bool());
		assertSame(m_ps.bool(), v.value());
	}

	@Override
	@Test
	public void compare_equals_and_hash_code() throws Exception {
		TypeValue v1 = m_type.make(m_ps.bool());
		TypeValue v2 = m_type.make(m_ps.bool());
		TypeValue v3 = m_type.make(m_ps.int8());
		
		assertTrue(v1.equals(v2));
		assertTrue(!v1.equals(v3));
		assertTrue(v1.hashCode() == v2.hashCode());
		assertTrue(v1.hashCode() != v3.hashCode());
	}

	@Override
	@Test
	public void create_check_type_string_description() throws Exception {
		assertEquals(TypeType.NAME, m_type.name());
	}

	@Override
	@Test
	public void create_check_value_string_description() throws Exception {
		TypeValue v = m_type.make(m_ps.bool());
		assertTrue(v.toString().contains(m_ps.bool().name()));
	}
}
