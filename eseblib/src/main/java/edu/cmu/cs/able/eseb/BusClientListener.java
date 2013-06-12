package edu.cmu.cs.able.eseb;


/**
 * Listener that will be informed of changes and events in the bus client.
 */
public interface BusClientListener {
	/**
	 * The client's state has changed.
	 */
	void client_state_changed();
}
