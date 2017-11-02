package incubator.il.srv;

import incubator.il.IMutexFailedToOpenServerException;
import incubator.il.IMutexManager;
import incubator.il.IMutexStatus;
import incubator.rmi.RmiCommException;
import incubator.rmi.RmiServerPublisher;

import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Mutex information server. This object, when created, opens a port using
 * the <code>incubator.rmi</code> package. In this port it receives connections
 * that get informations on the mutex manager.
 */
public class IMutexInfoServer implements IMutexManagerRemoteAccess {
	/**
	 * Mutex manager.
	 */
	private IMutexManager m_manager;
	
	/**
	 * Logger to use.
	 */
	private Logger m_logger;
	
	/**
	 * The port where the server was published.
	 */
	private int m_port;
	
	/**
	 * Creates a new server.
	 * @param manager the mutex manager
	 * @throws IMutexFailedToOpenServerException failed to create the server
	 */
	public IMutexInfoServer(IMutexManager manager)
			throws IMutexFailedToOpenServerException {
		if (manager == null) {
			throw new IllegalArgumentException("manager == null");
		}
		
		this.m_manager = manager;
		m_logger = Logger.getLogger(IMutexInfoServer.class);
		m_logger.info("Mutex info server starting up.");
		
		try {
			m_port = RmiServerPublisher.publish_service(
					IMutexManagerRemoteAccess.class, this);
			m_logger.info("Mutex info server opened in port " + m_port);
		} catch (RmiCommException e) {
			throw new IMutexFailedToOpenServerException(e);
		}
	}

	@Override
	public String manager_name() {
		return m_manager.name();
	}

	@Override
	public Map<String, IMutexStatus> getStatusReport() {
		return m_manager.report();
	}
	
	/**
	 * Obtains the port in which the mutex info server was published.
	 * @return the prot
	 */
	public int port() {
		return m_port;
	}
}
