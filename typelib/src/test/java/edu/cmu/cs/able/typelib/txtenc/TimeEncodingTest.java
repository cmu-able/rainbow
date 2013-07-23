package edu.cmu.cs.able.typelib.txtenc;

import java.io.IOException;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;

import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;
import edu.cmu.cs.able.typelib.prim.PeriodValue;
import edu.cmu.cs.able.typelib.prim.TimeValue;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Tests time and period encoding.
 */
@SuppressWarnings("javadoc")
public class TimeEncodingTest extends AbstractEncodingTestCase {
	@Test
	public void time_encode_positive_number() throws Exception {
		m_enc.encode(m_pscope.time().make(5), m_doutput);
		TimeValue v = (TimeValue) m_enc.decode(make_din(), m_pscope);
		assertEquals(m_pscope.time().make(5), v);
	}
	
	@Test
	public void time_encode_zero() throws Exception {
		m_enc.encode(m_pscope.time().make(0), m_doutput);
		TimeValue v = (TimeValue) m_enc.decode(make_din(), m_pscope);
		assertEquals(m_pscope.time().make(0), v);
	}
	
	@Test
	public void time_random_decode_invalid_values() throws Exception {
		for (int i = 0; i < RANDOM_TESTS; i++) {
			m_output.reset();
			long l;
			do {
				l = RandomUtils.nextLong();
			} while (l < 0);
			TimeValue bv = m_pscope.time().make(l);
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
	public void period_encode_positive_number() throws Exception {
		m_enc.encode(m_pscope.period().make(5), m_doutput);
		PeriodValue v = (PeriodValue) m_enc.decode(make_din(), m_pscope);
		assertEquals(m_pscope.period().make(5), v);
	}
	
	@Test
	public void period_encode_zero() throws Exception {
		m_enc.encode(m_pscope.period().make(0), m_doutput);
		PeriodValue v = (PeriodValue) m_enc.decode(make_din(), m_pscope);
		assertEquals(m_pscope.period().make(0), v);
	}
	
	@Test
	public void period_encode_negative_number() throws Exception {
		m_enc.encode(m_pscope.period().make(-5), m_doutput);
		PeriodValue v = (PeriodValue) m_enc.decode(make_din(), m_pscope);
		assertEquals(m_pscope.period().make(-5), v);
	}
	
	@Test
	public void period_random_decode_invalid_values() throws Exception {
		for (int i = 0; i < RANDOM_TESTS; i++) {
			m_output.reset();
			PeriodValue bv = m_pscope.period().make(RandomUtils.nextLong());
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
	public void time_performance_test() throws Exception {
		DataValue[] v = new DataValue[1000];
		for (int i = 0; i < v.length; i++) {
			long l = RandomUtils.nextLong();
			if (l < 0) {
				l = -l;
			}
			
			v[i] = m_pscope.time().make(l);
		}
		
		double unit_value = encode_decode(v, 500) * 1000;
		System.out.println("Time encoding time: " + unit_value + "us.");
		
		/*
		 * 50us is OK.
		 */
		assertTrue(50 > unit_value);
	}

	@Test
	public void period_performance_test() throws Exception {
		DataValue[] v = new DataValue[1000];
		for (int i = 0; i < v.length; i++) {
			v[i] = m_pscope.time().make(RandomUtils.nextLong());
		}
		
		double unit_value = encode_decode(v, 500) * 1000;
		System.out.println("Period encoding time: " + unit_value + "us.");
		
		/*
		 * 50us is OK.
		 */
		assertTrue(50 > unit_value);
	}
}
