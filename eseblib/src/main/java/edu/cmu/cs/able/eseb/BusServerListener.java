package edu.cmu.cs.able.eseb;



/**
 * Interface implemented by classes that receive events from the
 * {@link BusServer}.
 */
public interface BusServerListener {
	/**
	 * A client has been accepted.
	 * @param data the client's data
	 */
	void client_accepted(BusServerClientData data);
	
	/**
	 * A data value has been distributed to all clients.
	 * @param v the value
	 * @param source the client that published the value
	 */
	void distributed(BusData v, BusServerClientData source);
	
	/**
	 * A client has been disconnected.
	 * @param data the client's data
	 */
	void client_disconnected(BusServerClientData data);
}
