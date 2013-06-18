package incubator.efw;

import incubator.dmgr.DataManager;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/*
 * Implementation note: threading and use of the EfwContext is rather
 * complicated so I decided to explain it all here :) When an object
 * requests an entity manager, we must return a proxy to the entity manager.
 * This proxy requires to things: the entity manager itself (to whom calls
 * are delegated) and the EfwContext which is responsible for the database
 * transactions. The entity manager itself is obtained with an EntityControl
 * class. The context is thread-dependent. The conceptual principle is
 * simple: for each running thread there is an EfwContext object so a simple
 * map from thread to the EfwContext would do the trick. The problem is that
 * we don't know when threads are created or removed. So this is how we
 * solved the issue: when an entity manager is created we check in which
 * thread we're running. If we've never asked for an entity manager from
 * this thread we allocate a new ThreadUse instance (which keeps an
 * EfwContext object associated with it) and tell it we've allocated a
 * specific entity manager in the thread. The next time an entity manager is
 * requested from the same thread, we already have the ThreadUse instance
 * and will use its EfwContext class. Note that the ThreadUse class will
 * keep track of all entity managers that were allocated in that thread.
 * When an entity manager is released, we find the respective ThreadUse
 * object and will remove the entity manager from the set of entity managers
 * associated with the ThreadUse class. When no more entity managers remain
 * the ThreadUse and EfwContext may be discarded. This way, a ThreadUse
 * object and an EfwObject are allocated the first time a thread requests an
 * entity manager and are disposed as soon as all entity managers allocated
 * from that thread are released.
 */
/**
 * Class used to obtain entity managers.
 */
public final class EfwManager {
	/**
	 * Keyword used to specify that a listener relationship is an entity
	 * relationship.
	 */
	public static final String TYPE_ENTITY = "entity";

	/**
	 * Keyword used to specify that a listener relationship is a veto
	 * relationship.
	 */
	public static final String TYPE_VETO = "veto";

	/**
	 * Keyword used to identify that no method is defined for the operation.
	 */
	public static final String NULL_METHOD = "null";

	/**
	 * Regular expression that matches a class name.
	 */
	private static final String CLASS_REGEX = "\\w+(?:\\.\\w+)*";

	/**
	 * Regular expression that matches a listener registration.
	 */
	private static final String LISTENER_REGEX = "(" + CLASS_REGEX + ")->("
			+ CLASS_REGEX + ")\\((\\w+)\\)";

	/**
	 * Regular expression that matches the registration information.
	 */
	private static final String REG_REGEX = "(\\w+),(\\w+),(\\w+)";

	/**
	 * The logger to use.
	 */
	private static Logger logger = Logger.getLogger(EfwManager.class);

	/**
	 * The singleton instance.
	 */
	private static EfwManager instance;

	/**
	 * Maps the interfaces to the entities that control them.
	 */
	private Map<Class<?>, EntityControl> controllers;

	/**
	 * Hibernate session factory to use. The manager cannot be used while this
	 * hasn't been set with the {@link #setSessionFactory(SessionFactory)}
	 * method.
	 */
	private static SessionFactory factory;

	/**
	 * Maps threads to {@link ThreadUse} objects which allow us to keep track of
	 * the {@link EfwContext} associated with thread.
	 */
	private Map<Thread, ThreadUse> threads;

	/**
	 * Obtains the singleton instance.
	 * 
	 * @return the instance
	 */
	public static synchronized EfwManager getInstance() {
		if (instance == null) {
			instance = new EfwManager();
		}

		return instance;
	}

