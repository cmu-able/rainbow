package edu.cmu.cs.able.typelib.vtscope;

import org.junit.Before;
import org.junit.Test;

import auxtestlib.DefaultTCase;
import edu.cmu.cs.able.typelib.prim.AsciiValue;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Test case for value scopes.
 */
public class ValueScopeTest extends DefaultTCase {
	/**
	 * The primitive scope.
	 */
	private PrimitiveScope m_ps;
	
	/**
	 * The typed scope.
	 */
	private TypeScope m_ts;
	
	/**
	 * The valued scope.
	 */
	private ValueScope m_vs;
	
	/**
	 * Sets up the test fixture.
	 * @throws Exception test failed
	 */
	@Before
	public void set_up() throws Exception {
		m_ps = new PrimitiveScope();
		m_ts = new TypeScope();
		m_ts.add(new NamedType("a", m_ps.ascii()));
		
		TypeScope ts1 = new TypeScope();
		ts1.add(new NamedType("b", m_ps.string()));
		m_ts.link(ts1);
		
		TypeScope ts2 = new TypeScope();
		ts2.add(new NamedType("b", m_ps.string()));
		m_ts.link(ts2);
		
		m_vs = new ValueScope(m_ts);
	}
	
	/**
	 * Creates a valued scope, assigned values and obtains them.
	 * @throws Exception test failed
	 */
	@Test
	public void create_assign_obtain_values() throws Exception {
		m_vs.add(new NamedValue("a", m_ps.ascii().make("bar")));
		DataValue vl = m_vs.value("a");
		assertTrue(vl instanceof AsciiValue);
		AsciiValue sv = (AsciiValue) vl;
		assertEquals("bar", sv.value());
	}
	
	/**
	 * Assigns values to names without any corresponding type.
	 * @throws Exception test failed
	 */
	@Test
	public void assign_values_non_existing_types() throws Exception {
		try {
			m_vs.add(new NamedValue("foo", m_ps.ascii().make("bar")));
			fail();
		} catch (IllegalStateException e) {
			/*
			 * Expected.
			 */
		}
	}
	
	/**
	 * Assigns values to names that correspond to ambiguous types.
	 * @throws Exception test failed
	 */
	@Test
	public void assign_values_ambiguous_types() throws Exception {
		try {
			m_vs.add(new NamedValue("b", m_ps.string().make("bar")));
			fail();
		} catch (IllegalStateException | IllegalArgumentException e) {
			/*
			 * Expected.
			 */
		}
	}
	
	/**
	 * Assigns values to names with the wrong type.
	 * @throws Exception test failed
	 */
	@Test
	public void assign_values_wrong_types() throws Exception {
		try {
			m_vs.add(new NamedValue("a", m_ps.int32().make(14)));
			fail();
		} catch (IllegalStateException | IllegalArgumentException e) {
			/*
			 * Expected.
			 */
		}
	}
	
	/**
	 * Obtains the typed scope from the value scpoe.
	 * @throws Exception test failed.
	 */
	@Test
	public void obtain_typed_scope() throws Exception {
		assertEquals(m_ts, m_vs.typed_scope());
	}
	
	/**
	 * Obtains a value from the value scope which does not exist.
	 * @throws Exception test failed.
	 */
	@Test
	public void obtain_non_existing_value() throws Exception {
		assertNull(m_vs.value("h"));
	}
}
