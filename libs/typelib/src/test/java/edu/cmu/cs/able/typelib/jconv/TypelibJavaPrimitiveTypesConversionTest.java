package edu.cmu.cs.able.typelib.jconv;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.type.DataValue;
import auxtestlib.DefaultTCase;

/**
 * Test suite that checks conversion of primitive data types.
 */
@SuppressWarnings("javadoc")
public class TypelibJavaPrimitiveTypesConversionTest extends DefaultTCase {
	/**
	 * Primitive type scope.
	 */
	private PrimitiveScope m_pscope;
	
	/**
	 * The converter.
	 */
	private TypelibJavaConverter m_converter;
	
	@Before
	public void set_up() throws Exception {
		m_pscope = new PrimitiveScope();
		m_converter = DefaultTypelibJavaConverter.make(m_pscope);
	}
	
	@Test
	public void convert_java_byte() throws Exception {
		Object j = new Byte((byte) -14);
		DataValue v = m_pscope.int8().make((byte) -14);
		assertEquals(v, m_converter.from_java(j, null));
		assertEquals(v, m_converter.from_java(j, m_pscope.int8()));
		assertEquals(v, m_converter.from_java(j, m_pscope.any()));
	}
	
	@Test
	public void convert_java_short() throws Exception {
		Object j = new Short((short) 15);
		DataValue v = m_pscope.int16().make((short) 15);
		assertEquals(v, m_converter.from_java(j, null));
		assertEquals(v, m_converter.from_java(j, m_pscope.int16()));
		assertEquals(v, m_converter.from_java(j, m_pscope.any()));
	}
	
	@Test
	public void convert_java_int() throws Exception {
		Object j = new Integer(-16);
		DataValue v = m_pscope.int32().make(-16);
		assertEquals(v, m_converter.from_java(j, null));
		assertEquals(v, m_converter.from_java(j, m_pscope.int32()));
		assertEquals(v, m_converter.from_java(j, m_pscope.any()));
	}
	
	@Test
	public void convert_java_long() throws Exception {
		Object j = new Long(17);
		DataValue v = m_pscope.int64().make(17);
		assertEquals(v, m_converter.from_java(j, null));
		assertEquals(v, m_converter.from_java(j, m_pscope.int64()));
		assertEquals(v, m_converter.from_java(j, m_pscope.any()));
	}
	
	@Test
	public void convert_java_long_period() throws Exception {
		Object j = new Long(17);
		DataValue v = m_pscope.period().make(17);
		assertEquals(v, m_converter.from_java(j, m_pscope.period()));
	}
	
	@Test
	public void convert_java_string() throws Exception {
		DataValue r = m_pscope.string().make("foo");
		assertEquals(r, m_converter.from_java("foo", null));
		assertEquals(r, m_converter.from_java("foo", m_pscope.string()));
		assertEquals(r, m_converter.from_java("foo", m_pscope.any()));
	}
	
	@Test
	public void convert_java_string_to_ascii() throws Exception {
		DataValue r = m_pscope.ascii().make("foo");
		assertEquals(r, m_converter.from_java("foo", m_pscope.ascii()));
	}
	
	@Test
	public void convert_java_boolean() throws Exception {
		DataValue r = m_pscope.bool().make(true);
		assertEquals(r, m_converter.from_java(Boolean.TRUE, null));
		assertEquals(r, m_converter.from_java(Boolean.TRUE, m_pscope.bool()));
		assertEquals(r, m_converter.from_java(Boolean.TRUE, m_pscope.any()));
		
		r = m_pscope.bool().make(false);
		assertEquals(r, m_converter.from_java(Boolean.FALSE, null));
		assertEquals(r, m_converter.from_java(Boolean.FALSE, m_pscope.bool()));
		assertEquals(r, m_converter.from_java(Boolean.FALSE, m_pscope.any()));
	}
	
	@Test
	public void convert_java_float() throws Exception {
		DataValue r = m_pscope.float_type().make(2.5f);
		assertEquals(r, m_converter.from_java(2.5f, null));
		assertEquals(r, m_converter.from_java(2.5f, m_pscope.float_type()));
		assertEquals(r, m_converter.from_java(2.5f, m_pscope.any()));
	}
	
	@Test
	public void convert_java_double() throws Exception {
		DataValue r = m_pscope.double_type().make(2.5);
		assertEquals(r, m_converter.from_java(2.5, null));
		assertEquals(r, m_converter.from_java(2.5, m_pscope.double_type()));
		assertEquals(r, m_converter.from_java(2.5, m_pscope.any()));
	}
	
