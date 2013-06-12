package edu.cmu.cs.able.eseb;

import incubator.pval.Ensure;
import incubator.wt.CloseableWorkerThread;

/**
 * Thread that keeps reading data from a data type input stream and notifies
 * objects when data is received.
 */
public class DataTypeInputThread
		extends CloseableWorkerThread<DataTypeInputStream> {
	/**
	 * Queue group that is informed when data is received from the connection.
	 */
	private BusDataQueueGroupImpl m_queue_group;
	
	/**
	 * Creates a new input thread.
	 * @param name the thread name
	 * @param is the input stream
	 */
	public DataTypeInputThread(String name, DataTypeInputStream is) {
		super(name, is, true);
		
		Ensure.not_null(is);
		m_queue_group = new BusDataQueueGroupImpl();
	}
	
	/**
	 * Obtains the queue group used to register queues to receive data when
	 * it is read from the connection.
	 * @return the group
	 */
	public BusDataQueueGroup queue_group() {
		return m_queue_group;
	}
	
	@Override
	protected void do_cycle_operation(DataTypeInputStream closeable)
			throws Exception {
		final BusData bd = closeable.read();
		
		m_queue_group.add(bd);
	}
}
