package edu.cmu.cs.able.typelib.enumeration;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import auxtestlib.DefaultTCase;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;

/**
 * Checks creating and accessing enumerations and its values.
 */
@SuppressWarnings("javadoc")
public class EnumerationTypeTest extends DefaultTCase {
	/**
	 * The primitive scope.
	 */
	private PrimitiveScope m_pscope;
	
	@Before
	public void set_up() {
		m_pscope = new PrimitiveScope();
	}
	
	/**
	 * Creates a set of strings.
	 * @param strings the strings in the set
	 * @return the set
	 */
	private Set<String> make_set(String...strings) {
		Set<String> s = new HashSet<>();
		for (String str : strings) {
			s.add(str);
		}
		
		return s;
	}
	
	@Test
	public void enumeration_is_not_abstract() {
		EnumerationType t = EnumerationType.make("foo", make_set("x", "y"),
				m_pscope.any());
		assertFalse(t.is_abstract());
	}
	
	@Test
	public void create_enumeration_and_get_values() {
		EnumerationType t = EnumerationType.make("foo", make_set("x", "y"),
				m_pscope.any());
		assertEquals("foo", t.name());
		assertEquals(2, t.values().size());
		Iterator<EnumerationValue> it = t.values().iterator();
		EnumerationValue v1 = it.next();
		EnumerationValue v2 = it.next();
		
		if (v1.name().equals("x")) {
			assertEquals("y", v2.name());
		} else {
			assertEquals("y", v1.name());
			assertEquals("x", v2.name());
		}
	}
	
	@Test
	public void has_valid_value() throws Exception {
		EnumerationType t = EnumerationType.make("x", make_set("y", "z"),
				m_pscope.any());
		assertTrue(t.has_value("y"));
		assertTrue(t.has_value("z"));
	}
	
	@Test
	public void has_invalid_value() throws Exception {
		EnumerationType t = EnumerationType.make("x", make_set("y", "z"),
				m_pscope.any());
		assertFalse(t.has_value("w"));
	}
	
	@Test
	public void obtain_value_by_name() throws Exception {
		EnumerationType t = EnumerationType.make("x", make_set("y", "z"),
				m_pscope.any());
		EnumerationValue vy = t.value("y");
		EnumerationValue vz = t.value("z");
		assertNotNull(vy);
		assertNotNull(vz);
		assertEquals("y", vy.name());
		assertEquals("z", vz.name());
	}
	
	@Test
	public void equals_same_value() throws Exception {
		EnumerationType t = EnumerationType.make("x", make_set("y", "z"),
				m_pscope.any());
		EnumerationValue vy = t.value("y");
		assertTrue(vy.equals(vy));
	}
	
	@Test
	public void not_equals_different_type() throws Exception {
		EnumerationType t = EnumerationType.make("x", make_set("y", "z"),
				m_pscope.any());
		EnumerationValue vy = t.value("y");
		assertFalse(vy.equals("y"));
	}
	
	@Test
	public void not_equals_different_name() throws Exception {
		EnumerationType t = EnumerationType.make("x", make_set("y", "z"),
				m_pscope.any());
		EnumerationValue vy = t.value("y");
		EnumerationValue vz = t.value("z");
		assertFalse(vy.equals(vz));
	}
	
	@Test
	public void value_to_string() throws Exception {
		EnumerationType t = EnumerationType.make("x", make_set("y", "z"),
				m_pscope.any());
		EnumerationValue vy = t.value("y");
		assertEquals("x:y", vy.toString());
	}
}
