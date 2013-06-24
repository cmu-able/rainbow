package incubator.rmi;

import incubator.il.IMutexManager;
import incubator.rmi.RmiCommunicationPorts;
import incubator.rmi.RmiScanner;
import incubator.rmi.RmiScannerListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import auxtestlib.DefaultTCase;
import auxtestlib.TestHelper;

/**
 * Suite that checks how the RMI scanner thread works.
 */
public class RmiScannerTest extends DefaultTCase {
	/**
	 * Helper that shuts down RMI stuff :)
	 */
	@TestHelper
	public RmiHelper m_rmi_helper;
	
	/**
	 * Checks that the constructor validates the arguments.
	 * @throws Exception test failed
	 */
	@Test
	@SuppressWarnings("unused")
	public void constructor_validates_arguments() throws Exception {
		try {
			new RmiScanner(null, "x", new IMutexManager("x"), Object.class);
			fail();
		} catch (IllegalArgumentException e) {
			// Expected.
		}
		
		try {
			new RmiScanner("x", null, new IMutexManager("x"), Object.class);
			fail();
		} catch (IllegalArgumentException e) {
			// Expected.
		}
		
		try {
			new RmiScanner("x", "y", null, Object.class);
			fail();
		} catch (IllegalArgumentException e) {
			// Expected.
		}
		
		try {
			new RmiScanner("x", "y", new IMutexManager("x"), null);
			fail();
		} catch (IllegalArgumentException e) {
			// Expected.
		}
	}
	
	/**
	 * Checks that all listeners methods check their arguments.
	 * @throws Exception test failed
	 */
	@Test
	public void listener_methods_validate_arguments() throws Exception {
		RmiScanner rs = new RmiScanner("x", "x", new IMutexManager("x"),
				TServiceI.class);
		try {
			rs.add_listener(null);
			fail();
		} catch (IllegalArgumentException e) {
			// Expected.
		}
		
		try {
			rs.remove_listener(null);
			fail();
		} catch (IllegalArgumentException e) {
			// Expected.
		}
	}
	
	/**
	 * Tests scanning operation control. It will:
	 * <ol>
	 * <li>Start the scanning operation in port 6300.</li>
	 * <li>When we reach port 6302 start again and checks that it cannot
	 * start the scan. It also checks that we can't do a resume.</li>
	 * <li>When we reach port 6306, we pause.</li>
	 * <li>Checks that, after some time, there are no news in the scan
	 * ports.</li>
	 * <li>Tries again to tart and pause and checks that both fail.</li>
	 * <li>Resumes and wait until we reach port 6307.</li>
	 * <li>Resumes and starts and checks that both fail.</li>
	 * <li>Stops and checks that no more data arrive.</li>
	 * <li>Stops again, resumes and pause and checks that all fail.</li>
	 * <li>Restart and waits to reach the end.</li>
	 * </ol>
	 * While this is going on, we verify the state returned by the scanner.
	 * @throws Exception test failed
	 */
	@Test
	public void scan_operation_control() throws Exception {
		final List<Integer> scanned = Collections.synchronizedList(
				new ArrayList<Integer>());
		final boolean finished[] = new boolean[1];
		final boolean paused[] = new boolean[1];
		final boolean stopped[] = new boolean[1];
		final boolean resumed[] = new boolean[1];
		final int started[] = new int[1];
		
		RmiScannerListener l = new RmiScannerListener() {
			@Override
			public void client_found(int port, Object client) {
				fail();
			}
			@Override
			public void port_scanned(int port) {
				scanned.add(new Integer(port));
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					/*
					 * Weird.
					 */
				}
			}
			@Override
			public void scan_finished() {
				finished[0] = true;
			}
			@Override
			public void scan_paused() {
				paused[0] = true;
			}
			@Override
			public void scan_resumed() {
				resumed[0] = true;
			}
			@Override
			public void scan_started(int range) {
				started[0] = range;
			}
			@Override
			public void scan_stopped() {
				stopped[0] = true;
			}
		};
		
		String pp = RmiCommunicationPorts.class.getName();
		System.setProperty(pp + ".min-port", "6300");
		System.setProperty(pp + ".max-port", "6315");
		
