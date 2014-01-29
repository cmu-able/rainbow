package incubator.il;

import incubator.il.IMutex;
import incubator.il.IMutexImpl;
import incubator.il.IMutexRequest;
import incubator.il.IMutexStatistics;
import incubator.il.IMutexStatus;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;

import auxtestlib.AbstractControlledThread;
import auxtestlib.DefaultTCase;
import auxtestlib.ThreadFlag;

/**
 * Test suite that checks the mutex.
 */
public class MutexTest extends DefaultTCase {
	/**
	 * Creates two threads. One acquires the mutex and the second tries to
	 * acquire becoming blocked. The first acquires and releases the mutex
	 * two times while the second is blocked. The first releases the mutex
	 * and the second runs normally.
	 * @throws Exception test failed
	 */
	@Test
	public void simple_acquisition_with_nesting() throws Exception {
		final ThreadFlag thread1Started = new ThreadFlag(); 
		final ThreadFlag thread2Started = new ThreadFlag();
		final ThreadFlag thread1AcquiredFlag = new ThreadFlag();
		final ThreadFlag thread2AcquiredFlag = new ThreadFlag();
		final ThreadFlag thread1AcquiredReleasedFlag = new ThreadFlag();
		final ThreadFlag thread1TerminatedFlag = new ThreadFlag();
		final ThreadFlag thread2TerminatedFlag = new ThreadFlag();
		
		final IMutex m = new IMutexImpl();
		
		class Thread1 extends AbstractControlledThread {
			@Override
			public Object myRun() throws Exception {
				thread1Started.reach();
				
				m.acquire();
				thread1AcquiredFlag.reach();
				
				m.acquire();
				m.release();
				m.acquire();
				m.release();
				thread1AcquiredReleasedFlag.reach();
				
				m.release();
				thread1TerminatedFlag.reach();
				
				return null;
			}
		}
		
		class Thread2 extends AbstractControlledThread {
			@Override
			public Object myRun() throws Exception {
				thread2Started.reach();
				
				m.acquire();
				thread2AcquiredFlag.reach();
				
				m.release();
				thread2TerminatedFlag.reach();
				
				return null;
			}
		}
		
		Thread1 t1 = new Thread1();
		Thread2 t2 = new Thread2();
		t1.start();
		t2.start();
		
		Thread.sleep(100);
		assertEquals(1, thread1Started.reached().size());
		assertEquals(1, thread2Started.reached().size());
		
		thread1Started.allowContinue();
		Thread.sleep(100);
		thread2Started.allowContinue();
		Thread.sleep(100);
		assertEquals(1, thread1AcquiredFlag.reached().size());
		assertEquals(0, thread2AcquiredFlag.reached().size());
		
		thread1AcquiredFlag.allowContinue();
		Thread.sleep(100);
		assertEquals(1, thread1AcquiredReleasedFlag.reached().size());
		assertEquals(0, thread2AcquiredFlag.reached().size());
		
		thread1AcquiredReleasedFlag.allowContinue();
		Thread.sleep(100);
		assertEquals(1, thread1TerminatedFlag.reached().size());
		assertEquals(1, thread2AcquiredFlag.reached().size());
		
		thread1TerminatedFlag.allowContinue();
		thread2AcquiredFlag.allowContinue();
		Thread.sleep(100);
		assertEquals(1, thread2TerminatedFlag.reached().size());
		
		thread2TerminatedFlag.allowContinue();
		Thread.sleep(100);
		
		t1.invoke();
		t2.invoke();
	}
	
	/**
	 * Launches several threads acquiring and releasing the mutex to
	 * ensure that there are no deadlocks. Each time a thread acquires the
	 * mutex, it verifies that none of the other threads has it.
	 * @throws Exception test failed
	 */
	@Test
	public void multiple_acquire_and_release() throws Exception {
		final IMutex m = new IMutexImpl();
		
		final AbstractControlledThread threads[] =
				new AbstractControlledThread[50];
		
		class TestThread extends AbstractControlledThread {
			private boolean b;
			@Override
			public Object myRun() throws Exception {
				int cycles = RandomUtils.nextInt(1000);
				for (int i = 0; i < cycles; i++) {
					m.acquire();
					b = true;
					for (int j = 0; j < threads.length; j++) {
						if (threads[j] != this) {
							assertFalse(((TestThread) threads[j]).b);
						}
					}
					b = false;
					m.release();
				}
				
				return null;
			}
		}
		
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new TestThread();
		}
		
