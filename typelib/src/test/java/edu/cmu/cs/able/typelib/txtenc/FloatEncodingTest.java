package edu.cmu.cs.able.typelib.txtenc;

import java.io.IOException;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;

import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;
import edu.cmu.cs.able.typelib.prim.DoubleValue;
import edu.cmu.cs.able.typelib.prim.FloatValue;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Tests float encoding.
 */
@SuppressWarnings("javadoc")
public class FloatEncodingTest extends AbstractEncodingTestCase {
	@Test
	public void float_encode_positive_number() throws Exception {
		m_enc.encode(m_pscope.float_type().make(5.5f), m_doutput);
		FloatValue v = (FloatValue) m_enc.decode(make_din(), m_pscope);
		assertEquals(m_pscope.float_type().make(5.5f), v);
	}
	
	@Test
	public void float_encode_zero() throws Exception {
		m_enc.encode(m_pscope.float_type().make(0f), m_doutput);
		FloatValue v = (FloatValue) m_enc.decode(make_din(), m_pscope);
		assertEquals(m_pscope.float_type().make(0f), v);
	}
	
	@Test
	public void float_encode_negative_number() throws Exception {
		m_enc.encode(m_pscope.float_type().make(-5.5f), m_doutput);
		FloatValue v = (FloatValue) m_enc.decode(make_din(), m_pscope);
		assertEquals(m_pscope.float_type().make(-5.5f), v);
	}
	
	@Test
	public void float_random_decode_invalid_values() throws Exception {
		for (int i = 0; i < RANDOM_TESTS; i++) {
			m_output.reset();
			FloatValue bv = m_pscope.float_type().make(RandomUtils.nextFloat());
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
	public void double_encode_positive_number() throws Exception {
		m_enc.encode(m_pscope.double_type().make(5.5), m_doutput);
		DoubleValue v = (DoubleValue) m_enc.decode(make_din(), m_pscope);
		assertEquals(m_pscope.double_type().make(5.5), v);
	}
	
	@Test
	public void double_encode_zero() throws Exception {
		m_enc.encode(m_pscope.double_type().make(0.0), m_doutput);
		DoubleValue v = (DoubleValue) m_enc.decode(make_din(), m_pscope);
		assertEquals(m_pscope.double_type().make(0.0), v);
	}
	
	@Test
	public void double_encode_negative_number() throws Exception {
		m_enc.encode(m_pscope.double_type().make(-5.5), m_doutput);
		DoubleValue v = (DoubleValue) m_enc.decode(make_din(), m_pscope);
		assertEquals(m_pscope.double_type().make(-5.5), v);
	}
	
	@Test
	public void double_random_decode_invalid_values() throws Exception {
		for (int i = 0; i < RANDOM_TESTS; i++) {
			m_output.reset();
			DoubleValue bv = m_pscope.double_type().make(
					RandomUtils.nextDouble());
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
	public void float_performance_test() throws Exception {
		DataValue[] v = new DataValue[1000];
		for (int i = 0; i < v.length; i++) {
			v[i] = m_pscope.float_type().make(RandomUtils.nextFloat());
		}
		
		double unit_value = encode_decode(v, 500) * 1000;
		System.out.println("Float encoding time: " + unit_value + "us.");
		
		/*
		 * 50us is OK.
		 */
		assertTrue(50 > unit_value);
	}
	
	@Test
	public void double_performance_test() throws Exception {
		DataValue[] v = new DataValue[1000];
		for (int i = 0; i < v.length; i++) {
			v[i] = m_pscope.double_type().make(RandomUtils.nextDouble());
		}
		
		double unit_value = encode_decode(v, 500) * 1000;
		System.out.println("Double encoding time: " + unit_value + "us.");
		
		/*
		 * 50us is OK.
		 */
		assertTrue(50 > unit_value);
	}
}
