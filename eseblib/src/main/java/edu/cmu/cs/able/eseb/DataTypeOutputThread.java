package edu.cmu.cs.able.eseb;

import incubator.pval.Ensure;
import incubator.wt.CloseableWorkerThread;

import java.io.IOException;
import java.util.LinkedList;

import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Thread that outputs data to an output stream. Data is output in its own
 * thread asynchronously.
 */
public class DataTypeOutputThread
		extends CloseableWorkerThread<DataTypeOutputStream>
		implements DataTypeOutputStream {
	/**
	 * Queue of types currently pending.
	 */
	private LinkedList<BusData> m_queue;
	
	/**
	 * Creates a new thread that outputs data.
	 * @param name thread name
	 * @param os the output stream to use
	 */
	public DataTypeOutputThread(String name, DataTypeOutputStream os) {
		super(name, os, true);
		m_queue = new LinkedList<>();
	}

	@Override
	public synchronized void write(DataValue dt) throws IOException {
		Ensure.notNull(dt);
		write(new BusData(dt));
	}
	
	@Override
	public synchronized void write(BusData datum) throws IOException {
		Ensure.notNull(datum);
		m_queue.addLast(datum);
		notifyAll();
	}

	@Override
	protected void do_cycle_operation(DataTypeOutputStream closeable)
			throws Exception {
		BusData bd = null;
		synchronized (this) {
			if (m_queue.size() == 0) {
				wait();
			} else {
				bd = m_queue.removeFirst();
			}
		}
		
		if (bd != null) {
			closeable.write(bd);
		}
	}
}
