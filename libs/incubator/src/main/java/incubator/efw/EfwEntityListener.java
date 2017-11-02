package incubator.efw;

import incubator.dmgr.BeanPropertyChange;
import incubator.dmgr.BeanVetoPropertyListener;
import incubator.dmgr.DataManager;
import incubator.dmgr.EntityListener;

import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

/**
 * Class used to register listeners (veto or entity) informing entity managers
 * of changes on other entity managers.
 */
class EfwEntityListener {
	/**
	 * Logger to use.
	 */
	private static final Logger LOGGER = Logger
			.getLogger(EfwEntityManager.class);

	/**
	 * The interface of the manager that will be invoked.
	 */
	private final Class<?> manager;

	/**
	 * The interface of the manager that will be listened to.
	 */
	private final Class<?> dependent;

	/**
	 * The method to invoke when an 'add' is performed (may be <code>null</code>
	 * ).
	 */
	private final Method add;

	/**
	 * The method to invoke when a 'remove' is performed (may be
	 * <code>null</code>).
	 */
	private final Method remove;

	/**
	 * The method to invoke when a 'change' is performed (may be
	 * <code>null</code>).
	 */
	private final Method change;

	/**
	 * Is the listener registered a veto listener?
	 */
	private final boolean isVeto;

	/**
	 * Create an entity listener. Note that the singleton of the
	 * <code>EfwManager</code> may not yet exist (listeners are created during
	 * singleton initialization) so the constructor cannot rely on the
	 * {@link EfwManager#getInstance()} method.
	 * 
	 * @param manager the interface of the entity manager that will listen to
	 * events
	 * @param dependent the interface of the entity manager that will be
	 * observed
	 * @param normalListener is this listener a normal listener or a veto
	 * listener?
	 * @param addMethod the name of the method to invoke on the manager when the
	 * dependent reports an add (can be <code>null</code>)
	 * @param removeMethod the name of the method to invoke on the manager when
	 * the dependent reports a remove (can be <code>null</code>)
	 * @param changeMethod the name of the method to invoke on the manager when
	 * the dependent reports a change (can be <code>null</code>)
	 * @param mgr the entity manager (the singleton is not directly accessed
	 * during constructor initialization)
	 * 
	 * @throws EfwException failed to initialize the listener
	 */
	EfwEntityListener(Class<?> manager, Class<?> dependent,
			boolean normalListener, String addMethod, String removeMethod,
			String changeMethod, EfwManager mgr) throws EfwException {
		assert manager != null;
		assert dependent != null;

		this.manager = manager;
		this.dependent = dependent;

		isVeto = !normalListener;

		add = findMethod(manager, addMethod, 1);
		remove = findMethod(manager, removeMethod, 1);
		change = findMethod(manager, changeMethod, 2);

		DataManager dmgr = getEntityDataManager(mgr);
		if (normalListener) {
			dmgr.addListener(new EntityListener() {
				@Override
				public void objectAdded(Object object) {
					try {
						invokeMethod(add, object);
					} catch (PropertyVetoException e) {
						assert false;
					}
				}

				@Override
				public void objectRemoved(Object object) {
					try {
						invokeMethod(remove, object);
					} catch (PropertyVetoException e) {
						assert false;
					}
				}

				@Override
				public void propertiesChanged(BeanPropertyChange changes) {
					try {
						invokeMethod(change, changes.getOldValue(), changes
								.getNewValue());
					} catch (PropertyVetoException e) {
						assert false;
					}
				}
			});
		} else {
			dmgr.addVetoListener(new BeanVetoPropertyListener() {
				@Override
				public void objectAdded(Object object)
						throws PropertyVetoException {
					invokeMethod(add, object);
				}

				@Override
				public void objectRemoved(Object object)
						throws PropertyVetoException {
					invokeMethod(remove, object);
				}

				@Override
				public void propertiesChanged(BeanPropertyChange changes)
						throws PropertyVetoException {
					invokeMethod(change, changes.getOldValue(), changes
							.getNewValue());
				}

			});
		}
	}