		for (int i = 0; i < threads.length; i++) {
			threads[i].start();
		}
		
		Thread.sleep(100);
		
		for (int i = 0; i < threads.length; i++) {
			threads[i].invoke();
		}
	}
	
	/**
	 * Creates a mutex and acquires and releases it several times. Verifies
	 * as returned statistics. After a reset, the statistics show zeroes.
	 * @throws Exception test failed
	 */
	@Test
	public void get_and_reset_statistics() throws Exception {
		IMutex m = new IMutexImpl();
		final int run_count = 10;
		
		for (int i = 0; i < run_count; i++) {
			m.acquire();
			Thread.sleep(80);
			m.release();
		}
		
		IMutexStatistics stats = m.status_snapshot().statistics();
		assertEquals(run_count, stats.total_acquisition_count());
		assertEquals(0, stats.average_acquire_time());
		assertEquals(run_count, stats.counts_with_no_wait());
		assertTrue(stats.average_usage_time() >= 50);
		
		m.reset_statistics();
		
		stats = m.status_snapshot().statistics();
		assertEquals(0, stats.total_acquisition_count());
		assertEquals(0, stats.average_acquire_time());
		assertEquals(0, stats.counts_with_no_wait());
		assertEquals(0, stats.average_usage_time());
	}
	
	/**
	 * Creates a mutex and checks that the locker and the waiting queue are
	 * empty. Acquires the lock and checks that the locker now has data but
	 * the waiting queue is still empty. Creates a thread that tries to
	 * acquire the mutex and checks that the waiting queue has one element.
	 * @throws Exception test failed
	 */
	@Test
	public void get_locker_and_wait_queue() throws Exception {
		final IMutex m = new IMutexImpl();
		
		IMutexStatus status = m.status_snapshot();
		assertNull(status.lock_request());
		assertEquals(0, status.wait_list().size());
		
		m.acquire();
		
		status = m.status_snapshot();
		IMutexRequest r = status.lock_request();
		assertEquals(Thread.currentThread().getName(), r.acquisition_thread());
		assertEquals(0, status.wait_list().size());
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setName("xxx");
				m.acquire();
				m.release();
			}
		}).start();
		
		Thread.sleep(50);
		
		status = m.status_snapshot();
		r = status.lock_request();
		assertEquals(Thread.currentThread().getName(), r.acquisition_thread());
		assertEquals(1, status.wait_list().size());
		r = status.wait_list().get(0);
		assertEquals("xxx", r.acquisition_thread());
		
		m.release();
	}
	
	/**
	 * Creates several mutex and starts several thread trying to acquire the
	 * mutexes in a non-blocking way. When they can, they check that no one
	 * else has the mutexes.
	 * @throws Exception test failed
	 */
	@Test
	public void concurrent_access_to_try_acquire() throws Exception {
		final IMutex m = new IMutexImpl();
		
		final AbstractControlledThread threads[] =
				new AbstractControlledThread[50];
		
		final int cycles = 100 + RandomUtils.nextInt(1000);
		class TestThread extends AbstractControlledThread {
			private volatile boolean b;
			@Override
			public Object myRun() throws Exception {
				int ok = 0;
				for (int i = 0; i < cycles; i++) {
					if (m.try_acquire()) {
						b = true;
						for (int j = 0; j < threads.length; j++) {
							if (threads[j] != this) {
								assertFalse(((TestThread) threads[j]).b);
							}
						}
						b = false;
						ok++;
						Thread.sleep(1);
						m.release();
						Thread.sleep(9);
					} else {
						Thread.sleep(10);
					}
				}
				
				return ok;
			}
		}
		
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new TestThread();
		}
		
		for (int i = 0; i < threads.length; i++) {
			threads[i].start();
		}
		
		Thread.sleep(100);
		
		int result = 0;
		int exp = cycles;
		for (int i = 0; i < threads.length; i++) {
			result += (Integer) threads[i].invoke();
		}
		
		assertTrue("Expected around between " + exp + " and " + exp * 10
				+ " acquisitions but " + result + " found.",
				result >= exp && result < exp * 10);
	}
}
