package incubator.polling;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Equivalence class testing for the polling API.
 */
public class PollingEqTest extends Assert {
	/**
	 * Listener to use in tests.
	 */
	private TestPollerListener listener;

	/**
	 * Data source to use in tests.
	 */
	private TestDataSource ds;

	/**
	 * The poller created (<code>null</code> if none).
	 */
	private Poller<Object> poller;

	/**
	 * Prepares the test fixture.
	 */
	@Before
	public final void setUp() {
		listener = new TestPollerListener();
		ds = new TestDataSource();
		ds.setData(new ArrayList<>());
		poller = null;
	}

	/**
	 * Cleans up the test fixture.
	 */
	@After
	public final void tearDown() {
		listener = null;
		ds = null;
		if (poller != null) {
			try {
				poller.destroy();
			} catch (Exception e) {
				// We'll ignore any exceptions.
			}
		}
	}

	/**
	 * Creates a list of random strings.
	 * 
	 * @param count the number of objects to create.
	 * 
	 * @return the list of data
	 */
	private List<Object> createRandomList(int count) {
		assert count >= 0;

		List<Object> list = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			String rd = RandomStringUtils.randomAlphabetic(10);
			list.add(rd);
		}

		return list;
	}

	/**
	 * When the poller is created, all objects reported by the data source
	 * are informed as being added.
	 * 
	 * @throws Exception failed
	 */
	@Test
	public void whenCreatedAllObjectsAreAdded() throws Exception {
		List<Object> dt = createRandomList(3);
		ds.setData(dt);
		poller = new Poller<>(60000, listener, ds);
		Thread.sleep(500);
		TestPollingUtils.checkListenerChanged(new ArrayList<>(), dt,
				listener);
	}

	/**
	 * If new objects are added they are detected by the poller.
	 * 
	 * @throws Exception failed
	 */
	@Test
	public void detectsAdds() throws Exception {
		poller = new Poller<>(100, listener, ds);
		Thread.sleep(200);
		assertEquals(0, listener.objectsAdded.size());
		assertEquals(0, listener.objectsRemoved.size());

		List<Object> dt = createRandomList(2);
		ds.setData(dt);
		Thread.sleep(150);
		TestPollingUtils.checkListenerChanged(new ArrayList<>(), dt,
				listener);
	}

	/**
	 * If objects are removed they are detected by the poller.
	 * 
	 * @throws Exception failed
	 */
	@Test
	public void detectsRemoves() throws Exception {
		List<Object> dt1 = createRandomList(2);
		List<Object> dt2 = createRandomList(2);
		List<Object> dt3 = new ArrayList<>();
		dt3.add(dt1.get(0));
		dt3.add(dt2.get(0));
		dt3.add(dt1.get(1));
		dt3.add(dt2.get(1));

		ds.setData(dt3);
		poller = new Poller<>(100, listener, ds);
		Thread.sleep(200);

		listener.clear();
		ds.setData(dt1);
		Thread.sleep(200);
		TestPollingUtils.checkListenerChanged(dt3, dt1, listener);
	}

	/**
	 * Different objects are considered the same if they are equal according
	 * to the <code>equals</code> method.
	 * 
	 * @throws Exception failed
	 */
	@Test
	public void acceptsDifferentObjectWithSameEquals() throws Exception {
		List<Object> dt1 = createRandomList(3);
		List<Object> dt2 = new ArrayList<>();
		dt2.add(dt1.get(0));
		String s1 = (String) dt1.get(1);
		// String s2 = new String(s1) makes findbugs complain.
		String s2 = new String(s1.toCharArray());
		assertTrue(s1 != s2);
		dt2.add(s2);
		dt2.add(dt1.get(2));

		ds.setData(dt1);
		poller = new Poller<>(100, listener, ds);
		Thread.sleep(200);
		listener.clear();

		ds.setData(dt2);
		Thread.sleep(200);
		assertEquals(0, listener.added.size());
	}

	/**
	 * Pollers can be created with different polling intervals and the speed
	 * at which the data is refresh is the one configured.
	 * 
	 * @throws Exception failed
	 */
	@Test
	public void canAdjustPollingInterval() throws Exception {
		poller = new Poller<>(150, listener, ds);
		Thread.sleep(50);
		List<Object> dt1 = createRandomList(2);
		ds.setData(dt1);
		Thread.sleep(50);
		assertEquals(0, listener.added.size());
		Thread.sleep(100);
		assertEquals(2, listener.added.size());
	}

	/**
	 * When the poller is destroyed, the listener is no longer invoked.
	 * 
	 * @throws Exception failed
	 */
	@Test
	public void destroyStopsPolling() throws Exception {
		poller = new Poller<>(150, listener, ds);
		poller.destroy();
		ds.setData(createRandomList(2));
		Thread.sleep(300);
		assertEquals(0, listener.added.size());
	}

	/**
	 * Forcing polling will trigger an immediate update.
	 * 
	 * @throws Exception failed
	 */
	@Test
	public void canForcePolling() throws Exception {
		poller = new Poller<>(1000, listener, ds);
		Thread.sleep(50);
		List<Object> dt1 = createRandomList(2);
		ds.setData(dt1);
		Thread.sleep(50);
		assertEquals(0, listener.added.size());
		poller.forcePoll();
		assertEquals(2, listener.added.size());
	}

	/**
	 * The <code>forcePoll</code> method only returns after the new polling
	 * has been forced and the listeners updated.
	 * 
	 * @throws Exception failed
	 */
	@Test
	public void forcePollWaitsForPolling() throws Exception {
		ds.setDelay(100, 100);
		poller = new Poller<>(1000, listener, ds);
		Thread.sleep(200);

		ds.setData(createRandomList(2));
		assertEquals(0, listener.added.size());
		long tstart = System.currentTimeMillis();
		poller.forcePoll();
		long tend = System.currentTimeMillis();
		assertEquals(2, listener.added.size());
		assertTrue(tend - tstart > 75);
	}
}
