package incubator.scb.sync;

import incubator.Pair;
import incubator.dispatch.DispatchHelper;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import auxtestlib.DefaultTCase;
import auxtestlib.TestHelper;
import auxtestlib.ThreadCountTestHelper;

/**
 * Test suite for the SCB master.
 */
@SuppressWarnings("javadoc")
public class SyncScbMasterTest extends DefaultTCase {
	/**
	 * Number of milliseconds until slave expiration.
	 */
	private static final long SLAVE_EXPIRATION_MS = 200;
	
	@TestHelper
	private ThreadCountTestHelper m_thread_count_helper;
	
	@TestHelper
	private DispatchHelper m_dispatch_helper;
	
	/**
	 * The synchronization master.
	 */
	private SyncScbMasterImpl m_master;
	
	@Before
	public void set_up() {
		m_master = new SyncScbMasterImpl(SLAVE_EXPIRATION_MS);
		m_master.create_container("c0", Integer.class, TestSyncScb.class);
	}
	
	@After
	public void tear_down() {
		m_master.shutdown();
	}
	
	@Test
	public void new_slaves_receive_all_data_on_startup() throws Exception {
		Pair<Boolean, List<ScbOperation>> r0 = m_master.slave_contact(
				"x", new ArrayList<ScbOperation>());
		assertEquals(true, r0.first());
		assertEquals(0, r0.second().size());
		
		TestSyncScb t = new TestSyncScb(1, SyncStatus.UNKNOWN, "foo");
		List<ScbOperation> ops = new ArrayList<>();
		ops.add(ScbOperation.make_incoming("c0", t));
		
		Pair<Boolean, List<ScbOperation>> r1 = m_master.slave_contact(
				"y", ops);
		assertEquals(true, r1.first());
		assertEquals(1, r1.second().size());
		assertEquals("c0", r1.second().get(0).container_key());
		assertEquals(1,
				((TestSyncScb) r1.second().get(0).incoming()).id().intValue());
		assertEquals("foo",
				((TestSyncScb) r1.second().get(0).incoming()).data());
	}
	
	@Test
	public void slaves_receive_updates() throws Exception {
		TestSyncScb s0 = new TestSyncScb(0, SyncStatus.UNKNOWN, "foo");
		TestSyncScb s1 = new TestSyncScb(1, SyncStatus.UNKNOWN, "bar");
		List<ScbOperation> ops = new ArrayList<>();
		
		ops.add(ScbOperation.make_incoming("c0", s0));
		ops.add(ScbOperation.make_incoming("c0", s1));
		
		Pair<Boolean, List<ScbOperation>> r;
		r = m_master.slave_contact("x", ops);
		assertEquals(true, r.first());
		assertEquals(2, r.second().size());
		
		s1.data("glu");
		ops.clear();
		ops.add(ScbOperation.make_delete("c0", 0));
		ops.add(ScbOperation.make_incoming("c0", s1));
		r = m_master.slave_contact("y", ops);
		
		assertEquals(true, r.first());
		assertEquals(1, r.second().size());
		TestSyncScb b = (TestSyncScb) r.second().get(0).incoming();
		assertEquals(1, b.id().intValue());
		assertEquals("glu", b.data());
		
		ops.clear();
		r = m_master.slave_contact("x", ops);
		
		assertEquals(false, r.first());
		assertEquals(2, r.second().size());
		int del_idx;
		int inc_idx;
		if (r.second().get(0).delete_key() != null) {
			del_idx = 0;
			inc_idx = 1;
		} else {
			del_idx = 1;
			inc_idx = 0;
		}
		
		b = (TestSyncScb) r.second().get(inc_idx).incoming();
		assertEquals(1, b.id().intValue());
		assertEquals("glu", b.data());
		assertEquals(0, ((Integer) r.second().get(del_idx).delete_key()).intValue());
	}
	
	@Test
	public void slaves_expire_and_will_reset() throws Exception {
		Pair<Boolean, List<ScbOperation>> r;
		
		r = m_master.slave_contact("x", new ArrayList<ScbOperation>());
		assertTrue(r.first());
		
		r = m_master.slave_contact("x", new ArrayList<ScbOperation>());
		assertFalse(r.first());
		
		Thread.sleep(2 * SLAVE_EXPIRATION_MS);
		r = m_master.slave_contact("x", new ArrayList<ScbOperation>());
		assertTrue(r.first());
	}
	
	@Test(expected = UnknownContainerException.class)
	public void operation_on_unknown_container() throws Exception {
		TestSyncScb s0 = new TestSyncScb(0, SyncStatus.UNKNOWN, "foo");
		List<ScbOperation> ops = new ArrayList<>();
		
		ops.add(ScbOperation.make_incoming("c1", s0));
		
		m_master.slave_contact("x", ops);
	}
}
