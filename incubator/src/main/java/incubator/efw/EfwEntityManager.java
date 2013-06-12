package incubator.efw;

import incubator.dmgr.DataManager;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;

/**
 * <p>
 * An entity manager is an abstract superclass for all entity managers. Entity
 * managers are classes which are responsible for managing concepts.
 * </p>
 * <p>
 * Entity managers have a lifecycle associated with them: whenever an objects
 * requests an entity manager, one will be allocated (or reused) and will be
 * returned to the called until it is disposed of.
 * </p>
 */
public abstract class EfwEntityManager {
	/**
	 * Maps each entity manager class to their respective data managers.
	 */
	private static Map<Class<?>, DataManager> dataManagers;

	/**
	 * The session currently associated with the entity manager.
	 */
	private Session session;

	/**
	 * Entity managers acquired that should be released in the end. Note that
	 * technically this is a set but we can't invoke hashCode so we'll use it as
	 * a list and avoid all methods that use hashCode. We can't use hashCode
	 * because we're dealing with proxies and invoking hashCode will trigger
	 * hashCode on the proxy which will, in turn, complain that hashCode has no
	 * 
	 * @Transaction annotation defined.
	 */
	private final List<Object> acquired;

	static {
		dataManagers = new HashMap<>();
	}

	/**
	 * Creates a new entity manager.
	 */
	public EfwEntityManager() {
		session = null;
		acquired = new ArrayList<>();
	}

	/**
	 * This method is invoked after the entity manager is released and it is
	 * called to release all resources allocated to the entity manager.
	 * 
	 * @throws EfwException failed to release some acquired entity manager
	 */
	final void doReleased() throws EfwException {
		/*
		 * It doesn't make any sense to inform the entity manager that it has
		 * been released while it is still connected to a session. Check for the
		 * inconsistency here.
		 */
		assert session == null;

		/*
		 * This method can be called during initialization of the EfwManager and
		 * we don't want to force an initialization if one is already running
		 * (we'll get a stack overflow :)). We can skip this by simply making
		 * sure that we only acquire the EfwManager if required.
		 */
		if (acquired.size() == 0) {
			return;
		}

		EfwManager efwm = EfwManager.getInstance();

		for (Object entityManager : acquired) {
			efwm.releaseEntityManager(entityManager);
		}

		acquired.clear();
	}

	/**
	 * Defines the session the entity manager should be working on. This method
	 * is called by the proxy.
	 * 
	 * @param session the session which can be <code>null</code> if the object
	 * is not currently working on any session
	 */
	final void setSession(Session session) {
		this.session = session;
	}

	/**
	 * Obtains the current hibernate session.
	 * 
	 * @return the session or <code>null</code> if no session exists. Sessions
	 * are guaranteed to exist when objects are acquired or released and during
	 * the time between.
	 */
	protected final Session getSession() {
		return this.session;
	}

	/**
	 * <p>
	 * The entity manager has been initialized (created) and placed in the pool
	 * to be used by object requesting it. Note that the session is not defined
	 * during this method invocation.
	 * </p>
	 * <p>
	 * Before this object is used, {@link #acquired()} will be invoked.
	 * </p>
	 */
	protected void init() {
		/*
		 * Nothing to do.
		 */
	}

	/**
	 * <p>
	 * The entity manager has been destroyed (removed from the pool) and will no
	 * longer be used. Note that the session is not defined during this method
	 * invocation.
	 * </p>
	 * <p>
	 * This method is only called on objects in the pool so {@link #released()}
	 * is always called before {@link #destroyed()} unless the object has never
	 * been acquired.
	 * </p>
	 */
	protected void destroyed() {
		/*
		 * Nothing to do.
		 */
	}

	/**
	 * The entity manager has been removed from the pool and allocated to a
	 * user. Note that the session is already defined during this method
	 * invocation.
	 */
	protected void acquired() {
		/*
		 * Nothing to do.
		 */
	}

