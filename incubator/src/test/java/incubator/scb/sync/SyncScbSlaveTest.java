package incubator.scb.sync;

import incubator.dispatch.DispatchHelper;
import incubator.exh.ExhHelper;
import incubator.scb.ScbContainer;
import incubator.scb.ScbEditableContainerImpl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import auxtestlib.DefaultTCase;
import auxtestlib.TestHelper;
import auxtestlib.ThreadCountTestHelper;

/**
 * Tests synchronizing a slave with a master.
 */
@SuppressWarnings("javadoc")
public class SyncScbSlaveTest extends DefaultTCase {
	@TestHelper
	public ThreadCountTestHelper m_thread_helper;
	
	@TestHelper
	public DispatchHelper m_dispatcher_helper;
	
	@TestHelper
	public ExhHelper m_exh_helper;
	
	/**
	 * Synchronization master.
	 */
	private SyncScbMasterImpl m_master;
	
	/**
	 * Synchronization slave.
	 */
	private SyncScbSlave m_slave;
	
	/**
	 * The master c0 container.
	 */
	private ScbContainer<TestSyncScb> m_m0;
	
	/**
	 * The client-local c0 container.
	 */
	private ScbEditableContainerImpl<TestSyncScb> m_c0;
	
	@Before
	public void set_up() throws Exception {
		m_master = new SyncScbMasterImpl(3600_000);
		m_slave = new SyncScbSlave(m_master, 3600_000);
		m_m0 = m_master.create_container("c0", Integer.class,
				TestSyncScb.class);
		m_c0 = new ScbEditableContainerImpl<>();
		m_slave.sync_now_wait();
		m_dispatcher_helper.wait_dispatch_clear();
		
		m_slave.add_container("c0", m_c0, Integer.class, TestSyncScb.class);
		m_slave.sync_now_wait();
		m_dispatcher_helper.wait_dispatch_clear();
	}
	
	@After
	public void tear_down() throws Exception {
		m_master.shutdown();
		m_slave.shutdown();
	}
	
	@Test
	public void local_changes_are_propagated_to_server_and_back()
			throws Exception {
		/*
		 * Make sure we do a synchronization and wait.
		 */
		m_slave.sync_now_wait();
		
		TestSyncScb s = new TestSyncScb(0, SyncStatus.UNKNOWN, "foo");
		m_c0.add_scb(s);
		m_dispatcher_helper.wait_dispatch_clear();
		
		/*
		 * The SCB's status should have been changed to LOCAL_CHANGES but
		 * nothing should have been sent to the server.
		 */
		assertEquals(1, m_c0.all_scbs().size());
		assertEquals(SyncStatus.LOCAL_CHANGES, s.sync_status());
		assertEquals(0, m_m0.all_scbs().size());
		
		/*
		 * If we force a synchronization and wait a little bit, the status
		 * should change to SYNCHRONIZED and the master should have the
		 * object.
		 */
		m_slave.sync_now_wait();
		m_dispatcher_helper.wait_dispatch_clear();
		
		assertEquals(1, m_m0.all_scbs().size());
		assertEquals(1, m_c0.all_scbs().size());
		assertEquals(SyncStatus.SYNCHRONIZED, s.sync_status());
		assertEquals("foo", s.data());
		TestSyncScb ss = m_m0.all_scbs().iterator().next();
		assertEquals(SyncStatus.MASTER, ss.sync_status());
		assertEquals("foo", ss.data());
		
		/*
		 * If we update the object, the status should change to LOCAL_CHANGES
		 * and the server object should be left unmodified.
		 */
		s.data("bar");
		m_dispatcher_helper.wait_dispatch_clear();
		
		assertEquals(1, m_c0.all_scbs().size());
		assertEquals(1, m_m0.all_scbs().size());
		assertTrue(m_c0.all_scbs().contains(s));
		assertTrue(m_m0.all_scbs().contains(ss));
		assertEquals(SyncStatus.LOCAL_CHANGES, s.sync_status());
		assertEquals(SyncStatus.MASTER, ss.sync_status());
		assertEquals("bar", s.data());
		assertEquals("foo", ss.data());
		
		/*
		 * If we force a synchronization and wait a little bit, the status
		 * should change to SYNCHRONIZED and the master should have the
		 * updated object.
		 */
		m_slave.sync_now_wait();
		m_dispatcher_helper.wait_dispatch_clear();
		
		assertEquals(1, m_c0.all_scbs().size());
		assertEquals(1, m_m0.all_scbs().size());
		assertTrue(m_c0.all_scbs().contains(s));
		assertTrue(m_m0.all_scbs().contains(ss));
		assertEquals(SyncStatus.SYNCHRONIZED, s.sync_status());
		assertEquals(SyncStatus.MASTER, ss.sync_status());
		assertEquals("bar", s.data());
		assertEquals("bar", ss.data());
		
		/*
		 * If we delete the object from the slave, it should disappear from
		 * the local repository but still exist in the master.
		 */
		m_c0.remove_scb(s);
		m_dispatcher_helper.wait_dispatch_clear();
		
		assertEquals(0, m_c0.all_scbs().size());
		assertEquals(1, m_m0.all_scbs().size());
		assertTrue(m_m0.all_scbs().contains(ss));
		assertEquals(SyncStatus.MASTER, ss.sync_status());
		assertEquals("bar", ss.data());
		
		/*
		 * If we force a synchronization and wait a little bit, it should
		 * disappear from the master.
		 */
		m_slave.sync_now_wait();
		m_dispatcher_helper.wait_dispatch_clear();
		
		assertEquals(0, m_c0.all_scbs().size());
		assertEquals(0, m_m0.all_scbs().size());
	}
	
