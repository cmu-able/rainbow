package incubator.scb.sync;

import incubator.dispatch.DispatchHelper;

import org.junit.Before;
import org.junit.Test;

import auxtestlib.DefaultTCase;
import auxtestlib.TestHelper;
import auxtestlib.ThreadCountTestHelper;

/**
 * Test suite for the master SCB container.
 */
@SuppressWarnings("javadoc")
public class SyncScbMasterContainerTest extends DefaultTCase {
	@TestHelper
	public ThreadCountTestHelper m_thread_count_helper;
	
	@TestHelper
	public DispatchHelper m_dispatch_helper;
	
	/**
	 * The master container.
	 */
	private SyncScbMasterContainerImpl<Integer, TestSyncScb> m_master;
	
	/**
	 * The master listener.
	 */
	private TestScbContainerListener<TestSyncScb> m_master_listener;
	
	@Before
	public void set_up() {
		m_master = new SyncScbMasterContainerImpl<>();
		m_master_listener = new TestScbContainerListener<>();
		m_master.add_listener(m_master_listener);
	}
	
	@Test
	public void added_scbs_are_reported_and_sync_status_is_master()
			throws Exception {
		TestSyncScb s1 = new TestSyncScb(1, SyncStatus.LOCAL_CHANGES, "foo");
		TestSyncScb s2 = new TestSyncScb(2, SyncStatus.MASTER, "bar");
		TestSyncScb s3 = new TestSyncScb(3, SyncStatus.SYNCHRONIZED, "glu");
		TestSyncScb s4 = new TestSyncScb(4, SyncStatus.UNKNOWN, "gloo");
		
		m_master.incoming(s1);
		m_master.incoming(s2);
		m_master.incoming(s3);
		m_master.incoming(s4);
		
		m_dispatch_helper.wait_dispatch_clear();
		
		assertEquals(4, m_master_listener.m_added.size());
		assertEquals(1, m_master_listener.m_added.get(0).id().intValue());
		assertEquals(SyncStatus.MASTER,
				m_master_listener.m_added.get(0).sync_status());
		assertEquals("foo", m_master_listener.m_added.get(0).data());
		assertEquals(2, m_master_listener.m_added.get(1).id().intValue());
		assertEquals(SyncStatus.MASTER,
				m_master_listener.m_added.get(1).sync_status());
		assertEquals("bar", m_master_listener.m_added.get(1).data());
		assertEquals(3, m_master_listener.m_added.get(2).id().intValue());
		assertEquals(SyncStatus.MASTER,
				m_master_listener.m_added.get(2).sync_status());
		assertEquals("glu", m_master_listener.m_added.get(2).data());
		assertEquals(4, m_master_listener.m_added.get(3).id().intValue());
		assertEquals(SyncStatus.MASTER,
				m_master_listener.m_added.get(3).sync_status());
		assertEquals("gloo", m_master_listener.m_added.get(3).data());
		assertEquals(0, m_master_listener.m_removed.size());
		assertEquals(0, m_master_listener.m_updated.size());
	}
	
	@Test
	public void updated_scbs_are_reported_but_dont_change_sync_status()
			throws Exception {
		TestSyncScb s1 = new TestSyncScb(1, SyncStatus.LOCAL_CHANGES, "foo");
		TestSyncScb s2 = new TestSyncScb(1, SyncStatus.UNKNOWN, "bar");
		
		m_master.incoming(s1);
		m_master.incoming(s2);
		
		m_dispatch_helper.wait_dispatch_clear();
		
		assertEquals(1, m_master_listener.m_added.size());
		assertEquals(1, m_master_listener.m_updated.size());
		assertEquals(0, m_master_listener.m_removed.size());
		
		assertEquals(1, m_master_listener.m_updated.get(0).id().intValue());
		assertEquals(SyncStatus.MASTER,
				m_master_listener.m_updated.get(0).sync_status());
		assertEquals("bar", m_master_listener.m_updated.get(0).data());
		
		assertSame(m_master_listener.m_added.get(0),
				m_master_listener.m_updated.get(0));
	}
	
	@Test
	public void deleted_scbs_are_reported() throws Exception {
		TestSyncScb s1 = new TestSyncScb(1, SyncStatus.LOCAL_CHANGES, "foo");
		
		m_master.incoming(s1);
		m_master.delete(1);
		
		m_dispatch_helper.wait_dispatch_clear();
		
		assertEquals(1, m_master_listener.m_added.size());
		assertEquals(0, m_master_listener.m_updated.size());
		assertEquals(1, m_master_listener.m_removed.size());
		
		assertEquals(1, m_master_listener.m_removed.get(0).id().intValue());
		assertEquals(SyncStatus.MASTER,
				m_master_listener.m_removed.get(0).sync_status());
		assertEquals("foo", m_master_listener.m_removed.get(0).data());
		
		assertSame(m_master_listener.m_added.get(0),
				m_master_listener.m_removed.get(0));
	}
	
	@Test
	public void deleting_non_existing_scbs_is_ignored() throws Exception {
		m_master.delete(0);
		
		m_dispatch_helper.wait_dispatch_clear();
		
		assertEquals(0, m_master_listener.m_added.size());
		assertEquals(0, m_master_listener.m_updated.size());
		assertEquals(0, m_master_listener.m_removed.size());
	}
}
