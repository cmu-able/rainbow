package edu.cmu.cs.able.eseb.participant;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import auxtestlib.DefaultTCase;
import edu.cmu.cs.able.eseb.participant.ParticipantTypes;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.txtenc.typelib.DefaultTextEncoding;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Test suite for participant data types.
 */
@SuppressWarnings("javadoc")
public class ParticipantTypeTest extends DefaultTCase {
	/**
	 * The primitive type scope.
	 */
	private PrimitiveScope m_pscope;
	
	/**
	 * The participant data types.
	 */
	private ParticipantTypes m_types;
	
	/**
	 * Text encoding.
	 */
	private DefaultTextEncoding m_enc;
	
	@Before
	public void set_up() throws Exception {
		m_pscope = new PrimitiveScope();
		m_enc = new DefaultTextEncoding(m_pscope);
		m_types = new ParticipantTypes(m_pscope, m_enc);
	}
	
	@Test
	public void announce_write_and_read_no_meta_data() throws Exception {
		DataValue v = m_types.announce(34, null);
		assertNotNull(v);
		assertEquals(34, m_types.announce_id(v));
		assertEquals(0, m_types.announce_meta_data_keys(v).size());
	}
	
	@Test
	public void announce_write_read_with_meta_data() throws Exception {
		DataValue md1 = m_pscope.ascii().make("foo");
		DataValue md2 = m_pscope.int8().make((byte) 9);
		Map<String, DataValue> md = new HashMap<>();
		md.put("A", md1);
		md.put("B", md2);
		DataValue v = m_types.announce(-8, md);
		
		assertTrue(m_types.is_announce(v));
		assertEquals(-8, m_types.announce_id(v));
		Set<String> ks = m_types.announce_meta_data_keys(v);
		assertEquals(2, ks.size());
		assertTrue(ks.contains("A"));
		assertTrue(ks.contains("B"));
		assertEquals(md1, m_types.announce_meta_data(v, "A"));
		assertEquals(md2, m_types.announce_meta_data(v, "B"));
	}
	
	@Test
	public void announce_check_valid_value() throws Exception {
		DataValue v = m_types.announce(34, null);
		assertNotNull(v);
		assertTrue(m_types.is_announce(v));
	}
	
	@Test
	public void announce_check_invalid_value() throws Exception {
		assertFalse(m_types.is_announce(m_pscope.int64().make(7)));
	}
}
