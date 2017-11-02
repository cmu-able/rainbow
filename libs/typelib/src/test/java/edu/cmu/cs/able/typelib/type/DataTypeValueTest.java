package edu.cmu.cs.able.typelib.type;

import java.util.Set;

import org.junit.Test;

import edu.cmu.cs.able.typelib.TestDataType;
import edu.cmu.cs.able.typelib.TestDataValue;
import auxtestlib.DefaultTCase;

/**
 * Tests data values.
 */
public class DataTypeValueTest extends DefaultTCase {
	/**
	 * Creates a data value and ensures it has the right type.
	 * @throws Exception test failed
	 */
	@Test
	public void create_and_obtain_data_type() throws Exception {
		DataTypeScope gs = new DataTypeScope();
		TestDataType tdt = new TestDataType("x");
		gs.add(tdt);
		DataValue dv = new TestDataValue(tdt, 3);
		assertEquals(tdt, dv.type());
	}
	
	/**
	 * Obtains all super and sub types of a data type.
	 * @throws Exception test failed
	 */
	@Test
	public void obtaining_sub_and_super_types() throws Exception {
		DataTypeScope gs = new DataTypeScope();
		TestDataType t1 = new TestDataType("x");
		gs.add(t1);
		
		assertEquals(0, t1.sub_types().size());
		assertEquals(0, t1.super_types().size());
		
		TestDataType t1_x = new TestDataType("x_x", t1);
		gs.add(t1_x);
		
		assertEquals(1, t1.sub_types().size());
		assertTrue(t1.sub_types().contains(t1_x));
		assertEquals(0, t1.super_types().size());
		
		assertEquals(0, t1_x.sub_types().size());
		assertEquals(1, t1_x.super_types().size());
		assertTrue(t1_x.super_types().contains(t1));
		
		TestDataType t1_y = new TestDataType("x_y", t1);
		gs.add(t1_y);
		
		assertEquals(2, t1.sub_types().size());
		assertTrue(t1.sub_types().contains(t1_x));
		assertTrue(t1.sub_types().contains(t1_y));
		assertEquals(0, t1.super_types().size());
		
		assertEquals(0, t1_x.sub_types().size());
		assertEquals(1, t1_x.super_types().size());
		assertTrue(t1_x.super_types().contains(t1));
		
		assertEquals(0, t1_y.sub_types().size());
		assertEquals(1, t1_y.super_types().size());
		assertTrue(t1_y.super_types().contains(t1));
		
		TestDataType t1_z = new TestDataType("x_y_z", t1_y);
		gs.add(t1_z);
		
		assertEquals(2, t1.sub_types().size());
		assertTrue(t1.sub_types().contains(t1_x));
		assertTrue(t1.sub_types().contains(t1_y));
		assertEquals(0, t1.super_types().size());
		
		assertEquals(0, t1_x.sub_types().size());
		assertEquals(1, t1_x.super_types().size());
		assertTrue(t1_x.super_types().contains(t1));
		
		assertEquals(1, t1_y.sub_types().size());
		assertTrue(t1_y.sub_types().contains(t1_z));
		assertEquals(1, t1_y.super_types().size());
		assertTrue(t1_y.super_types().contains(t1));
		
		assertEquals(0, t1_z.sub_types().size());
		assertEquals(1, t1_z.super_types().size());
		assertTrue(t1_z.super_types().contains(t1_y));
		
		assertFalse(t1.sub_of(t1));
		assertFalse(t1.sub_of(t1_x));
		assertFalse(t1.sub_of(t1_y));
		assertFalse(t1.sub_of(t1_z));
		assertTrue(t1_x.sub_of(t1));
		assertFalse(t1_x.sub_of(t1_x));
		assertFalse(t1_x.sub_of(t1_y));
		assertFalse(t1_x.sub_of(t1_z));
		assertTrue(t1_y.sub_of(t1));
		assertFalse(t1_y.sub_of(t1_x));
		assertFalse(t1_y.sub_of(t1_y));
		assertFalse(t1_y.sub_of(t1_z));
		assertTrue(t1_z.sub_of(t1));
		assertFalse(t1_z.sub_of(t1_x));
		assertTrue(t1_z.sub_of(t1_y));
		assertFalse(t1_z.sub_of(t1_z));
		
		assertFalse(t1.super_of(t1));
		assertTrue(t1.super_of(t1_x));
		assertTrue(t1.super_of(t1_y));
		assertTrue(t1.super_of(t1_z));
		assertFalse(t1_x.super_of(t1));
		assertFalse(t1_x.super_of(t1_x));
		assertFalse(t1_x.super_of(t1_y));
		assertFalse(t1_x.super_of(t1_z));
		assertFalse(t1_y.super_of(t1));
		assertFalse(t1_y.super_of(t1_x));
		assertFalse(t1_y.super_of(t1_y));
		assertTrue(t1_y.super_of(t1_z));
		assertFalse(t1_z.super_of(t1));
		assertFalse(t1_z.super_of(t1_x));
		assertFalse(t1_z.super_of(t1_y));
		assertFalse(t1_z.super_of(t1_z));
	}
	
