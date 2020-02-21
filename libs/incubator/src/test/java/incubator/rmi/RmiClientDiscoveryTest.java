package incubator.rmi;

import incubator.rmi.RmiClientDiscovery;
import incubator.rmi.RmiServerPublisher;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import auxtestlib.DefaultTCase;

/**
 * Test suite that checks RMI client discovery.
 */
public class RmiClientDiscoveryTest extends DefaultTCase {
	/**
	 * Minimum port number.
	 */
	private int m_pmin;
	
	/**
	 * Maximum port number.
	 */
	private int m_pmax;
	
	/**
	 * Prepares the test fixture.
	 */
	@Before
	public void rmi_set_up() {
		m_pmin = -1;
		m_pmax = -1;
	}
	
	/**
	 * Cleans up after the test case.
	 */
	@After
	public void rmi_tear_down() {
		RandomService.cleanup();
	}
	
	/**
	 * Looks for open ports when none is.
	 * @throws Exception test failed
	 */
	@Test
	public void find_open_ports_with_no_open() throws Exception {
		generate_port_spawn(10);
		PortListener pl = new PortListener();
		ScanListener sl = new ScanListener();
		RmiClientDiscovery.find_open_ports("localhost", m_pmin, m_pmax, pl, sl);
		
		assertEquals(0, pl.m_ports.length);
		assertEquals(10, sl.m_count);
		assertEquals(m_pmin, sl.m_min);
		assertEquals(m_pmax, sl.m_max);
	}
	
	/**
	 * Searches open ports when some are. Some of the open ports are outside
	 * the search range.
	 * @throws Exception test failed
	 */
	@Test
	public void find_open_ports_with_random_open() throws Exception {
		generate_port_spawn(10);
		PortListener pl = new PortListener();
		ScanListener sl = new ScanListener();
		open_random_ports(1, 3, m_pmin - 5, m_pmin - 1);
		open_random_ports(1, 3, m_pmax + 1, m_pmax + 5);
		int inside[] = open_random_ports(1, 5, m_pmin, m_pmax);
		RmiClientDiscovery.find_open_ports("localhost", m_pmin, m_pmax, pl, sl);
		
		assertEquals(inside.length, pl.m_ports.length);
		assertEquals(10, sl.m_count);
		assertEquals(m_pmin, sl.m_min);
		assertEquals(m_pmax, sl.m_max);
	}
	
	/**
	 * Tries to obtain the RMI client accessing a closed port.
	 * @throws Exception test failed
	 */
	@Test
	public void find_client_with_closed_port() throws Exception {
		generate_port_spawn(5);
		assertNull(RmiClientDiscovery.find_rmi_client("localhost", m_pmin,
				RIntf.class));
	}
	
	/**
	 * Tries to find a client in a port which is open but is not an RMI
	 * service.
	 * @throws Exception test failed
	 */
	@Test
	public void find_client_with_non_rmi_in_port() throws Exception {
		generate_port_spawn(5);
		@SuppressWarnings("unused")
		RandomService rs = new RandomService(m_pmin);
		assertNull(RmiClientDiscovery.find_rmi_client("localhost", m_pmin,
				RIntf.class));
	}
	
	/**
	 * Tries to find a client in a an RMI open port but the registered object
	 * has a different class.
	 * @throws Exception test failed
	 */
	@Test
	public void testFindClientWithRmiWrongClassInPort() throws Exception {
		generate_port_spawn(5);
		RmiServerPublisher.publish_service(RIntf2.class, new RImpl2(), m_pmin);
		Object o = RmiClientDiscovery.find_rmi_client("localhost", m_pmin,
				RIntf.class);
		assertNull(o);
	}
	
	/**
	 * Tries to find a client in an open RMI port with the right class.
	 * @throws Exception test failed
	 */
	@Test
	public void testFindClientWithRmiRightClassInPort() throws Exception {
		generate_port_spawn(5);
		RmiServerPublisher.publish_service(RIntf.class, new RImpl(), m_pmin);
		Object o = RmiClientDiscovery.find_rmi_client("localhost", m_pmin,
				RIntf.class);
		assertNotNull(o);
		assertTrue(o instanceof RIntf);
	}
	
