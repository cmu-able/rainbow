package incubator.rmi;

import incubator.il.IMutex;
import incubator.il.IMutexManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that scans a server looking for RMI services.
 */
public class RmiScanner {
	/**
	 * State: scanning is stopped and hasn't finished successfully.
	 */
	public static final int STOPPED = 100;
	
	/**
	 * State: scanning is ongoing.
	 */
	public static final int SCANNING = 101;
	
	/**
	 * State: scanning is paused.
	 */
	public static final int PAUSED = 102;
	
	/**
	 * State: scanning finished successfully.
	 */
	public static final int FINISHED = 103;
	
	/**
	 * Mutex used to synchronize the thread scan context.
	 */
	private IMutex m_mutex;
	
	/**
	 * Scanner state, also used by the thread to know what is has to do.
	 */
	private int m_state;
	
	/**
	 * Host being scanned.
	 */
	private String m_host;
	
	/**
	 * Scanning thread.
	 */
	private ScanThread m_thread;
	
	/**
	 * Scanner listeners.
	 */
	private List<RmiScannerListener> m_listeners;
	
	/**
	 * Description of the scan context.
	 */
	private String m_context;
	
	/**
	 * Interface we're looking for.
	 */
	private Class<?> m_iface;
	
	/**
	 * Minimum port to scan.
	 */
	private int m_min_port;
	
	/**
	 * Maximum port to scan.
	 */
	private int m_max_port;
	
	/**
	 * Creates a new scanner.
	 * @param host the host to scan
	 * @param scan_context a name for the scanning context (used to uniquely
	 * identify mutexes)
	 * @param mutex_manager the mutex manager
	 * @param iface the interface we're looking for
	 */
	public RmiScanner(String host, String scan_context,
			IMutexManager mutex_manager, Class<?> iface) {
		this(host, scan_context, mutex_manager, iface, 0, 0);
	}
	
	/**
	 * Creates a new scanner.
	 * @param host the host to scan
	 * @param scan_context a name for the scanning context (used to uniquely
	 * identify mutexes)
	 * @param mutex_manager the mutex manager
	 * @param iface the interface we're looking for
	 * @param start_port the first port to scan
	 * @param end_port the last port to scan
	 */
	public RmiScanner(String host, String scan_context,
			IMutexManager mutex_manager, Class<?> iface, int start_port,
			int end_port) {
		if (host == null) {
			throw new IllegalArgumentException("host == null");
		}
		
		if (scan_context == null) {
			throw new IllegalArgumentException("scanContext == null");
		}
		
		if (mutex_manager == null) {
			throw new IllegalArgumentException("mutexManager == null");
		}
		
		if (iface == null) {
			throw new IllegalArgumentException("iface == null");
		}
		
		if (start_port <= 0 || end_port <= 0 || end_port < start_port) {
			start_port = 0;
			end_port = 0;
		}
		
		m_mutex = mutex_manager.get(RmiScanner.class.getName() + "::" +
				scan_context + " (" + host + ")");
		this.m_host = host;
		this.m_iface = iface;
		m_context = scan_context;
		m_state = STOPPED;
		m_thread = null;
		m_listeners = new ArrayList<>();
		
		if (start_port == 0) {
			RmiCommunicationPorts ports;
			ports = new RmiCommunicationPorts();
			m_min_port = ports.min_port();
			m_max_port = ports.max_port();
		} else {
			m_min_port = start_port;
			m_max_port = end_port;
		}
	}
	
	/**
	 * Adds a new scan listener.
	 * @param listener o scan listener
	 */
	public void add_listener(RmiScannerListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException("listener == null");
		}
		
