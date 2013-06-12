package incubator.wt;

import java.util.ArrayList;
import java.util.List;

import incubator.dispatch.DispatchHelper;

import org.junit.Test;

import auxtestlib.DefaultTCase;
import auxtestlib.TestHelper;
import auxtestlib.ThreadCountTestHelper;

/**
 * Checks that worker threads are working.
 */
@SuppressWarnings("javadoc")
public class WorkerThreadTest extends DefaultTCase {
	@TestHelper
	private DispatchHelper m_dispatcher;
	
	@TestHelper
	private ThreadCountTestHelper m_threadCountHelper;
	
	@Test
	public void create_and_get_data_from_worker() throws Exception {
		WorkerThread wt = new WorkerThread("foo");
		assertEquals("foo", wt.name());
		assertNull(wt.description());
		wt.description("bar");
		assertEquals("bar", wt.description());
		assertEquals(WtState.STOPPED, wt.state());
	}
	
	@Test
	public void start_and_stop_worker() throws Exception {
		final int[] cnt = new int[1];
		cnt[0] = 0;
		
		WorkerThread wt = new WorkerThread("") {
			@Override
			protected void do_cycle_operation() throws Exception {
				cnt[0]++;
				super.do_cycle_operation();
			}
		};
		
		for (int i = 0; i < 10; i++) {
			assertEquals(WtState.STOPPED, wt.state());
			wt.start();
			Thread.sleep(50);
			assertEquals(WtState.RUNNING, wt.state());
			wt.stop();
			Thread.sleep(50);
			assertEquals(i + 1, cnt[0]);
		}
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void invalid_creation() throws Exception {
		new WorkerThread(null);
	}
	
	@Test(expected = IllegalStateException.class)
	public void start_running_thread() throws Exception {
		WorkerThread wt = new WorkerThread("x");
		wt.start();
		
		try {
			wt.start();
		} finally {
			wt.stop();
		}
	}
	
	@Test(expected = IllegalStateException.class)
	public void stop_stopped_thread() throws Exception {
		WorkerThread wt = new WorkerThread("x");
		wt.start();
		wt.stop();
		wt.stop();
	}
	
	@Test(expected = IllegalStateException.class)
	public void stop_thread_before_starting() throws Exception {
		WorkerThread wt = new WorkerThread("x");
		wt.stop();
	}
	
	@Test
	public void throw_interrupted_exception_in_cycle() throws Exception {
		WorkerThread wt1 = new WorkerThread("foo") {
			@Override
			protected void do_cycle_operation() throws Exception {
				throw new InterruptedException();
			}
		};
		
		wt1.start();
		Thread.sleep(10);
		wt1.stop();
		
		assertEquals(0, wt1.collector().throwables().size());
	}
	
	@Test
	public void throw_exception_in_cycle() throws Exception {
		WorkerThread wt1 = new WorkerThread("foo") {
			@Override
			protected void do_cycle_operation() throws Exception {
				throw new Exception("foo");
			}
		};
		
		wt1.start();
		Thread.sleep(50);
		
		assertEquals(WtState.ABORTED, wt1.state());
		assertEquals(1, wt1.collector().throwables().size());
		assertEquals("foo",
				wt1.collector().throwables().get(0).throwable().getMessage());
	}
	
	@Test
	public void abort_thread_when_stopping() throws Exception {
		WorkerThread wt1 = new WorkerThread("foo") {
			@Override
			protected void do_cycle_operation() throws Exception {
				synchronized (this) {
					try {
						wait();
					} catch (InterruptedException e) {
						throw new Exception("Abort!");
					}
				}
			}

			@Override
			protected void interrupt_wait() {
				thread().interrupt();
			}
		};
		
		wt1.start();
		Thread.sleep(50);
		
		assertEquals(WtState.RUNNING, wt1.state());
		wt1.stop();
		assertEquals(WtState.ABORTED, wt1.state());
	}
	
	@Test
	public void start_aborted_thread() throws Exception {
		WorkerThread wt1 = new WorkerThread("foo") {
			@Override
			protected void do_cycle_operation() throws Exception {
				throw new Exception("fooo");
			}
		};
		
		wt1.start();
		Thread.sleep(50);
		
		assertEquals(WtState.ABORTED, wt1.state());
		wt1.start();
		assertEquals(WtState.RUNNING, wt1.state());
		wt1.stop();
	}
	
	@Test
	public void change_state_informs_registered_listeners() throws Exception {
		final Exception[] to_throw = new Exception[1];
		WorkerThread wt = new WorkerThread("x") {
			@Override
			protected synchronized void do_cycle_operation() throws Exception {
				/*
				 * Being synchronized makes sure we don't throw while in the
				 * middle of starting otherwise we would have a race condition
				 * and would not know whether to expect 2 notifications
				 * (STARTING; ABORTED) or 3 (STARTING; RUNNING; ABOTED).
				 */
				if (to_throw[0] != null) {
					throw to_throw[0];
				}
				
				wait(10);
			}
		};
		
		final List<Boolean> i1 = new ArrayList<>();
		WorkerThreadListener l1 = new WorkerThreadListener() {
			@Override
			public void state_changed() {
				i1.add(true);
			}
		};
		wt.add_listener(l1);
		
		final List<Boolean> i2 = new ArrayList<>();
		WorkerThreadListener l2 = new WorkerThreadListener() {
			@Override
			public void state_changed() {
				i2.add(true);
			}
		};
		wt.add_listener(l2);
		wt.remove_listener(l2);
		
		assertEquals(0, i1.size());
		assertEquals(0, i2.size());
		
		/*
		 * STOPPED -> STARTING -> RUNNING
		 */
		wt.start();
		Thread.sleep(50);
		
		assertEquals(WtState.RUNNING, wt.state());
		assertEquals(2, i1.size());
		assertEquals(0, i2.size());
		
		/*
		 * RUNNING -> STOPPING -> STOPPED
		 */
		wt.stop();
		Thread.sleep(50);
		
		assertEquals(WtState.STOPPED, wt.state());
		assertEquals(4, i1.size());
		assertEquals(0, i2.size());
		
		/*
		 * STOPPED -> STARTING -> RUNNING -> ABORTED
		 */
		to_throw[0] = new Exception("die");
		wt.start();
		Thread.sleep(50);
		
		assertEquals(WtState.ABORTED, wt.state());
		assertEquals(7, i1.size());
		assertEquals(0, i2.size());
	}
	
	@Test
	public void enumeration_checks() {
		WtState[] states = WtState.values();
		for (WtState s : states) {
			WtState.valueOf(s.name());
		}
	}
	
	@Test
	public void close_worker_thread_from_within() throws Exception {
		WorkerThread wt = new WorkerThread("x") {
			@Override
			protected void do_cycle_operation() throws Exception {
				stop();
			}
		};
		
		wt.start();
		
		Thread.sleep(50);
		
		assertEquals(0, wt.collector().throwables().size());
		assertEquals(WtState.STOPPED, wt.state());
	}
}
