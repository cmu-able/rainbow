package incubator.rmi;

/**
 * Listener implemented by objects that are to be informed of changes in the
 * RMI scanner.
 */
public interface RmiScannerListener {
	/**
	 * The scan has started.
	 * @param range the number of ports to be scanned
	 */
	void scan_started (int range);
	
	/**
	 * Scan has been paused.
	 */
	void scan_paused ();
	
	/**
	 * Scan has resumed.
	 */
	void scan_resumed ();
	
	/**
	 * Scan has stopped.
	 */
	void scan_stopped ();
	
	/**
	 * Scan has finished.
	 */
	void scan_finished ();
	
	/**
	 * A port has been scanned.
	 * @param port the scanned port
	 */
	void port_scanned (int port);
	
	/**
	 * A client has been found.
	 * @param port the port where the client has been found
	 * @param client the clinet found
	 */
	void client_found (int port, Object client);
}
