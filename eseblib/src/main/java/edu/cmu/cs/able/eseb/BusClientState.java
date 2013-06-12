package edu.cmu.cs.able.eseb;

/**
 * States in which the bus client can be in.
 */
public enum BusClientState {
	/**
	 * Bus client is disconnected and not attempting to connect.
	 */
	DISCONNECTED,
	
	/**
	 * Bus client is disconnected and attempting to connect.
	 */
	CONNECTING,
	
	/**
	 * Bus client is connected.
	 */
	CONNECTED
}