	@Test
	public void convert_java_date() throws Exception {
		Date d = new Date();
		DataValue r = m_pscope.time().make(d.getTime());
		assertEquals(r, m_converter.from_java(d, null));
		assertEquals(r, m_converter.from_java(d, m_pscope.time()));
		assertEquals(r, m_converter.from_java(d, m_pscope.any()));
	}
	
	@Test
	public void convert_typelib_int8() throws Exception {
		Object j = new Byte((byte) 8);
		DataValue v = m_pscope.int8().make((byte) 8);
		assertEquals(j, m_converter.to_java(v, null));
		assertEquals(j, m_converter.to_java(v, Byte.class));
		assertEquals(j, m_converter.to_java(v, Number.class));
		assertEquals(j, m_converter.to_java(v, Object.class));
	}
	
	@Test
	public void convert_typelib_int16() throws Exception {
		Object j = new Short((short) -17);
		DataValue v = m_pscope.int16().make((short) -17);
		assertEquals(j, m_converter.to_java(v, null));
		assertEquals(j, m_converter.to_java(v, Short.class));
		assertEquals(j, m_converter.to_java(v, Number.class));
		assertEquals(j, m_converter.to_java(v, Object.class));
	}
	
	@Test
	public void convert_typelib_int32() throws Exception {
		Object j = new Integer(18);
		DataValue v = m_pscope.int32().make(18);
		assertEquals(j, m_converter.to_java(v, null));
		assertEquals(j, m_converter.to_java(v, Integer.class));
		assertEquals(j, m_converter.to_java(v, Number.class));
		assertEquals(j, m_converter.to_java(v, Object.class));
	}
	
	@Test
	public void convert_typelib_int64() throws Exception {
		Object j = new Long(-19);
		DataValue v = m_pscope.int64().make(-19);
		assertEquals(j, m_converter.to_java(v, null));
		assertEquals(j, m_converter.to_java(v, Long.class));
		assertEquals(j, m_converter.to_java(v, Number.class));
		assertEquals(j, m_converter.to_java(v, Object.class));
	}
	
	@Test
	public void convert_typelib_ascii() throws Exception {
		Object j = new String("foo");
		DataValue v = m_pscope.ascii().make("foo");
		assertEquals(j, m_converter.to_java(v, null));
		assertEquals(j, m_converter.to_java(v, String.class));
		assertEquals(j, m_converter.to_java(v, Object.class));
	}
	
	@Test
	public void convert_typelib_string() throws Exception {
		Object j = new String("bar");
		DataValue v = m_pscope.string().make("bar");
		assertEquals(j, m_converter.to_java(v, null));
		assertEquals(j, m_converter.to_java(v, String.class));
		assertEquals(j, m_converter.to_java(v, Object.class));
	}
	
	@Test
	public void convert_typelib_float() throws Exception {
		Object j = new Float(2.5f);
		DataValue v = m_pscope.float_type().make(2.5f);
		assertEquals(j, m_converter.to_java(v, null));
		assertEquals(j, m_converter.to_java(v, Float.class));
		assertEquals(j, m_converter.to_java(v, Number.class));
		assertEquals(j, m_converter.to_java(v, Object.class));
	}
	
	@Test
	public void convert_typelib_double() throws Exception {
		Object j = new Double(2.5);
		DataValue v = m_pscope.double_type().make(2.5);
		assertEquals(j, m_converter.to_java(v, null));
		assertEquals(j, m_converter.to_java(v, Double.class));
		assertEquals(j, m_converter.to_java(v, Number.class));
		assertEquals(j, m_converter.to_java(v, Object.class));
	}
	
	@Test
	public void convert_typelib_boolean() throws Exception {
		Object j = new Boolean(true);
		DataValue v = m_pscope.bool().make(true);
		assertEquals(j, m_converter.to_java(v, null));
		assertEquals(j, m_converter.to_java(v, Boolean.class));
		assertEquals(j, m_converter.to_java(v, Object.class));
		
		j = new Boolean(false);
		v = m_pscope.bool().make(false);
		assertEquals(j, m_converter.to_java(v, null));
		assertEquals(j, m_converter.to_java(v, Boolean.class));
		assertEquals(j, m_converter.to_java(v, Object.class));
	}
	
