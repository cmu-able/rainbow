package edu.cmu.cs.able.eseb.filter;

import incubator.dispatch.Dispatcher;
import incubator.dispatch.LocalDispatcher;
import incubator.pval.Ensure;
import incubator.scb.ScbContainer;
import incubator.scb.ScbContainerListener;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Information about a filter chain.
 */
public class EventFilterChainInfo implements Serializable,
		ScbContainer<EventFilterInfo> {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Information about the filters in the chain.
	 */
	private List<EventFilterInfo> m_filters;
	
	/**
	 * Dispatcher used to send events.
	 */
	private transient LocalDispatcher<ScbContainerListener<EventFilterInfo>>
			m_dispatcher;
	
	/**
	 * Creates a new information object collecting data from the chain.
	 * @param chain the chain
	 */
	public EventFilterChainInfo(EventFilterChain chain) {
		Ensure.not_null(chain);
		m_filters = new ArrayList<>();
		for (EventFilter f : chain.filters()) {
			m_filters.add(f.info());
		}
		
		m_dispatcher = new LocalDispatcher<>();
	}
	
	/**
	 * Obtains information about the filters in the chain.
	 * @return the information on the filters
	 */
	public List<EventFilterInfo> filters() {
		return new ArrayList<>(m_filters);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((m_filters == null) ? 0 : m_filters.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EventFilterChainInfo other = (EventFilterChainInfo) obj;
		if (m_filters == null) {
			if (other.m_filters != null)
				return false;
		} else if (!m_filters.equals(other.m_filters))
			return false;
		return true;
	}
	
	@Override
	public Dispatcher<ScbContainerListener<EventFilterInfo>> dispatcher() {
		return m_dispatcher;
	}

	@Override
	public Collection<EventFilterInfo> all_scbs() {
		return filters();
	}
	
	@SuppressWarnings("javadoc")
	private void readObject(java.io.ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		Ensure.not_null(in);
		in.defaultReadObject();
		m_dispatcher = new LocalDispatcher<>();
	}
}
