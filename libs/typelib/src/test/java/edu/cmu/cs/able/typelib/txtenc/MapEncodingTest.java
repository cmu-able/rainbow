package edu.cmu.cs.able.typelib.txtenc;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import edu.cmu.cs.able.typelib.comp.MapDataType;
import edu.cmu.cs.able.typelib.comp.MapDataValue;
import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Tests map encoding and decoding.
 */
@SuppressWarnings("javadoc")
public class MapEncodingTest extends AbstractEncodingTestCase {
	/**
	 * Map data type mapping an int32 to a string.
	 */
	private MapDataType m_int32_to_string;
	
	@Before
	public void map_set_up() throws Exception {
		m_int32_to_string = MapDataType.map_of(m_pscope.int32(),
				m_pscope.string(), m_pscope);
	}
	
	@Test
	public void encode_decode_empty_map() throws Exception {
		MapDataValue v = m_int32_to_string.make();
		m_enc.encode(v, m_doutput);
		DataValue dv = m_enc.decode(make_din(), m_pscope);
		assertEquals(v, dv);
	}
	
	@Test
	public void encode_decode_map_with_data() throws Exception {
		MapDataValue v = m_int32_to_string.make();
		v.put(m_pscope.int32().make(34), m_pscope.string().make("foo"));
		m_enc.encode(v, m_doutput);
		DataValue dv = m_enc.decode(make_din(), m_pscope);
		assertEquals(v, dv);
	}
	
	@Test
	public void encode_decode_corrupt_map() throws Exception {
		MapDataValue v = m_int32_to_string.make();
		v.put(m_pscope.int32().make(34), m_pscope.string().make("foo"));
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
