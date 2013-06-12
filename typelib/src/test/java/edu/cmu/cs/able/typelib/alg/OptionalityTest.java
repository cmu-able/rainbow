package edu.cmu.cs.able.typelib.alg;

import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import edu.cmu.cs.able.typelib.TestDataType;
import edu.cmu.cs.able.typelib.TestDataValue;
import edu.cmu.cs.able.typelib.comp.OptionalDataType;
import edu.cmu.cs.able.typelib.comp.OptionalDataValue;
import edu.cmu.cs.able.typelib.type.DataTypeScope;
import auxtestlib.DefaultTCase;

/**
 * Test case for the optionality algorithms.
 */
@SuppressWarnings("javadoc")
public class OptionalityTest extends DefaultTCase {
	private DataTypeScope m_dts;
	private TestDataType m_dt;
	private TestDataValue m_dv;
	private OptionalDataType m_odt;
	private OptionalDataValue m_odv;
	private OptionalDataValue m_odvn;
	private OptionalDataType m_oodt;
	private OptionalDataValue m_oodv;
	private OptionalDataValue m_oodvn;
	private OptionalDataValue m_oodvin;
	
	@Before
	public void set_up() throws Exception {
		m_dts = new DataTypeScope();
		
		m_dt = new TestDataType("foo");
		m_dts.add(m_dt);
		m_dv = new TestDataValue(m_dt, 1);
		
		m_odt = new OptionalDataType(m_dt, new HashSet<OptionalDataType>());
		m_dts.add(m_odt);
		m_odv = m_odt.make(m_dv);
		m_odvn = m_odt.make(null);
		
		m_oodt = new OptionalDataType(m_odt, new HashSet<OptionalDataType>());
		m_dts.add(m_oodt);
		m_oodv = m_oodt.make(m_odv);
		m_oodvn = m_oodt.make(null);
		m_oodvin = m_oodt.make(m_odvn);
	}
	
	@Test
	public void detects_optional_data_type() throws Exception {
		assertTrue(Optionality.is_optional(m_odt));
	}
	
	@Test
	public void detects_non_optional_data_type() throws Exception {
		assertFalse(Optionality.is_optional(m_dt));
	}
	
	@Test
	public void detects_optional_data_value() throws Exception {
		assertTrue(Optionality.is_optional(m_odv));
	}
	
	@Test
	public void detects_non_optional_data_value() throws Exception {
		assertFalse(Optionality.is_optional(m_dv));
	}
	
	@Test
	public void unoptionalize_optional_data_type() throws Exception {
		assertEquals(m_dt, Optionality.unoptionalize(m_odt));
	}
	
	@Test
	public void unoptionalize_non_optional_data_type() throws Exception {
		assertEquals(m_dt, Optionality.unoptionalize(m_dt));
	}
	
	@Test
	public void unoptionalize_deep_optional_data_type() throws Exception {
		assertEquals(m_odt, Optionality.unoptionalize(m_oodt));
	}
	
	@Test
	public void unoptionalize_non_null_optional_data_value() throws Exception {
		assertEquals(m_dv, Optionality.unoptionalize(m_odv));
	}
	
	@Test
	public void unoptionalize_non_optional_data_value() throws Exception {
		assertEquals(m_dv, Optionality.unoptionalize(m_dv));
	}
	
	@Test
	public void unoptionalize_non_null_deep_optional_data_value()
			throws Exception {
		assertEquals(m_odv, Optionality.unoptionalize(m_oodv));
		assertEquals(m_odvn, Optionality.unoptionalize(m_oodvin));
	}
	
	@Test
	public void unoptionalize_null_optional_data_value() throws Exception {
		assertNull(Optionality.unoptionalize(m_odvn));
	}
	
	@Test
	public void unoptionalize_null_deep_optional_data_value()
			throws Exception {
		assertNull(Optionality.unoptionalize(m_oodvn));
	}
	
	@Test
	public void deep_unoptionalize_optional_data_type() throws Exception {
		assertEquals(m_dt, Optionality.deep_unoptionalize(m_odt));
	}
	
	@Test
	public void deep_unoptionalize_non_optional_data_type() throws Exception {
		assertEquals(m_dt, Optionality.deep_unoptionalize(m_dt));
	}
	
	@Test
	public void deep_unoptionalize_deep_optional_data_type() throws Exception {
		assertEquals(m_dt, Optionality.deep_unoptionalize(m_oodt));
	}
	
	@Test
	public void deep_unoptionalize_non_null_optional_data_value()
			throws Exception {
		assertEquals(m_dv, Optionality.deep_unoptionalize(m_odv));
	}
	
	@Test
	public void deep_unoptionalize_non_optional_data_value() throws Exception {
		assertEquals(m_dv, Optionality.deep_unoptionalize(m_dv));
	}
	
	@Test
	public void deep_unoptionalize_non_null_deep_optional_data_value()
			throws Exception {
		assertEquals(m_dv, Optionality.deep_unoptionalize(m_oodv));
	}
	
	@Test
	public void deep_unoptionalize_null_optional_data_value()
			throws Exception {
		assertNull(Optionality.deep_unoptionalize(m_odvn));
	}
	
	@Test
	public void deep_unoptionalize_null_deep_optional_data_value()
			throws Exception {
		assertNull(Optionality.deep_unoptionalize(m_oodvn));
		assertNull(Optionality.deep_unoptionalize(m_oodvin));
	}
	
