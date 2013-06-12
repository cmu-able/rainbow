package edu.cmu.cs.able.typelib.txtenc;

import java.io.IOException;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;

import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;
import edu.cmu.cs.able.typelib.prim.AsciiValue;
import edu.cmu.cs.able.typelib.prim.StringValue;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Tests encoding of strings.
 */
@SuppressWarnings("javadoc")
public class StringEncodingTest extends AbstractEncodingTestCase {
	@Test
	public void ascii_encode_empty_string() throws Exception {
		m_enc.encode(m_pscope.ascii().make(""), m_doutput);
		AsciiValue v = (AsciiValue) m_enc.decode(make_din(), m_pscope);
		assertEquals(m_pscope.ascii().make(""), v);
	}
	
	@Test
	public void ascii_encode_nonempty_string() throws Exception {
		m_enc.encode(m_pscope.ascii().make("foobar"), m_doutput);
		AsciiValue v = (AsciiValue) m_enc.decode(make_din(), m_pscope);
		assertEquals(m_pscope.ascii().make("foobar"), v);
	}
	
	@Test
	public void ascii_random_decode_invalid_values() throws Exception {
		for (int i = 0; i < RANDOM_TESTS; i++) {
			m_output.reset();
			AsciiValue bv = m_pscope.ascii().make(
					RandomStringUtils.randomAlphanumeric(10));
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
	public void string_encode_empty_string() throws Exception {
		m_enc.encode(m_pscope.string().make(""), m_doutput);
		StringValue v = (StringValue) m_enc.decode(make_din(), m_pscope);
		assertEquals(m_pscope.string().make(""), v);
	}
	
	@Test
	public void string_encode_nonempty_string() throws Exception {
		m_enc.encode(m_pscope.string().make("foobar"), m_doutput);
		StringValue v = (StringValue) m_enc.decode(make_din(), m_pscope);
		assertEquals(m_pscope.string().make("foobar"), v);
	}
	
	@Test
	public void string_encode_nonempty_unicode_string() throws Exception {
		m_enc.encode(m_pscope.string().make("foobar\u00ae"), m_doutput);
		StringValue v = (StringValue) m_enc.decode(make_din(), m_pscope);
		assertEquals(m_pscope.string().make("foobar\u00ae"), v);
	}
	
	@Test
	public void string_random_decode_invalid_values() throws Exception {
		for (int i = 0; i < RANDOM_TESTS; i++) {
			m_output.reset();
			StringValue bv = m_pscope.string().make(
					RandomStringUtils.random(10));
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
	public void string_performance_test() throws Exception {
		DataValue[] v = new DataValue[1000];
		for (int i = 0; i < v.length; i++) {
			v[i] = m_pscope.string().make(RandomStringUtils.random(
					1 + RandomUtils.nextInt(18)));
		}
		
		double unit_value = encode_decode(v, 500);
		System.out.println("String (avg. 10 chars) encoding time: "
				+ unit_value + "ms.");
		
		/*
		 * 50us is OK.
		 */
		assertTrue(0.05 > unit_value);
	}
	
	@Test
	public void ascii_performance_test() throws Exception {
		DataValue[] v = new DataValue[1000];
		for (int i = 0; i < v.length; i++) {
			v[i] = m_pscope.ascii().make(RandomStringUtils.randomAlphabetic(
					1 + RandomUtils.nextInt(18)));
		}
		
		double unit_value = encode_decode(v, 500);
		System.out.println("Ascii (avg. 10 chars) encoding time: "
				+ unit_value + "ms.");
		
		/*
		 * 50us is OK.
		 */
		assertTrue(0.05 > unit_value);
	}
}
