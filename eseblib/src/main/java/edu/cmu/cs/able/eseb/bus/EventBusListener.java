package edu.cmu.cs.able.eseb.bus;

import edu.cmu.cs.able.eseb.BusData;




/**
 * Interface implemented by classes that receive events from the
 * {@link EventBus}.
 */
public interface EventBusListener {
	/**
	 * A connection has been accepted.
	 * @param data the client's data
	 */
	void connection_accepted(EventBusConnectionData data);
	
	/**
	 * A data value has been distributed to all connections.
	 * @param v the value
	 * @param source the client that published the value
	 */
	void distributed(BusData v, EventBusConnectionData source);
	
	/**
	 * A connection has been disconnected.
	 * @param data the client's data
	 */
	void connection_disconnected(EventBusConnectionData data);
}