	/**
	 * Creates a new manager.
	 */
	private EfwManager() {
		/*
		 * Ensure both controllers and threads object are thread-safe.
		 */
		controllers = new HashMap<>();
		controllers = Collections.synchronizedMap(controllers);
		threads = new HashMap<>();
		threads = Collections.synchronizedMap(threads);

		/*
		 * Load the efw-objects.properties file.
		 */
		Properties efwObjects = new Properties();
		try {
			try (InputStream is = getClass().getResourceAsStream(
					"/efw-objects.properties")) {
				if (is == null) {
					logger.error("The 'efw-objects.properties' resource could "
							+ "not be loaded. No entity managers will be "
							+ "available.");
				} else {
					efwObjects.load(is);
				}
			}
		} catch (IOException e) {
			logger.error("Failed to load resource "
					+ "'efw-objects.properties'. Some entity managers "
					+ "may not be available.", e);
		}

		/*
		 * Process all entries in the properties file. Listener registrations
		 * are saved in a map in order to be processed after the class
		 * registrations.
		 */
		Map<String, String> lreg = new HashMap<>();
		for (Map.Entry<Object, Object> e : efwObjects.entrySet()) {
			Object k = e.getKey();
			Object v = e.getValue();

			String ks = ((String) k).trim();
			String vs = ((String) v).trim();

			/*
			 * Check if the key and value look like class names.
			 */
			if (Pattern.matches(CLASS_REGEX, ks)
					&& Pattern.matches(CLASS_REGEX, vs)) {
				processEntityDefinition(ks, vs);
				continue;
			}

			if (Pattern.matches(LISTENER_REGEX, ks)
					&& Pattern.matches(REG_REGEX, vs)) {
				lreg.put(ks, vs);
				continue;
			}

			/*
			 * If we didn't do anything with the entry, log it.
			 */
			logger.error("Entry '" + ks + "' ignored in "
					+ "efw-objects.properties.");
		}

		/*
		 * Now process all listener registrations.
		 */
		for (Map.Entry<String, String> e : lreg.entrySet()) {
			String k = e.getKey();
			String v = e.getValue();
			processListenerRegistration(k, v);
		}
	}

	/**
	 * Processes an entity definition in the property configuration file.
	 * 
	 * @param intfName the interface name
	 * @param clazzName the class name
	 */
	private void processEntityDefinition(String intfName, String clazzName) {
		Class<?> intf = null;
		Class<?> clazz = null;
		try {
			intf = Class.forName(intfName);
		} catch (Exception e) {
			logger.error("Failed to load interface {" + intfName + "}. "
					+ "The entity manager will not be available.", e);
			return;
		}

		try {
			clazz = Class.forName(clazzName);
		} catch (Exception e) {
			logger.error("Failed to load class {" + clazzName + "}. "
					+ "The entity manager will not be available.", e);
			return;
		}

		if (!intf.isInterface()) {
			logger.error("Class {" + intf + "} is not an interface. The "
					+ "entity manager will not be available.");
			return;
		}

		if (clazz.isInterface()) {
			logger.error("Class {" + clazz + "} is an interface but should "
					+ "be an implementing class. The entity manager will "
					+ "not be available.");
			return;
		}

		if (!intf.isAssignableFrom(clazz)) {
			logger.error("Class {" + clazz + "} should implement "
					+ "interface {" + intf + "}. The entity manager will "
					+ "not be available.");
			return;
		}

		/*
		 * Now that we've passed 999999999 tests we can set up the entity
		 * controller.
		 */
		EntityControl controller = new EntityControl(clazz);
		controllers.put(intf, controller);
	}

	/**
	 * Processes a listener registration.
	 * 
	 * @param l the listener definition according to the {@link #LISTENER_REGEX}
	 * regular expression
	 * @param reg the registration definition according to the
	 * {@link #REG_REGEX} regular expression
	 */
	private void processListenerRegistration(String l, String reg) {
		assert l != null;
		assert reg != null;

		/*
		 * Extract the names of the observer and observed interfaces. Also
		 * extract the type of observer.
		 */
		Pattern p = Pattern.compile(LISTENER_REGEX);
		Matcher m = p.matcher(l);
		boolean matches = m.matches();
		assert matches;
		int i = 0; // Gotta use this otherwise checkstyle complains.
		String observerInterface = m.group(++i);
		String observedInterface = m.group(++i);
		String type = m.group(++i);

		/*
		 * Ensure that the type is correct.
		 */
		if (!type.equals(TYPE_ENTITY) && !type.equals(TYPE_VETO)) {
			logger.error("Relationship type '" + type + "' invalid in "
					+ "definition '" + l + "'. Only '" + TYPE_ENTITY + "' or '"
					+ TYPE_VETO + "' are allowed.");
			return;
		}

		/*
		 * Ensure that both interfaces are registered as entity interfaces.
		 */
		Class<?> observerClass = null;
		Class<?> observedClass = null;
		try {
			observerClass = Class.forName(observerInterface);
		} catch (Exception e) {
			logger.error("Failed to load class '" + observerInterface + "' "
					+ "referred to in listener registration '" + l + "'.", e);
			return;
		}

		try {
			observedClass = Class.forName(observedInterface);
		} catch (Exception e) {
			logger.error("Failed to load class '" + observerInterface + "' "
					+ "referred to in listener registration '" + l + "'.", e);
			return;
		}

		if (!controllers.containsKey(observerClass)) {
			logger.error("No entity manager registered for interface '"
					+ observerInterface + "'.");
		}

		if (!controllers.containsKey(observedClass)) {
			logger.error("No entity manager registered for interface '"
					+ observedInterface + "'.");
		}

		/*
		 * Obtain the names of the methods (and detect nulls).
		 */
		p = Pattern.compile(REG_REGEX);
		m = p.matcher(reg);
		matches = m.matches();
		assert matches;
		String add = null;
		i = 0; // Gotta use this otherwise checkstyle complains.
		if (!m.group(++i).equals(NULL_METHOD)) {
			add = m.group(i).trim();
		}

		String remove = null;
		if (!m.group(++i).equals(NULL_METHOD)) {
			remove = m.group(i).trim();
		}

		String change = null;
		if (!m.group(++i).equals(NULL_METHOD)) {
			change = m.group(i).trim();
		}

		try {
			@SuppressWarnings("unused")
			EfwEntityListener efl = new EfwEntityListener(observerClass,
					observedClass, type.equals(TYPE_ENTITY), add, remove,
					change, this);
		} catch (EfwException e) {
			logger.error("Failed to create listener according to "
					+ "registration '" + l + "': '" + reg + "'.", e);
		}
	}

