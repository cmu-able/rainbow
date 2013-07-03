package edu.cmu.cs.able.eseb.filter;

import incubator.pval.Ensure;

import java.io.Serializable;

/**
 * Information about an event filter.
 */
public class EventFilterInfo implements Serializable {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Name of the class of the filter.
	 */
	private String m_filter_class;
	
	/**
	 * Creates a new event filter information.
	 * @param f the filter
	 */
	public EventFilterInfo(EventFilter f) {
		Ensure.not_null(f);
		m_filter_class = f.getClass().getName();
	}
	
	/**
	 * Obtains the name of the filter class.
	 * @return the class name
	 */
	public String filter_class() {
		return m_filter_class;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((m_filter_class == null) ? 0 : m_filter_class.hashCode());
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
		EventFilterInfo other = (EventFilterInfo) obj;
		if (m_filter_class == null) {
			if (other.m_filter_class != null)
				return false;
		} else if (!m_filter_class.equals(other.m_filter_class))
			return false;
		return true;
	}
	
	
}
