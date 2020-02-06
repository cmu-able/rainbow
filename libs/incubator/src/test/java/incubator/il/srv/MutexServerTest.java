package incubator.il.srv;

import incubator.il.IMutex;
import incubator.il.IMutexManager;
import incubator.il.IMutexStatus;
import incubator.rmi.RmiCommunicationPorts;
import incubator.rmi.RmiHelper;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;

import auxtestlib.DefaultTCase;
import auxtestlib.TestHelper;
import net.ladypleaser.rmilite.Client;

/**
 * Test suite that checks mutex manager server.
 */
public class MutexServerTest extends DefaultTCase {
	/**
	 * RMI helper.
	 */
	@TestHelper
	public RmiHelper m_rmi_helper;
	
	/**
	 * Checks that we cannot create the manager without giving a mutex
	 * manager in the constructor.
	 * @throws Exception test failed
	 */
	@Test(expected = IllegalArgumentException.class)
	@SuppressWarnings("unused")
	public void cannot_create_without_manager() throws Exception {
		new IMutexInfoServer(null);
	}
	
	/**
	 * Creates a server and checks that the port is open in the ranger defined
	 * by the constants.
	 * @throws Exception test failed
	 */
	@Test
	public void create_will_open_port_within_range() throws Exception {
		int occupiedPortsBefore = 0;
		int occupiedPortsAfter = 0;
		
		int minPort = 15000 + RandomUtils.nextInt(100);
		int maxPort = minPort + 5;
		
		String pbase = RmiCommunicationPorts.class.getName();
		System.setProperty(pbase + ".min-port", "" + minPort);
		System.setProperty(pbase + ".max-port", "" + maxPort);
		
		for (int i = minPort; i <= maxPort; i++) {
			try (Socket s = new Socket(InetAddress.getLocalHost(), i)) {
				occupiedPortsBefore++;
			} catch (IOException e) {
				/*
				 * OK, port is closed.
				 */
			}
		}
		
		@SuppressWarnings("unused")
		IMutexManager mm = new IMutexManager("xpto"); 
		
		for (int i = minPort; i <= maxPort; i++) {
			try (Socket s = new Socket(InetAddress.getLocalHost(), i)) {
				occupiedPortsAfter++;
			} catch (IOException e) {
				/*
				 * OK, port is closed.
				 */
			}
		}
		
		assertEquals(occupiedPortsBefore + 1, occupiedPortsAfter);
	}
	
	/**
	 * It is possible to create several servers for the same manager
	 * and they will open different ports.
	 * @throws Exception test failed
	 */
	@Test
	public void can_create_several_servers() throws Exception {
		int occupiedPortsBefore = 0;
		int occupiedPortsAfter = 0;
		
		int minPort = 15000 + RandomUtils.nextInt(100);
		int maxPort = minPort + 5;
		
		String pbase = RmiCommunicationPorts.class.getName();
		System.setProperty(pbase + ".min-port", "" + minPort);
		System.setProperty(pbase + ".max-port", "" + maxPort);
		
		for (int i = minPort; i <= maxPort; i++) {
			try (Socket s = new Socket(InetAddress.getLocalHost(), i)) {
				occupiedPortsBefore++;
			} catch (IOException e) {
				/*
				 * OK, port is closed.
				 */
			}
		}
		
		IMutexManager mm = new IMutexManager("xpto");
		@SuppressWarnings("unused")
		IMutexInfoServer mis1 = new IMutexInfoServer(mm); 
		@SuppressWarnings("unused")
		IMutexInfoServer mis2 = new IMutexInfoServer(mm); 
		@SuppressWarnings("unused")
		IMutexInfoServer mis3 = new IMutexInfoServer(mm); 
		@SuppressWarnings("unused")
		IMutexInfoServer mis4 = new IMutexInfoServer(mm); 
		
		for (int i = minPort; i <= maxPort; i++) {
			try (Socket s = new Socket(InetAddress.getLocalHost(), i)) {
				occupiedPortsAfter++;
			} catch (IOException e) {
				/*
				 * OK, port is closed.
				 */
			}
		}
		
		assertEquals(occupiedPortsBefore + 5, occupiedPortsAfter);
	}
	
	/**
	 * Creates a server and then remotely accesses the server asking the name
	 * of the mutex server. Checks that the name is the correct one.
	 * @throws Exception test failed
	 */
	@Test
	public void can_request_name_remotely() throws Exception {
		IMutexManager mm = new IMutexManager("www");
		
		int minPort = 15000 + RandomUtils.nextInt(100);
		int maxPort = minPort + 5;
		String pbase = RmiCommunicationPorts.class.getName();
		System.setProperty(pbase + ".min-port", "" + minPort);
		System.setProperty(pbase + ".max-port", "" + maxPort);
		
		IMutexInfoServer mis = new IMutexInfoServer(mm);
		Client c = new Client("localhost", mis.port());
		IMutexManagerRemoteAccess ra = (IMutexManagerRemoteAccess) c.lookup(
				IMutexManagerRemoteAccess.class);
		assertNotNull(ra);
		
		String n = ra.manager_name();
		assertNotNull(n);
		
		assertEquals("www", n);
	}
	
	/**
	 * Creates a manager and a server. Accesses the server remotely asking
	 * the manager's state. The state should be empty. After, a mutex is
	 * acquired. When requesting the state remotely again, the acquisition
	 * should be present.
	 * @throws Exception test failed
	 */
	@Test
	public void can_request_status_remotely() throws Exception {
		IMutexManager mm = new IMutexManager("www");
		
		int minPort = 15000 + RandomUtils.nextInt(100);
		int maxPort = minPort + 5;
		String pbase = RmiCommunicationPorts.class.getName();
		System.setProperty(pbase + ".min-port", "" + minPort);
		System.setProperty(pbase + ".max-port", "" + maxPort);
		
		@SuppressWarnings("unused")
		IMutexInfoServer mis = new IMutexInfoServer(mm);
		Client c = new Client("localhost", minPort);
		IMutexManagerRemoteAccess ra = (IMutexManagerRemoteAccess) c.lookup(
				IMutexManagerRemoteAccess.class);
		assertNotNull(ra);
		
		Map<String, IMutexStatus> m1 = ra.getStatusReport();
		assertNotNull(m1);
		
		assertEquals(0, m1.size());
		
		IMutex m = mm.get("mutecz");
		m.acquire();
		
		Map<String, IMutexStatus> m2 = ra.getStatusReport();
		assertNotNull(m2);
		
		assertEquals(1, m2.size());
		assertEquals("mutecz", m2.keySet().iterator().next());
		IMutexStatus st = m2.get("mutecz");
		assertNotNull(st.lock_request());
		assertEquals(0, st.wait_list().size());
	}
}
