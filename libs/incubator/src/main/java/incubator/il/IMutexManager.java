package incubator.il;

import incubator.il.srv.IMutexInfoServer;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Class that manages mutexes.
 */
public class IMutexManager {
	/**
	 * Manager name.
	 */
	private String m_name;
	
	/**
	 * Maps mutex names to mutexes.
	 */
	private Map<String, IMutex> m_mutexes;
	
	/**
	 * Logger.
	 */
	private Logger m_logger;
	
	/**
	 * Creates a new manager
	 * @param name the manager's name
	 */
	public IMutexManager(String name) {
		if (name == null) {
			throw new IllegalArgumentException("name == null");
		}
		
		this.m_name = name;
		m_mutexes = new HashMap<>();
		m_logger = Logger.getLogger(IMutexManager.class);
		
		m_logger.info("Mutex manager '" + name + "' starting up.");
		
		try {
			@SuppressWarnings("unused")
			IMutexInfoServer mis = new IMutexInfoServer(this);
		} catch (Exception e) {
			m_logger.error(e);
		}
	}
	
	/**
	 * Obtains a reference to the mutex with the given name. If a mutex
	 * has not been created with the given one, a new one is created.
	 * @param mutex_name the name of the mutex
	 * @return the mutex
	 */
	public IMutex get(String mutex_name) {
		if (mutex_name == null) {
			throw new IllegalArgumentException("mutexName == null");
		}
		
		synchronized(this) {
			IMutex m = m_mutexes.get(mutex_name);
			if (m == null) {
				m = new IMutexImpl();
				m_mutexes.put(mutex_name, m);
			}
			
			return m;
		}
	}
	
	/**
	 * Removes a mutex from the managed list.
	 * @param m the mutex
	 */
	public void destroy(IMutex m) {
		if (m == null) {
			throw new IllegalArgumentException("m == null");
		}
		
		synchronized(this) {
			for (String n : m_mutexes.keySet()) {
				IMutex found = m_mutexes.get(n);
				if (found == m) {
					m_mutexes.remove(n);
					return;
				}
			}
		}
		
		throw new IllegalArgumentException("Unknown mutex: " + m);
	}
	
	/**
	 * Obtains a map that maps mutex names to their status report.
	 * @return the report for all mutexes
	 */
	public Map<String, IMutexStatus> report() {
		Map<String, IMutexStatus> m = new HashMap<>();
		synchronized(this) {
			for (String n : m_mutexes.keySet()) {
				IMutex mutex = m_mutexes.get(n);
				m.put(n, mutex.status_snapshot());
			}
		}
		
		return m;
	}
	
	/**
	 * Obtains the manager's name.
	 * @return the manager's name
	 */
	public String name() {
		return m_name;
	}
}
