package incubator.wt;

import incubator.dispatch.DispatchHelper;

import org.junit.Test;

import auxtestlib.DefaultTCase;
import auxtestlib.TestHelper;
import auxtestlib.ThreadCountTestHelper;

/**
 * Checks that worker thread groups work as advertised :)
 */
public class WorkerThreadGroupTest extends DefaultTCase {
	/**
	 * Dispatcher that resets the global dispatcher
	 */
	@TestHelper
	private DispatchHelper m_dispatch_helper;
	
	/**
	 * Test helper that ensures no threads are left running.
	 */
	@TestHelper
	private ThreadCountTestHelper m_thread_helper;
	
	/**
	 * Checks that thread groups provide the correct information.
	 * @throws Exception test failed
	 */
	@Test
	public void group_properties() throws Exception {
		WorkerThreadGroup wtg = new WorkerThreadGroup("foo");
		assertEquals("foo", wtg.name());
		assertNull(wtg.description());
		wtg.description("bar");
		assertEquals("bar", wtg.description());
		assertEquals(0, wtg.threads().size());
	}
	
	/**
	 * Thread groups cannot be created with invalid data.
	 * @throws Exception test failed
	 */
	@Test(expected = AssertionError.class)
	@SuppressWarnings("unused")
	public void invalid_creation() throws Exception {
		new WorkerThreadGroup(null);
	}
	
	/**
	 * Threads can be added or removed to the thread group.
	 * @throws Exception test failed
	 */
	@Test
	public void add_remove_threads() throws Exception {
		WorkerThreadGroup wtg = new WorkerThreadGroup("foo");
		WorkerThread wt1 = new WorkerThread("bar");
		WorkerThread wt2 = new WorkerThread("glu");
		
		assertEquals(0, wtg.threads().size());
		wtg.add_thread(wt1);
		assertEquals(1, wtg.threads().size());
		assertTrue(wtg.threads().contains(wt1));
		wtg.add_thread(wt2);
		assertEquals(2, wtg.threads().size());
		assertTrue(wtg.threads().contains(wt1));
		assertTrue(wtg.threads().contains(wt2));
		wtg.remove_thread(wt1);
		assertEquals(1, wtg.threads().size());
		assertTrue(wtg.threads().contains(wt2));
	}
	
	/**
	 * Null threads cannot be added or removed from the thread group.
	 * Threads cannot be added twice and threads can only be removed once.
	 * @throws Exception test failed
	 */
	@Test
	public void add_remove_invalid_thread() throws Exception {
		WorkerThreadGroup wtg = new WorkerThreadGroup("foo");
		boolean fail = false;
		try {
			wtg.add_thread(null);
			fail = true;
		} catch (AssertionError e) {
			/*
			 * Expected.
			 */
		}
		
		try {
			wtg.remove_thread(null);
			fail = true;
		} catch (AssertionError e) {
			/*
			 * Expected.
			 */
		}
		
		WorkerThread wt1 = new WorkerThread("wt1");
		try {
			wtg.remove_thread(wt1);
			fail = true;
		} catch (AssertionError e) {
			/*
			 * Expected.
			 */
		}
		
		wtg.add_thread(wt1);
		try {
			wtg.add_thread(wt1);
			fail = true;
		} catch (AssertionError e) {
			/*
			 * Expected.
			 */
		}
		
		wtg.remove_thread(wt1);
		try {
			wtg.remove_thread(wt1);
			fail = true;
		} catch (AssertionError e) {
			/*
			 * Expected.
			 */
		}
		
		if (fail) {
			fail();
		}
	}
	
	/**
	 * Starting threads will start all threads not already running and
	 * stopping threads will stop all running threads in a group.
	 * @throws Exception test failed 
	 */
	@Test
	public void start_stop_threads() throws Exception {
		WorkerThreadGroup wtg = new WorkerThreadGroup("foo");
		WorkerThread wt1 = new WorkerThread("wt1");
		WorkerThread wt2 = new WorkerThread("wt2");
		WorkerThread wta = new WorkerThread("wt3") {
			@Override
			protected void do_cycle_operation() throws Exception {
				throw new Exception("I always die immediately!");
			}
		};
		
		wtg.add_thread(wt1);
		wtg.add_thread(wt2);
		wtg.add_thread(wta);
		
		wt1.start();
		wta.start();
		Thread.sleep(50);
		
		assertEquals(WtState.RUNNING, wt1.state());
		assertEquals(WtState.STOPPED, wt2.state());
		assertEquals(WtState.ABORTED, wta.state());
		assertEquals(1, wta.collector().throwables().size());
		
		wtg.start();
		Thread.sleep(50);
		
		assertEquals(WtState.RUNNING, wt1.state());
		assertEquals(WtState.RUNNING, wt2.state());
		assertEquals(WtState.ABORTED, wta.state());
		assertEquals(2, wta.collector().throwables().size());
		
		wt1.stop();
		
		assertEquals(WtState.STOPPED, wt1.state());
		assertEquals(WtState.RUNNING, wt2.state());
		assertEquals(WtState.ABORTED, wta.state());
		assertEquals(2, wta.collector().throwables().size());
		
		wtg.stop();
		
		assertEquals(WtState.STOPPED, wt1.state());
		assertEquals(WtState.STOPPED, wt2.state());
		assertEquals(WtState.ABORTED, wta.state());
		assertEquals(2, wta.collector().throwables().size());
	}
	
