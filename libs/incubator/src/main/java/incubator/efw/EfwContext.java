package incubator.efw;

import java.util.Stack;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 * <p>
 * Context of a process. An instance of this class will exist per executing
 * thread executing and should be used to create sessions if required. The
 * {@link #getSession(TransactionRequirement)} method will be called upon
 * method entry and the {@link #commit(Session)} will be called upon exit
 * unless an exception is thrown in which case the
 * {@link #rollback(Session)} will be called.
 * </p>
 * <p>
 * If multiple {@link #getSession(TransactionRequirement)} calls are
 * performed, several {@link #commit(Session)} and
 * {@link #rollback(Session)} invocations are expected.
 * <strong>Important:</strong>: only the last invocation of
 * <code>commit</code> or <code>rollback</code> will take effect.
 * </p>
 * <p>
 * If there is possibility of existence of an invalid state due to an
 * exception being thrown, then transactions should be created with
 * {@link TransactionRequirement#REQUIRE_NEW}.
 * </p>
 */
class EfwContext {
	/**
	 * Logger to use.
	 */
	private static final Logger LOGGER = Logger.getLogger(EfwContext.class);

	/**
	 * The factory of hibernate sessions.
	 */
	private static SessionFactory sessionFactory;

	/**
	 * Stack with session requests. Each time a session is requested, a new
	 * request is placed on the stack irrespectively of a new transaction
	 * being issued or the same transaction being reused.
	 */
	private final Stack<SessionRequest> requests;

	/**
	 * Creates a new context.
	 * 
	 * @param factory the session factory
	 */
	EfwContext(SessionFactory factory) {
		assert factory != null;

		requests = new Stack<>();
		sessionFactory = factory;
	}

	/**
	 * Requests the creation of a session with a specific transaction
	 * requirement. If the requirement allows reuse of an existing
	 * transaction, the current transaction may be returned. Note that
	 * {@link #commit(Session)} or {@link #rollback(Session)} will be called
	 * once per invocation of {@link #getSession(TransactionRequirement)}.
	 * 
	 * @param treq the transaction requirement
	 * 
	 * @return a session which can be a new one or a reused one
	 * 
	 * @throws EfwException the exception
	 */
	Session getSession(TransactionRequirement treq) throws EfwException {
		assert treq != null;

		boolean createNew = false;
		if (treq == TransactionRequirement.REQUIRE_NEW
				|| requests.size() == 0) {
			/*
			 * Allocate a new transaction if it is required or if we can't reuse
			 * a previous one.
			 */
			createNew = true;
		}

		Session session;
		if (createNew) {
			session = createHibernateSession();
		} else {
			session = requests.peek().getSession();
		}

		SessionRequest request = new SessionRequest(session, createNew);
		requests.push(request);

		session.beginTransaction();

		return session;
	}

	/**
	 * Commits a session. Note that if
	 * {@link #getSession(TransactionRequirement)} returns the same session
	 * several times, {@link #commit(Session)} will be called several times.
	 * 
	 * @param s the session
	 */
	void commit(Session s) {
		assert s != null;
		assert requests.size() > 0;

		/*
		 * Since transactions are created and committed in a LIFO basis, this
		 * commit must refer to the last transaction.
		 */
		assert requests.peek().getSession() == s;

		/*
		 * Note that even if we get an hibernate failure, the stack is cleared.
		 */
		SessionRequest req = requests.pop();
		if (req.isFirst()) {
			/*
			 * The commit will only be a real commit this is the first use of
			 * the session and it is not an inner transaction.
			 */
			try {
				s.getTransaction().commit();
				s.close();
				s = null;
			} finally {
				/*
				 * We must always close the connection, even if commit fails.
				 */
				if (s != null) {
					try {
						s.close();
					} catch (Exception e) {
						LOGGER.error("Failed to close database connection "
								+ "after a failed commit.", e);
					}
				}
			}
		}
	}

	/**
	 * Rollsback a session. Note that if
	 * {@link #getSession(TransactionRequirement)} returns the same session
	 * several times, {@link #rollback(Session)} may be called several times
	 * and the real rollback should only occur in the last one.
	 * 
	 * @param s the session
	 */
	void rollback(Session s) {
		assert s != null;
		assert requests.size() > 0;

		/*
		 * Since transactions are created and committed in a LIFO basis, this
		 * rollback must refer to the last transaction.
		 */
		assert requests.peek().getSession() == s;

		/*
		 * Note that even if we get an hibernate failure, the stack is cleared.
		 */
		SessionRequest req = requests.pop();
		if (req.isFirst()) {
			/*
			 * The rollback will only be a real rollback this is the first use
			 * of the session and it is not an inner transaction.
			 */
			try {
				s.getTransaction().rollback();
				s.close();
				s = null;
			} finally {
				if (s != null) {
					try {
						s.close();
					} catch (Exception e) {
						LOGGER.error("Failed to close the database "
								+ "connection after a failed rollback.", e);
					}
				}
			}
		}
	}

	/**
	 * Creates an hibernate session.
	 * 
	 * @return the hibernate session
	 * 
	 * @throws EfwException failed to create hibernate session
	 */
	private Session createHibernateSession() throws EfwException {
		/*
		 * Note that we create the session outside a synchronized block because
		 * hibernate is thread-safe and we don't want unneeded "slowliness".
		 */

		try {
			return sessionFactory.openSession();
		} catch (HibernateException e) {
			throw new EfwException("Failed to create hibernate session.", e);
		}
	}

	/**
	 * Each time {@link EfwContext#getSession(TransactionRequirement)} is
	 * invoked, a new <code>SessionRequest</code> is created. This class
	 * will contain the returned session and whether the session was created
	 * in this request. This will allow deciding whether the transaction
	 * should be committed / rolled back or if the commit / rollback request
	 * should be ignored.
	 */
	private static final class SessionRequest {
		/**
		 * The session.
		 */
		private final Session session;

		/**
		 * Is this the first usage of the session?
		 */
		private final boolean first;

		/**
		 * Creates a new session request.
		 * 
		 * @param session the hibernate session returned
		 * @param first is this the first usage of the session?
		 */
		private SessionRequest(Session session, boolean first) {
			assert session != null;

			this.session = session;
			this.first = first;
		}

		/**
		 * Obtains the hibernate session.
		 * 
		 * @return the hibernate session
		 */
		private Session getSession() {
			return session;
		}

		/**
		 * Determines if this is the first usage of the session.
		 * 
		 * @return is it the first usage?
		 */
		private boolean isFirst() {
			return first;
		}
	}
}
