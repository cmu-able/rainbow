package edu.cmu.cs.able.typelib.prim;

import org.junit.Test;

import auxtestlib.DefaultTCase;

/**
 * Tests the {@link AnyType} data type.
 */
public class AnyTest extends DefaultTCase {
	/**
	 * Checks that the type's properties are correct.
	 * @throws Exception test failed
	 */
	@Test
	public void any_properties() throws Exception {
		PrimitiveScope ps = new PrimitiveScope();
		AnyType any = ps.any();
		assertTrue(any.is_abstract());
		assertEquals(AnyType.NAME, any.name());
	}
}