	/**
	 * Obtains an entity manager which implements a given interface. Before this
	 * method may be called {@link #setSessionFactory(SessionFactory)} must be
	 * invoked to define the hibernate session factory.
	 * 
	 * @param <T> the type of the entity manager
	 * @param intf the interface to implement
	 * 
	 * @return the entity manager. Note that only the methods on the interface
	 * should be invoked. Even if the caller knows which class is implementing
	 * this interface <strong>the result cannot be cast</strong> to the class.
	 * 
	 * @throws EfwException failed to obtain the entity manager
	 */
	public <T> T getEntityManager(Class<T> intf) throws EfwException {
		if (intf == null) {
			throw new IllegalArgumentException("intf == null");
		}

		if (factory == null) {
			throw new IllegalStateException("setSessionFactory must be "
					+ "called before entity managers can be obtained.");
		}

		/*
		 * Don't forget this method is invoked in a multi-threaded environment.
		 */

		/*
		 * First we must get the entity manager.
		 */
		EfwEntityManager manager = null;
		synchronized (controllers) {
			EntityControl controller = controllers.get(intf);
			if (controller == null) {
				throw new EfwException("No entity manager is registered for "
						+ "interface {" + intf + "}.");
			}

			manager = controller.get();
		}

		/*
		 * Secondly we must obtain a context in which the entity manager is
		 * called. The context is dependent on the current thread.
		 */
		EfwContext context;
		Thread current = Thread.currentThread();
		ThreadUse currentThreadUse = threads.get(current);
		if (currentThreadUse == null) {
			/*
			 * There is no racing condition here.
			 */
			currentThreadUse = new ThreadUse(factory);
			threads.put(current, currentThreadUse);
		}

		context = currentThreadUse.getContext();
		currentThreadUse.addEntityManager(manager);

		/*
		 * Thirdly we create the proxy.
		 */
		InvocationHandler ih = new ProxyInvocationHandler(manager, context,
				intf);
		ClassLoader ldr = getClass().getClassLoader();
		Object proxy = Proxy.newProxyInstance(ldr, new Class[] { intf }, ih);

		/*
		 * Hack because of unchecked conversion :) Stupid erasure!
		 */
		@SuppressWarnings("unchecked")
		T t = (T) proxy;

		/*
		 * At the end, we inform the manager that it has been acquired.
		 */
		Session s = context.getSession(TransactionRequirement.REQUIRE_ANY);
		manager.setSession(s);
		manager.acquired();
		context.commit(s);
		manager.setSession(null);

		return t;
	}

