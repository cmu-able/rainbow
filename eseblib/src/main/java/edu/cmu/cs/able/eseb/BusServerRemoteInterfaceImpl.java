package edu.cmu.cs.able.eseb;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import incubator.pval.Ensure;

/**
 * Implementation of the remote interface access of a remote server.
 */
public class BusServerRemoteInterfaceImpl implements BusServerRemoteInterface {
	/**
	 * Default expiration time, in milliseconds.
	 */
	private static final long DEFAULT_EXPIRE_LIMIT_MS = 30_000;
	
	/**
	 * The bus server.
	 */
	private BusServer m_server;
	
	/**
	 * Distribution queues mapped by queue name.
	 */
	private Map<String, QueueInfo> m_queues;
	
	/**
	 * The queue limit.
	 */
	private int m_queue_limit;
	
	/**
	 * Time, in milliseconds, to expire a queue if it is not retrieved.
	 */
	private long m_expire_limit;
	
	/**
	 * Creates a new remote interface implementation.
	 * @param srv the server
	 * @param queue_limit the queue size limit
	 */
	public BusServerRemoteInterfaceImpl(BusServer srv, int queue_limit) {
		Ensure.not_null(srv);
		Ensure.greater(queue_limit, 0);
		m_server = srv;
		m_queues = new HashMap<>();
		m_queue_limit = queue_limit;
		m_expire_limit = DEFAULT_EXPIRE_LIMIT_MS;
		
		srv.add_listener(new BusServerListener() {
			@Override
			public void distributed(BusData v, BusServerClientData source) {
				distribute(source.id(), v);
			}
			
			@Override
			public void client_disconnected(BusServerClientData data) {
				/*
				 * Nothing to do.
				 */
			}
			
			@Override
			public void client_accepted(BusServerClientData data) {
				/*
				 * Nothing to do.
				 */
			}
		});
	}
	
	/**
	 * Adds a distributed event to all queues.
	 * @param client_id the client ID that distributed the event
	 * @param d the bus data
	 */
	private synchronized void distribute(int client_id, BusData d) {
		Ensure.not_null(d);
		Date dt = new Date();
		LimitedDistributionQueueElement dqe =
				new LimitedDistributionQueueElement(dt, d.encoding(),
				client_id);
		for (String k : new HashSet<>(m_queues.keySet())) {
			if (!evict(k)) {
				m_queues.get(k).m_queue.add(dqe);
			}
		}
	}
	
	@Override
	public short port() {
		return m_server.port();
	}

	@Override
	public synchronized LimitedDistributionQueue
			distribution_queue(String key) {
		Ensure.not_null(key);
		
		QueueInfo qi = m_queues.get(key);
		if (qi != null && evict(key)) {
			qi = null;
		}
		
		if (qi == null) {
			qi = new QueueInfo();
			qi.m_last = System.currentTimeMillis();
			qi.m_queue = new LimitedDistributionQueue(m_queue_limit);
			m_queues.put(key, qi);
		}
		
		LimitedDistributionQueue q = qi.m_queue;
		qi.m_queue = new LimitedDistributionQueue(m_queue_limit);
		qi.m_last = System.currentTimeMillis();
		return q;
	}
	
	/**
	 * Checks if the queue information with the given key should be thrown
	 * out by not being used in more than time than the specified limit.
	 * @param key the queue key
	 * @return was the queue evicted?
	 */
	private boolean evict(String key) {
		Ensure.not_null(key);
		Ensure.is_true(m_queues.containsKey(key));
		if (m_queues.get(key).m_last >= System.currentTimeMillis()
				- m_expire_limit) {
			return false;
		} else {
			m_queues.remove(key);
			return true;
		}
	}
	
	/**
	 * Sets how long much time should elapse until a queue expires.
	 * @param limit_ms the time in milliseconds
	 */
	void expire_limit(long limit_ms) {
		Ensure.greater(limit_ms, 0);
		m_expire_limit = limit_ms;
	}
	
	/**
	 * Information kept for a distribution queue.
	 */
	private static class QueueInfo {
		/**
		 * The queue.
		 */
		private LimitedDistributionQueue m_queue;
		
		/**
		 * Last time the queue was obtained.
		 */
		private long m_last;
	}
}