	@Test
	public void convert_typelib_period() throws Exception {
		Object j = new Long(-8);
		DataValue v = m_pscope.period().make(-8);
		assertEquals(j, m_converter.to_java(v, null));
		assertEquals(j, m_converter.to_java(v, Long.class));
		assertEquals(j, m_converter.to_java(v, Number.class));
		assertEquals(j, m_converter.to_java(v, Object.class));
	}
	
	@Test
	public void convert_typelib_time() throws Exception {
		Object j = new Date();
		DataValue v = m_pscope.time().make(((Date) j).getTime());
		assertEquals(j, m_converter.to_java(v, null));
		assertEquals(j, m_converter.to_java(v, Date.class));
		assertEquals(j, m_converter.to_java(v, Object.class));
	}
	
	@Test(expected = ValueConversionException.class)
	public void failed_conversion_java_boolean() throws Exception {
		m_converter.from_java(new Boolean(false), m_pscope.int8());
	}
	
	@Test(expected = ValueConversionException.class)
	public void failed_conversion_java_byte() throws Exception {
		m_converter.from_java(new Byte((byte) 0), m_pscope.int16());
	}
	
	@Test(expected = ValueConversionException.class)
	public void failed_conversion_java_short() throws Exception {
		m_converter.from_java(new Short((short) 0), m_pscope.int8());
	}
	
	@Test(expected = ValueConversionException.class)
	public void failed_conversion_java_integer() throws Exception {
		m_converter.from_java(new Integer(0), m_pscope.int8());
	}
	
	@Test(expected = ValueConversionException.class)
	public void failed_conversion_java_long() throws Exception {
		m_converter.from_java(new Long(0), m_pscope.int8());
	}
	
	@Test(expected = ValueConversionException.class)
	public void failed_conversion_java_date() throws Exception {
		m_converter.from_java(new Date(), m_pscope.int8());
	}
	
	@Test(expected = ValueConversionException.class)
	public void failed_conversion_java_float() throws Exception {
		m_converter.from_java(new Float(0), m_pscope.int8());
	}
	
	@Test(expected = ValueConversionException.class)
	public void failed_conversion_java_double() throws Exception {
		m_converter.from_java(new Double(0), m_pscope.int8());
	}
	
	@Test(expected = ValueConversionException.class)
	public void failed_conversion_java_string() throws Exception {
		m_converter.from_java(new String(), m_pscope.int8());
	}
	
	@Test(expected = ValueConversionException.class)
	public void failed_conversion_typelib_ascii() throws Exception {
		m_converter.to_java(m_pscope.ascii().make(""), Byte.class);
	}
	
	@Test(expected = ValueConversionException.class)
	public void failed_conversion_typelib_boolean() throws Exception {
		m_converter.to_java(m_pscope.bool().make(true), Byte.class);
	}
	
	@Test(expected = ValueConversionException.class)
	public void failed_conversion_typelib_double() throws Exception {
		m_converter.to_java(m_pscope.double_type().make(0), Byte.class);
	}
	
	@Test(expected = ValueConversionException.class)
	public void failed_conversion_typelib_float() throws Exception {
		m_converter.to_java(m_pscope.float_type().make(0), Byte.class);
	}
	
	@Test(expected = ValueConversionException.class)
	public void failed_conversion_typelib_int16() throws Exception {
		m_converter.to_java(m_pscope.int16().make((short) 0), Byte.class);
	}
	
	@Test(expected = ValueConversionException.class)
	public void failed_conversion_typelib_int32() throws Exception {
		m_converter.to_java(m_pscope.int32().make(0), Byte.class);
	}
	
	@Test(expected = ValueConversionException.class)
	public void failed_conversion_typelib_int64() throws Exception {
		m_converter.to_java(m_pscope.int64().make(0), Byte.class);
	}
	
	@Test(expected = ValueConversionException.class)
	public void failed_conversion_typelib_int8() throws Exception {
		m_converter.to_java(m_pscope.int8().make((byte) 0), Short.class);
	}
	
	@Test(expected = ValueConversionException.class)
	public void failed_conversion_typelib_period() throws Exception {
		m_converter.to_java(m_pscope.period().make(0), Byte.class);
	}
	
	@Test(expected = ValueConversionException.class)
	public void failed_conversion_typelib_string() throws Exception {
		m_converter.to_java(m_pscope.string().make(""), Byte.class);
	}
	
	@Test(expected = ValueConversionException.class)
	public void failed_conversion_typelib_time() throws Exception {
		m_converter.to_java(m_pscope.time().make(0), Byte.class);
	}
}