	/**
	 * The entity manager has been moved back into the pool. Note that the
	 * session is still defined during this method invocation.
	 */
	protected void released() {
		/*
		 * Nothing to do.
		 */
	}

	/**
	 * Acquires an entity manager. This method is used to simplify acquisition
	 * of entity managers. Entity managers would have to be acquired through the
	 * use of the EFW manager. However, they would have to be released before
	 * the method terminates. Acquiring the entity manager with this method
	 * ensures that it will be disposed when the method terminates.
	 * 
	 * @param <T> the type of the entity manager
	 * @param clazz the interface of the entity manager
	 * @return the entity manager
	 * @throws EfwException failed to acquire the entity manager
	 */
	protected final <T> T acquireEntityManager(Class<T> clazz)
			throws EfwException {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz == null");
		}

		EfwManager efwm = EfwManager.getInstance();

		T t = efwm.getEntityManager(clazz);
		assert t != null;

		/*
		 * Note can't use acquired.contains because it will invoke hashCode() on
		 * the proxy and hashCode doesn't has @Transaction annotation.
		 */
		for (Object obj : acquired) {
			assert obj != t;
		}

		acquired.add(t);

		return t;
	}

	/**
	 * Obtains the data manager associated with this entity manager. Note that
	 * there exists only one data manager per entity manager type (class). All
	 * entity managers of the same type share the same data manager.
	 * 
	 * @return the data manager
	 */
	protected final DataManager getDataManager() {
		return getDataManager(getClass());
	}

	/**
	 * Obtains the data manager associated with a type of entity manager. Note
	 * that there exists only one data manager per entity manager type (class).
	 * All entity managers of the same type share the same data manager.
	 * 
	 * @param entityManagerClass the entityManager
	 * @return the data manager
	 */
	protected static final DataManager getDataManager(
			Class<?> entityManagerClass) {
		synchronized (EfwEntityManager.class) {
			DataManager dmgr;
			dmgr = dataManagers.get(entityManagerClass);
			if (dmgr == null) {
				dmgr = new DataManager();
				dataManagers.put(entityManagerClass, dmgr);
			}

			return dmgr;
		}
	}

	/**
	 * Determines whether an object can be added.
	 * 
	 * @param obj the object
	 * 
	 * @throws PropertyVetoException object add has been vetoed
	 */
	protected final void fireCanAdd(Object obj) throws PropertyVetoException {
		DataManager dmgr = getDataManager();
		dmgr.canAdd(obj);
	}

	/**
	 * Determines whether an object can be removed.
	 * 
	 * @param obj the object
	 * 
	 * @throws PropertyVetoException object add has been vetoed
	 */
	protected final void fireCanRemove(Object obj) throws PropertyVetoException {
		DataManager dmgr = getDataManager();
		dmgr.canRemove(obj);
	}

	/**
	 * Determines whether an object can change.
	 * 
	 * @param oldObj the old version of the object
	 * @param newObj new new version of the object
	 * 
	 * @throws PropertyVetoException object add has been vetoed
	 */
	protected final void fireCanChange(Object oldObj, Object newObj)
			throws PropertyVetoException {
		DataManager dmgr = getDataManager();
		dmgr.canChange(oldObj, newObj);
	}

	/**
	 * Informs all listeners that an object has been added.
	 * 
	 * @param obj the object
	 */
	protected final void fireAdded(Object obj) {
		DataManager dmgr = getDataManager();
		dmgr.added(obj);
	}

	/**
	 * Informs all listeners that an object has been removed.
	 * 
	 * @param obj the object
	 */
	protected final void fireRemoved(Object obj) {
		DataManager dmgr = getDataManager();
		dmgr.removed(obj);
	}

	/**
	 * Informs all listeners that an object has been changed.
	 * 
	 * @param oldObj the old version of the object
	 * @param newObj new new version of the object
	 */
	protected final void fireChanged(Object oldObj, Object newObj) {
		DataManager dmgr = getDataManager();
		dmgr.changed(oldObj, newObj);
	}
}