	/**
	 * Finds a method in an interface which has a given name and the specified
	 * number of parameters.
	 * 
	 * @param intf the interface
	 * @param name the method name (can be <code>null</code>)
	 * @param params the number of parameters
	 * @return the method found or <code>null</code> if <code>name</code> is
	 * <code>null</code>
	 * @throws EfwException failed to find the method (but a name was provided)
	 */
	private Method findMethod(Class<?> intf, String name, int params)
			throws EfwException {
		assert intf != null;
		assert params >= 0;

		if (name == null) {
			return null;
		}

		Method found = null;
		for (Method m : intf.getMethods()) {
			if (!m.getName().equals(name)) {
				continue;
			}

			if (m.getParameterTypes().length == params) {
				if (found != null) {
					throw new EfwException("More than one method with name '"
							+ name + "' with " + params + " parameters was "
							+ "found in class '" + intf + "'.");
				}

				found = m;
			}
		}

		if (found == null) {
			throw new EfwException("No method was found with name '" + name
					+ "' with " + params + " parameters.");
		}

		return found;
	}

	/**
	 * Obtains the dependent's data manager.
	 * 
	 * @param mgr the efw manager
	 * 
	 * @return the dependent data manager
	 * 
	 * @throws EfwException failed to obtain the data manager
	 */
	private DataManager getEntityDataManager(EfwManager mgr)
			throws EfwException {
		Object obj = mgr.getEntityManager(dependent);
		EfwEntityManager emgr = mgr.unmangleProxy(obj);
		DataManager dmgr = emgr.getDataManager();
		mgr.releaseEntityManager(obj);
		return dmgr;
	}

	/**
	 * Invokes a method on the entity manager.
	 * 
	 * @param m the method to invoke (if <code>null</code> nothing is done)
	 * @param parameters the parameters for the invocation
	 * @throws PropertyVetoException the listener is a veto listener and the
	 * invoked method has vetoed
	 */
	private void invokeMethod(Method m, Object... parameters)
			throws PropertyVetoException {
		assert parameters != null;

		if (m == null) {
			return;
		}

		/*
		 * This is kinda messy. We'll get *any* exception we find as a failure.
		 * This will keep the first exception caugt. We'll always try to release
		 * the entity manager if it was acquired but, if it failed, we'll only
		 * record the exception if a previous one hasn't been thrown. At the end
		 * we'll look for a property veto and will throw it if we're a vetoable
		 * listener.
		 */
		Exception failure = null;
		PropertyVetoException pve = null;

		EfwManager mgr = EfwManager.getInstance();
		try {
			Object obj = mgr.getEntityManager(manager);
			try {
				m.invoke(obj, parameters);
			} catch (Exception e) {
				failure = e;
			}

			mgr.releaseEntityManager(obj);
		} catch (Exception e) {
			if (failure == null) {
				failure = e;
			}
		}

		if (failure != null) {
			/*
			 * We're only looking for PropertyVetoException if we're a veto
			 * listener.
			 */
			if (isVeto) {
				if (failure instanceof InvocationTargetException) {
					InvocationTargetException ite;
					ite = (InvocationTargetException) failure;
					Throwable target = ite.getTargetException();
					if (target instanceof PropertyVetoException) {
						pve = (PropertyVetoException) ite.getTargetException();
					}
				}
			}

			if (pve == null) {
				/*
				 * We can't throw errors back so we just register the error.
				 */
				LOGGER.error("Error while invoking method {" + m + "} on "
						+ "entity manager {" + dependent + "}. (Is veto? "
						+ isVeto + ").", failure);
			}
		}

		if (pve != null) {
			throw pve;
		}
	}

	/**
	 * Useless method. Only exists so that checkstyle doesn't complain this is
	 * an utility class :)
	 */
	public void dummy() {
		/*
		 * Nothing to do.
		 */
	}
}
