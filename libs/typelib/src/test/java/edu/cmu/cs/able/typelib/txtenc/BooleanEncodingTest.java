package edu.cmu.cs.able.typelib.txtenc;

import java.io.IOException;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;

import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;
import edu.cmu.cs.able.typelib.prim.BooleanValue;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Tests boolean encoding.
 */
@SuppressWarnings("javadoc")
public class BooleanEncodingTest extends AbstractEncodingTestCase {
	@Test
	public void encode_decode_true() throws Exception {
		m_enc.encode(m_pscope.bool().make(true), m_doutput);
		BooleanValue v = (BooleanValue) m_enc.decode(make_din(), m_pscope);
		assertEquals(m_pscope.bool().make(true), v);
	}
	
	@Test
	public void encode_decode_false() throws Exception {
		m_enc.encode(m_pscope.bool().make(false), m_doutput);
		BooleanValue v = (BooleanValue) m_enc.decode(make_din(), m_pscope);
		assertEquals(m_pscope.bool().make(false), v);
	}
	
	@Test
	public void random_decode_invalid_values() throws Exception {
		for (int i = 0; i < RANDOM_TESTS; i++) {
			m_output.reset();
			BooleanValue bv = m_pscope.bool().make(RandomUtils.nextBoolean());
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
	public void performance_test() throws Exception {
		DataValue[] v = new DataValue[1000];
		for (int i = 0; i < v.length; i++) {
			v[i] = m_pscope.bool().make(RandomUtils.nextBoolean());
		}
		
		double unit_value = encode_decode(v, 500) * 1000;
		System.out.println("Boolean encoding time: " + unit_value + "us.");
		
		/*
		 * 50us is OK.
		 */
		assertTrue(50 > unit_value);
	}
}
