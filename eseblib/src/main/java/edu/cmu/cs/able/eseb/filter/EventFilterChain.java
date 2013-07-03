package edu.cmu.cs.able.eseb.filter;

import incubator.pval.Ensure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.cmu.cs.able.eseb.BusData;

/**
 * A chain of event filters has many filters connected to each other and
 * ends with a sink. The chain allows filters to be added or removed. Although
 * filters can be manipulated 
 */
public class EventFilterChain implements EventSink {
	/**
	 * The chain sink.
	 */
	private EventSink m_sink;
	
	/**
	 * Event filters. The first filter in the chain is the last filter
	 * in the list.
	 */
	private List<EventFilter> m_filters;
	
	/**
	 * Creates a new chain with the given sink.
	 * @param sink the sink
	 */
	public EventFilterChain(EventSink sink) {
		Ensure.not_null(sink);
		m_sink = sink;
		m_filters = new ArrayList<>();
	}
	
	/**
	 * Obtains the chain's sink.
	 * @return the sink
	 */
	public synchronized EventSink chain_sink() {
		return m_sink;
	}
	
	/**
	 * Obtains all filters in the chain.
	 * @return all filters
	 */
	public synchronized List<EventFilter> filters() {
		List<EventFilter> r = new ArrayList<>(m_filters);
		Collections.reverse(r);
		return r;
	}
	
	/**
	 * Obtains the first sink in the chain.
	 * @return the first sink
	 */
	private EventSink first() {
		EventSink first = m_sink;
		if (m_filters.size() > 0) {
			first = m_filters.get(m_filters.size() - 1);
		}
		
		return first;
	}
	
	/**
	 * Adds a new filter to the chain.
	 * @param f the filter to add
	 */
	public synchronized void add_filter(EventFilter f) {
		Ensure.not_null(f);
		Ensure.is_false(f.locked());
		
		f.connect(first());
		m_filters.add(f);
		f.lock(m_filters);
	}
	
	/**
	 * Removes a filter from the chain.
	 * @param f the filter to remove
	 */
	public synchronized void remove_filter(EventFilter f) {
		Ensure.not_null(f);
		Ensure.is_true(m_filters.contains(f));
		
		int idx = m_filters.indexOf(f);
		EventFilter prev = null;
		if (idx != (m_filters.size() - 1)) {
			prev = m_filters.get(idx + 1);
		}
		
		if (prev != null) {
			prev.unlock(m_filters);
			prev.connect(null);
			prev.connect(f.connected());
			prev.lock(m_filters);
		}
		
		f.unlock(m_filters);
		f.connect(null);
		m_filters.remove(f);
	}
	
	/**
	 * Removes all filters from the chain.
	 */
	public synchronized void clear() {
		while (m_filters.size() > 0) {
			remove_filter(m_filters.get(m_filters.size() - 1));
		}
	}
	
	@Override
	public synchronized void sink(BusData data) throws IOException {
		Ensure.not_null(data);
		first().sink(data);
	}
	
	/**
	 * Obtains a description of the filter chain.
	 * @return a description of the chain
	 */
	public synchronized EventFilterChainInfo info() {
		return new EventFilterChainInfo(this);
	}
}
