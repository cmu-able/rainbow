package edu.cmu.cs.able.eseb;

import incubator.dispatch.Dispatcher;
import incubator.wt.CloseableListener;
import incubator.wt.WorkerThreadGroupCI;

import java.io.IOException;

import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Connection that works over a socket. This connection will generate both
 * an input and output threads to allow reading and writing from the socket.
 * If either fails, the socket is closed and listeners informed.
 */
public interface DataTypeSocketConnection extends DataTypeOutputStream {
	/**
	 * Obtains the closeable dispatcher.
	 * @return the dispatcher
	 */
	Dispatcher<CloseableListener> closeable_dispatcher();

	@Override
	void write(DataValue dt) throws IOException;

	@Override
	void close() throws IOException;

	/**
	 * Starts the connection. Equivalent to call
	 * {@link WorkerThreadGroupCI#start()} on the thread group obtained by
	 * calling {@link #thread_group()}
	 */
	void start();

	/**
	 * Stops the connection. Equivalent to call
	 * {@link WorkerThreadGroupCI#stop()} on the thread group obtained by
	 * calling {@link #thread_group()}
	 */
	void stop();

	/**
	 * Obtains the worker thread group of this socket connection. The thread
	 * group can be used to start and stop the connection.
	 * @return the thread group
	 */
	WorkerThreadGroupCI thread_group();
	
	/**
	 * Obtains the group to register queues that receive data from the
	 * connection.
	 * @return the group
	 */
	BusDataQueueGroup queue_group();
}