	/**
	 * Checks whether tests of instance work.
	 * @throws Exception test failed
	 */
	@Test
	public void is_instance() throws Exception {
		DataTypeScope gs = new DataTypeScope();
		TestDataType t1 = new TestDataType("x");
		gs.add(t1);
		TestDataType t2 = new TestDataType("y", t1);
		gs.add(t2);
		
		TestDataValue v1 = new TestDataValue(t1, 2);
		assertTrue(t1.is_instance(v1));
		assertFalse(t2.is_instance(v1));
		
		TestDataValue v2 = new TestDataValue(t2, 3);
		assertTrue(t1.is_instance(v2));
		assertTrue(t2.is_instance(v2));
	}
	
	/**
	 * Checks that super/sub and instance testing methods account for
	 * <code>null</code>.
	 * @throws Exception test failed
	 */
	@Test
	public void super_sub_instance_null_check() throws Exception {
		TestDataType t1 = new TestDataType("foo");
		new DataTypeScope().add(t1);
		
		try {
			t1.sub_of(null);
			fail();
		} catch (IllegalArgumentException e) {
			/*
			 * Expected.
			 */
		}
		
		try {
			t1.super_of(null);
			fail();
		} catch (IllegalArgumentException e) {
			/*
			 * Expected.
			 */
		}
		
		try {
			t1.is_instance(null);
			fail();
		} catch (IllegalArgumentException e) {
			/*
			 * Expected.
			 */
		}
	}
	
	/**
	 * Cannot create with invalid parameters.
	 * @throws Exception test failed
	 */
	@SuppressWarnings("unused")
	@Test(expected = AssertionError.class)
	public void create_invalid_paramters() throws Exception {
		new TestDataType(null);
	}
	
	/**
	 * Can create with no parents using <code>null</code> but not with a
	 * parent set to <code>null</code>.
	 * @throws Exception test failed
	 */
	@Test
	@SuppressWarnings("unused")
	public void create_with_null_parents() throws Exception {
		try {
			new TestDataType("foo", (TestDataType) null);
			fail();
		} catch (AssertionError e) {
			/*
			 * Expected.
			 */
		}
		
		new TestDataType("foo", (Set<DataType>) null);
	}
	
	/**
	 * Cannot create sub types form types in different hierarchies.
	 * @throws Exception test failed
	 */
	@Test
	public void sub_typing_in_different_hierarchy() throws Exception {
		DataTypeScope gs1 = new DataTypeScope();
		TestDataType dtx = new TestDataType("x");
		gs1.add(dtx);
		DataTypeScope gs2 = new DataTypeScope();
		
		try {
			TestDataType tdy = new TestDataType("y", dtx);
			gs2.add(tdy);
			fail();
		} catch (IllegalArgumentException | IllegalStateException e) {
			/*
			 * Expected.
			 */
		}
	}
}
