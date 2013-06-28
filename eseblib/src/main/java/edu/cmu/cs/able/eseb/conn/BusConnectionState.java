package edu.cmu.cs.able.eseb.conn;

/**
 * States in which the bus connection can be in.
 */
public enum BusConnectionState {
	/**
	 * Bus connection is disconnected and not attempting to connect.
	 */
	DISCONNECTED,
	
	/**
	 * Bus connection is disconnected and attempting to connect.
	 */
	CONNECTING,
	
	/**
	 * Bus connection is connected.
	 */
	CONNECTED
}
