package incubator.rmi;

import java.io.IOException;
import java.net.Socket;

import net.ladypleaser.rmilite.Client;

/**
 * Class responsible for discovering where RMI clients are.
 */
public class RmiClientDiscovery {
	/**
	 * Looks for open ports in a host.
	 * @param host the host
	 * @param min_port the host's minimum port
	 * @param max_port the host's maximum port
	 * @param listener listener that is informed of the ports found
	 * @param scan_listener listener that is informed of the scan progress
	 */
	public static void find_open_ports(String host, int min_port, int max_port,
			PortFoundListener listener, PortScanListener scan_listener) {
		if (host == null) {
			throw new IllegalArgumentException("host == null");
		}
		
		if (listener == null) {
			throw new IllegalArgumentException("listener == null");
		}
		
		if (min_port <= 0) {
			throw new IllegalArgumentException("minPort <= 0");
		}
		
		if (max_port < min_port) {
			throw new IllegalArgumentException("maxPort < minPort");
		}
		
		for (int i = min_port; i <= max_port; i++) {
			try (Socket s = new Socket(host, i)) {
				if (scan_listener != null) {
					scan_listener.port_scanned(i);
				}
				s.close();
				listener.port_found(i);
			} catch (IOException e) {
				// Não está aberto.
				if (scan_listener != null) {
					scan_listener.port_scanned(i);
				}
			}
		}
	}
	
	/**
	 * Looks if there is an RMI client in the given host and port.
	 * @param host the host name
	 * @param port the port to look for
	 * @param iface the remote interface
	 * @return the found client or <code>null</code> if none
	 */
	public static <T> T find_rmi_client(String host, int port,
			Class<T> iface) {
		try {
			Client c = new Client(host, port);
			return iface.cast(c.lookup(iface));
		} catch (Exception e) {
			/*
			 * It doesn't exist, it wasn't found...
			 */
		}
		
		return null;
	}
	
	/**
	 * Searches for RMI clients at a given host.
	 * @param host the host
	 * @param min_port minimum port number
	 * @param max_port maximum port number
	 * @param iface the remote interface
	 * @param listener the listener to inform about the found clients
	 * @param scan_listener listener to scans
	 */
	public static void find_rmi_client(final String host, int min_port,
			int max_port, final Class<?> iface,
			final ClientFoundListener listener,
			PortScanListener scan_listener) {
		if (host == null) {
			throw new IllegalArgumentException("host == null");
		}
		
		if (min_port <= 0 ) {
			throw new IllegalArgumentException("minPort <= 0");
		}
		
		if (max_port < min_port) {
			throw new IllegalArgumentException("maxPort < minPort");
		}
		
		if (iface == null) {
			throw new IllegalArgumentException("iface == null");
		}
		
		if (listener == null) {
			throw new IllegalArgumentException("listener == null");
		}
		
		find_open_ports(host, min_port, max_port, new PortFoundListener() {
			@Override
			public void port_found(int port) {
				Object obj = find_rmi_client(host, port, iface);
				if (obj != null) {
					listener.client_found(obj, host, port, iface);
				}
			}
		}, scan_listener);
	}
	
	/**
	 * Searches for RMi clients at a given host. Ports are obtained using the
	 * {@link RmiCommunicationPorts} class.
	 * @param host the host
	 * @param iface the remote class
	 * @param listener a listener to be informed of found
	 * @param scan_listener listener that is informed of the scans
	 */
	public static void find_rmi_client(String host, Class<?> iface,
			ClientFoundListener listener, PortScanListener scan_listener) {
		if (host == null) {
			throw new IllegalArgumentException("host == null");
		}
		
		if (iface == null) {
			throw new IllegalArgumentException("iface == null");
		}
		
		if (listener == null) {
			throw new IllegalArgumentException("listener == null");
		}
		
		RmiCommunicationPorts ports = new RmiCommunicationPorts();
		find_rmi_client(host, ports.min_port(), ports.max_port(), iface,
				listener, scan_listener);
	}
	
	/**
	 * Interface implemented by objects that are informed when an open port
	 * is found.
	 */
	public interface PortFoundListener {
		/**
		 * A port was found
		 * @param port the port
		 */
		public void port_found(int port);
	}
	
	/**
	 * Interface implemented by objects that are informed when an RMI client
	 * is found.
	 */
	public interface ClientFoundListener {
		/**
		 * Invoked when a client was found.
		 * @param object the client found
		 * @param host the host where the client was found
		 * @param port the port where the client was found
		 * @param iface the RMI client class
		 */
		public void client_found(Object object, String host, int port,
				Class<?> iface);
	}
	
	/**
	 * Interface implemented by objects that are informed when a port is
	 * searched, regardless of whether anything was found or not.
	 */
	public interface PortScanListener {
		/**
		 * A port has been scanned.
		 * @param port the port
		 */
		public void port_scanned(int port);
	}
}
