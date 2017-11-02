package edu.cmu.cs.able.eseb.filter;

import incubator.pval.Ensure;
import edu.cmu.cs.able.eseb.BusData;

/**
 * Event filter that accepts or rejects events based on a state machine.
 * Filter states are represented in an enumeration which should implement the
 * {@link Blocker}. This class will use the information on the current state
 * to decide whether an event is accepted or not. Subclasses will implement
 * the {@link #handle(BusData)} method which is invoked for every message
 * to decide whether a state change should be performed or not.
 * @param <S> the enumeration with all states
 */
public abstract class StateBasedBlockerFilter<S extends Enum<S> & Blocker>
		extends AcceptRejectEventFilter {
	/**
	 * The current state.
	 */
	private S m_state;
	
	/**
	 * Creates a new filter.
	 * @param initial the filter's initial state
	 */
	public StateBasedBlockerFilter(S initial) {
		Ensure.not_null(initial);
		m_state = initial;
	}
	
	/**
	 * Obtains the current state.
	 * @return the state
	 */
	public synchronized S state() {
		return m_state;
	}
	
	/**
	 * Changes the current state.
	 * @param s the new state
	 */
	protected synchronized void state(S s) {
		Ensure.not_null(s);
		m_state = s;
	}
	
	@Override
	protected boolean accepts(BusData d) {
		handle(d);
		return !state().block();
	}
	
	/**
	 * Method invoked every time an event arrives. The event is accepted
	 * or not depending on the state of the filter when this method finishes.
	 * @param d the event to analyze
	 */
	protected abstract void handle(BusData d);

	@Override
	public EventFilterInfo info() {
		return new StateBasedBlockerFilterInfo<>(this);
	}
}
