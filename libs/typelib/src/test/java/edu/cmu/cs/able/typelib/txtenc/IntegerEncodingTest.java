package edu.cmu.cs.able.typelib.txtenc;

import java.io.IOException;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;

import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;
import edu.cmu.cs.able.typelib.prim.Int16Value;
import edu.cmu.cs.able.typelib.prim.Int32Value;
import edu.cmu.cs.able.typelib.prim.Int64Value;
import edu.cmu.cs.able.typelib.prim.Int8Value;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Tests integer encoding.
 */
@SuppressWarnings("javadoc")
public class IntegerEncodingTest extends AbstractEncodingTestCase {
	@Test
	public void int8_encode_positive_number() throws Exception {
		m_enc.encode(m_pscope.int8().make((byte) 5), m_doutput);
		Int8Value v = (Int8Value) m_enc.decode(make_din(), m_pscope);
		assertEquals(m_pscope.int8().make((byte) 5), v);
	}
	
	@Test
	public void int8_encode_zero() throws Exception {
		m_enc.encode(m_pscope.int8().make((byte) 0), m_doutput);
		Int8Value v = (Int8Value) m_enc.decode(make_din(), m_pscope);
		assertEquals(m_pscope.int8().make((byte) 0), v);
	}
	
	@Test
	public void int8_encode_negative_number() throws Exception {
		m_enc.encode(m_pscope.int8().make((byte) -3), m_doutput);
		Int8Value v = (Int8Value) m_enc.decode(make_din(), m_pscope);
		assertEquals(m_pscope.int8().make((byte) -3), v);
	}
	
	@Test
	public void int8_random_decode_invalid_values() throws Exception {
		for (int i = 0; i < RANDOM_TESTS; i++) {
			m_output.reset();
			Int8Value bv = m_pscope.int8().make((byte) RandomUtils.nextInt());
			m_enc.encode(bv, m_doutput);
			try {
				m_enc.decode(make_corrupt_din(), m_pscope);
				fail();
			} catch (IOException|InvalidEncodingException|AssertionError e) {
				/*
				 * Expected.
				 */
			}
		}
	}
	
	@Test
	public void int16_encode_positive_number() throws Exception {
		m_enc.encode(m_pscope.int16().make((short) 5), m_doutput);
		Int16Value v = (Int16Value) m_enc.decode(make_din(), m_pscope);
		assertEquals(m_pscope.int16().make((short) 5), v);
	}
	
	@Test
	public void int16_encode_zero() throws Exception {
		m_enc.encode(m_pscope.int16().make((short) 0), m_doutput);
		Int16Value v = (Int16Value) m_enc.decode(make_din(), m_pscope);
		assertEquals(m_pscope.int16().make((short) 0), v);
	}
	
	@Test
	public void int16_encode_negative_number() throws Exception {
		m_enc.encode(m_pscope.int16().make((short) -3), m_doutput);
		Int16Value v = (Int16Value) m_enc.decode(make_din(), m_pscope);
		assertEquals(m_pscope.int16().make((short) -3), v);
	}
	
	@Test
	public void int16_random_decode_invalid_values() throws Exception {
		for (int i = 0; i < RANDOM_TESTS; i++) {
			m_output.reset();
			Int16Value bv = m_pscope.int16().make(
					(short) RandomUtils.nextInt());
			m_enc.encode(bv, m_doutput);
			try {
				m_enc.decode(make_corrupt_din(), m_pscope);
				fail();
			} catch (IOException|InvalidEncodingException|AssertionError e) {
				/*
				 * Expected.
				 */
			}
		}
	}
	
	@Test
	public void int32_encode_positive_number() throws Exception {
		m_enc.encode(m_pscope.int32().make(5), m_doutput);
		Int32Value v = (Int32Value) m_enc.decode(make_din(), m_pscope);
		assertEquals(m_pscope.int32().make(5), v);
	}
	
	@Test
	public void int32_encode_zero() throws Exception {
		m_enc.encode(m_pscope.int32().make(0), m_doutput);
		Int32Value v = (Int32Value) m_enc.decode(make_din(), m_pscope);
		assertEquals(m_pscope.int32().make(0), v);
	}
	