		RmiScanner scanner = new RmiScanner("localhost", "ctx",
				new IMutexManager("xx"), TServiceI.class);
		scanner.add_listener(l);
		
		assertEquals(RmiScanner.STOPPED, scanner.state());
		
		scanner.start();
		wait_for_port(scanned, 6302);
		
		assertEquals(RmiScanner.SCANNING, scanner.state());
		assertEquals(16, started[0]);
		assertFalse(stopped[0]);
		assertFalse(paused[0]);
		assertFalse(resumed[0]);
		assertFalse(finished[0]);
		started[0] = 0;
		
		try {
			scanner.start();
			fail();
		} catch (IllegalStateException e) {
			// OK.
		}
		
		try {
			scanner.resume();
			fail();
		} catch (IllegalStateException e) {
			// OK.
		}
		
		wait_for_port(scanned, 6305);
		scanner.pause();
		Thread.sleep(200);		// We need to wait some time for the scanner.
		
		assertEquals(RmiScanner.PAUSED, scanner.state());
		assertEquals(0, started[0]);
		assertFalse(stopped[0]);
		assertTrue(paused[0]);
		assertFalse(resumed[0]);
		assertFalse(finished[0]);
		paused[0] = false;
		
		Thread.sleep(200);
		assertFalse(scanned.contains(new Integer(6307)));
		
		try {
			scanner.start();
			fail();
		} catch (IllegalStateException e) {
			// OK.
		}
		
		try {
			scanner.pause();
			fail();
		} catch (IllegalStateException e) {
			// OK.
		}
		
		scanner.resume();
		Thread.sleep(60);
		
		assertEquals(0, started[0]);
		assertFalse(stopped[0]);
		assertFalse(paused[0]);
		assertTrue(resumed[0]);
		assertFalse(finished[0]);
		resumed[0] = false;
		
		wait_for_port(scanned, 6307);
		
		try {
			scanner.start();
			fail();
		} catch (IllegalStateException e) {
			// OK.
		}
		
		try {
			scanner.resume();
			fail();
		} catch (IllegalStateException e) {
			// OK.
		}
		
		scanner.stop();
		Thread.sleep(200);
		
		assertEquals(0, started[0]);
		assertTrue(stopped[0]);
		assertFalse(paused[0]);
		assertFalse(resumed[0]);
		assertFalse(finished[0]);
		stopped[0] = false;
		
		assertEquals(RmiScanner.STOPPED, scanner.state());
		
		Thread.sleep(200);
		assertFalse(scanned.contains(new Integer(6309)));
		
		try {
			scanner.stop();
			fail();
		} catch (IllegalStateException e) {
			// OK.
		}
		
		try {
			scanner.pause();
			fail();
		} catch (IllegalStateException e) {
			// OK.
		}
		
		try {
			scanner.resume();
			fail();
		} catch (IllegalStateException e) {
			// OK.
		}
		
		scanner.start();
		Thread.sleep(250);
		
		assertEquals(16, started[0]);
		assertFalse(stopped[0]);
		assertFalse(paused[0]);
		assertFalse(resumed[0]);
		assertFalse(finished[0]);
		started[0] = 0;
		
		while (!scanned.contains(new Integer(6315))) {
			Thread.sleep(50);
		}
		
		Thread.sleep(500);
		
		assertEquals(RmiScanner.FINISHED, scanner.state());
		assertEquals(0, started[0]);
		assertFalse(stopped[0]);
		assertFalse(paused[0]);
		assertFalse(resumed[0]);
		assertTrue(finished[0]);
	}
	
	/**
	 * Waits for a port to be added to the list.
	 * @param l the list
	 * @param p the port to wait for
	 * @throws Exception test failed
	 */
	private void wait_for_port(List<Integer> l, int p) throws Exception {
		long start = System.currentTimeMillis();
		do {
			if (l.contains(new Integer(p))) {
				return;
			}
			
			Thread.sleep(10);
		} while ((System.currentTimeMillis() - start) < 10000);
		fail();
	}
}
