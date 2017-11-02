package edu.cmu.cs.able.typelib.txtenc;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import edu.cmu.cs.able.typelib.comp.TupleDataType;
import edu.cmu.cs.able.typelib.comp.TupleDataValue;
import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Tests tuple encoding and decoding.
 */
@SuppressWarnings("javadoc")
public class TupleEncodingTest extends AbstractEncodingTestCase {
	/**
	 * Tuple data type with an int32 inside.
	 */
	private TupleDataType m_int32;
	
	/**
	 * Tuple data type with an int32 and an int16 inside.
	 */
	private TupleDataType m_int32_int16;
	
	@Before
	public void tuple_set_up() throws Exception {
		m_int32 = new TupleDataType(Arrays.asList((DataType) m_pscope.int32()),
				m_pscope.any());
		m_pscope.add(m_int32);
		m_int32_int16 = new TupleDataType(Arrays.asList(
				m_pscope.int32(), m_pscope.int16()), m_pscope.any());
		m_pscope.add(m_int32_int16);
	}
	
	@Test
	public void encode_decode_simple_tuple() throws Exception {
		TupleDataValue v = m_int32.make(Arrays.asList(
				(DataValue) m_pscope.int32().make(5)));
		
		m_enc.encode(v, m_doutput);
		DataValue dv = m_enc.decode(make_din(), m_pscope);
		assertEquals(v, dv);
	}
	
	@Test
	public void encode_decode_corrupt_simple_tuple() throws Exception {
		TupleDataValue v = m_int32.make(Arrays.asList(
				(DataValue) m_pscope.int32().make(5)));
		
		m_enc.encode(v, m_doutput);
		
		for (int i = 0; i < 50; i++) {
			try {
				m_enc.decode(make_corrupt_din(), m_pscope);
			} catch (IOException | InvalidEncodingException
					| AssertionError e) {
				/*
				 * Either is OK.
				 */
			}
		}
	}
	
	@Test
	public void encode_decode_complex_tuple() throws Exception {
		TupleDataValue v = m_int32_int16.make(Arrays.asList(
				(DataValue) m_pscope.int32().make(5),
				m_pscope.int16().make((short) 3)));
		
		m_enc.encode(v, m_doutput);
		DataValue dv = m_enc.decode(make_din(), m_pscope);
		assertEquals(v, dv);
	}
	
	@Test
	public void encode_decode_corrupt_complex_tuple() throws Exception {
		TupleDataValue v = m_int32_int16.make(Arrays.asList(
				(DataValue) m_pscope.int32().make(5),
				m_pscope.int16().make((short) 3)));
		
		m_enc.encode(v, m_doutput);
		
		for (int i = 0; i < 50; i++) {
			try {
				m_enc.decode(make_corrupt_din(), m_pscope);
			} catch (IOException | InvalidEncodingException
					| AssertionError e) {
				/*
				 * Either is OK.
				 */
			}
		}
	}
}
