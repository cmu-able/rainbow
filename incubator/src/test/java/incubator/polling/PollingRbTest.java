package incubator.polling;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Robustness tests for the polling API.
 */
public class PollingRbTest extends Assert {
	/**
	 * Default polling interval.
	 */
	private static final int DEFAULT_POLLING = 10;
	
	/**
	 * Data source used for testing.
	 */
	private TestDataSource tds;
	
	/**
	 * Prepares the test environment.
	 */
	@Before
	public final void setUp() {
		tds = new TestDataSource();
		tds.setData(new ArrayList<>());
	}
	
	/**
	 * Cleans up the test environment.
	 */
	@After
	public final void tearDown() {
		tds = null;
	}
	
	/**
	 * Cannot create a poller with <code>null</code> source (constructor 1).
	 * 
	 * @throws Exception failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void cannotCreateWithNullSourceC1() throws Exception {
		new Poller<>(new TestPollerListener(), null);
	}
	
	/**
	 * Cannot create a poller with <code>null</code> listener (constructor 1).
	 * 
	 * @throws Exception failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void cannotCreateWithNullListenerC1() throws Exception {
		new Poller<>(null, new TestDataSource());
	}
	
	/**
	 * Cannot create a poller with <code>0</code> polling time.
	 * 
	 * @throws Exception failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void cannotCreateWithZeroPollingTime() throws Exception {
		new Poller<>(0, new TestPollerListener(), new TestDataSource());
	}
	
	/**
	 * Cannot create a poller with <code>null</code> source (constructor 2).
	 * 
	 * @throws Exception failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void cannotCreateWithNullSourceC2() throws Exception {
		new Poller<>(DEFAULT_POLLING, new TestPollerListener(), null);
	}
	
	/**
	 * Cannot create a poller with <code>null</code> listener (constructor 2).
	 * 
	 * @throws Exception failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void cannotCreateWithNullListenerC2() throws Exception {
		new Poller<>(DEFAULT_POLLING, null, new TestDataSource());
	}
	
	/**
	 * Cannot force a poll if the poller has been destroyed.
	 * 
	 * @throws Exception failed
	 */
	@Test(expected = IllegalStateException.class)
	public final void cannotForcePollAfterDestroy() throws Exception {
		Poller<Object> p = new Poller<>(new TestPollerListener(), tds);
		p.destroy();
		p.forcePoll();
	}
	
	/**
	 * Cannot destroy a poller that has been already destroyed.
	 * 
	 * @throws Exception failed
	 */
	@Test(expected = IllegalStateException.class)
	public final void cannotDestroyTwice() throws Exception {
		Poller<Object> p = new Poller<>(new TestPollerListener(), tds);
		p.destroy();
		p.destroy();
	}
}