	/**
	 * Creates multiple groups and adds and removes them changing the
	 * hierarchy.
	 * @throws Exception test failed
	 */
	@Test
	public void adding_removing_groups() throws Exception {
		WorkerThreadGroup wtg1 = new WorkerThreadGroup("g1");
		WorkerThreadGroup wtg2 = new WorkerThreadGroup("g2");
		WorkerThreadGroup wtg3 = new WorkerThreadGroup("g3");
		
		assertEquals(0, wtg1.direct_subgroups().size());
		assertEquals(0, wtg1.all_subgroups().size());
		assertEquals(0, wtg2.direct_subgroups().size());
		assertEquals(0, wtg2.all_subgroups().size());
		
		wtg1.add_subgroup(wtg2);
		assertEquals(1, wtg1.direct_subgroups().size());
		assertTrue(wtg1.direct_subgroups().contains(wtg2));
		assertEquals(1, wtg1.all_subgroups().size());
		assertTrue(wtg1.all_subgroups().contains(wtg2));
		assertEquals(0, wtg2.direct_subgroups().size());
		assertEquals(0, wtg2.all_subgroups().size());
		
		wtg2.add_subgroup(wtg3);
		assertEquals(1, wtg1.direct_subgroups().size());
		assertTrue(wtg1.direct_subgroups().contains(wtg2));
		assertEquals(2, wtg1.all_subgroups().size());
		assertTrue(wtg1.all_subgroups().contains(wtg2));
		assertTrue(wtg1.all_subgroups().contains(wtg3));
		assertEquals(1, wtg2.direct_subgroups().size());
		assertTrue(wtg2.direct_subgroups().contains(wtg3));
		assertEquals(1, wtg2.all_subgroups().size());
		assertTrue(wtg2.all_subgroups().contains(wtg3));
		
		wtg1.add_subgroup(wtg3);
		assertEquals(2, wtg1.direct_subgroups().size());
		assertTrue(wtg1.direct_subgroups().contains(wtg2));
		assertTrue(wtg1.direct_subgroups().contains(wtg3));
		assertEquals(2, wtg1.all_subgroups().size());
		assertTrue(wtg1.all_subgroups().contains(wtg2));
		assertTrue(wtg1.all_subgroups().contains(wtg3));
		assertEquals(1, wtg2.direct_subgroups().size());
		assertTrue(wtg2.direct_subgroups().contains(wtg3));
		assertEquals(1, wtg2.all_subgroups().size());
		assertTrue(wtg2.all_subgroups().contains(wtg3));
		
		wtg1.remove_subgroup(wtg2);
		assertEquals(1, wtg1.direct_subgroups().size());
		assertTrue(wtg1.direct_subgroups().contains(wtg3));
		assertEquals(1, wtg1.all_subgroups().size());
		assertTrue(wtg1.all_subgroups().contains(wtg3));
		assertEquals(1, wtg2.direct_subgroups().size());
		assertTrue(wtg2.direct_subgroups().contains(wtg3));
		assertEquals(1, wtg2.all_subgroups().size());
		assertTrue(wtg2.all_subgroups().contains(wtg3));
		
		wtg2.remove_subgroup(wtg3);
		assertEquals(1, wtg1.direct_subgroups().size());
		assertTrue(wtg1.direct_subgroups().contains(wtg3));
		assertEquals(1, wtg1.all_subgroups().size());
		assertTrue(wtg1.all_subgroups().contains(wtg3));
		assertEquals(0, wtg2.direct_subgroups().size());
		assertEquals(0, wtg2.all_subgroups().size());
	}
	
	/**
	 * It is illegal that build cyclic thread group hierarchies.
	 * @throws Exception test failed
	 */
	@Test
	public void building_cyclic_thread_group_hierarchies() throws Exception {
		WorkerThreadGroup wtg1 = new WorkerThreadGroup("g1");
		WorkerThreadGroup wtg2 = new WorkerThreadGroup("g2");
		WorkerThreadGroup wtg3 = new WorkerThreadGroup("g3");
		
		wtg1.add_subgroup(wtg2);
		wtg2.add_subgroup(wtg3);
		
		boolean fail = false;
		try {
			wtg3.add_subgroup(wtg1);
			fail = true;
		} catch (AssertionError e) {
			/*
			 * Expected.
			 */
		}
		
		if (fail) {
			fail();
		}
	}
	
