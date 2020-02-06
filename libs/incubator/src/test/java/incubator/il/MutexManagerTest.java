package incubator.il;

import incubator.il.IMutex;
import incubator.il.IMutexManager;
import incubator.il.IMutexStatus;

import java.util.Map;

import org.junit.Test;

import auxtestlib.DefaultTCase;

/**
 * Checks that the mutex manager is working.
 */
public class MutexManagerTest extends DefaultTCase {
	/**
	 * A manager cannot be created with a <code>null</code> name.
	 * @throws Exception test failed
	 */
	@Test(expected = IllegalArgumentException.class)
	@SuppressWarnings("unused")
	public void cannot_create_with_null_name() throws Exception {
		new IMutexManager(null);
	}
	
	/**
	 * Cannot ask a mutex with <code>null</code> name.
	 * @throws Exception test failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void cannot_get_mutex_with_null_name() throws Exception {
		IMutexManager mm = new IMutexManager("x");
		mm.get(null);
	}
	
	/**
	 * Mutexes with the same name are the same mutex.
	 * @throws Exception test failed
	 */
	@Test
	public void testGettingMutexesWithSameNameAreEqual() throws Exception {
		IMutexManager mm = new IMutexManager("x");
		IMutex m1 = mm.get("xpto");
		IMutex m2 = mm.get("xpto");
		assertNotNull(m1);
		assertNotNull(m2);
		assertEquals(m1, m2);
	}
	
	/**
	 * Mutexes with different names are different.
	 * @throws Exception test failed
	 */
	@Test
	public void getting_mutexes_with_different_names() throws Exception {
		IMutexManager mm = new IMutexManager("x");
		IMutex m1 = mm.get("xpto");
		IMutex m2 = mm.get("xpty");
		assertNotNull(m1);
		assertNotNull(m2);
		assertNotSame(m1, m2);
	}
	
	/**
	 * The name obtained by the mutex manager is the name used to create the
	 * mutex manager.
	 * @throws Exception test failed
	 */
	@Test
	public void get_manager_name() throws Exception {
		IMutexManager mm = new IMutexManager("x");
		assertEquals("x", mm.name());
	}
	
	/**
	 * If there is no mutex, the report is empty. If mutexes are created,
	 * the report brings the new mutexes.
	 * @throws Exception test failed
	 */
	@Test
	public void reports_returns_1_entry_per_mutex() throws Exception {
		IMutexManager mm = new IMutexManager("x");
		Map<String, IMutexStatus> m = mm.report();
		assertEquals(0, m.size());
		
		mm.get("foo");
		m = mm.report();
		assertEquals(1, m.size());
		assertEquals("foo", m.keySet().iterator().next());
	}
	
	/**
	 * Destroys a <code>null</code> mutex.
	 * @throws Exception test failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void cannot_destroy_null_mutex() throws Exception {
		IMutexManager mm = new IMutexManager("xx");
		mm.destroy(null);
	}
	
	/**
	 * Creates and destroys a mutex. If we ask for the mutex again, we get
	 * a new one.
	 * @throws Exception test failed
	 */
	@Test
	public void destroy_and_get_mutex_will_pick_another_one()
			throws Exception {
		IMutexManager mm = new IMutexManager("xx");
		IMutex m = mm.get("xpto");
		mm.destroy(m);
		IMutex m2 = mm.get("xpto");
		
		assertNotSame(m, m2);
	}
	
	/**
	 * Create a mutex a destroy it. Trying to destroy it again fails.
	 * @throws Exception test failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void cannot_double_destroy_mutex() throws Exception {
		IMutexManager mm = new IMutexManager("xx");
		IMutex m = mm.get("xpto");
		mm.destroy(m);
		mm.destroy(m);
	}
	
	/**
	 * Destroying a mutex created by another manager fails.
	 * @throws Exception test failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testCannoDestroyUnknownMutex() throws Exception {
		IMutexManager mm1 = new IMutexManager("mm");
		IMutexManager mm2 = new IMutexManager("mm");
		
		mm1.get("xpto");
		IMutex m2 = mm2.get("xpto");
		mm1.destroy(m2);
	}
	
	/**
	 * Creates two managers and ask the same mutex to each one of them. The
	 * mutexes returned are different.
	 * @throws Exception test failed
	 */
	@Test
	public void testDifferentManagersReturnDifferentMutexes()
			throws Exception {
		IMutexManager mm1 = new IMutexManager("mm");
		IMutexManager mm2 = new IMutexManager("mm");
		
		IMutex m1 = mm1.get("xpto");
		IMutex m2 = mm2.get("xpto");
		
		assertNotSame(m1, m2);
	}
}
