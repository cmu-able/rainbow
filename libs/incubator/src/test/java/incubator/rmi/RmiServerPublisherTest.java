package incubator.rmi;

import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.rmi.RemoteException;

import net.ladypleaser.rmilite.Client;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;

import auxtestlib.DefaultTCase;
import auxtestlib.TestHelper;

/**
 * Test suite for the {@link RmiServerPublisher} class.
 */
public class RmiServerPublisherTest extends DefaultTCase {
	/**
	 * RMI test helper.
	 */
	@TestHelper
	private RmiHelper m_rmi_helper;
	
	/**
	 * We cannot publish a service with a null object.
	 * @throws Exception test failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void publish_null_object() throws Exception {
		RmiServerPublisher.publish_service(TService.class, null);
		RmiServerPublisher.publish_service(null, new TService());
	}
	
	/**
	 * We cannot publish a service with a null interface.
	 * @throws Exception test failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void publish_null_interface() throws Exception {
		RmiServerPublisher.publish_service(null, new TService());
	}
	
	/**
	 * Several services are publishes and it is verified that they are all
	 * accessible in separate ports. After a shutdown, all services are
	 * unavailable.
	 * @throws Exception test failed
	 */
	@Test
	public void publish_several_services_and_shutdown() throws Exception {
		int cnt = 10;
		int basep = -1;
		for (int i = 0; i < cnt; i++) {
			TService ts = new TService();
			ts.m_val = i;
			int p = RmiServerPublisher.publish_service(TServiceI.class, ts);
			assertTrue(p > 0);
			if (basep == -1) {
				basep = p;
				assertTrue(basep >= RmiCommunicationPorts.MINIMUM_PORT);
			} else {
				assertEquals(basep + i, p);
			}
		}
		
		Thread.sleep(250);
		
		/*
		 * We should find 10 open ports.
		 */
		boolean ok[] = new boolean[cnt];
		for (int i = 0; i < cnt; i++) {
			Client c = new Client("localhost", basep + i);
			Object obj = c.lookup(TServiceI.class);
			assertNotNull(obj);
			TServiceI ts = (TServiceI) obj;
			ok[ts.val()] = true;
		}
		
		for (int i = 0; i < cnt; i++) {
			assertTrue(ok[i]);
		}
		
		/*
		 * We shutdown the registry and check that there are no open
		 * ports.
		 */
		RmiServerPublisher.shutdown_all();
		
		for (int i = 0; i < cnt; i++) {
			try {
				Client c = new Client("localhost", basep + i);
				c.lookup(TServiceI.class);
				fail();
			} catch (RemoteException e) {
				/*
				 * Expected.
				 */
			}
		}
	}
	
	/**
	 * When publishing, all ports defined by
	 * {@link RmiCommunicationPorts} are honored.
	 * @throws Exception test failed
	 */
	@Test
	public void publish_respects_communication_ports() throws Exception {
		int cnt = 5;
		for (int i = 0; i < cnt; i++) {
			TService ts = new TService();
			ts.m_val = i;
			
			String pp = RmiCommunicationPorts.class.getName();
			int rnd = 14000 + RandomUtils.nextInt(2000);
			System.setProperty(pp + ".min-port", "" + rnd);
			System.setProperty(pp + ".max-port", "" + (rnd + 5));
			
			int p = RmiServerPublisher.publish_service(TServiceI.class, ts);
			assertEquals(rnd, p);
			Client c = new Client("localhost", rnd);
			Object obj = c.lookup(TServiceI.class);
			assertNotNull(obj);
			assertTrue(obj instanceof TServiceI);
		}
	}
	
	/**
	 * Publishes services in random ports (inside or outside the range) and
	 * checks that they are published in the given port.
	 * @throws Exception test failed
	 */
	@Test
	public void publish_on_specific_port() throws Exception {
		final int iterationCount = 100;
		final int minAllowOk = 80;
		
		int done = 0;
		/*
		 * We do not know which ports are open (some may be in use) so, some
		 * may fail.
		 */
		for (int i = 0; i < iterationCount; i++) {
			int p = 1025 + RandomUtils.nextInt(10000);
			try (Socket s = new Socket()) {
				s.connect(new InetSocketAddress(InetAddress.getLocalHost(), p),
						50);
				continue;
			} catch (ConnectException|SocketTimeoutException e) {
				/*
				 * Port is free.
				 */
			}
			
			done++;
			TService ts = new TService();
			int pp = RmiServerPublisher.publish_service(TServiceI.class, ts, p);
			assertEquals(p, pp);
			
			try (Socket s = new Socket("localhost", p)) {
				/*
				 * We don't do anything with the socket.
				 */
			}
		}
		
		assertTrue(done >= minAllowOk);
	}
}
