package edu.cmu.cs.able.eseb;

import incubator.dispatch.Dispatcher;
import incubator.dispatch.LocalDispatcher;
import incubator.wt.CloseableListener;
import incubator.wt.WorkerThreadGroup;
import incubator.wt.WorkerThreadGroupCI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Test data type socket connection that collects information.
 */
public class TestDataTypeSocketConnection implements DataTypeSocketConnection {
	/**
	 * Data written to the connection.
	 */
	public List<BusData> m_written;
	
	/**
	 * Queue group for queues that receive data from the connection.
	 */
	public BusDataQueueGroupImpl m_queue_group;
	
	/**
	 * Used to dispatch close events (pretending the connection has been
	 * closed).
	 */
	public LocalDispatcher<CloseableListener> m_cd;
	
	/**
	 * Number of times {@link #close()} has been invoked.
	 */
	public int m_closed;
	
	/**
	 * Number of times {@link #start()} has been invoked.
	 */
	public int m_start;
	
	/**
	 * Number of times {@link #stop()} has been invoked.
	 */
	public int m_stop;
	
	/**
	 * Fake thread group.
	 */
	private WorkerThreadGroup m_group;
	
	/**
	 * Creates a new connection.
	 */
	public TestDataTypeSocketConnection() {
		m_written = new ArrayList<>();
		m_queue_group = new BusDataQueueGroupImpl();
		m_cd = new LocalDispatcher<>();
		m_closed = 0;
		m_group = new WorkerThreadGroup("test");
	}

	@Override
	public synchronized void write(BusData bd) throws IOException {
		m_written.add(bd);
	}

	@Override
	public synchronized void write(DataValue dt) throws IOException {
		m_written.add(new BusData(dt));
	}

	@Override
	public synchronized void close() throws IOException {
		m_closed++;
	}

	@Override
	public synchronized void start() {
		m_start++;
	}

	@Override
	public synchronized void stop() {
		m_stop++;
	}

	@Override
	public WorkerThreadGroupCI thread_group() {
		return m_group;
	}

	@Override
	public Dispatcher<CloseableListener> closeable_dispatcher() {
		return m_cd;
	}

	@Override
	public BusDataQueueGroup queue_group() {
		return m_queue_group;
	}
	
	/**
	 * Adds data to the queue group pretending it has been received from the
	 * connection.
	 * @param d the data
	 */
	public synchronized void add_to_queue(BusData d) {
		m_queue_group.add(d);
	}
}
