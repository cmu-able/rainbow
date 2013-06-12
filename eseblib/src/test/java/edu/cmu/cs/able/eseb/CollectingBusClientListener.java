package edu.cmu.cs.able.eseb;

import java.util.ArrayList;
import java.util.List;

/**
 * Bus client listener that collects values.
 */
public class CollectingBusClientListener implements BusClientListener {
	/**
	 * State changed invocations.
	 */
	public List<Boolean> m_state_changed;
	
	/**
	 * Creates a new listener.
	 */
	public CollectingBusClientListener() {
		m_state_changed = new ArrayList<>();
	}

	@Override
	public synchronized void client_state_changed() {
		m_state_changed.add(true);
	}
}
