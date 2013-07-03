package edu.cmu.cs.able.eseb.filter;

import java.io.IOException;

import incubator.pval.Ensure;
import edu.cmu.cs.able.eseb.BusData;

/**
 * <p>Event sink which may forward the event to another sink, actually working
 * as an event filter. Although it is common for event filters to forward the
 * received event, maybe after modifying it, filters may generate events on
 * their own. Each filter can be connected at most to one sink at every
 * time.</p>
 * <p>Event filters may be added to other structures, such as event filter
 * chains implemented in class {@link EventFilterChain}, which may place
 * restrictions on can be done in the filter. To support this, event filters
 * can be <em>locked</em>. If they are locked they will not allow the
 * connect sink to be changed.</p>
 */
public abstract class EventFilter implements EventSink {
	/**
	 * The event sink.
	 */
	private EventSink m_sink;
	
	/**
	 * The lock in the event filter.
	 */
	private Object m_lock;
	
	/**
	 * Creates a new filter.
	 */
	public EventFilter() {
		m_sink = null;
		m_lock = null;
	}
	
	/**
	 * Connects the filter to the given sink. The sink may be <code>null</code>
	 * in which case the filter is disconnected from whatever sink it was
	 * connected to, if any.
	 * @param s the sink to connect to, <code>null</code> to disconnect the
	 * filter
	 */
	public synchronized void connect(EventSink s) {
		Ensure.is_true(m_sink == null || s == null);
		Ensure.is_null(m_lock);
		m_sink = s;
	}
	
	/**
	 * Obtains the sink this filter is connected to.
	 * @return the sink or <code>null</code> if the filter is not connected
	 * to any sink.
	 */
	public synchronized EventSink connected() {
		return m_sink;
	}
	
	/**
	 * Forwards an event to the sink. If the filter is disconnected, this
	 * request is ignored.
	 * @param data the event to forward
	 * @throws IOException failed to send the data
	 */
	protected void forward(BusData data) throws IOException {
		Ensure.not_null(data);
		
		EventSink sink;
		synchronized (this) {
			sink = m_sink;
		}
		
		if (sink != null) {
			sink.sink(data);
		}
	}
	
	/**
	 * Obtains information about this filter.
	 * @return filter information
	 */
	public EventFilterInfo info() {
		return new EventFilterInfo(this);
	}
	
	/**
	 * Locks the event filter.
	 * @param l the lock to use
	 */
	public synchronized void lock(Object l) {
		Ensure.not_null(l);
		Ensure.is_null(m_lock);
		m_lock = l;
	}
	
	/**
	 * Unlocks the event filter.
	 * @param l the lock used to lock the filter when {@link #lock(Object)}
	 * was invoked
	 */
	public synchronized void unlock(Object l) {
		Ensure.not_null(l);
		Ensure.equals(m_lock, l);
		m_lock = null;
	}
	
	/**
	 * Checks whether the filter is locked or not.
	 * @return is the filter locked?
	 */
	public synchronized boolean locked() {
		return m_lock != null;
	}
}
