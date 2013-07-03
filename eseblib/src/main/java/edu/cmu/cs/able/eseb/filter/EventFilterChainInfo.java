package edu.cmu.cs.able.eseb.filter;

import incubator.pval.Ensure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Information about a filter chain.
 */
public class EventFilterChainInfo implements Serializable {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Information about the filters in the chain.
	 */
	private List<EventFilterInfo> m_filters;
	
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
}