	/**
	 * Opens several random ports with some objects registered with the
	 * right class but some with the wrong class. Some ports are open
	 * without being RMI. Checks that the right clients are found. Some
	 * clients are published outside the port range.
	 * @throws Exception test failed
	 */
	@Test
	public void find_rmi_clients() throws Exception {
		for (int ii = 0; ii < 4; ii++) {
			generate_port_spawn(30);
			ClientListener cl = new ClientListener();
			ScanListener sl = new ScanListener();
			int before[] = generate_random_ports(1, 6, m_pmin - 10, m_pmin - 1);
			int after[] = generate_random_ports(1, 6, m_pmax + 1, m_pmax + 10);
			int inside[] = generate_random_ports(3, 15, m_pmin, m_pmax);
			
			int insidemin = -1;
			int insidemax = -1;
			int insidecnt = 0;
			
			Set<Integer> f = new HashSet<>();
			
			for (int i = 0; i < before.length; i++) {
				if (i % 3 == 0) {
					@SuppressWarnings("unused")
					RandomService rs = new RandomService(before[i]);
				} else if (i % 3 == 1) {
					RmiServerPublisher.publish_service(RIntf.class, new RImpl(),
							before[i]);
				} else {
					RmiServerPublisher.publish_service(RIntf2.class,
							new RImpl2(), before[i]);
				}
			}
			
			for (int i = 0; i < after.length; i++) {
				if (i % 3 == 0) {
					@SuppressWarnings("unused")
					RandomService rs = new RandomService(after[i]);
				} else if (i % 3 == 1) {
					RmiServerPublisher.publish_service(RIntf.class, new RImpl(),
							after[i]);
				} else {
					RmiServerPublisher.publish_service(RIntf2.class,
							new RImpl2(), after[i]);
				}
			}

			for (int i = 0; i < inside.length; i++) {
				if (i % 3 == 0) {
					@SuppressWarnings("unused")
					RandomService rs = new RandomService(inside[i]);
				} else if (i % 3 == 1) {
					if (insidecnt == 0) {
						insidemin = inside[i];
						insidemax = inside[i];
					} else {
						if (insidemin > inside[i]) {
							insidemin = inside[i];
						}
						
						if (insidemax > inside[i]) {
							insidemax = inside[i];
						}
					}
					insidecnt++;
					RmiServerPublisher.publish_service(RIntf.class, new RImpl(),
							inside[i]);
					f.add(new Integer(inside[i]));
				} else {
					RmiServerPublisher.publish_service(RIntf2.class,
							new RImpl2(), inside[i]);
				}
			}

			RmiClientDiscovery.find_rmi_client("localhost", m_pmin, m_pmax,
					RIntf.class, cl, sl);
			
			assertEquals(insidecnt, cl.m_ports.length);
			assertEquals(m_pmin, sl.m_min);
			assertEquals(m_pmax, sl.m_max);
			
			for (int i = 0; i < cl.m_ports.length; i++) {
				assertTrue(f.contains(new Integer(cl.m_ports[i])));
			}
			
			RmiServerPublisher.shutdown_all();
		}
	}
	
	/**
	 * Opens several random ports in the defined interval
	 * @param min minimum number of ports to open
	 * @param max maximum number of ports to open
	 * @param pmin minimum port
	 * @param pmax maximum port
	 * @return the open ports
	 * @throws Exception failed to open the ports
	 */
	private int []open_random_ports(int min, int max, int pmin, int pmax)
			throws Exception {
		int p[] = generate_random_ports(min, max, pmin, pmax);
		for (int i = 0; i < p.length; i++) {
			@SuppressWarnings("unused")
			RandomService rs = new RandomService(p[i]);
		}
		
		return p;
	}
	
	/**
	 * Generates a random range of ports in this object's instance variables.
	 * @param range the range to generate
	 */
	private void generate_port_spawn(int range) {
		m_pmin = 5000 + RandomUtils.nextInt(5000);
		m_pmax = m_pmin + range - 1;
	}
	
	/**
	 * Generates a random set of ports.
	 * @param min minimum number of ports to open
	 * @param max maximum number of ports to open
	 * @param pmin minimum port
	 * @param pmax maximum port
	 * @return the ports generated
	 */
	private int []generate_random_ports(int min, int max, int pmin, int pmax) {
		int pcount = min + RandomUtils.nextInt(max - min);
		int ports[] = new int[pcount];
		
		for (int i = 0; i < pcount; i++) {
			ports[i] = pmin + RandomUtils.nextInt(pmax - pmin);
			for (int j = 0; j < i; j++) {
				if (ports[j] == ports[i]) {
					i--;
					break;
				}
			}
		}
		
		return ports;
	}
	