	/**
	 * Thread groups in hierarchies can be started or stopped in isolation
	 * or with the whole hierarchy.
	 * @throws Exception test failed
	 */
	@Test
	public void starting_stopping_group_hierarchies() throws Exception {
		WorkerThreadGroup wtg1 = new WorkerThreadGroup("g1");
		WorkerThreadGroup wtg2 = new WorkerThreadGroup("g2");
		WorkerThread wt1_1 = new WorkerThread("wt1-1");
		WorkerThread wt1_2 = new WorkerThread("wt1-2");
		WorkerThread wt2_1 = new WorkerThread("wt2-1");
		WorkerThread wtc = new WorkerThread("wtc");
		
		wtg1.add_thread(wt1_1);
		wtg1.add_thread(wt1_2);
		wtg1.add_thread(wtc);
		wtg2.add_thread(wt2_1);
		wtg2.add_thread(wtc);
		wtg1.add_subgroup(wtg2);
		
		assertEquals(3, wtg1.threads().size());
		assertEquals(2, wtg2.threads().size());
		
		assertEquals(WtState.STOPPED, wt1_1.state());
		assertEquals(WtState.STOPPED, wt1_2.state());
		assertEquals(WtState.STOPPED, wt2_1.state());
		assertEquals(WtState.STOPPED, wtc.state());
		
		wtg1.start();
		assertEquals(WtState.RUNNING, wt1_1.state());
		assertEquals(WtState.RUNNING, wt1_2.state());
		assertEquals(WtState.STOPPED, wt2_1.state());
		assertEquals(WtState.RUNNING, wtc.state());
		
		wt2_1.start();
		wt1_2.stop();
		assertEquals(WtState.RUNNING, wt1_1.state());
		assertEquals(WtState.STOPPED, wt1_2.state());
		assertEquals(WtState.RUNNING, wt2_1.state());
		assertEquals(WtState.RUNNING, wtc.state());
		
		wtg2.stop();
		assertEquals(WtState.RUNNING, wt1_1.state());
		assertEquals(WtState.STOPPED, wt1_2.state());
		assertEquals(WtState.STOPPED, wt2_1.state());
		assertEquals(WtState.STOPPED, wtc.state());
		
		wtg2.start_all();
		assertEquals(WtState.RUNNING, wt1_1.state());
		assertEquals(WtState.STOPPED, wt1_2.state());
		assertEquals(WtState.RUNNING, wt2_1.state());
		assertEquals(WtState.RUNNING, wtc.state());
		
		wtg1.stop_all();
		assertEquals(WtState.STOPPED, wt1_1.state());
		assertEquals(WtState.STOPPED, wt1_2.state());
		assertEquals(WtState.STOPPED, wt2_1.state());
		assertEquals(WtState.STOPPED, wtc.state());
		
		wtg1.start_all();
		assertEquals(WtState.RUNNING, wt1_1.state());
		assertEquals(WtState.RUNNING, wt1_2.state());
		assertEquals(WtState.RUNNING, wt2_1.state());
		assertEquals(WtState.RUNNING, wtc.state());
		
		wtg1.stop_all();
		assertEquals(WtState.STOPPED, wt1_1.state());
		assertEquals(WtState.STOPPED, wt1_2.state());
		assertEquals(WtState.STOPPED, wt2_1.state());
		assertEquals(WtState.STOPPED, wtc.state());
	}
	
	/**
	 * A thread group cannot be added or removed from itself.
	 * @throws Exception test failed
	 */
	@Test
	public void adding_removing_self_group() throws Exception {
		WorkerThreadGroup wtg1 = new WorkerThreadGroup("g1");
		
		boolean fail = false;
		try {
			wtg1.add_subgroup(wtg1);
			fail = true;
		} catch (AssertionError e) {
			/*
			 * Expected.
			 */
		}
		
		try {
			wtg1.remove_subgroup(wtg1);
			fail = true;
		} catch (AssertionError e) {
			/*
			 * Expected.
			 */
		}
		
		if (fail) {
			fail = true;
		}
	}
	
	/**
	 * <code>null</code> cannot be added or removed from the thread group. 
	 * @throws Exception test failed
	 */
	@Test
	public void adding_removing_null_group() throws Exception {
		WorkerThreadGroup wtg1 = new WorkerThreadGroup("g1");
		
		boolean fail = false;
		try {
			wtg1.add_subgroup(null);
			fail = true;
		} catch (AssertionError e) {
			/*
			 * Expected.
			 */
		}
		
		try {
			wtg1.remove_subgroup(null);
			fail = true;
		} catch (AssertionError e) {
			/*
			 * Expected.
			 */
		}
		
		if (fail) {
			fail();
		}
	}
}
