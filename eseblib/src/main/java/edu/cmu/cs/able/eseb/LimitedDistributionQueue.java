package edu.cmu.cs.able.eseb;

import incubator.pval.Ensure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Class that keeps distributed events up to a maximum defined. Events added
 * after that maximum are pushed into the queue and older events removed to
 * maintain the queue size. The queue keeps information of how many events
 * were lost.
 */
public class LimitedDistributionQueue implements Serializable {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Queue limit.
	 */
	private int m_limit;
	
	/**
	 * The queue contents.
	 */
	private LinkedList<LimitedDistributionQueueElement> m_queue;
	
	/**
	 * Date of first element added to the queue.
	 */
	private Date m_started;
	
	/**
	 * Number of elements lost from the queue.
	 */
	private int m_lost;

	/**
	 * Creates a new queue.
	 * @param limit the maximum number of events in the queue.
	 */
	public LimitedDistributionQueue(int limit) {
		Ensure.greater(limit, 0);
		m_limit = limit;
		m_queue = new LinkedList<>();
		m_started = null;
		m_lost = 0;
	}
	
	/**
	 * Date when the queue collected the first event.
	 * @return the time first event collected, even if older events have been
	 * discarded, this date is maintained; if no events have ever been added
	 * to the queue, this method returns <code>null</code>
	 */
	public synchronized Date started() {
		return m_started;
	}
	
	/**
	 * Obtains the number of elements in the queue.
	 * @return the number of elements
	 */
	public synchronized int size() {
		return m_queue.size();
	}
	
	/**
	 * Obtains the maximum size of the queue.
	 * @return the size of the queue
	 */
	public synchronized int limit() {
		return m_limit;
	}
	
	/**
	 * Obtains the number of elements that have been removed to the queue
	 * because they would make the queue pass the limit.
	 * @return the number of elements
	 */
	public synchronized int lost() {
		return m_lost;
	}
	
	/**
	 * Adds an element to the queue, removing the oldest in the queue, if
	 * it would exceed the limit.
	 * @param e the element to add
	 */
	public synchronized void add(LimitedDistributionQueueElement e) {
		Ensure.not_null(e);
		if (m_queue.size() == m_limit) {
			m_queue.removeFirst();
			m_lost++;
		}
		
		Ensure.less(m_queue.size(), m_limit);
		m_queue.addLast(e);
		
		if (m_started == null) {
			m_started = e.date();
		}
	}
	
	/**
	 * Obtains a copy of all elements in the queue.
	 * @return all elements in the queue
	 */
	public List<LimitedDistributionQueueElement> all() {
		return new ArrayList<>(m_queue);
	}
}
