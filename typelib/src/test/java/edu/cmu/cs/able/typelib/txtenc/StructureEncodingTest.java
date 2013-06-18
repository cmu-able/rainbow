package edu.cmu.cs.able.typelib.txtenc;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;
import edu.cmu.cs.able.typelib.struct.Field;
import edu.cmu.cs.able.typelib.struct.FieldDescription;
import edu.cmu.cs.able.typelib.struct.StructureDataType;
import edu.cmu.cs.able.typelib.struct.StructureDataValue;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Tests structure encoding.
 */
@SuppressWarnings("javadoc")
public class StructureEncodingTest extends AbstractEncodingTestCase {
	private StructureDataType m_simple;
	private StructureDataValue m_simple_v;
	private StructureDataType m_complex;
	private StructureDataValue m_complex_v;
	
	@Before
	public void set_up_structure() throws Exception {
		FieldDescription f1 = new FieldDescription("f1", m_pscope.int32());
		Set<FieldDescription> fs = new HashSet<>();
		fs.add(f1);
		
		m_simple = new StructureDataType("simple", false, fs, m_pscope.any());
		m_pscope.add(m_simple);
		
		Map<Field, DataValue> mv = new HashMap<>();
		mv.put(m_simple.field("f1"), m_pscope.int32().make(40));
		
		m_simple_v = m_simple.make(mv);
		
		fs.add(new FieldDescription("f2", m_simple));
		
		m_complex = new StructureDataType("complex", false, fs,
				m_pscope.any());
		m_pscope.add(m_complex);
		
		mv = new HashMap<>();
		mv.put(m_complex.field("f1"), m_pscope.int32().make(50));
		mv.put(m_complex.field("f2"), m_simple_v);
		
		m_complex_v = m_complex.make(mv);
	}
	
	@Test
	public void simple_encoding_and_decoding() throws Exception {
		m_enc.encode(m_simple_v, m_doutput);
		DataValue v = m_enc.decode(make_din(), m_pscope);
		assertEquals(m_simple_v, v);
	}
	
	@Test
	public void complex_encoding_and_decoding() throws Exception {
		m_enc.encode(m_complex_v, m_doutput);
		DataValue v = m_enc.decode(make_din(), m_pscope);
		assertEquals(m_complex_v, v);
	}
	
	@Test
	public void decoding_corrupt_simple_structure() throws Exception {
		for (int i = 0; i < RANDOM_TESTS; i++) {
			m_output.reset();
			m_enc.encode(m_simple_v, m_doutput);
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
	public void decoding_corrupt_complex_structure() throws Exception {
		for (int i = 0; i < RANDOM_TESTS; i++) {
			m_output.reset();
			m_enc.encode(m_complex_v, m_doutput);
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
}
