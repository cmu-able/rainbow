package edu.cmu.cs.able.eseb.bus;

import edu.cmu.cs.able.eseb.ControlledDataTypeSocketConnection;

/**
 * Interface of objects that will pre-process incoming connections to the event
 * bus before they can be used. A typical use is to install event filters
 * in the event bus.
 */
public interface EventBusAcceptPreprocessor {
	/**
	 * Preprocesses an incoming connection.
	 * @param connection the connection
	 * @return should the connection be accepted? If <code>false</code>, the
	 * connection will be closed
	 */
	boolean preprocess(ControlledDataTypeSocketConnection connection);
}
