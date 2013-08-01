package edu.cmu.cs.able.eseb;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import auxtestlib.TestPropertiesDefinition;
import edu.cmu.cs.able.eseb.bus.EventBus;
import edu.cmu.cs.able.eseb.bus.rci.EventBusRemoteControlInterfaceImpl;
import edu.cmu.cs.able.eseb.bus.rci.LimitedDistributionQueue;
import edu.cmu.cs.able.eseb.conn.BusConnection;
import edu.cmu.cs.able.typelib.enc.DataValueEncoding;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.txtenc.typelib.DefaultTextEncoding;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Test suite that verifies the bus server remote interface.
 */
@SuppressWarnings("javadoc")
public class BusServerRemoteInterfaceTest extends EsebTestCase {
	/**
	 * Minimum network time quantum, in milliseconds.
	 */
	private static final long TIME_NQ_MS = 150;
	
	/**
	 * The primitive data scope.
	 */
	private PrimitiveScope m_pscope;
	
	/**
	 * Port where server is running.
	 */
	private short m_port;
	
	/**
	 * The running server.
	 */
	private EventBus m_server;
	
	/**
	 * Implementation of the remote interface.
	 */
	private EventBusRemoteControlInterfaceImpl m_ri;
	
	/**
	 * A bus client.
	 */
	private BusConnection m_client;
	
	@Before
	public void set_up() throws Exception {
		m_port = (short) TestPropertiesDefinition.getInt(
				"free-port-zone-start");
		m_pscope = new PrimitiveScope();
		m_server = new EventBus(m_port, m_pscope);
		m_server.start();
		m_ri = new EventBusRemoteControlInterfaceImpl(m_server, 3, (short) 4);
		m_client = new BusConnection("localhost", m_port, m_pscope);
		m_client.start();
	}
	
	@After
	public void tear_down() throws Exception {
		m_client.close();
		m_server.close();
	}
	
	/**
	 * Decodes a byte array received to a data value.
	 * @param b the byte array
	 * @return the data value
	 * @throws Exception failed to decode
	 */
	private DataValue decode(byte[] b) throws Exception {
		assertNotNull(b);
		assertTrue(b.length > 0);
		
		DataValueEncoding dve = new DefaultTextEncoding(m_pscope);
		return dve.decode(new DataInputStream(new ByteArrayInputStream(b)),
				m_pscope);
	}
	
	@Test
	public void obtain_running_port() throws Exception {
		assertEquals(m_port, m_ri.port());
	}
	
	@Test
	public void obtain_data_master_port() throws Exception {
		assertEquals((short) 4, m_ri.data_master_port());
	}
	
	@Test
	public void events_before_initial_queue_are_not_seen() throws Exception {
		m_client.send(m_pscope.int32().make(20));
		Thread.sleep(TIME_NQ_MS);
		LimitedDistributionQueue ldq = m_ri.distribution_queue("foo");
		assertNotNull(ldq);
		assertEquals(0, ldq.size());
		assertEquals(3, ldq.limit());
	}
	
	@Test
	public void events_after_initial_queue_are_reported_incrementally()
			throws Exception {
		LimitedDistributionQueue ldq_0 = m_ri.distribution_queue("foo");
		m_client.send(m_pscope.int32().make(20));
		Thread.sleep(TIME_NQ_MS);
		LimitedDistributionQueue ldq_1 = m_ri.distribution_queue("foo");
		m_client.send(m_pscope.int32().make(22));
		m_client.send(m_pscope.int32().make(-25));
		Thread.sleep(TIME_NQ_MS);
		LimitedDistributionQueue ldq_2 = m_ri.distribution_queue("foo");
		Thread.sleep(TIME_NQ_MS);
		LimitedDistributionQueue ldq_3 = m_ri.distribution_queue("foo");
		
		assertNotNull(ldq_0);
		assertEquals(0, ldq_0.size());
		assertEquals(0, ldq_0.lost());
		assertEquals(0, ldq_0.all().size());
		
		assertNotNull(ldq_1);
		assertEquals(1, ldq_1.size());
		assertEquals(0, ldq_1.lost());
		assertEquals(1, ldq_1.all().size());
		assertEquals(m_pscope.int32().make(20),
				decode(ldq_1.all().get(0).contents()));
		
		assertNotNull(ldq_2);
		assertEquals(2, ldq_2.size());
		assertEquals(0, ldq_2.lost());
		assertEquals(2, ldq_2.all().size());
		assertEquals(m_pscope.int32().make(22),
				decode(ldq_2.all().get(0).contents()));
		assertEquals(m_pscope.int32().make(-25),
				decode(ldq_2.all().get(1).contents()));
		
		assertNotNull(ldq_3);
		assertEquals(0, ldq_3.size());
		assertEquals(0, ldq_3.lost());
		assertEquals(0, ldq_3.all().size());
	}
	
