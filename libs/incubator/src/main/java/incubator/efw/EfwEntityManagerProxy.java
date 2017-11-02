package incubator.efw;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;

/**
 * Class that will be used as proxy to the entity manager. This class will be
 * responsible for transaction management.
 */
class EfwEntityManagerProxy {
	/**
	 * Class logger.
	 */
	private static Logger logger = Logger
			.getLogger(EfwEntityManagerProxy.class);

	/**
	 * The entity manager.
	 */
	private final EfwEntityManager entityManager;

	/**
	 * The execution context.
	 */
	private final EfwContext context;

	/**
	 * Creates a new proxy.
	 * 
	 * @param mgr the entity manager
	 * @param ctx the context
	 */
	EfwEntityManagerProxy(EfwEntityManager mgr, EfwContext ctx) {
		assert mgr != null;
		assert ctx != null;

		entityManager = mgr;
		context = ctx;
	}

	/**
	 * Invokes a method. This method will take care of transaction management
	 * (looking at annotations and invoking transaction management routines).
	 * 
	 * @param method the method to be invoked
	 * @param arguments the arguments of the invocation
	 * @return the result of the method execution
	 * @throws Throwable execution failed (exception thrown)
	 */
	Object invoke(Method method, Object[] arguments) throws Throwable {
		assert method != null;

		/*
		 * This is a sanity-check. The entity manager must implement the method
		 * we want to execute or otherwise we kinda screw up somewhere :)
		 */
		assert method.getDeclaringClass().isInstance(entityManager);

		/*
		 * Find out what are the transactional requirements of the method.
		 */
		TransactionRequirement treq = getMethodTransactionRequirement(method);
		if (treq == null) {
			throw new EfwException("Method {" + method + "} does not define "
					+ "a transaction requirement annotation (annotation {"
					+ Transaction.class + "}).");
		}

		/*
		 * The method's return value or thrown exception.
		 */
		Object returnValue = null;
		Throwable thrown = null;

		/*
		 * Open the transaction and set the respective context in the entity
		 * manager. Note that hibernate throws HibernateException which are
		 * unchecked exceptions.
		 */
		Session session;
		try {
			session = context.getSession(treq);
		} catch (HibernateException e) {
			throw new EfwException("Failed to open hibernate session while "
					+ "invoking {" + method + "}.", e);
		}

		/*
		 * We must ensure that there are no pending updates in memory.
		 */
		session.flush();

		entityManager.setSession(session);

		try {
			returnValue = method.invoke(entityManager, arguments);
		} catch (Exception e) {
			thrown = e;
		}

		/*
		 * Method invocation is over, clear the session info from the entity
		 * manager.
		 */
		entityManager.setSession(null);

		/*
		 * If no error has happened, try to commit.
		 */
		if (thrown == null) {
			try {
				context.commit(session);
			} catch (HibernateException e) {
				throw new EfwException("Failed to commit after execution "
						+ "of method {" + method + "}.", e);
			}
		} else {
			/*
			 * We had an exception so try to rollback.
			 */
			try {
				context.rollback(session);
			} catch (HibernateException e) {
				/*
				 * Ok, so now we have a double exception. Just log it and move
				 * forward.
				 */
				logger.warn("Hibernate exception ignored because it was "
						+ "thrown in the context of an exception (current "
						+ "operation is rollback). Method invocation is {"
						+ method + "}, exception is {" + thrown + "}.", e);
			}
		}

		/*
		 * Now that we've handled the session stuff, just return the value or
		 * exception.
		 */
		if (thrown != null) {
			if (thrown instanceof InvocationTargetException) {
				InvocationTargetException ite;
				ite = (InvocationTargetException) thrown;
				thrown = ite.getTargetException();
			}

			throw thrown;
		}

		return returnValue;
	}

	/**
	 * Obtains the entity manager associated with this proxy.
	 * 
	 * @return the entity manager
	 */
	EfwEntityManager getEntityManager() {
		return entityManager;
	}

	/**
	 * Determines what is the transaction requirement of a method.
	 * 
	 * @param method the method
	 * @return the method's transaction requirement
	 */
	private TransactionRequirement getMethodTransactionRequirement(Method method) {
		assert method != null;

		Annotation ann = method.getAnnotation(Transaction.class);
		if (ann == null) {
			return null;
		}

		Transaction tann = (Transaction) ann;
		return tann.requirement();
	}
}
