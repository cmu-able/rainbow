package edu.cmu.cs.able.eseb.bus.rci;

import java.util.HashMap;
import java.util.Map;

import edu.cmu.cs.able.eseb.BusData;
import edu.cmu.cs.able.eseb.bus.EventBus;
import edu.cmu.cs.able.eseb.bus.EventBusConnectionData;
import edu.cmu.cs.able.eseb.bus.EventBusListener;
import incubator.pval.Ensure;
import incubator.scb.ScbEditableContainer;
import incubator.wt.WorkerThread;

/**
 * Class that keeps an editable container of
 * {@link EventBusRemoteConnectionInfo} updated with respect to the current
 * state of the event bus.
 */
public class ConnectionInformationScbSynchronizer {
	/**
	 * Milliseconds between interval.
	 */
	public static final long COUNT_UPDATE_INTERVAL_MS = 500;
	
	/**
	 * The event bus.
	 */
	private EventBus m_bus;
	
	/**
	 * The container to keep up-to-date.
	 */
	private ScbEditableContainer<EventBusRemoteConnectionInfo> m_container;
	
	/**
	 * Maps connection ID to the connection information objects which are
	 * in {@link #m_container}.
	 */
	private Map<Integer, EventBusRemoteConnectionInfo> m_connections;
	
	/**
	 * Maps connection ID to the local connection data.
	 */
	private Map<Integer, EventBusConnectionData> m_local;
	
	/**
	 * Worker thread that updates the connection information.
	 */
	private WorkerThread m_worker;
	
	/**
	 * THe listener registered in the event bus.
	 */
	private EventBusListener m_bus_listener;
	
	/**
	 * Creates a new synchronizer.
	 * @param bus the event bus
	 * @param container the container
	 */
	public ConnectionInformationScbSynchronizer(EventBus bus,
			ScbEditableContainer<EventBusRemoteConnectionInfo> container) {
		Ensure.not_null(bus);
		Ensure.not_null(container);
		
		m_bus = bus;
		m_container = container;
		m_connections = new HashMap<>();
		m_local = new HashMap<>();
		m_worker = new WorkerThread("Connection information updater") {

			@Override
			protected void do_cycle_operation() throws Exception {
				update_counts();
				synchronized (this) {
					wait(COUNT_UPDATE_INTERVAL_MS);
				}
			}
		};
		
		m_worker.start();
		
		m_bus_listener = new EventBusListener() {
			@Override
			public void distributed(BusData v, EventBusConnectionData source) {
				/*
				 * We don't care about distributed events.
				 */
			}
			
			@Override
			public void connection_disconnected(EventBusConnectionData data) {
				Ensure.not_null(data);
				remove_connection(data.id());
			}
			
			@Override
			public void connection_accepted(EventBusConnectionData data) {
				Ensure.not_null(data);
				add_connection(data);
			}
		};
		
		bus.add_listener(m_bus_listener);
	}
	
	/**
	 * Shuts down the synchronizer.
	 */
	public synchronized void shutdown() {
		Ensure.not_null(m_worker);
		
		m_worker.stop();
		m_worker = null;
		m_bus.remove_listener(m_bus_listener);
	}
	
	/**
	 * Invoked when a connection has been shutdown.
	 * @param id the ID of the connection
	 */
	private synchronized void remove_connection(int id) {
		Ensure.is_true(m_connections.containsKey(id));
		Ensure.is_true(m_local.containsKey(id));
		EventBusRemoteConnectionInfo c = m_connections.get(id);
		m_container.remove_scb(c);
		m_connections.remove(id);
		m_local.remove(id);
	}
	
	/**
	 * Invoked when a connection has been created.
	 * @param data the connection
	 */
	private synchronized void add_connection(EventBusConnectionData data) {
		Ensure.not_null(data);
		Ensure.is_false(m_connections.containsKey(data.id()));
		Ensure.is_false(m_local.containsKey(data.id()));
		EventBusRemoteConnectionInfo c = new EventBusRemoteConnectionInfo(
				data.id(), data.address().toString(), data.connect_time(),
				data.publish_count(), data.subscribe_count(),
				data.incoming_chain(), data.outgoing_chain());
		m_container.add_scb(c);
		m_connections.put(data.id(), c);
		m_local.put(data.id(), data);
	}
	
	/**
	 * Updates the counts of all connections.
	 */
	private synchronized void update_counts() {
		for (EventBusConnectionData dt : m_local.values()) {
			EventBusRemoteConnectionInfo i = m_connections.get(dt.id());
			Ensure.not_null(i);
			i.publish_count(dt.publish_count());
			i.subscribe_count(dt.subscribe_count());
			i.incoming_chain(dt.incoming_chain());
			i.outgoing_chain(dt.outgoing_chain());
		}
	}
}
