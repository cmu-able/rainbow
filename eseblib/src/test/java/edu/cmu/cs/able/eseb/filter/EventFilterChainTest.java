package edu.cmu.cs.able.eseb.filter;

import org.junit.Before;
import org.junit.Test;

import edu.cmu.cs.able.eseb.BusData;

/**
 * Test cases for event chains.
 */
@SuppressWarnings("javadoc")
public class EventFilterChainTest extends FilterTestCase {
	private EventFilterChain m_chain;
	
	@Before
	public void set_up() {
		m_chain = new EventFilterChain(m_sink);
	}
	
	@Test
	public void process_empty_event_chain() throws Exception {
		BusData d = bus_data();
		m_chain.sink(d);
		assertEquals(1, m_sink.m_data.size());
		assertSame(d, m_sink.m_data.get(0));
	}
	
	@Test
	public void obtain_chain_sink() throws Exception {
		assertSame(m_sink, m_chain.chain_sink());
	}
	
	@Test
	public void multiple_chains_same_sink() throws Exception {
		assertSame(m_sink, m_chain.chain_sink());
		assertSame(m_sink, new EventFilterChain(m_sink).chain_sink());
	}
	
	@Test
	public void add_filter_to_chain_and_process() throws Exception {
		BusData d1 = bus_data();
		BusData d2 = bus_data();
		m_chain.add_filter(m_filter);
		m_filter.m_forward = true;
		m_chain.sink(d1);
		assertEquals(1, m_sink.m_data.size());
		assertSame(d1, m_sink.m_data.get(0));
		m_filter.m_forward = false;
		m_chain.sink(d2);
		assertEquals(1, m_sink.m_data.size());
		assertSame(d1, m_sink.m_data.get(0));
	}

	@Test
	public void remove_filter_from_chain() throws Exception {
		m_chain.add_filter(m_filter);
		m_filter.m_forward = false;
		m_chain.remove_filter(m_filter);
		m_chain.sink(bus_data());
		assertEquals(1, m_sink.m_data.size());
	}
	
	@Test
	public void remove_middle_filter_from_chain() throws Exception {
		TestEventFilter first = new TestEventFilter();
		first.m_forward = true;
		TestEventFilter last = new TestEventFilter();
		last.m_forward = true;
		m_chain.add_filter(first);
		m_chain.add_filter(m_filter);
		m_chain.add_filter(last);
		m_filter.m_forward = false;
		m_chain.sink(bus_data());
		assertEquals(0, m_sink.m_data.size());
		assertEquals(3, m_chain.filters().size());
		
		m_chain.remove_filter(m_filter);
		m_chain.sink(bus_data());
		assertEquals(1, m_sink.m_data.size());
		assertEquals(2, m_chain.filters().size());
	}
	
	@Test
	public void add_removed_filter_from_chain() throws Exception {
		m_filter.m_forward = false;
		m_chain.add_filter(m_filter);
		m_chain.remove_filter(m_filter);
		m_chain.add_filter(m_filter);
		m_chain.sink(bus_data());
		assertEquals(0, m_sink.m_data.size());
	}
	
	@Test
	public void clear_chain() throws Exception {
		m_filter.m_forward = false;
		m_chain.add_filter(m_filter);
		m_chain.clear();
		m_chain.sink(bus_data());
		assertEquals(1, m_sink.m_data.size());
	}
	
	@Test
	public void obtaining_chain_filter_list() throws Exception {
		assertEquals(0, m_chain.filters().size());
		m_chain.add_filter(m_filter);
		assertEquals(1, m_chain.filters().size());
		assertSame(m_filter, m_chain.filters().get(0));
	}
	
	@Test
	public void obtaining_chain_information() throws Exception {
		m_chain.add_filter(m_filter);
		EventFilterChainInfo info = m_chain.info();
		assertEquals(1, info.filters().size());
		assertEquals(TestEventFilter.class.getName(),
				info.filters().get(0).filter_class());
	}
	
	@Test(expected = AssertionError.class)
	public void adding_null_filter_to_chain() throws Exception {
		m_chain.add_filter(null);
	}
	
	@Test(expected = AssertionError.class)
	public void adding_filter_twice_to_same_chain() throws Exception {
		m_chain.add_filter(m_filter);
		m_chain.add_filter(m_filter);
	}
	
	@Test(expected = AssertionError.class)
	public void adding_filter_in_another_chain() throws Exception {
		m_chain.add_filter(m_filter);
		new EventFilterChain(m_sink).add_filter(m_filter);
	}
	
	@Test(expected = AssertionError.class)
	public void removing_null_filter_from_chain() throws Exception {
		m_chain.remove_filter(null);
	}
	
	@Test(expected = AssertionError.class)
	public void removed_filter_not_in_chain() throws Exception {
		m_chain.remove_filter(m_filter);
	}
	
	@Test(expected = AssertionError.class)
	public void processing_null() throws Exception {
		m_chain.sink(null);
	}
	
	@Test(expected = AssertionError.class)
	@SuppressWarnings("unused")
	public void create_chain_with_null_sink() throws Exception {
		new EventFilterChain(null);
	}
	
	@Test(expected = AssertionError.class)
	@SuppressWarnings("unused")
	public void create_event_chain_info_with_null_chain() throws Exception {
		new EventFilterChainInfo(null);
	}
	
	@Test
	public void equals_and_hash_code() throws Exception {
		SaveSink ss = new SaveSink();
		EventFilterChain c1 = new EventFilterChain(ss);
		EventFilterChain c2 = new EventFilterChain(ss);
		
		EventFilterChainInfo i1 = new EventFilterChainInfo(c1);
		EventFilterChainInfo i2 = new EventFilterChainInfo(c2);
		
		assertEquals(i1, i2);
		assertEquals(i1.hashCode(), i2.hashCode());
		
		c1.add_filter(new TestEventFilter());
		i1 = new EventFilterChainInfo(c1);
		
		assertFalse(i1.equals(i2));
		assertNotSame(i1.hashCode(), i2.hashCode());
		
		assertFalse(i1.equals(null));
		assertFalse(i1.equals(new Integer(2)));
		assertTrue(i1.equals(i1));
	}
}