	@Test
	public void optionalize_non_null_to_normal_data_type() throws Exception {
		assertEquals(m_dv, Optionality.optionalize(m_dv, m_dt));
	}
	
	@Test
	public void optionalize_non_null_to_optional_data_type() throws Exception {
		assertEquals(m_odv, Optionality.optionalize(m_dv, m_odt));
	}
	
	@Test
	public void optionalize_non_null_to_deep_optional_data_type()
			throws Exception {
		assertEquals(m_oodv, Optionality.optionalize(m_dv, m_oodt));
	}
	
	@Test
	public void optionalize_null_to_optional_data_type() throws Exception {
		assertEquals(m_odvn, Optionality.optionalize(null, m_odt));
	}
	
	@Test
	public void optionalize_null_to_deep_optional_data_type() throws Exception {
		assertEquals(m_oodvn, Optionality.optionalize(null, m_oodt));
	}
	
	@Test
	public void optionalize_optional_non_null_value_to_optional_data_type()
			throws Exception {
		assertEquals(m_odv, Optionality.optionalize(m_odv, m_odt));
	}
	
	@Test
	public void optionalize_optional_non_null_value_to_deep_optional_data_type()
			throws Exception {
		assertEquals(m_oodv, Optionality.optionalize(m_odv, m_oodt));
	}
	
	@Test
	public void optionalize_optional_null_value_to_optional_data_type()
			throws Exception {
		assertEquals(m_odvn, Optionality.optionalize(m_odvn, m_odt));
	}
	
	@Test
	public void optionalize_optional_null_value_to_deep_optional_data_type()
			throws Exception {
		assertEquals(m_oodvn, Optionality.optionalize(m_odvn, m_oodt));
	}
	
	@Test
	public void optionalize_deep_optional_non_null_value_to_deep_optional()
			throws Exception {
		assertEquals(m_oodv, Optionality.optionalize(m_oodv, m_oodt));
	}
	
	@Test
	public void optionalize_deep_optional_null_value_to_deep_optional()
			throws Exception {
		assertEquals(m_oodvn, Optionality.optionalize(m_oodvn, m_oodt));
	}
	
	@Test
	public void check_non_optional_optional_data_type_of_non_optional()
			throws Exception {
		assertFalse(Optionality.is_optional_of(m_dt, m_dt));
	}
	
	@Test
	public void check_non_optional_optional_data_type_of_optional()
			throws Exception {
		assertTrue(Optionality.is_optional_of(m_odt, m_dt));
	}
	
	@Test
	public void check_non_optional_optional_data_type_of_deep_optional()
			throws Exception {
		assertFalse(Optionality.is_optional_of(m_oodt, m_dt));
	}
	
	@Test
	public void check_optional_optional_data_type_of_non_optional()
			throws Exception {
		assertFalse(Optionality.is_optional_of(m_dt, m_odt));
	}
	
	@Test
	public void check_optional_optional_data_type_of_optional()
			throws Exception {
		assertFalse(Optionality.is_optional_of(m_odt, m_odt));
	}
	
	@Test
	public void check_optional_optional_data_type_of_deep_optional()
			throws Exception {
		assertTrue(Optionality.is_optional_of(m_oodt, m_odt));
	}
	
	@Test
	public void check_deep_optional_optional_data_type_of_non_optional()
			throws Exception {
		assertFalse(Optionality.is_optional_of(m_dt, m_oodt));
	}
	
	@Test
	public void check_deep_optional_optional_data_type_of_optional()
			throws Exception {
		assertFalse(Optionality.is_optional_of(m_odt, m_oodt));
	}
	
	@Test
	public void check_deep_optional_optional_data_type_of_deep_optional()
			throws Exception {
		assertFalse(Optionality.is_optional_of(m_oodt, m_oodt));
	}
	
	@Test
	public void check_non_optional_deep_optional_data_type_of_non_optional()
			throws Exception {
		assertFalse(Optionality.is_deep_optional_of(m_dt, m_dt));
	}
	
	@Test
	public void check_non_optional_deep_optional_data_type_of_optional()
			throws Exception {
		assertTrue(Optionality.is_deep_optional_of(m_odt, m_dt));
	}
	
	@Test
	public void check_non_optional_deep_optional_data_type_of_deep_optional()
			throws Exception {
		assertTrue(Optionality.is_deep_optional_of(m_oodt, m_dt));
	}
	
	@Test
	public void check_optional_deep_optional_data_type_of_non_optional()
			throws Exception {
		assertFalse(Optionality.is_deep_optional_of(m_dt, m_odt));
	}
	
	@Test
	public void check_optional_deep_optional_data_type_of_optional()
			throws Exception {
		assertFalse(Optionality.is_deep_optional_of(m_odt, m_odt));
	}
	
	@Test
	public void check_optional_deep_optional_data_type_of_deep_optional()
			throws Exception {
		assertTrue(Optionality.is_deep_optional_of(m_oodt, m_odt));
	}
	
	@Test
	public void check_deep_optional_deep_optional_data_type_of_non_optional()
			throws Exception {
		assertFalse(Optionality.is_deep_optional_of(m_dt, m_oodt));
	}
	
	@Test
	public void check_deep_optional_deep_optional_data_type_of_optional()
			throws Exception {
		assertFalse(Optionality.is_deep_optional_of(m_odt, m_oodt));
	}
	
	@Test
	public void check_deep_optional_deep_optional_data_type_of_deep_optional()
			throws Exception {
		assertFalse(Optionality.is_deep_optional_of(m_oodt, m_oodt));
	}
}
