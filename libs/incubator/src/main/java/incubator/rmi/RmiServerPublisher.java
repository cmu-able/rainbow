package incubator.rmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Set;

import net.ladypleaser.rmilite.impl.RemoteInvocationHandlerImpl;

import org.apache.log4j.Logger;

/**
 * Publishes an object through RMI.
 */
public class RmiServerPublisher {
	/**
	 * Logger to use.
	 */
	private static final Logger LOGGER;
	
	/**
	 * Registries that have been created.
	 */
	private static Set<Registry> m_registries;
	
	static {
		LOGGER = Logger.getLogger(RmiServerPublisher.class);
		m_registries = new HashSet<>();
	}
	
	/**
	 * Publishes a service.
	 * @param cls the service class
	 * @param obj the service object
	 * @return port the port used to publish the service
	 * @throws RmiCommException failed to publish the service
	 */
	public static int publish_service(Class<?> cls, Object obj)
			throws RmiCommException {
		return publish_service(cls, obj, 0);
	}
	
	/**
	 * Publishes a service.
	 * @param cls the service class
	 * @param obj the service object
	 * @param port the port to publish the service; if <code>0</code> then
	 * several default ports are tried
	 * @return port the port used to publish the service
	 * @throws RmiCommException failed to publish the service
	 */
	public static int publish_service(Class<?> cls, Object obj, int port)
			throws RmiCommException {
		if (cls == null) {
			throw new IllegalArgumentException("cls == null");
		}
		
		if (obj == null) {
			throw new IllegalArgumentException("obj == null");
		}
		
		LOGGER.debug("Publishing service '" + cls.getName() + "'...");
		
		/*
		 * We simulate what rmilite does in a Server().publish() but we
		 * change it to keep the registry.
		 */
		Registry registry = null;
		int minPort;
		int maxPort;
		
		if (port == 0) {
			RmiCommunicationPorts ports = new RmiCommunicationPorts();
			minPort = ports.min_port();
			maxPort = ports.max_port();
		} else {
			minPort = port;
			maxPort = port;
		}
		
		for (port = minPort; port <= maxPort; port++) {
			try {
				registry = LocateRegistry.createRegistry(port);
				break;
			} catch (RemoteException e) {
				LOGGER.debug("Failed to publish in port " + port + ".", e);
			}
		}
		
		if (registry == null) {
			throw new RmiCommException("Failed to launch RMI server in port "
					+ "range [" + minPort + ", " + maxPort + "]. is there any "
					+ "port free?", null);
		}
		
		synchronized(RmiServerPublisher.class) {
			m_registries.add(registry);
		}
		
		try {
			RemoteInvocationHandlerImpl handler;
			handler = new RemoteInvocationHandlerImpl(obj,
					new HashSet<Class<?>>());
			registry.rebind(cls.getName(), handler);
		} catch (RemoteException e) {
			throw new RmiCommException("Failed to publish object '" + obj
					+ "' in RMI server at port " + port + " with interface '"
					+ cls.getName() + "'.", e);
		}
		
		return port;
	}
	
	/**
	 * Shuts down all published registries.
	 */
	public static void shutdown_all() {
		LOGGER.debug("Shutting down all RMI services...");
		
		Set<Registry> copy = new HashSet<>();
		synchronized(RmiServerPublisher.class) {
			copy.addAll(m_registries);
			m_registries.clear();
		}
		
		for (Registry r : copy) {
			try {
				UnicastRemoteObject.unexportObject(r, true);
			} catch (RemoteException e) {
				LOGGER.debug("Failed to unexport registry '" + r + "'.", e);
			}
		}
	}
}
