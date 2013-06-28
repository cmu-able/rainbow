package edu.cmu.cs.able.eseb.conn;


/**
 * Listener that will be informed of changes and events in the bus connection.
 */
public interface BusConnectionListener {
	/**
	 * The connections's state has changed.
	 */
	void connection_state_changed();
}
