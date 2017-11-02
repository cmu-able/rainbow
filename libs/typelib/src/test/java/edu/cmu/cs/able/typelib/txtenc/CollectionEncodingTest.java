package edu.cmu.cs.able.typelib.txtenc;

import java.io.IOException;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.Before;
import org.junit.Test;

import edu.cmu.cs.able.typelib.comp.BagDataType;
import edu.cmu.cs.able.typelib.comp.BagDataValue;
import edu.cmu.cs.able.typelib.comp.ListDataType;
import edu.cmu.cs.able.typelib.comp.ListDataValue;
import edu.cmu.cs.able.typelib.comp.SetDataType;
import edu.cmu.cs.able.typelib.comp.SetDataValue;
import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;

/**
 * Tests encoding of collections: sets, lists and bags.
 */
@SuppressWarnings("javadoc")
public class CollectionEncodingTest extends AbstractEncodingTestCase {
	private SetDataType m_set_of_bool;
	private ListDataType m_list_of_bool;
	private BagDataType m_bag_of_bool;
	
	@Before
	public void collection_set_up() {
		m_set_of_bool = new SetDataType(m_pscope.bool(), m_pscope.any());
		m_pscope.add(m_set_of_bool);
		m_list_of_bool = new ListDataType(m_pscope.bool(), m_pscope.any());
		m_pscope.add(m_list_of_bool);
		m_bag_of_bool = new BagDataType(m_pscope.bool(), m_pscope.any());
		m_pscope.add(m_bag_of_bool);
	}
	
	@Test
	public void encode_decode_empty_set() throws Exception {
		SetDataValue set = m_set_of_bool.make();
		m_enc.encode(set, m_doutput);
		SetDataValue v = (SetDataValue) m_enc.decode(make_din(), m_pscope);
		assertEquals(set, v);
	}
	
	@Test
	public void encode_decode_set() throws Exception {
		SetDataValue set = m_set_of_bool.make();
		set.add(m_pscope.bool().make(true));
		set.add(m_pscope.bool().make(false));
		
		m_enc.encode(set, m_doutput);
		SetDataValue v = (SetDataValue) m_enc.decode(make_din(), m_pscope);
		assertEquals(set, v);
	}
	
	@Test
	public void random_decode_invalid_set() throws Exception {
		for (int i = 0; i < RANDOM_TESTS; i++) {
			m_output.reset();
			SetDataValue set = m_set_of_bool.make();
			int ri = RandomUtils.nextInt(5);
			for (int j = 0; j < ri; j++) {
				set.add(m_pscope.bool().make(RandomUtils.nextBoolean()));
			}
			
			m_enc.encode(set, m_doutput);
			try {
				m_enc.decode(make_corrupt_din(), m_pscope);
				fail();
			} catch (InvalidEncodingException|AssertionError|IOException e) {
				/*
				 * Expected.
				 */
			}
		}
	}
	
	@Test
	public void encode_decode_empty_list() throws Exception {
		ListDataValue list = m_list_of_bool.make();
		m_enc.encode(list, m_doutput);
		ListDataValue v = (ListDataValue) m_enc.decode(make_din(), m_pscope);
		assertEquals(list, v);
	}
	
	@Test
	public void encode_decode_list() throws Exception {
		ListDataValue list = m_list_of_bool.make();
		list.add(m_pscope.bool().make(true));
		list.add(m_pscope.bool().make(false));
		
		m_enc.encode(list, m_doutput);
		ListDataValue v = (ListDataValue) m_enc.decode(make_din(), m_pscope);
		assertEquals(list, v);
	}
	
	@Test
	public void random_decode_invalid_list() throws Exception {
		for (int i = 0; i < RANDOM_TESTS; i++) {
			m_output.reset();
			ListDataValue list = m_list_of_bool.make();
			int ri = RandomUtils.nextInt(5);
			for (int j = 0; j < ri; j++) {
				list.add(m_pscope.bool().make(RandomUtils.nextBoolean()));
			}
			
			m_enc.encode(list, m_doutput);
			try {
				m_enc.decode(make_corrupt_din(), m_pscope);
				fail();
			} catch (InvalidEncodingException|AssertionError|IOException e) {
				/*
				 * Expected.
				 */
			}
		}
	}
	
	@Test
	public void encode_decode_empty_bag() throws Exception {
		BagDataValue bag = m_bag_of_bool.make();
		m_enc.encode(bag, m_doutput);
		BagDataValue v = (BagDataValue) m_enc.decode(make_din(), m_pscope);
		assertEquals(bag, v);
	}
	
	@Test
	public void encode_decode_bag() throws Exception {
		BagDataValue bag = m_bag_of_bool.make();
		bag.add(m_pscope.bool().make(true));
		bag.add(m_pscope.bool().make(false));
		
		m_enc.encode(bag, m_doutput);
		BagDataValue v = (BagDataValue) m_enc.decode(make_din(), m_pscope);
		assertEquals(bag, v);
	}
	
	@Test
	public void random_decode_invalid_bag() throws Exception {
		for (int i = 0; i < RANDOM_TESTS; i++) {
			m_output.reset();
			BagDataValue bag = m_bag_of_bool.make();
			int ri = RandomUtils.nextInt(5);
			for (int j = 0; j < ri; j++) {
				bag.add(m_pscope.bool().make(RandomUtils.nextBoolean()));
			}
			
			m_enc.encode(bag, m_doutput);
			try {
				m_enc.decode(make_corrupt_din(), m_pscope);
				fail();
			} catch (InvalidEncodingException|AssertionError|IOException e) {
				/*
				 * Expected.
				 */
			}
		}
	}
}
