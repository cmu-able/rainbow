package incubator.il;

import incubator.pval.Ensure;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Date;

/**
 * Thread request implementation.
 */
class IMutexRequestImpl implements IMutexRequest, Serializable {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1;
	
	/**
	 * Name of thread that requested the mutex.
	 */
	private String m_thread_name;
	
	/**
	 * Thread acquisition trace.
	 */
	private String m_trace;
	
	/**
	 * Request date. 
	 */
	private Date m_request;
	
	/**
	 * Total wait time (<code>-1</code> if not acquired yet).
	 */
	private long m_wait_time;
	
	/**
	 * Have we waited for this lock? 
	 */
	private boolean m_waited;
	
	/**
	 * Creates a new mutex request. The request data is obtained automatically
	 * from the current context.
	 */
	public IMutexRequestImpl() {
		m_thread_name = Thread.currentThread().getName();
		m_request = new Date();
		m_wait_time = -1;
		
		Exception e = new Exception();
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		m_trace = sw.toString();
		m_waited = false;
	}
	
	@Override
	public String acquisition_thread() {
		return m_thread_name;
	}

	@Override
	public String acquisition_trace() {
		return m_trace;
	}

	@Override
	public Date request_time() {
		return m_request;
	}

	@Override
	public long wait_time() {
		if (m_waited) {
			if (m_wait_time == -1) {
				return new Date().getTime() - m_request.getTime();
			} else {
				return m_wait_time;
			}
		} else {
			return 0;
		}
	}
	
	/**
	 * Marks that we have waited for the lock.
	 */
	void mark_waited() {
		m_waited = true;
	}
	
	/**
	 * Invoked when the mutex has been acquired.
	 */
	void acquired() {
		Ensure.equals(-1l, m_wait_time);
		
		m_wait_time = new Date().getTime() - m_request.getTime();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof IMutexRequestImpl)) {
			return false;
		}
		
		IMutexRequestImpl impl = (IMutexRequestImpl) obj;
		
		if (!m_thread_name.equals(impl.m_thread_name)) {
			return false;
		}
		
		if (!m_request.equals(impl.m_request)) {
			return false;
		}
		
		if (!m_trace.equals(impl.m_trace)) {
			return false;
		}
		
		if (m_wait_time != impl.m_wait_time) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public int hashCode() {
		return m_thread_name.hashCode() * 7 + m_request.hashCode() * 5
				+ m_trace.hashCode() * 3 + ((int) m_wait_time) * 2;
	}
}
