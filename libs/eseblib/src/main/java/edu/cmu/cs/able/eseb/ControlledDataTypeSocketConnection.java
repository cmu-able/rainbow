package edu.cmu.cs.able.eseb;

import incubator.dispatch.Dispatcher;
import incubator.wt.CloseableListener;

import java.io.Closeable;
import java.io.IOException;

import edu.cmu.cs.able.eseb.filter.EventFilterChain;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Interface for socket connection that can handle control commands like
 * ping and publish-only (meaning no data is to be sent).
 */
public interface ControlledDataTypeSocketConnection extends Closeable {
	/**
	 * Obtains the dispatcher that notifies when the connection is closed.
	 * @return the dispatcher
	 */
	Dispatcher<CloseableListener> closeable_dispatcher();
	
	/**
	 * Obtains the queue group used to register queues to receive data when
	 * data is received from the event bus.
	 * @return the group
	 */
	BusDataQueueGroup queue_group();
	
	/**
	 * Writes a data value to the connection.
	 * @param v the value
	 * @throws IOException failed to write
	 */
	void write(DataValue v) throws IOException;
	
	/**
	 * Writes a data value to the connection
	 * @param bd the value
	 * @throws IOException failed to write
	 */
	void write(BusData bd) throws IOException;
	
	/**
	 * Starts the connection.
	 */
	void start();
	
	/**
	 * Stops the connection.
	 */
	void stop();
	
	/**
	 * Marks the connection as being publish only and informs the other end
	 * that data is not to be sent.
	 * @throws IOException failed to mark the connection
	 */
	void publish_only() throws IOException;
	
	/**
	 * Obtains the incoming event filter chain.
	 * @return the chain
	 */
	EventFilterChain incoming_chain();
	
	/**
	 * Obtains the outgoing event filter chain.
	 * @return the chain
	 */
	EventFilterChain outgoing_chain();
}
