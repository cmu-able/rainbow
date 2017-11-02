package edu.cmu.cs.able.eseb;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.cs.able.eseb.bus.EventBusConnectionData;
import edu.cmu.cs.able.eseb.bus.EventBusListener;

/**
 * Bus server listener that collects data from all invocations.
 */
public class CollectingBusServerListener implements EventBusListener {
	/**
	 * Accepted clients.
	 */
	public List<EventBusConnectionData> m_accepted;
	
	/**
	 * Distributed values.
	 */
	public List<BusData> m_distributed_values;
	
	/**
	 * Distributed sources.
	 */
	public List<EventBusConnectionData> m_distributed_sources;
	
	/**
	 * Disconnected clients.
	 */
	public List<EventBusConnectionData> m_disconnected;
	
	/**
	 * Creates a new bus server listener.
	 */
	public CollectingBusServerListener() {
		m_accepted = new ArrayList<>();
		m_disconnected = new ArrayList<>();
		m_distributed_values = new ArrayList<>();
		m_distributed_sources = new ArrayList<>();
		m_disconnected = new ArrayList<>();
	}
	
	@Override
	public void connection_accepted(EventBusConnectionData data) {
		m_accepted.add(data);
	}

	@Override
	public void distributed(BusData v, EventBusConnectionData source) {
		m_distributed_values.add(v);
		m_distributed_sources.add(source);
	}

	@Override
	public void connection_disconnected(EventBusConnectionData data) {
		m_disconnected.add(data);
	}
}
