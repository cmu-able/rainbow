package edu.cmu.cs.able.typelib.vtscope;

import org.junit.Test;

import edu.cmu.cs.able.typelib.prim.PrimitiveScope;

import auxtestlib.DefaultTCase;

/**
 * Tests type scopes.
 */
public class TypeScopeTest extends DefaultTCase {
	/**
	 * Creates and finds types in the typed scope.
	 * @throws Exception test failed
	 */
	@Test
	public void create_find_types() throws Exception {
		TypeScope ts = new TypeScope();
		PrimitiveScope ps = new PrimitiveScope();
		ts.add(new NamedType("a", ps.int16()));
		ts.add(new NamedType("d", ps.int32()));
		assertEquals(ps.int16(), ts.type("a"));
		assertEquals(ps.int32(), ts.type("d"));
		assertNull(ts.type("e"));
	}
}