	@Test
	public void remote_changes_are_propagated_to_server_and_to_client()
			throws Exception {
		SyncScbSlave slave = new SyncScbSlave(m_master, 3600_000);
		ScbEditableContainerImpl<TestSyncScb> cont =
				new ScbEditableContainerImpl<>();
		slave.sync_now_wait();
		m_dispatcher_helper.wait_dispatch_clear();
		
		slave.add_container("c0", cont, Integer.class, TestSyncScb.class);
		slave.sync_now_wait();
		m_dispatcher_helper.wait_dispatch_clear();
		
		/*
		 * Initially, the slave should be empty.
		 */
		assertEquals(0, cont.all_scbs().size());
		
		/*
		 * If we add an SCB to another slave and force both to synchronize,
		 * we should get the SCB here.
		 */
		TestSyncScb r_scb = new TestSyncScb(0, SyncStatus.UNKNOWN, "glu");
		m_c0.add_scb(r_scb);
		m_dispatcher_helper.wait_dispatch_clear();
		m_slave.sync_now_wait();
		m_dispatcher_helper.wait_dispatch_clear();
		slave.sync_now_wait();
		m_dispatcher_helper.wait_dispatch_clear();
		
		assertEquals(1, cont.all_scbs().size());
		TestSyncScb scb = cont.all_scbs().iterator().next();
		assertEquals(0, scb.id().intValue());
		assertEquals("glu", scb.data());
		assertEquals(SyncStatus.SYNCHRONIZED, scb.sync_status());
		
		/*
		 * If we update the SCB in another container, it should be updated
		 * here.
		 */
		r_scb.data("gloo");
		m_dispatcher_helper.wait_dispatch_clear();
		m_slave.sync_now_wait();
		m_dispatcher_helper.wait_dispatch_clear();
		slave.sync_now_wait();
		m_dispatcher_helper.wait_dispatch_clear();
		
		assertEquals(1, cont.all_scbs().size());
		assertTrue(cont.all_scbs().contains(scb));
		assertEquals(0, scb.id().intValue());
		assertEquals("gloo", scb.data());
		assertEquals(SyncStatus.SYNCHRONIZED, scb.sync_status());
		
		/*
		 * If we delete the SCB in another container, it should be updated
		 * here.
		 */
		m_c0.remove_scb(r_scb);
		m_dispatcher_helper.wait_dispatch_clear();
		m_slave.sync_now_wait();
		m_dispatcher_helper.wait_dispatch_clear();
		slave.sync_now_wait();
		m_dispatcher_helper.wait_dispatch_clear();
		
		assertEquals(0, cont.all_scbs().size());
		
		/*
		 * Terminate the slave.
		 */
		slave.shutdown();
	}
}
