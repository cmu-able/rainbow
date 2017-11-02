package edu.cmu.cs.able.eseb.filter;

import incubator.pval.Ensure;
import incubator.scb.ScbField;
import incubator.scb.ScbIntegerField;
import incubator.scb.ScbTextField;
import incubator.scb.SerializableScb;

import java.util.ArrayList;
import java.util.List;

/**
 * Information about an event filter.
 */
public class EventFilterInfo extends SerializableScb<EventFilterInfo> {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Name of the class of the filter.
	 */
	private String m_filter_class;
	
	/**
	 * Index of the filter in the chain, if any.
	 */
	private int m_index;
	
	/**
	 * Creates a new event filter information.
	 * @param f the filter
	 */
	public EventFilterInfo(EventFilter f) {
		Ensure.not_null(f);
		m_filter_class = f.getClass().getName();
		m_index = 0;
	}
	
	/**
	 * Obtains the name of the filter class.
	 * @return the class name
	 */
	public String filter_class() {
		return m_filter_class;
	}
	
	/**
	 * Obtains the index of the filter in the chain, if the filter belongs
	 * to the chain.
	 * @return the index, <code>0</code> if the filter does not belong
	 * to a chain
	 */
	public int index() {
		return m_index;
	}
	
	/**
	 * Sets the filter index in the chain.
	 * @param idx the index
	 */
	public void index(int idx) {
		m_index = idx;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((m_filter_class == null) ? 0 : m_filter_class.hashCode());
		result = prime * result + m_index;
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
		return m_index == other.m_index;
	}
	
	@SuppressWarnings("javadoc")
	public static ScbTextField<EventFilterInfo> c_filter_class_field() {
		return new ScbTextField<EventFilterInfo>("Filter class", false, null) {
			@Override
			public void set(EventFilterInfo t, String value) {
				Ensure.unreachable();
			}

			@Override
			public String get(EventFilterInfo t) {
				return t.filter_class();
			}
		};
	}
	
	@SuppressWarnings("javadoc")
	public static ScbIntegerField<EventFilterInfo> c_index_field() {
		return new ScbIntegerField<EventFilterInfo>("Chain index", false,null) {
			@Override
			public void set(EventFilterInfo t, Integer value) {
				Ensure.unreachable();
			}

			@Override
			public Integer get(EventFilterInfo t) {
				return t.index();
			}
		};
	}
	
	@SuppressWarnings("javadoc")
	public static List<ScbField<EventFilterInfo, ?>> c_fields() {
		List<ScbField<EventFilterInfo, ?>> l = new ArrayList<>();
		l.add(c_filter_class_field());
		l.add(c_index_field());
		return l;
	}

	@Override
	public List<ScbField<EventFilterInfo, ?>> fields() {
		return c_fields();
	}

	@Override
	protected Class<EventFilterInfo> my_class() {
		return EventFilterInfo.class;
	}
}
