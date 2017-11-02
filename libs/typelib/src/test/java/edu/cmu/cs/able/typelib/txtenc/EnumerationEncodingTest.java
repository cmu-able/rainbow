package edu.cmu.cs.able.typelib.txtenc;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;
import edu.cmu.cs.able.typelib.enumeration.EnumerationType;
import edu.cmu.cs.able.typelib.enumeration.EnumerationValue;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Tests encoding of strings.
 */
@SuppressWarnings("javadoc")
public class EnumerationEncodingTest extends AbstractEncodingTestCase {
	@Test
	public void encode_decode_enumeration() throws Exception {
		Set<String> values = new HashSet<>();
		values.add("x");
		EnumerationType t = EnumerationType.make("foo", values,
				m_pscope.any());
		m_pscope.add(t);
		EnumerationValue x = t.value("x");
		m_enc.encode(x, m_doutput);
		EnumerationValue xx = (EnumerationValue) m_enc.decode(make_din(),
				m_pscope);
		assertEquals(x, xx);
	}
	
	@Test
	public void decode_invalid_enumeration() throws Exception {
		Set<String> values = new HashSet<>();
		values.add("x");
		EnumerationType t = EnumerationType.make("foo", values,
				m_pscope.any());
		m_pscope.add(t);
		EnumerationValue x = t.value("x");
		
		for (int i = 0; i < RANDOM_TESTS; i++) {
			m_output.reset();
			m_enc.encode(x, m_doutput);
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
	public void enumeration_performance_test() throws Exception {
		Set<String> values = new HashSet<>();
		values.add("x");
		EnumerationType t = EnumerationType.make("foo", values,
				m_pscope.any());
		m_pscope.add(t);

		DataValue[] v = new DataValue[1000];
		for (int i = 0; i < v.length; i++) {
			v[i] = t.value("x");
		}
		
		double unit_value = encode_decode(v, 500) * 1000;
		System.out.println("Enumeration (1 chars) encoding time: "
				+ unit_value + "us.");
		
		/*
		 * 50us is OK.
		 */
		assertTrue(50 > unit_value);
	}
}
