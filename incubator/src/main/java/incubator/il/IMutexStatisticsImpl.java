package incubator.il;

import java.io.Serializable;

/**
 * Implementation of mutex statistics.
 */
class IMutexStatisticsImpl implements IMutexStatistics, Serializable {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1;
	
	/**
	 * Total acquisition count.
	 */
	private int m_acquire_total_count;
	
	/**
	 * Total acquires without need to wait.
	 */
	private int m_acquire_no_wait_count;
	
	/**
	 * Average acquisition time.
	 */
	private double m_average_acquire_time;
	
	/**
	 * Average waiting time (not counting acquisitions without wait).
	 */
	private double m_average_wait_time;
	
	/**
	 * Average use time in milliseconds.
	 */
	private double m_average_usage_time;
	
	/**
	 * Creates a new statistics implementation.
	 */
	public IMutexStatisticsImpl() {
		reset();
	}
	
	/**
	 * Copy constructor.
	 * @param impl the object to copy
	 */
	IMutexStatisticsImpl(IMutexStatisticsImpl impl) {
		m_acquire_total_count = impl.m_acquire_total_count;
		m_acquire_no_wait_count = impl.m_acquire_no_wait_count;
		m_average_acquire_time = impl.m_average_acquire_time;
		m_average_wait_time = impl.m_average_wait_time;
		m_average_usage_time = impl.m_average_usage_time;
	}
	
	@Override
	public long average_acquire_time() {
		return (long) m_average_acquire_time;
	}
	
	@Override
	public long average_usage_time() {
		return (long) m_average_usage_time;
	}
	
	@Override
	public long average_wait_time() {
		return (long) m_average_wait_time;
	}
	
	@Override
	public int counts_with_no_wait() {
		return m_acquire_no_wait_count;
	}
	
	@Override
	public int total_acquisition_count() {
		return m_acquire_total_count;
	}
	
	/**
	 * Invoked when the mutex has been acquired.
	 * @param wait_time the mutex wait time in milliseconds
	 */
	void acquired(long wait_time) {
		assert wait_time >= 0;
		
		double wt = wait_time;
		
		if (wait_time > 0) {
			int wait_count = m_acquire_total_count - m_acquire_no_wait_count;
			m_average_wait_time = ((m_average_wait_time * wait_count) + wt)
					/ (wait_count + 1);
		} else {
			m_acquire_no_wait_count++;
		}
		
		m_average_acquire_time = ((m_average_acquire_time * m_acquire_total_count)
				+ wt) / (m_acquire_total_count + 1);
		m_acquire_total_count++;
	}
	
	/**
	 * Informs that a model has been released.
	 * @param usage_time the usage time in milliseconds
	 */
	void released(long usage_time) {
		assert usage_time >= 0;
		assert m_acquire_total_count > 0;
		
		double ut = usage_time;
		
		m_average_usage_time = (m_average_usage_time * (m_acquire_total_count - 1)
				+ ut) / m_acquire_total_count;
	}
	
	/**
	 * Resets all statistics.
	 */
	void reset() {
		m_acquire_total_count = 0;
		m_acquire_no_wait_count = 0;
		m_average_acquire_time = 0;
		m_average_wait_time = 0;
		m_average_usage_time = 0;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof IMutexStatisticsImpl)) {
			return false;
		}
		
		IMutexStatisticsImpl impl = (IMutexStatisticsImpl) obj;
		if (impl.m_acquire_no_wait_count != m_acquire_no_wait_count) {
			return false;
		}
		
		if (impl.m_acquire_total_count != m_acquire_total_count) {
			return false;
		}
		
		if (impl.m_average_acquire_time != m_average_acquire_time) {
			return false;
		}
		
		if (impl.m_average_usage_time != m_average_usage_time) {
			return false;
		}

		return impl.m_average_wait_time == m_average_wait_time;

	}
	
	@Override
	public int hashCode() {
		return m_acquire_no_wait_count * 9 + m_acquire_total_count * 7
				+ ((int) m_average_acquire_time) * 5
				+ ((int) m_average_usage_time) * 3 
				+ ((int) m_average_wait_time) * 2;
	}
}
