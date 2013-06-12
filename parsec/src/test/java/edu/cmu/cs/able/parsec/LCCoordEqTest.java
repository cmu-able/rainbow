package edu.cmu.cs.able.parsec;

import org.junit.Test;

import auxtestlib.DefaultTCase;

/**
 * Equivalence class tests for the {@link LCCoord} class.
 */
@SuppressWarnings("javadoc")
public class LCCoordEqTest extends DefaultTCase {
	@Test
	public void create_non_empty_coordinate() throws Exception {
		LCCoord c = new LCCoord(5, 8);
		assertEquals(5, c.line());
		assertEquals(8, c.column());
	}
	
	@Test
	public void create_coordinates_with_zero_values() throws Exception {
		LCCoord c = new LCCoord(0, 0);
		assertEquals(1, c.line());
		assertEquals(1, c.column());
	}
}