	/**
	 * Class that opens a server socket accepting connections but not
	 * doing anything with them. It provides a static clean up method
	 * that closes all open sockets. Every time a connection is made,
	 * a thread will read from the socket but does nothing. When an I/O
	 * error occurs, the thread will close. When cleaning up, all threads are
	 * interrupting forcing an I/O error.
	 */
	private static class RandomService {
		/**
		 * List with all open services.
		 */
		private static List<RandomService> m_open = new ArrayList<>();
		
		/**
		 * The service.
		 */
		private ServerSocket m_server;
		
		/**
		 * Threads created that are reading from the client socket.
		 */
		private List<Thread> m_threads = new ArrayList<>();
		
		/**
		 * Thread listening to the server socket.
		 */
		private Thread m_main_thread;
		
		/**
		 * Creates a new service in the specified port.
		 * @param port the port to open
		 * @throws Exception failed to open the port
		 */
		private RandomService(int port) throws Exception {
			m_server = new ServerSocket(port);
			m_open.add(this);
			m_main_thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						@SuppressWarnings("resource")
						final Socket s = m_server.accept();
						s.getOutputStream().write(new byte[] {
								1, 2, 3, 4, 5, 6, 7, 8, 9, 0
						});
						Thread t = new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									InputStream is = s.getInputStream();
									while(true) {
										is.read();
									}
								} catch (IOException e) {
									/*
									 * We ignore.
									 */
								}
								
								try {
									s.close();
								} catch (IOException e) {
									/*
									 * We ignore.
									 */
								}
							}
						});
						t.start();
						m_threads.add(t);
					} catch (IOException e) {
						/*
						 * We ignore.
						 */
					}
					
					try {
						m_server.close();
					} catch (IOException e) {
						/*
						 * We ignore.
						 */
					}
				}
			});
			m_main_thread.start();
		}
		
		/**
		 * Closes all open connections.
		 */
		private static void cleanup() {
			for (RandomService rs : m_open) {
				rs.m_main_thread.interrupt();
				for (Thread t : rs.m_threads) {
					t.interrupt();
				}
			}
			
			m_open.clear();
		}
	}
	
	/**
	 * Listener that saves the ports
	 */
	private static class PortListener
			implements RmiClientDiscovery.PortFoundListener {
		/**
		 * Ports
		 */
		private int m_ports[] = new int[0];
		
		@Override
		public void port_found(int port) {
			int nports[] = new int[m_ports.length + 1];
			System.arraycopy(m_ports, 0, nports, 0, m_ports.length);
			nports[m_ports.length] = port;
			m_ports = nports;
		}
	}
	
	/**
	 * Listener that keeps scanned ports.
	 */
	private static class ScanListener
			implements RmiClientDiscovery.PortScanListener {
		/**
		 * Number of scanned ports.
		 */
		private int m_count = 0;
		
		/**
		 * Lowest scanned port.
		 */
		private int m_min = -1;
		
		/**
		 * Highest scanned port.
		 */
		private int m_max = -1;

		@Override
		public void port_scanned(int port) {
			if (m_count == 0) {
				m_min = port;
				m_max = port;
			} else {
				if (port < m_min) {
					m_min = port;
				}
				
				if (port > m_max) {
					m_max = port;
				}
			}
			
			m_count++;
		}
	}

	/**
	 * Listener that keeps found clients.
	 */
	private static class ClientListener
			implements RmiClientDiscovery.ClientFoundListener {
		/**
		 * Ports.
		 */
		private int m_ports[] = new int[0];

		@Override
		public void client_found(Object object, String host, int port,
				Class<?> iface) {
			int nports[] = new int[m_ports.length + 1];
			System.arraycopy(m_ports, 0, nports, 0, m_ports.length);
			nports[m_ports.length] = port;
			m_ports = nports;
		}
	}
	
	/**
	 * Remote interface
	 */
	private static interface RIntf {
		/**
		 * Dummy method.
		 */
		public void myMethod();
	}
	
	/**
	 * Remote interface 2.
	 */
	private static interface RIntf2 {
		/**
		 * Dummy method.
		 */
		public void myMethod2();
	}
	
	/**
	 * Implementation of the remote interface.
	 */
	private static class RImpl implements RIntf {
		@Override
		public void myMethod() {
			/*
			 * Ignored.
			 */
		}
	}
	
	/**
	 * Implementation of the second remote interface.
	 */
	private static class RImpl2 implements RIntf2 {
		@Override
		public void myMethod2() {
			/*
			 * Ignored.
			 */
		}
	}
}
