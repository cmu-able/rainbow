package edu.cmu.cs.able.eseb;

import java.util.ArrayList;
import java.util.List;

/**
 * Bus server listener that collects data from all invocations.
 */
public class CollectingBusServerListener implements BusServerListener {
	/**
	 * Accepted clients.
	 */
	public List<BusServerClientData> m_accepted;
	
	/**
	 * Distributed values.
	 */
	public List<BusData> m_distributed_values;
	
	/**
	 * Distributed sources.
	 */
	public List<BusServerClientData> m_distributed_sources;
	
	/**
	 * Disconnected clients.
	 */
	public List<BusServerClientData> m_disconnected;
	
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
	public void client_accepted(BusServerClientData data) {
		m_accepted.add(data);
	}

	@Override
	public void distributed(BusData v, BusServerClientData source) {
		m_distributed_values.add(v);
		m_distributed_sources.add(source);
	}

	@Override
	public void client_disconnected(BusServerClientData data) {
		m_disconnected.add(data);
	}
}
