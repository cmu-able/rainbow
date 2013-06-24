package incubator.il.ui;

import incubator.il.IMutexRequest;

import java.util.Date;

/**
 * Data of the waiting queue.
 */
class DataBean {
	/**
	 * Position in the waiting queue.
	 */
	private int m_pos;
	
	/**
	 * Name of the waiting thread.
	 */
	private String m_thread;
	
	/**
	 * Date in which the mutex was requested.
	 */
	private Date m_requested;
	
	/**
	 * Time waited.
	 */
	private long m_waited;
	
	
	/**
	 * Creates a new bean.
	 * @param pos the mutex position
	 * @param request the request
	 */
	DataBean(int pos, IMutexRequest request) {
		m_pos = pos;
		m_requested = request.request_time();
		m_thread = request.acquisition_thread();
		m_waited = request.wait_time();
	}
	
	/**
	 * Obtains the position.
	 * @return the position
	 */
	public int getPosition() {
		return m_pos;
	}
	
	/**
	 * Obtains the requesting thread
	 * @return the requesting thread
	 */
	public String getThread() {
		return m_thread;
	}
	
	/**
	 * Obtains the date in which the request was made.
	 * @return the date
	 */
	public Date getRequested() {
		return m_requested;
	}
	
	/**
	 * Obtains the waiting time.
	 * @return the waiting time
	 */
	public long getWaited() {
		return m_waited;
	}
}