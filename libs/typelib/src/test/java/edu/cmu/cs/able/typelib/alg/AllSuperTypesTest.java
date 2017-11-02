package edu.cmu.cs.able.typelib.alg;

import java.util.Set;

import org.junit.Test;

import edu.cmu.cs.able.typelib.TestDataType;
import edu.cmu.cs.able.typelib.type.DataType;

import auxtestlib.DefaultTCase;

/**
 * Tests the all super types algorithm.
 */
@SuppressWarnings("javadoc")
public class AllSuperTypesTest extends DefaultTCase {
	@Test
	public void no_super_types_no_itself() throws Exception {
		TestDataType t1 = new TestDataType("x");
		Set<DataType> r = AllSuperTypes.run(t1, false);
		assertEquals(0, r.size());
	}
	
	@Test
	public void no_super_types_with_itself() throws Exception {
		TestDataType t1 = new TestDataType("x");
		Set<DataType> r = AllSuperTypes.run(t1, true);
		assertEquals(1, r.size());
		assertTrue(r.contains(t1));
	}
	
	@Test
	public void one_super_type_no_itself() throws Exception {
		TestDataType t_x = new TestDataType("x");
		TestDataType t_y = new TestDataType("y", t_x);
		Set<DataType> r = AllSuperTypes.run(t_y, false);
		assertEquals(1, r.size());
		assertTrue(r.contains(t_x));
	}
	
	@Test
	public void one_super_type_with_itself() throws Exception {
		TestDataType t_x = new TestDataType("x");
		TestDataType t_y = new TestDataType("y", t_x);
		Set<DataType> r = AllSuperTypes.run(t_y, true);
		assertEquals(2, r.size());
		assertTrue(r.contains(t_x));
		assertTrue(r.contains(t_y));
	}
	
	@Test
	public void multiple_super_types_no_itself() throws Exception {
		TestDataType t_x = new TestDataType("x");
		TestDataType t_y = new TestDataType("y", t_x);
		TestDataType t_z = new TestDataType("z", t_x, t_y);
		TestDataType t_w = new TestDataType("w");
		TestDataType t_v = new TestDataType("v", t_z, t_w);
		Set<DataType> r = AllSuperTypes.run(t_v, false);
		assertEquals(4, r.size());
		assertTrue(r.contains(t_x));
		assertTrue(r.contains(t_y));
		assertTrue(r.contains(t_z));
		assertTrue(r.contains(t_w));
	}
	
	@Test
	public void multiple_super_types_with_itself() throws Exception {
		TestDataType t_x = new TestDataType("x");
		TestDataType t_y = new TestDataType("y", t_x);
		TestDataType t_z = new TestDataType("z", t_x, t_y);
		TestDataType t_w = new TestDataType("w");
		TestDataType t_v = new TestDataType("v", t_z, t_w);
		Set<DataType> r = AllSuperTypes.run(t_v, true);
		assertEquals(5, r.size());
		assertTrue(r.contains(t_x));
		assertTrue(r.contains(t_y));
		assertTrue(r.contains(t_z));
		assertTrue(r.contains(t_w));
		assertTrue(r.contains(t_v));
	}
}
