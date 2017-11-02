package edu.cmu.cs.able.typelib.alg;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import edu.cmu.cs.able.typelib.TestDataType;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataTypeScope;

import auxtestlib.DefaultTCase;

/**
 * Tests the common lower super types algorithm.
 */
@SuppressWarnings("javadoc")
public class CommonLowerSuperTypesTest extends DefaultTCase {
	private DataTypeScope m_dts;
	
	@Before
	public void set_up() throws Exception {
		m_dts = new DataTypeScope();
	}
	
	@Test
	public void same_type_not_included() throws Exception {
		TestDataType x = new TestDataType("x");
		m_dts.add(x);
		Set<DataType> r = CommonLowerSuperTypes.run(x, x, false);
		assertEquals(0, r.size());
	}
	
	@Test
	public void same_type_with_included() throws Exception {
		TestDataType x = new TestDataType("x");
		m_dts.add(x);
		Set<DataType> r = CommonLowerSuperTypes.run(x, x, true);
		assertEquals(1, r.size());
		assertTrue(r.contains(x));
	}
	
	@Test
	public void super_and_sub_type_not_included() throws Exception {
		TestDataType x = new TestDataType("x");
		TestDataType y = new TestDataType("y", x);
		m_dts.add(x);
		m_dts.add(y);
		Set<DataType> r = CommonLowerSuperTypes.run(x, y, false);
		assertEquals(0, r.size());
	}
	
	@Test
	public void super_and_sub_type_with_included() throws Exception {
		TestDataType x = new TestDataType("x");
		TestDataType y = new TestDataType("y", x);
		m_dts.add(x);
		m_dts.add(y);
		Set<DataType> r = CommonLowerSuperTypes.run(x, y, true);
		assertEquals(1, r.size());
		assertTrue(r.contains(x));
	}
	
	@Test
	public void two_types_with_common_super_type() throws Exception {
		TestDataType x = new TestDataType("x");
		TestDataType y = new TestDataType("y", x);
		TestDataType z = new TestDataType("z", x);
		m_dts.add(x);
		m_dts.add(y);
		m_dts.add(z);
		Set<DataType> r = CommonLowerSuperTypes.run(y, z, true);
		assertEquals(1, r.size());
		assertTrue(r.contains(x));
	}
	
	@Test
	public void two_types_with_two_common_super_type() throws Exception {
		TestDataType x = new TestDataType("x");
		TestDataType y = new TestDataType("y");
		TestDataType z = new TestDataType("z", x, y);
		TestDataType w = new TestDataType("w", x, y);
		m_dts.add(x);
		m_dts.add(y);
		m_dts.add(z);
		m_dts.add(w);
		Set<DataType> r = CommonLowerSuperTypes.run(w, z, true);
		assertEquals(2, r.size());
		assertTrue(r.contains(x));
		assertTrue(r.contains(y));
	}
	
	@Test
	public void two_types_with_two_common_related_super_types()
			throws Exception {
		TestDataType x = new TestDataType("x");
		TestDataType y = new TestDataType("y", x);
		TestDataType z = new TestDataType("z", x, y);
		TestDataType w = new TestDataType("w", x, y);
		m_dts.add(x);
		m_dts.add(y);
		m_dts.add(z);
		m_dts.add(w);
		Set<DataType> r = CommonLowerSuperTypes.run(w, z, true);
		assertEquals(1, r.size());
		assertTrue(r.contains(y));
	}
}