	@Test
	public void int32_encode_negative_number() throws Exception {
		m_enc.encode(m_pscope.int32().make(-3), m_doutput);
		Int32Value v = (Int32Value) m_enc.decode(make_din(), m_pscope);
		assertEquals(m_pscope.int32().make(-3), v);
	}
	
	@Test
	public void int32_random_decode_invalid_values() throws Exception {
		for (int i = 0; i < RANDOM_TESTS; i++) {
			m_output.reset();
			Int32Value bv = m_pscope.int32().make(RandomUtils.nextInt());
			m_enc.encode(bv, m_doutput);
			try {
				m_enc.decode(make_corrupt_din(), m_pscope);
				fail();
			} catch (IOException|InvalidEncodingException|AssertionError e) {
				/*
				 * Expected.
				 */
			}
		}
	}
	
	@Test
	public void int64_encode_positive_number() throws Exception {
		m_enc.encode(m_pscope.int64().make(5), m_doutput);
		Int64Value v = (Int64Value) m_enc.decode(make_din(), m_pscope);
		assertEquals(m_pscope.int64().make(5), v);
	}
	
	@Test
	public void int64_encode_zero() throws Exception {
		m_enc.encode(m_pscope.int64().make(0), m_doutput);
		Int64Value v = (Int64Value) m_enc.decode(make_din(), m_pscope);
		assertEquals(m_pscope.int64().make(0), v);
	}
	
	@Test
	public void int64_encode_negative_number() throws Exception {
		m_enc.encode(m_pscope.int64().make(-3), m_doutput);
		Int64Value v = (Int64Value) m_enc.decode(make_din(), m_pscope);
		assertEquals(m_pscope.int64().make(-3), v);
	}
	
	@Test
	public void int64_random_decode_invalid_values() throws Exception {
		for (int i = 0; i < RANDOM_TESTS; i++) {
			m_output.reset();
			Int64Value bv = m_pscope.int64().make(RandomUtils.nextLong());
			m_enc.encode(bv, m_doutput);
			try {
				m_enc.decode(make_corrupt_din(), m_pscope);
				fail();
			} catch (IOException|InvalidEncodingException|AssertionError e) {
				/*
				 * Expected.
				 */
			}
		}
	}
	
	@Test
	public void int8_performance_test() throws Exception {
		DataValue[] v = new DataValue[1000];
		for (int i = 0; i < v.length; i++) {
			v[i] = m_pscope.int8().make((byte) (RandomUtils.nextInt(256)
					- 128));
		}
		
		double unit_value = encode_decode(v, 500) * 1000;
		System.out.println("Int8 encoding time: " + unit_value + "us.");
		
		/*
		 * 50us is OK.
		 */
		assertTrue(50 > unit_value);
	}
	
	@Test
	public void int16_performance_test() throws Exception {
		DataValue[] v = new DataValue[1000];
		for (int i = 0; i < v.length; i++) {
			v[i] = m_pscope.int16().make((short) (RandomUtils.nextInt(65536)
					- 32768));
		}
		
		double unit_value = encode_decode(v, 500) * 1000;
		System.out.println("Int16 encoding time: " + unit_value + "us.");
		
		/*
		 * 50us is OK.
		 */
		assertTrue(50 > unit_value);
	}
	
	@Test
	public void int32_performance_test() throws Exception {
		DataValue[] v = new DataValue[1000];
		for (int i = 0; i < v.length; i++) {
			v[i] = m_pscope.int32().make(RandomUtils.nextInt());
		}
		
		double unit_value = encode_decode(v, 500) * 1000;
		System.out.println("Int32 encoding time: " + unit_value + "us.");
		
		/*
		 * 50us is OK.
		 */
		assertTrue(50 > unit_value);
	}
	
	@Test
	public void int64_performance_test() throws Exception {
		DataValue[] v = new DataValue[1000];
		for (int i = 0; i < v.length; i++) {
			v[i] = m_pscope.int64().make(RandomUtils.nextLong());
		}
		
		double unit_value = encode_decode(v, 500) * 1000;
		System.out.println("Int64 encoding time: " + unit_value + "us.");
		
		/*
		 * 50us is OK.
		 */
		assertTrue(50 > unit_value);
	}
}
