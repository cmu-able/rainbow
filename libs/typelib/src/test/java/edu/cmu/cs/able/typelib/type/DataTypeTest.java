package edu.cmu.cs.able.typelib.type;

import org.junit.Before;
import org.junit.Test;

import auxtestlib.DefaultTCase;

import edu.cmu.cs.able.typelib.TestDataType;

/**
 * Checks the data type class.
 */
@SuppressWarnings("javadoc")
public class DataTypeTest extends DefaultTCase {
	private DataTypeScope m_dts;
	
	@Before
	public void set_up() throws Exception {
		m_dts = new DataTypeScope();
	}
	
	@Test
	public void super_and_sub_of_unrelated_types() throws Exception {
		TestDataType x = new TestDataType("x");
		TestDataType y = new TestDataType("y");
		m_dts.add(x);
		m_dts.add(y);
		assertFalse(x.sub_of(y));
		assertFalse(x.super_of(y));
		assertFalse(y.sub_of(x));
		assertFalse(y.super_of(x));
	}
	
	@Test
	public void super_and_sub_of_itself() throws Exception {
		TestDataType x = new TestDataType("x");
		m_dts.add(x);
		assertFalse(x.sub_of(x));
		assertFalse(x.super_of(x));
	}
	
	@Test
	public void super_and_sub_of_directly_related_types() throws Exception {
		TestDataType x = new TestDataType("x");
		TestDataType y = new TestDataType("y", x);
		m_dts.add(x);
		m_dts.add(y);
		assertFalse(x.sub_of(y));
		assertTrue(x.super_of(y));
		assertTrue(y.sub_of(x));
		assertFalse(y.super_of(x));
	}
	
	@Test
	public void super_and_sub_of_indirectly_related_types() throws Exception {
		TestDataType x = new TestDataType("x");
		TestDataType y = new TestDataType("y", x);
		TestDataType z = new TestDataType("z", y);
		m_dts.add(x);
		m_dts.add(y);
		m_dts.add(z);
		assertFalse(x.sub_of(z));
		assertTrue(x.super_of(z));
		assertTrue(z.sub_of(x));
		assertFalse(z.super_of(x));
	}
}