	/**
	 * Releases an entity manager.
	 * 
	 * @param obj the entity manager which has been previously allocated with
	 * the {@link #getEntityManager(Class)} method
	 * 
	 * @throws EfwException failed to release the entity manager
	 */
	public void releaseEntityManager(Object obj) throws EfwException {
		if (obj == null) {
			throw new IllegalArgumentException("obj == null");
		}

		/*
		 * Firstly, lets make sure obj is a proxy and get the entity manager
		 * associated with it.
		 */
		InvocationHandler ih = null;
		ProxyInvocationHandler pih = null;

		if (Proxy.isProxyClass(obj.getClass())) {
			ih = Proxy.getInvocationHandler(obj);
			if (ih instanceof ProxyInvocationHandler) {
				pih = (ProxyInvocationHandler) ih;
			}
		}

		if (pih == null) {
			throw new IllegalArgumentException("Object {" + obj + "} was not "
					+ "returned by the EfwManager class.");
		}

		EfwEntityManager mgr = pih.getEntityManager();

		/*
		 * Ok, now that we've got the real entity manager, lets find the
		 * ThreadUSe object associated with this thread and ensure that this
		 * manager was obtained in the thread. If everything is ok, remove the
		 * association.
		 */
		Thread current = Thread.currentThread();
		ThreadUse threadUse = threads.get(current);
		if (threadUse != null && threadUse.containsEntityManager(mgr)) {
			boolean stillInUse = threadUse.removeEntityManager(mgr);
			if (!stillInUse) {
				/*
				 * Ok, so the ThreadUse is no longer useful (no more managers
				 * are registered). Remove it.
				 */
				threads.remove(current);
			}
		} else {
			/*
			 * Uh, uh, oh my bad! We've tried to release an object from a thread
			 * different than the one we've allocated the object in. Naughtly
			 * code. Thankfully, we've detected and will throw an exception :)
			 */
			throw new IllegalStateException("Cannot release an entity "
					+ "manager from a thread different from the one "
					+ "we've created the entity manager in (entity "
					+ "manager is {" + obj + "} and " + "thread is {" + current
					+ "}.");
		}

		/*
		 * Cool, now all we need to do at the end is to return the object to the
		 * pool.
		 */
		Class<?> intf = pih.getInterface();
		EntityControl ec = controllers.get(intf);
		assert ec != null;
		ec.put(mgr);

		/*
		 * At the end, we inform the manager that it has been released.
		 */
		EfwContext context = threadUse.getContext();
		Session s = context.getSession(TransactionRequirement.REQUIRE_ANY);
		mgr.setSession(s);
		mgr.released();
		context.commit(s);
		mgr.setSession(null);
		mgr.doReleased();
	}

	/**
	 * Obtains the entity manager inside a given proxy object.
	 * 
	 * @param obj the proxy object
	 * 
	 * @return the entity manager
	 */
	EfwEntityManager unmangleProxy(Object obj) {
		InvocationHandler ih = null;
		ProxyInvocationHandler pih = null;

		if (Proxy.isProxyClass(obj.getClass())) {
			ih = Proxy.getInvocationHandler(obj);
			if (ih instanceof ProxyInvocationHandler) {
				pih = (ProxyInvocationHandler) ih;
			}
		}

		if (pih == null) {
			throw new IllegalArgumentException("Object {" + obj + "} was not "
					+ "returned by the EfwManager class.");
		}

		return pih.getEntityManager();
	}

	/**
	 * Unit test method. Adds registration of a new entity manager.
	 * 
	 * @param intf the entity manager's interface
	 * @param impl the entity manager's implementation
	 */
	public void junitAddEntityManager(Class<?> intf, Class<?> impl) {
		assert intf != null;
		assert impl != null;

		processEntityDefinition(intf.getName(), impl.getName());
	}

	/**
	 * Unit test method. Adds a listener relationship.
	 * 
	 * @param l the listener definition. Should be in the form
	 * <code>interface1->interface2(type)</code> where <code>interface1</code>
	 * will be informed when <code>interface2</code> fires an event.
	 * <code>type</code> should be <code>entity</code> or <code>veto</code>
	 * depending on whether this relationship is normal or veto relationship.
	 * For instance, if entity manager <code>foo.Foo</code> needs to be a veto
	 * listener of entity manager <code>bar.Bar</code> then this parameter
	 * should be <code>foo.Foo->bar.Bar(veto)</code>
	 * @param reg registration information. This contains the names of the
	 * methods on the listener that will be invoked when the observed entity
	 * manager fires an event. The entry contains three words separated by
	 * commas which indicate the names of the methods invoked upon add, remove
	 * and change (respectively). The indicated methods will be invoked when the
	 * observed entity fires the respective event. If any entry is
	 * <code>null</code> no method will be invoked. For instance, in the example
	 * given for the <code>l</code> parameter, to have methods
	 * <code>barAdded</code> and <code>barRemoved</code> invoked when entity
	 * manager <code>bar.Bar</code> fires an add or remove event and to have no
	 * method be invoked when <code>bar.Bar</code> fires a change event, this
	 * parameter should be <code>barAdded,barRemoved,null</code>
	 */
	public void junitAddListener(String l, String reg) {
		assert l != null;
		assert reg != null;
		assert Pattern.matches(LISTENER_REGEX, l);
		assert Pattern.matches(REG_REGEX, reg);

		processListenerRegistration(l, reg);
	}

