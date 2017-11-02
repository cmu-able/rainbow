package incubator.il;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of a mutex status.
 */
class IMutexStatusImpl implements IMutexStatus, Serializable {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1;
	
	/**
	 * Request that holds the lock.
	 */
	private IMutexRequestImpl m_locking;
	
	/**
	 * List of the waiting requests.
	 */
	private List<IMutexRequest> m_wait_list;
	
	/**
	 * Mutex statistics.
	 */
	private IMutexStatisticsImpl m_statistics;
	
	
	/**
	 * Created a new status.
	 */
	public IMutexStatusImpl() {
		m_locking = null;
		m_wait_list = new ArrayList<>();
		m_statistics = new IMutexStatisticsImpl();
	}
	
	/**
	 * Creates a new status.
	 * 
	 * @param lock the request that holds the lock
	 * @param wait_list list with request queue which may be empty but cannot
	 * be <code>null</code>
	 * @param stats mutex statistics
	 */
	IMutexStatusImpl(IMutexRequestImpl lock, List<IMutexRequest> wait_list,
			IMutexStatisticsImpl stats) {
		assert wait_list != null;
		assert stats != null;
		
		m_locking = lock;
		m_wait_list = new ArrayList<>(wait_list);
		m_statistics = stats;
	}

	@Override
	public IMutexRequest lock_request() {
		return m_locking;
	}

	@Override
	public List<IMutexRequest> wait_list() {
		return Collections.unmodifiableList(m_wait_list);
	}

	@Override
	public IMutexStatistics statistics() {
		return m_statistics;
	}
}
