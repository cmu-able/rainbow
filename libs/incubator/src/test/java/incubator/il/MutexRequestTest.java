package incubator.il;

import incubator.il.IMutexRequestImpl;

import java.util.Date;

import org.junit.Test;

import auxtestlib.DefaultTCase;

/**
 * Checks requesting mutexes.
 */
public class MutexRequestTest extends DefaultTCase {
	/**
	 * Checks that the thread name is correctly defined.
	 * @throws Exception test failed
	 */
	@Test
	public void thread_name() throws Exception {
		IMutexRequestImpl impl = new IMutexRequestImpl();
		assertEquals(Thread.currentThread().getName(),
				impl.acquisition_thread());
	}
	
	/**
	 * Checks that the trace obtained by the request is not empty.
	 * @throws Exception test failed
	 */
	@Test
	public void testTrace() throws Exception {
		IMutexRequestImpl impl = new IMutexRequestImpl();
		assertNotNull(impl.acquisition_trace());
	}
	
	/**
	 * Checks that the wait time is the same as what has elapsed since
	 * the mutex was created until it was acquired. Afterwards, the wait
	 * time does not change.
	 * @throws Exception test failed
	 */
	@Test
	public void testWaitTime() throws Exception {
		IMutexRequestImpl impl = new IMutexRequestImpl();
		impl.mark_waited();
		assertTrue(impl.wait_time() < 50);
		
		Thread.sleep(100);
		assertTrue(impl.wait_time() > 50);
		assertTrue(impl.wait_time() < 150);
		
		Thread.sleep(100);
		assertTrue(impl.wait_time() > 150);
		assertTrue(impl.wait_time() < 250);
		
		Thread.sleep(100);
		impl.acquired();
		long wt = impl.wait_time();
		assertTrue(impl.wait_time() > 250);
		assertTrue(impl.wait_time() < 350);
		
		Thread.sleep(100);
		assertEquals(wt, impl.wait_time());
	}
	
	/**
	 * Checks that the returned date is correct.
	 * @throws Exception test failed
	 */
	@Test
	public void request_time() throws Exception {
		Date d1 = new Date();
		IMutexRequestImpl impl = new IMutexRequestImpl();
		Date d2 = new Date();
		
		Thread.sleep(100);
		
		assertTrue(d1.getTime() <= impl.request_time().getTime());
		assertTrue(d2.getTime() >= impl.request_time().getTime());
	}
}
