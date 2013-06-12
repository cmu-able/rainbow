package edu.cmu.cs.able.typelib.txtenc;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import edu.cmu.cs.able.typelib.comp.OptionalDataType;
import edu.cmu.cs.able.typelib.comp.OptionalDataValue;
import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Test case that checks the encoding of optional values.
 */
@SuppressWarnings("javadoc")
public class OptionalEncodingTestCase extends AbstractEncodingTestCase {
	/**
	 * "any?" type.
	 */
	private OptionalDataType m_any_opt;
	
	/**
	 * "int32?" type.
	 */
	private OptionalDataType m_int32_opt;
	
	@Before
	public void optional_set_up() throws Exception {
		m_any_opt = new OptionalDataType(m_pscope.any(),
				new HashSet<OptionalDataType>());
		m_pscope.add(m_any_opt);
		m_int32_opt = new OptionalDataType(m_pscope.int32(),
				new HashSet<>(Arrays.asList(m_any_opt)));
		m_pscope.add(m_int32_opt);
	}
	
	@Test
	public void encode_decode_non_null_optional_value() throws Exception {
		OptionalDataValue v1 = m_int32_opt.make(m_pscope.int32().make(-17));
		
		m_enc.encode(v1, m_doutput);
		DataValue dv = m_enc.decode(make_din(), m_pscope);
		assertEquals(v1, dv);
	}
	
	@Test
	public void encode_decode_corrupt_non_null_optional_value()
			throws Exception {
		OptionalDataValue v1 = m_int32_opt.make(m_pscope.int32().make(-17));
		
		m_enc.encode(v1, m_doutput);
		
		for (int i = 0; i < 50; i++) {
			try {
				m_enc.decode(make_corrupt_din(), m_pscope);
			} catch (IOException | InvalidEncodingException
					| AssertionError e) {
				/*
				 * Expected.
				 */
			}
		}
	}
	
	@Test
	public void encode_decode_null_optional_value() throws Exception {
		OptionalDataValue v1 = m_int32_opt.make(null);
		
		m_enc.encode(v1, m_doutput);
		DataValue dv = m_enc.decode(make_din(), m_pscope);
		assertEquals(v1, dv);
	}
	
	@Test
	public void encode_decode_corrupt_null_optional_value()
			throws Exception {
		OptionalDataValue v1 = m_int32_opt.make(null);
		
		m_enc.encode(v1, m_doutput);
		
		for (int i = 0; i < 50; i++) {
			try {
				m_enc.decode(make_corrupt_din(), m_pscope);
			} catch (IOException | InvalidEncodingException
					| AssertionError e) {
				/*
				 * Expected.
				 */
			}
		}
	}
}
