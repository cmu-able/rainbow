package incubator.il.ui;

import incubator.il.IMutexStatus;

import java.util.Date;

/**
 * Bean representing mutex information.
 */
public class IMutexStatusBean {
	/**
	 * Mutex name.
	 */
	private String m_name;
	
	/**
	 * Mutex state.
	 */
	private IMutexStatus m_status;
	
	
	/**
	 * Creates a new bean.
	 * @param name the mutex name
	 * @param status the mutex state
	 */
	IMutexStatusBean(String name, IMutexStatus status) {
		m_name = name;
		m_status = status;
	}
	
	/**
	 * Obtains the mutex name.
	 * @return the mutex name
	 */
	public String getName() {
		return m_name;
	}
	
	/**
	 * Obtains the name of the thread that owns the mutex.
	 * @return the thread name
	 */
	public String getThread() {
		return m_status.lock_request() == null? null :
			m_status.lock_request().acquisition_thread();
	}
	
	/**
	 * Obtains the date in which the mutex was acquired.
	 * @return the date in which the mutex was acquired
	 */
	public Date getRequest() {
		return m_status.lock_request() == null? null :
			m_status.lock_request().request_time();
	}
	
	/**
	 * Obtains how much time we waited for the mutex.
	 * @return how much time we waited for the mutex
	 */
	public long getWaited() {
		return m_status.lock_request() == null? 0 :
			m_status.lock_request().wait_time();
	}
	
	/**
	 * Obtains how many threads are waiting for the mutex.
	 * @return the thread count
	 */
	public int getQueue() {
		return m_status.wait_list().size();
	}
	
	/**
	 * Obtains the number of mutex acquisitions.
	 * @return the number of acquisitions
	 */
	public int getAqCount() {
		return m_status.statistics().total_acquisition_count();
	}
	
	/**
	 * Obtains the average acquisition time with or without waiting.
	 * @return the acquisition time
	 */
	public long getAvAqTime() {
		return m_status.statistics().average_acquire_time();
	}
	
	/**
	 * Obtains the average waiting time.
	 * @return the average waiting time
	 */
	public long getAvWait() {
		return m_status.statistics().average_wait_time();
	}
	
	/**
	 * Obtains the average use time.
	 * @return Obtains the average use time
	 */
	public long getAvUTime() {
		return m_status.statistics().average_usage_time();
	}
	
	/**
	 * Obtains the number of acquisitions without wait.
	 * @return the number
	 */
	public int getAqNowait() {
		return m_status.statistics().counts_with_no_wait();
	}
}