	/**
	 * Obtains the total number of managers currently active. This method is
	 * useful for debugging purposes (to detect leaks).
	 * 
	 * @return the number of active managers
	 */
	public int junitGetTotalNumberOfActiveManagers() {
		int total = 0;

		for (EntityControl c : controllers.values()) {
			total += c.getNumberActiveManagers();
		}

		return total;
	}

	/**
	 * Obtains the data manager for a given entity manager type. This method can
	 * be used to add listeners during unit testing.
	 * 
	 * @param entityManager the entity manager
	 * 
	 * @return the data manager
	 */
	public DataManager junitGetDataManager(Class<?> entityManager) {
		if (entityManager == null) {
			throw new IllegalArgumentException("entityManager == null");
		}

		EntityControl ec = controllers.get(entityManager);
		if (ec == null) {
			throw new IllegalStateException("No controller is associated with "
					+ "entity manager interface '" + entityManager + "'.");
		}

		Class<?> emClass = ec.clazz;
		DataManager dm = EfwEntityManager.getDataManager(emClass);
		return dm;
	}

	/**
	 * Defines the session factory to use to create hibernate sessions.
	 * 
	 * @param factory the factory to use
	 */
	public static synchronized void setSessionFactory(SessionFactory factory) {
		if (factory == null) {
			throw new IllegalArgumentException("factory == null");
		}

		if (EfwManager.factory != null) {
			throw new IllegalStateException("Session factory already set.");
		}

		EfwManager.factory = factory;
	}

	/**
	 * Class responsible for controlling an entity. (Maintaining the pool,
	 * allocating objects, etc.)
	 */
	private static final class EntityControl {
		/**
		 * Maximum number of active objects in a pool. There is no theoretical
		 * limit on the number of active objects in the pool but if we get more
		 * than 100 we're probably doing something wrong...
		 */
		private static final int MAX_ACTIVE_OBJECTS = 10;

		/**
		 * The pool of entity managers.
		 */
		private final ObjectPool pool;

		/**
		 * The class of the entity managers.
		 */
		private final Class<?> clazz;

		/**
		 * Creates a new entity control.
		 * 
		 * @param clazz the class implementing the entity manager
		 */
		private EntityControl(Class<?> clazz) {
			assert clazz != null;

			this.clazz = clazz;

			GenericObjectPool.Config cfg = new GenericObjectPool.Config();

			cfg.maxActive = MAX_ACTIVE_OBJECTS;
			cfg.maxIdle = GenericObjectPool.DEFAULT_MAX_IDLE;
			cfg.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_FAIL;
			cfg.testOnBorrow = false;
			cfg.testOnReturn = false;
			cfg.timeBetweenEvictionRunsMillis = -1;
			cfg.minEvictableIdleTimeMillis = -1;
			cfg.testWhileIdle = false;
			cfg.numTestsPerEvictionRun = -1;

			pool = new GenericObjectPool(new PoolableObjectFactory() {
				@Override
				public void activateObject(Object obj) throws Exception {
					/*
					 * Nothing to do.
					 */
				}

				@Override
				public void destroyObject(Object obj) throws Exception {
					if (obj == null) {
						throw new IllegalArgumentException("obj == null");
					}

					if (!(obj instanceof EfwEntityManager)) {
						throw new IllegalArgumentException("!(obj instanceof "
								+ "EfwEntityManager)");
					}

					EfwEntityManager mgr = (EfwEntityManager) obj;
					mgr.destroyed();
				}

				@Override
				public Object makeObject() throws Exception {
					Object object = EntityControl.this.clazz.newInstance();
					EfwEntityManager mgr = (EfwEntityManager) object;
					mgr.init();
					return mgr;
				}

				@Override
				public void passivateObject(Object arg0) throws Exception {
					/*
					 * Nothing to do.
					 */
				}

				@Override
				public boolean validateObject(Object obj) {
					return true;
				}
			}, cfg);
		}

