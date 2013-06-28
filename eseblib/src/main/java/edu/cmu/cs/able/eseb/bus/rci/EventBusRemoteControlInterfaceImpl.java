package edu.cmu.cs.able.eseb.bus.rci;

import incubator.pval.Ensure;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import edu.cmu.cs.able.eseb.BusData;
import edu.cmu.cs.able.eseb.bus.EventBus;
import edu.cmu.cs.able.eseb.bus.EventBusConnectionData;
import edu.cmu.cs.able.eseb.bus.EventBusListener;

/**
 * Implementation of the remote interface access to an event bus.
 */
public class EventBusRemoteControlInterfaceImpl
		implements EventBusRemoteControlInterface {
	/**
	 * Default expiration time, in milliseconds.
	 */
	private static final long DEFAULT_EXPIRE_LIMIT_MS = 30_000;
	
	/**
	 * The event bus.
	 */
	private EventBus m_bus;
	
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
	 * The port where the data master is running.
	 */
	private short m_data_master_port;
	
	/**
	 * Creates a new remote interface implementation.
	 * @param srv the server
	 * @param queue_limit the queue size limit
	 * @param dm_port the port where the data master is running
	 */
	public EventBusRemoteControlInterfaceImpl(EventBus srv, int queue_limit,
			short dm_port) {
		Ensure.not_null(srv);
		Ensure.greater(queue_limit, 0);
		Ensure.greater(dm_port, 0);
		
		m_bus = srv;
		m_queues = new HashMap<>();
		m_queue_limit = queue_limit;
		m_expire_limit = DEFAULT_EXPIRE_LIMIT_MS;
		m_data_master_port = dm_port;
		
		srv.add_listener(new EventBusListener() {
			@Override
			public void distributed(BusData v, EventBusConnectionData source) {
				distribute(source.id(), v);
			}
			
			@Override
			public void connection_disconnected(EventBusConnectionData data) {
				/*
				 * Nothing to do.
				 */
			}
			
			@Override
			public void connection_accepted(EventBusConnectionData data) {
				/*
				 * Nothing to do.
				 */
			}
		});
	}
	
	/**
	 * Adds a distributed event to all queues.
	 * @param connection_id the connection ID that distributed the event
	 * @param d the bus data
	 */
	private synchronized void distribute(int connection_id, BusData d) {
		Ensure.not_null(d);
		Date dt = new Date();
		LimitedDistributionQueueElement dqe =
				new LimitedDistributionQueueElement(dt, d.encoding(),
				connection_id);
		for (String k : new HashSet<>(m_queues.keySet())) {
			if (!evict(k)) {
				m_queues.get(k).m_queue.add(dqe);
			}
		}
	}
	
	@Override
	public short port() {
		return m_bus.port();
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
	public void expire_limit(long limit_ms) {
		Ensure.greater(limit_ms, 0);
		m_expire_limit = limit_ms;
	}

	@Override
	public short data_master_port() {
		return m_data_master_port;
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