	@Test
	public void different_clients_have_different_ids() throws Exception {
		try (BusConnection c2 = new BusConnection("localhost", m_port, m_pscope)) {
			c2.start();
			
			m_ri.distribution_queue("x");
			
			m_client.send(m_pscope.int32().make(1));
			Thread.sleep(TIME_NQ_MS);
			c2.send(m_pscope.int32().make(2));
			Thread.sleep(TIME_NQ_MS);
			m_client.send(m_pscope.int32().make(3));
			Thread.sleep(TIME_NQ_MS);
			
			LimitedDistributionQueue q = m_ri.distribution_queue("x");
			assertEquals(3, q.size());
			assertEquals(m_pscope.int32().make(1),
					decode(q.all().get(0).contents()));
			assertEquals(m_pscope.int32().make(2),
					decode(q.all().get(1).contents()));
			assertEquals(m_pscope.int32().make(3),
					decode(q.all().get(2).contents()));
			assertNotSame(q.all().get(0).client_id(), 
					q.all().get(1).client_id());
			assertEquals(q.all().get(0).client_id(), 
					q.all().get(2).client_id());
		}
	}
	
	@Test
	public void different_queues_are_independent() throws Exception {
		m_ri.distribution_queue("x");
		m_ri.distribution_queue("y");
		
		m_client.send(m_pscope.int32().make(1));
		Thread.sleep(TIME_NQ_MS);
		
		LimitedDistributionQueue x = m_ri.distribution_queue("x");
		assertEquals(1, x.size());
		assertEquals(0, x.lost());
		assertEquals(1, x.all().size());
		assertEquals(m_pscope.int32().make(1),
				decode(x.all().get(0).contents()));
		
		LimitedDistributionQueue y = m_ri.distribution_queue("y");
		assertEquals(1, y.size());
		assertEquals(0, y.lost());
		assertEquals(1, y.all().size());
		assertEquals(m_pscope.int32().make(1),
				decode(y.all().get(0).contents()));
	}
	
	@Test
	public void queues_not_requested_for_long_are_discarded()
			throws Exception {
		m_ri.expire_limit(2 * TIME_NQ_MS);
		m_ri.distribution_queue("x");
		
		m_client.send(m_pscope.int32().make(1));
		Thread.sleep(TIME_NQ_MS);
		
		LimitedDistributionQueue x = m_ri.distribution_queue("x");
		assertEquals(1, x.size());
		assertEquals(0, x.lost());
		assertEquals(1, x.all().size());
		assertEquals(m_pscope.int32().make(1),
				decode(x.all().get(0).contents()));
		
		m_client.send(m_pscope.int32().make(1));
		Thread.sleep(TIME_NQ_MS);
		
		x = m_ri.distribution_queue("x");
		assertEquals(1, x.size());
		assertEquals(0, x.lost());
		assertEquals(1, x.all().size());
		assertEquals(m_pscope.int32().make(1),
				decode(x.all().get(0).contents()));
		
		m_client.send(m_pscope.int32().make(1));
		Thread.sleep(3 * TIME_NQ_MS);
		
		x = m_ri.distribution_queue("x");
		assertEquals(0, x.size());
		assertEquals(0, x.lost());
		assertEquals(0, x.all().size());
	}
}
