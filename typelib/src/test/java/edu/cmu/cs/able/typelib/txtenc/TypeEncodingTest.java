package edu.cmu.cs.able.typelib.txtenc;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import edu.cmu.cs.able.typelib.comp.SetDataType;
import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;
import edu.cmu.cs.able.typelib.prim.TypeValue;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Tests type reference encoding.
 */
@SuppressWarnings("javadoc")
public class TypeEncodingTest extends AbstractEncodingTestCase {
	/**
	 * Type that references an int32.
	 */
	private TypeValue m_int32_ref;
	
	private TypeValue m_set_int32_ref;
	
	@Before
	public void type_set_up() throws Exception {
		m_int32_ref = m_pscope.type().make(m_pscope.int32());
		m_set_int32_ref = m_pscope.type().make(SetDataType.set_of(
				m_pscope.int32(), m_pscope));
	}
	
	@Test
	public void encode_decode_primitive_type_type() throws Exception {
		m_enc.encode(m_int32_ref, m_doutput);
		DataValue dv = m_enc.decode(make_din(), m_pscope);
		assertEquals(m_int32_ref, dv);
	}
	
	@Test
	public void encode_decode_set_type_type() throws Exception {
		m_enc.encode(m_set_int32_ref, m_doutput);
		DataValue dv = m_enc.decode(make_din(), m_pscope);
		assertEquals(m_set_int32_ref, dv);
	}
	
	@Test
	public void encode_decode_corrupt_primitive_type_type() throws Exception {
		m_enc.encode(m_int32_ref, m_doutput);
		
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
	public void encode_decode_corrupt_set_type_type() throws Exception {
		m_enc.encode(m_set_int32_ref, m_doutput);
		
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
