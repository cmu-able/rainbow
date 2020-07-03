package incubator.rmi;

/**
 * Defines the port range for communications. The range is predefined by the
 * {@link #MINIMUM_PORT} and {@link #MAXIMUM_PORT} constants but may be
 * overridden by setting the
 * <code>incubator.RmiCommunicationPorts.min-port</code> and
 * <code>max-port</code> system properties.
 */
public class RmiCommunicationPorts {
	/**
	 * Default minimum port for RMI communication.
	 */
	public static final int MINIMUM_PORT = 13700;
	
	/**
	 * Default maximum port for RMI communications.
	 */
	public static final int MAXIMUM_PORT = 13800;
	
	/**
	 * Minimum port.
	 */
	private int m_min_port;
	
	/**
	 * Maximum port.
	 */
	private int m_max_port;
	
	/**
	 * Creates a new instance that will identify the minimum and maximum
	 * ports.
	 */
	public RmiCommunicationPorts() {
		String pbase = getClass().getName();
		String pmin = System.getProperty(pbase + ".min-port");
		String pmax = System.getProperty(pbase + ".max-port");
		
		m_min_port = 0;
		if (pmin != null) {
			try {
				m_min_port = Integer.parseInt(pmin);
			} catch (NumberFormatException e) {
				/*
				 * Ok, will be set to zero.
				 */
			}
		}
		
		m_max_port = 0;
		if (pmax != null) {
			try {
				m_max_port = Integer.parseInt(pmax);
			} catch (NumberFormatException e) {
				/*
				 * Ok, will be set to zero.
				 */
			}
		}
		
		if (m_max_port < m_min_port || m_min_port <= 0) {
			m_max_port = 0;
		}
		
		if (m_min_port == 0 || m_max_port == 0){
			m_min_port = RmiCommunicationPorts.MINIMUM_PORT;
			m_max_port = RmiCommunicationPorts.MAXIMUM_PORT;
		}
	}
	
	/**
	 * Obtains the minimum port.
	 * @return the port number
	 */
	public int min_port() {
		return m_min_port;
	}
	
	/**
	 * Obtains the maximum port.
	 * @return the port number
	 */
	public int max_port() {
		return m_max_port;
	}
}
