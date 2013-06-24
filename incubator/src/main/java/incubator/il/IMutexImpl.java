package incubator.il;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of a mutex.
 */
class IMutexImpl implements IMutex {
	/**
	 * Thread that owns the mutex, <code>null</code> if it hasn't been
	 * acquired.
	 */
	private Thread m_owner;
	
	/**
	 * Nesting level: <code>0</code> means the mutex is free, <code>1</code>
	 * if it is acquired without nesting. Higher values means there is
	 * nesting.
	 */
	private int m_nesting;
	
	/**
	 * Wait queue.
	 */
	private List<IMutexRequest> m_waiting;
	
	/**
	 * Request that acquired the mutex.
	 */
	private IMutexRequestImpl m_locker;
	
	/**
	 * Statistics.
	 */
	private IMutexStatisticsImpl m_statistics;
	
	/**
	 * Timestamp when the lock was acquired (in system milliseconds).
	 */
	private long m_start_time;
	
	/**
	 * Creates a new mutex.
	 */
	IMutexImpl() {
		m_owner = null;
		m_locker = null;
		m_nesting = 0;
		m_waiting = new ArrayList<>();
		m_statistics = new IMutexStatisticsImpl();
		m_start_time = 0;
	}
	
	@Override
	public void acquire() {
		IMutexRequestImpl req = null;
		synchronized(this) {
			if (Thread.currentThread() == m_owner) {
				m_nesting++;
				return;
			}
			
			req = new IMutexRequestImpl();
			m_waiting.add(req);
		}
		
		wait_for_acquire(req);
	}

	@Override
	public boolean try_acquire() {
		synchronized(this) {
			if (Thread.currentThread() == m_owner) {
				m_nesting++;
				return true;
			}
			
			if (m_owner != null) {
				return false;
			}
			
			do_acquire(new IMutexRequestImpl());
			return true;
		}
	}
	
	@Override
	public synchronized IMutexStatus status_snapshot() {
		return new IMutexStatusImpl(m_locker, m_waiting,
				new IMutexStatisticsImpl(m_statistics));
	}

	@Override
	public void release() {
		synchronized(this) {
			if (Thread.currentThread() == m_owner) {
				m_nesting--;
				
				if (m_nesting == 0) {
					m_owner = null;
					m_locker = null;
					m_statistics.released(System.currentTimeMillis()
							- m_start_time);
					notifyAll();
				}
			} else {
				throw new IllegalStateException("O thread actual '"
						+ Thread.currentThread().getName() + "' não é dono do "
						+ "mutex " + (m_owner == null? "(o mutex não tem dono)"
						: "(o dono é '" + m_owner.getName() + "')") + ".");
			}
		}
	}

	@Override
	public void reset_statistics() {
		m_statistics.reset();
	}
	
	/**
	 * Waits until there is no current lock and the given request is the first
	 * in the queue. Then, it uses this request to lock the mutex and exits.
	 * @param req the lock request
	 */
	private void wait_for_acquire(IMutexRequestImpl req) {
		assert req != null;
		
		synchronized(this) {
			while(m_owner != null || m_waiting.get(0) != req) {
				try {
					wait();
				} catch (InterruptedException e) {
					// Ignoramos.
				}
			}
			
			m_waiting.remove(0);
			
			do_acquire(req);
		}
	}
	
	/**
	 * Acquires the lock.
	 * @param req the lock request
	 */
	private void do_acquire(IMutexRequestImpl req) {
		m_owner = Thread.currentThread();
		assert m_nesting == 0;
		m_nesting = 1;
		m_locker = req;
		m_locker.acquired();
		m_statistics.acquired(m_locker.wait_time());
		m_start_time = System.currentTimeMillis();
	}

	@Override
	public synchronized boolean is_acquired() {
		return m_owner == null;
	}
}
