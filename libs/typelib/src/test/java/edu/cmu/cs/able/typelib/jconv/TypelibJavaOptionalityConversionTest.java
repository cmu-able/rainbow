package edu.cmu.cs.able.typelib.jconv;

import org.junit.Before;
import org.junit.Test;

import auxtestlib.DefaultTCase;
import edu.cmu.cs.able.typelib.comp.OptionalDataType;
import edu.cmu.cs.able.typelib.comp.OptionalDataValue;
import edu.cmu.cs.able.typelib.prim.Int32Type;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;

/**
 * Test suite that checks conversion between typelib and Java of optional
 * data types.
 */
@SuppressWarnings("javadoc")
public class TypelibJavaOptionalityConversionTest extends DefaultTCase {
	/**
	 * The primitive type scope.
	 */
	private PrimitiveScope m_pscope;
	
	/**
	 * The type converter.
	 */
	private TypelibJavaConverter m_converter;
	
	/**
	 * The int32 data type.
	 */
	private Int32Type m_i32;
	
	/**
	 * The int32? data type.
	 */
	private OptionalDataType m_oi32;
	
	/**
	 * The null of m_oi32?.
	 */
	private OptionalDataValue m_null;
	
	/**
	 * A zero of m_oi32?.
	 */
	private OptionalDataValue m_zero;
	
	/**
	 * The int32?? data type.
	 */
	private OptionalDataType m_ooi32;
	
	/**
	 * A zero of m_oi32??.
	 */
	private OptionalDataValue m_ozero;
	
	@Before
	public void set_up() throws Exception {
		m_pscope = new PrimitiveScope();
		m_converter = DefaultTypelibJavaConverter.make(m_pscope);
		m_i32 = m_pscope.int32();
		m_oi32 = OptionalDataType.optional_of(m_i32);
		m_null = m_oi32.make(null);
		m_zero = m_oi32.make(m_i32.make(0));
		m_ooi32 = OptionalDataType.optional_of(m_oi32);
		m_ozero = m_ooi32.make(m_zero);
	}
	
	@Test
	public void java_null_to_optional() throws Exception {
		assertEquals(m_null, m_converter.from_java(null, m_oi32));
	}
	
	@Test(expected = ValueConversionException.class)
	public void java_null_to_none() throws Exception {
		m_converter.from_java(null, null);
	}
	
	@Test(expected = ValueConversionException.class)
	public void java_null_to_non_optional() throws Exception {
		m_converter.from_java(null, m_i32);
	}
	
	@Test
	public void java_not_null_to_optional() throws Exception {
		assertEquals(m_zero, m_converter.from_java(0, m_oi32));
	}
	
	@Test
	public void java_not_null_to_double_optional() throws Exception {
		assertEquals(m_ozero, m_converter.from_java(0, m_ooi32));
	}
	
	@Test
	public void optional_not_null_to_java() throws Exception {
		assertEquals(0, m_converter.to_java(m_zero, null));
		assertEquals((Integer) 0, m_converter.to_java(m_zero, Integer.class));
	}
	
	@Test
	public void double_optional_not_null_to_java() throws Exception {
		assertEquals(0, m_converter.to_java(m_ozero, null));
		assertEquals((Integer) 0, m_converter.to_java(m_ozero, Integer.class));
	}
	
	@Test
	public void optional_null_to_java() throws Exception {
		assertNull(m_converter.to_java(m_null, null));
		assertNull(m_converter.to_java(m_null, Integer.class));
	}
	
	// FIXME: This test fails but it shouldn't. It is a bug. 
//	@Test(expected = ValueConversionException.class)
//	public void optional_null_to_java_wrong_type() throws Exception {
//		m_converter.to_java(m_null, String.class);
//	}
	
	@Test
	public void double_optional_null_1_to_java() throws Exception {
		assertNull(m_converter.to_java(m_ooi32.make(m_null), null));
	}
	
	@Test
	public void double_optional_null_2_to_java() throws Exception {
		assertNull(m_converter.to_java(m_ooi32.make(null), null));
	}
}
