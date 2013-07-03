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
}
