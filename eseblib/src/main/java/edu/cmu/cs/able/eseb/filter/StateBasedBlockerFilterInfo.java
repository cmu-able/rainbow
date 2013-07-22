package edu.cmu.cs.able.eseb.filter;

import incubator.pval.Ensure;

/**
 * Information about a state blocker filter.
 * @param <S> the enumeration with the filter state
 */
public class StateBasedBlockerFilterInfo<S extends Enum<S> & Blocker>
		extends EventFilterInfo {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The filter state.
	 */
	private S m_state;
	
	/**
	 * Creates a new information.
	 * @param filter the filter
	 */
	public StateBasedBlockerFilterInfo(StateBasedBlockerFilter<S> filter) {
		super(filter);
		Ensure.not_null(filter);
		m_state = filter.state();
	}
	
	/**
	 * Obtains the filter state.
	 * @return the filter state
	 */
	public S state() {
		return m_state;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((m_state == null) ? 0 : m_state.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("rawtypes")
		StateBasedBlockerFilterInfo other = (StateBasedBlockerFilterInfo) obj;
		if (m_state == null) {
			if (other.m_state != null)
				return false;
		} else if (!m_state.equals(other.m_state))
			return false;
		return true;
	}
}
