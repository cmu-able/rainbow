package edu.cmu.cs.able.eseb;

import incubator.pval.Ensure;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of a bus data queue group. 
 */
public class BusDataQueueGroupImpl implements BusDataQueueGroup {
	/**
	 * The queues registered in the group.
	 */
	private List<BusDataQueue> m_queues;
	
	/**
	 * Creates a new group.
	 */
	public BusDataQueueGroupImpl() {
		m_queues = new ArrayList<>();
	}

	@Override
	public synchronized void add(BusDataQueue q) {
		Ensure.not_null(q, "q == null");
		m_queues.add(q);
	}
	
	/**
	 * Adds data to all queues in the group.
	 * @param d the data to add
	 */
	public synchronized void add(BusData d) {
		Ensure.not_null(d);
		for (BusDataQueue q : m_queues) {
			q.add(d);
		}
	}
	
	public synchronized void remove(BusDataQueue q) {
		Ensure.not_null(q, "q == null");
		Ensure.is_true(m_queues.contains(q), "queue not known");
		m_queues.remove(q);
	}
	
	/**
	 * Copies all available data in a queue to all the queues in this group
	 * in a thread-safe way.
	 * @param q the queue
	 * @return all data transfered, an empty list if none
	 */
	public synchronized List<BusData> transfer_from(BusDataQueue q) {
		Ensure.not_null(q);
		
		List<BusData> transferred = new ArrayList<>();
		synchronized (q) {
			BusData r;
			while ((r = q.poll()) != null) {
				transferred.add(r);
				add(r);
			}
		}
		
		return transferred;
	}
}