		m_listeners.add(listener);
	}
	
	/**
	 * Removes an RMI scan listener.
	 * @param listener the listener to remove.
	 */
	public void remove_listener(RmiScannerListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException("listener == null");
		}
		
		m_listeners.remove(listener);
	}
	
	/**
	 * Gets the scanner state.
	 * @return one of the state constants
	 */
	public int state() {
		m_mutex.acquire();
		try {
			return m_state;
		} finally {
			m_mutex.release();
		}
	}
	
	/**
	 * Start the scanning operation.
	 */
	public void start() {
		m_mutex.acquire();
		try {
			if (m_state != STOPPED && m_state != FINISHED) {
				throw new IllegalStateException("state must be STOPPED or "
						+ "FINISHED.");
			}
			
			m_state = SCANNING;
			if (m_thread == null) {
				m_thread = new ScanThread();
				m_thread.start();
			}
		} finally {
			m_mutex.release();
		}
	}
	
	/**
	 * Pauses scanning
	 */
	public void pause() {
		m_mutex.acquire();
		try {
			if (m_state != SCANNING) {
				throw new IllegalStateException("state must be SCANNING");
			}
			
			m_state = PAUSED;
		} finally {
			m_mutex.release();
		}
	}
	
	/**
	 * Resumes scanning.
	 */
	public void resume() {
		m_mutex.acquire();
		try {
			if (m_state != PAUSED) {
				throw new IllegalStateException("state must be PAUSED");
			}
			
			m_state = SCANNING;
		} finally {
			m_mutex.release();
		}
	}
	
	/**
	 * Stops the scanning operation.
	 */
	public void stop() {
		stop(true);
	}
	
	/**
	 * Stops the scanning operation.
	 * @param wait should this method return only after the thread has
	 * stopped?
	 */
	public void stop(boolean wait) {
		Thread currentThread;
		
		m_mutex.acquire();
		try {
			if (m_state != SCANNING && m_state != PAUSED) {
				throw new IllegalStateException("state must be SCANNING "
						+ "or PAUSED");
			}
			
			currentThread = m_thread;
			m_state = STOPPED;
		} finally {
			m_mutex.release();
		}
		
		/*
		 * We wait for the thread to die if we're not the thread :) 
		 */
		if (wait && Thread.currentThread() != currentThread) {
			while (m_thread == currentThread) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					/*
					 * Ignored.
					 */
				}
			}
		}
	}
	
	/**
	 * Obtains the name of the host being scanned.
	 * @return the host name
	 */
	public String host() {
		return m_host;
	}
	
	/**
	 * Obtains the minimum port to scan.
	 * @return the port number
	 */
	public int min_port() {
		return m_min_port;
	}
	
	/**
	 * Obtains the maximum port to scan.
	 * @return the port number
	 */
	public int max_port() {
		return m_max_port;
	}
	
	/**
	 * Scanning thread.
	 */
	private class ScanThread extends Thread {
		/**
		 * Creates a new thread.
		 */
		private ScanThread() {
			super("RMI Scanner " + m_context + "(" + m_host + ")");
		}
		
		@Override
		public void run() {
			final int SCAN = 1;
			final int SLEEP = 2;
			final int QUIT_FINISHED = 3;
			final int QUIT_STOPPED = 4;
			final int INFORM_PAUSE_AND_SLEEP = 5;
			final int INFORM_RESUME_AND_SCAN = 6;
			
			/*
			 * Minimum and maximum ports and last port scanned.
			 */
			int min_port = -1;
			int max_port = -1;
			int last_port = -1;
			
			/*
			 * Last state we know of.
			 */
			int last_state = 0;
			
			/*
			 * What should we do in the this iteration?
			 */
			int what_to_do;
			
			while (true) {
				/*
				 * Check what we need to do.
				 */
				m_mutex.acquire();
				try {
					switch (m_state) {
						case PAUSED:
							if (last_state != PAUSED) {
								what_to_do = INFORM_PAUSE_AND_SLEEP;
							} else {
								what_to_do = SLEEP;
							}
							break;
						case STOPPED:
							what_to_do = QUIT_STOPPED;
							m_thread = null;
							break;
						case FINISHED:
							what_to_do = QUIT_FINISHED;
							m_thread = null;
							break;
						case SCANNING:
							if (last_state == PAUSED) {
								what_to_do = INFORM_RESUME_AND_SCAN;
							} else {
								what_to_do = SCAN;
							}
							break;
						default:
							what_to_do = 0;
							assert false;
					}
					
					last_state = m_state;
				} finally {
					m_mutex.release();
				}
				
				if (what_to_do == QUIT_FINISHED || what_to_do == QUIT_STOPPED) {
					List<RmiScannerListener> lcp;
					
					m_mutex.acquire();
					try {
						lcp = new ArrayList<>(m_listeners);
					} finally {
						m_mutex.release();
					}
					
					for (RmiScannerListener rsl : lcp) {
						if (what_to_do == QUIT_FINISHED) {
							rsl.scan_finished();
						} else {
							rsl.scan_stopped();
						}
					}
					
					break;
				} else if (what_to_do == SLEEP
						|| what_to_do == INFORM_PAUSE_AND_SLEEP) {
					if (what_to_do == INFORM_PAUSE_AND_SLEEP) {
						List<RmiScannerListener> lcp;
						m_mutex.acquire();
						try {
							lcp = new ArrayList<>(m_listeners);
						} finally {
							m_mutex.release();
						}
						
						for (RmiScannerListener rsl : lcp) {
							rsl.scan_paused();
						}
					}
					
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						/*
						 * Skip to the next iteration, although we're not
						 * expecting this to happen.
						 */
					}
				} else if (what_to_do == SCAN
						|| what_to_do == INFORM_RESUME_AND_SCAN) {
					if (what_to_do == INFORM_RESUME_AND_SCAN) {
						List<RmiScannerListener> lcp;
						m_mutex.acquire();
						try {
							lcp = new ArrayList<>(m_listeners);
						} finally {
							m_mutex.release();
						}
						
						for (RmiScannerListener rsl : lcp) {
							rsl.scan_resumed();
						}
					}
					
					int port_to_scan = -1;
					
					if (min_port == -1 || max_port == -1) {
						/*
						 * We have to start a new scan.
						 */
						min_port = RmiScanner.this.m_min_port;
						max_port = RmiScanner.this.m_max_port;
						
						List<RmiScannerListener> lcp;
						m_mutex.acquire();
						try {
							lcp = new ArrayList<>(m_listeners); 
						} finally {
							m_mutex.release();
						}
						
						for (RmiScannerListener rsl : lcp) {
							rsl.scan_started(max_port - min_port + 1);
						}
						
						port_to_scan = min_port;
					} else if (last_port == max_port) {
						/*
						 * The scan has finished.
						 */
						port_to_scan = -1;
						
						/*
						 * Let's try to update the state if noone did
						 * anything yet.
						 */
						m_mutex.acquire();
						try {
							if (m_state == SCANNING) {
								m_state = FINISHED;
							}
						} finally {
							m_mutex.release();
						}
					} else {
						port_to_scan = last_port + 1;
					}
					
					if (port_to_scan > 0) {
						Object client = RmiClientDiscovery.find_rmi_client(
								m_host, port_to_scan, m_iface);
						List<RmiScannerListener> lcp;
						m_mutex.acquire();
						try {
							lcp = new ArrayList<>(m_listeners);
						} finally {
							m_mutex.release();
						}
						
						last_port = port_to_scan;
						for (RmiScannerListener rsl : lcp) {
							rsl.port_scanned(port_to_scan);
							if (client != null) {
								rsl.client_found(port_to_scan, client);
							}
						}
					}
				}
			}
		}
	}
}
