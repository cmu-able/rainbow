package incubator.rcli;

import incubator.pval.Ensure;
import incubator.wt.WorkerThread;

/**
 * Thread that processes a session.
 */
class SessionThread extends WorkerThread {
	/**
	 * The session.
	 */
	private SessionImpl m_session;
	
	/**
	 * Creates a new thread.
	 * @param si the session implementation
	 * @param proc the command processor
	 */
	SessionThread(SessionImpl si, CommandProcessor proc) {
		super("Session '" + Ensure.not_null(si).sid() + "'");
		
		m_session = si;
	}
	
	/**
	 * Obtains the session.
	 * @return the session
	 */
	SessionImpl session() {
		return m_session;
	}
}
