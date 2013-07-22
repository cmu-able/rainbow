package edu.cmu.cs.able.eseb.filter;

import java.io.IOException;

import org.junit.Test;

import edu.cmu.cs.able.eseb.BusData;

/**
 * Test suite that verifies abstract filter behavior.
 */
@SuppressWarnings("javadoc")
public class EventFilterTest extends FilterTestCase {
	@Test
	public void setting_getting_filter_sink() throws Exception {
		assertNull(m_filter.connected());
		m_filter.connect(m_sink);
		assertSame(m_sink, m_filter.connected());
	}
	
	@Test
	public void forwarding_with_sink_set() throws Exception {
		m_filter.connect(m_sink);
		m_filter.m_forward = true;
		BusData d = bus_data();
		m_filter.sink(d);
		assertEquals(1, m_sink.m_data.size());
		assertSame(d, m_sink.m_data.get(0));
	}
	
	@Test
	public void forwarding_without_sink_set() throws Exception {
		m_filter.m_forward = true;
		m_filter.sink(bus_data());
	}
	
	@Test(expected = AssertionError.class)
	public void setting_sink_with_sink_already_set() throws Exception {
		m_filter.connect(m_sink);
		m_filter.connect(m_sink);
	}
	
	@Test
	public void resetting_sink_legally() throws Exception {
		m_filter.connect(m_sink);
		m_filter.connect(null);
		m_filter.connect(null);
		m_filter.connect(m_sink);
	}
	
	@Test
	public void obtaining_filter_information() throws Exception {
		EventFilterInfo i = m_filter.info();
		assertEquals(TestEventFilter.class.getName(), i.filter_class());
	}
	
	@Test
	public void lock_unlock_filter() throws Exception {
		assertFalse(m_filter.locked());
		Object l = new Object();
		m_filter.lock(l);
		assertTrue(m_filter.locked());
		m_filter.unlock(l);
		assertFalse(m_filter.locked());
	}
	
	@Test(expected = AssertionError.class)
	public void event_filter_forward_null() throws Exception {
		m_filter.forward(null);
	}
	
	@Test(expected = AssertionError.class)
	@SuppressWarnings("unused")
	public void event_info_create_with_null_event() throws Exception {
		new EventFilterInfo(null);
	}

	@Test(expected = AssertionError.class)
	public void cannot_lock_with_null_lock() throws Exception {
		m_filter.lock(null);
	}

	@Test(expected = AssertionError.class)
	public void cannot_unlock_with_null_lock() throws Exception {
		m_filter.unlock(null);
	}

	@Test(expected = AssertionError.class)
	public void cannot_unlock_with_different_lock() throws Exception {
		m_filter.lock(new Object());
		m_filter.unlock(new Object());
	}
	
	@Test(expected = AssertionError.class)
	public void cannot_change_sink_when_locked() throws Exception {
		m_filter.lock(new Object());
		m_filter.connect(m_sink);
	}
	
	@Test
	public void equals_and_hash_code() throws Exception {
		EventFilter f1 = new TestEventFilter();
		EventFilter f2 = new EventFilter() {
			@Override
			public void sink(BusData data) throws IOException {
				/* */
			}
		};
		
		EventFilterInfo i1 = new EventFilterInfo(f1);
		EventFilterInfo i2 = new EventFilterInfo(f1);
		EventFilterInfo i3 = new EventFilterInfo(f2);
		
		assertEquals(i1.hashCode(), i2.hashCode());
		assertEquals(i1, i2);
		assertNotSame(i1.hashCode(), i3.hashCode());
		assertFalse(i1.equals(i3));
		
		assertFalse(i1.equals(null));
		assertFalse(i1.equals(new Integer(3)));
		assertTrue(i1.equals(i1));
	}
}