		/**
		 * Gets an entity manager.
		 * 
		 * @return the entity manager
		 * 
		 * @throws EfwException failed to get an entity manager from the pool
		 */
		private EfwEntityManager get() throws EfwException {
			try {
				EfwEntityManager emgr;
				emgr = (EfwEntityManager) pool.borrowObject();
				return emgr;
			} catch (Exception e) {
				throw new EfwException("Failed to get an entity manager from "
						+ "the pool (class is {" + clazz + "}).", e);
			}
		}

		/**
		 * Returns an entity manager to the pool.
		 * 
		 * @param emgr the entity manager
		 * 
		 * @throws EfwException failed to put the entity manager back in the
		 * pool
		 */
		private void put(EfwEntityManager emgr) throws EfwException {
			assert emgr != null;
			try {
				pool.returnObject(emgr);
			} catch (Exception e) {
				throw new EfwException("Failed to return an entity manager to "
						+ "the pool (class is {" + clazz + "}).", e);
			}
		}

		/**
		 * Obtains the number of active managers.
		 * 
		 * @return the number of active managers
		 */
		private int getNumberActiveManagers() {
			return pool.getNumActive();
		}
	}

	/**
	 * Class that keeps track of an {@link EfwContext} class and the entity
	 * managers that were allocated using that context.
	 */
	private static final class ThreadUse {
		/**
		 * The context associated with this thread.
		 */
		private final EfwContext context;

		/**
		 * The set of entity managers associated with this thread.
		 */
		private final Set<EfwEntityManager> managers;

		/**
		 * Creates a new object.
		 * 
		 * @param factory session factory
		 */
		private ThreadUse(SessionFactory factory) {
			assert factory != null;

			context = new EfwContext(factory);
			managers = new HashSet<>();
		}

		/**
		 * Obtains the context associated with the thread.
		 * 
		 * @return the context
		 */
		private EfwContext getContext() {
			return context;
		}

		/**
		 * Adds a new entity manager to the list of managers associated with
		 * this thread.
		 * 
		 * @param manager the manager
		 */
		private void addEntityManager(EfwEntityManager manager) {
			assert manager != null;
			assert !managers.contains(manager);

			managers.add(manager);
		}

		/**
		 * Checks whether the thread is associated with an entity manager.
		 * 
		 * @param manager the entity manager to check
		 * @return is the thread associated with the manager?
		 */
		private boolean containsEntityManager(EfwEntityManager manager) {
			assert manager != null;

			return managers.contains(manager);
		}

		/**
		 * Removes an entity manager from the list of managers.
		 * 
		 * @param manager the manager to remove
		 * @return are there still more managers associated with this thread?
		 */
		private boolean removeEntityManager(EfwEntityManager manager) {
			assert manager != null;
			assert containsEntityManager(manager);

			managers.remove(manager);
			return managers.size() > 0;
		}
	}

	/**
	 * Adapter class that implements an invocation handler required by the java
	 * proxy interface to the {@link EfwEntityManagerProxy} class.
	 */
	private static final class ProxyInvocationHandler implements
			InvocationHandler {
		/**
		 * The proxy class.
		 */
		private final EfwEntityManagerProxy proxy;

		/**
		 * The interface implemented by the proxy.
		 */
		private final Class<?> interf;

		/**
		 * Creates a new invocation handler.
		 * 
		 * @param manager the entity manager
		 * @param context the execution context
		 * @param interf the interface implemented by the proxy
		 */
		private ProxyInvocationHandler(EfwEntityManager manager,
				EfwContext context, Class<?> interf) {
			assert manager != null;
			assert context != null;
			assert interf != null;

			proxy = new EfwEntityManagerProxy(manager, context);
			this.interf = interf;
		}

		/**
		 * Obtains the entity manager associated with the proxy.
		 * 
		 * @return the entity manager
		 */
		private EfwEntityManager getEntityManager() {
			return proxy.getEntityManager();
		}

		/**
		 * Obtains the interface implemented by the proxy.
		 * 
		 * @return the interface
		 */
		private Class<?> getInterface() {
			return interf;
		}

		@Override
		public Object invoke(Object obj, Method method, Object[] args)
				throws Throwable {
			return proxy.invoke(method, args);
		}
	}

	/**
	 * Obtains the factory of the entity manager.
	 * 
	 * @return the factory of the entity manager.
	 */
	public static SessionFactory getEntityFactory() {
		return factory;
	}
}
