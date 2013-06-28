package edu.cmu.cs.able.eseb;

import java.util.Date;

import incubator.dispatch.DispatchHelper;

import org.junit.Before;
import org.junit.Test;

import auxtestlib.DefaultTCase;
import auxtestlib.TestHelper;
import auxtestlib.TestPropertiesDefinition;
import auxtestlib.ThreadCountTestHelper;
import edu.cmu.cs.able.eseb.bus.EventBus;
import edu.cmu.cs.able.eseb.bus.EventBusConnectionData;
import edu.cmu.cs.able.eseb.conn.BusConnectionState;
import edu.cmu.cs.able.eseb.conn.BusConnection;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Test case that connects clients to servers and checks that publishing and
 * subscribing work.
 */
@SuppressWarnings("javadoc")
public class ClientServerTest extends DefaultTCase {
	@TestHelper
	private DispatchHelper m_dispatcher_helper;
	@TestHelper
	private ThreadCountTestHelper m_thread_count_helper;
	private short m_port;
	private PrimitiveScope m_scope;
	
	@Before
	public void set_up() throws Exception {
		m_port = (short) TestPropertiesDefinition.getInt(
				"free-port-zone-start");
		m_scope = new PrimitiveScope();
	}

	@Test
	public void connect_and_disconnect_client() throws Exception {
		try (EventBus srv = new EventBus(m_port, m_scope)) {
			srv.start();
			Thread.sleep(50);
			try (BusConnection c = new BusConnection("localhost", m_port, m_scope)) {
				assertEquals(BusConnectionState.DISCONNECTED, c.state());
				
				c.start();
				/*
				 * This may fail if the client connects *very* fast.
				 */
				assertEquals(BusConnectionState.CONNECTING, c.state());
				
				Thread.sleep(250);
				assertEquals(BusConnectionState.CONNECTED, c.state());
				
				c.stop();
				assertEquals(BusConnectionState.DISCONNECTED, c.state());
			}
		}
		
		/*
		 * Wait for threads to stop.
		 */
		Thread.sleep(250);
	}
	
	@Test
	public void publish_values_from_client_and_checks_statistics()
			throws Exception {
		CollectingBusServerListener bsl = new CollectingBusServerListener();
		CollectingBusClientListener bcl = new CollectingBusClientListener();
		Date start_time = new Date();
		try (EventBus srv = new EventBus(m_port, m_scope)) {
			srv.add_listener(bsl);
			srv.start();
			Thread.sleep(50);
			
			TestArraySaveQueue asq = new TestArraySaveQueue();
			try (BusConnection c = new BusConnection("localhost", m_port, m_scope)) {
				c.add_listener(bcl);
				c.queue_group().add(asq);
				
				c.start();
				Thread.sleep(250);
				Date connected_time = new Date();
				
				assertEquals(1, bsl.m_accepted.size());
				assertEquals(0, bsl.m_distributed_sources.size());
				assertEquals(0, bsl.m_distributed_values.size());
				assertEquals(0, bsl.m_disconnected.size());
				
				assertEquals(1, c.connect_count());
				assertEquals(0, c.receive_count());
				assertEquals(0, c.sent_count());
				assertTrue(!c.connection_time().before(start_time));
				assertTrue(!c.connection_time().after(connected_time));
				
				assertEquals(2, bcl.m_state_changed.size());
				assertEquals(0, asq.m_values.size());
				
				DataValue v = m_scope.int32().make(15);
				
				c.send(v);
				Thread.sleep(50);
				
				assertEquals(1, bsl.m_accepted.size());
				assertEquals(1, bsl.m_distributed_sources.size());
				EventBusConnectionData cd = bsl.m_distributed_sources.get(0);
				assertEquals(cd, bsl.m_distributed_sources.get(0));
				assertEquals(v, bsl.m_distributed_values.get(0).value());
				assertEquals(0, bsl.m_disconnected.size());
				
				assertTrue(!cd.connect_time().before(start_time));
				assertTrue(!cd.connect_time().after(connected_time));
				assertEquals(1, cd.publish_count());
				assertEquals(1, cd.subscribe_count());
				
				assertEquals(1, c.connect_count());
				assertEquals(1, c.receive_count());
				assertEquals(1, c.sent_count());
				assertTrue(!c.connection_time().before(start_time));
				assertTrue(!c.connection_time().after(connected_time));
				
				assertEquals(2, bcl.m_state_changed.size());
				assertEquals(1, asq.m_values.size());
				assertEquals(v, asq.m_values.get(0));
				
				c.close();
				Thread.sleep(150);
				Date disconnect_time = new Date();
				
				assertEquals(1, bsl.m_accepted.size());
				assertEquals(1, bsl.m_distributed_sources.size());
				assertEquals(cd, bsl.m_distributed_sources.get(0));
				assertEquals(v, bsl.m_distributed_values.get(0).value());
				assertEquals(1, bsl.m_disconnected.size());
				assertEquals(cd, bsl.m_disconnected.get(0));
				
				assertTrue(!cd.connect_time().before(start_time));
				assertTrue(!cd.connect_time().after(connected_time));
				assertEquals(1, cd.publish_count());
				assertEquals(1, cd.subscribe_count());
				
				assertEquals(1, c.connect_count());
				assertEquals(1, c.receive_count());
				assertEquals(1, c.sent_count());
				assertTrue(!c.connection_time().before(connected_time));
				assertTrue(!c.connection_time().after(disconnect_time));
				
				assertEquals(3, bcl.m_state_changed.size());
				assertEquals(1, asq.m_values.size());
				assertEquals(v, asq.m_values.get(0));
			}
		}
		
		/*
		 * Wait for threads to stop.
		 */
		Thread.sleep(250);
	}
	
	@Test
	public void connects_server_down_and_reestablishes_connection()
			throws Exception {
		CollectingBusClientListener bcl = new CollectingBusClientListener();
		TestArraySaveQueue asq = new TestArraySaveQueue();
		Date start_time = new Date();
		
		@SuppressWarnings("resource")
		EventBus srv = new EventBus(m_port, m_scope);
		try {
			srv.start();
			Thread.sleep(50);
			
			try (BusConnection c = new BusConnection("localhost", m_port, m_scope)) {
				c.add_listener(bcl);
				c.queue_group().add(asq);
				
				c.start();
				Thread.sleep(250);
				Date connected_time = new Date();
				
				assertEquals(1, c.connect_count());
				assertEquals(0, c.receive_count());
				assertEquals(0, c.sent_count());
				assertTrue(!c.connection_time().before(start_time));
				assertTrue(!c.connection_time().after(connected_time));
				
				assertEquals(2, bcl.m_state_changed.size());
				assertEquals(0, asq.m_values.size());
				
				srv.close();
				
				srv = new EventBus(m_port, m_scope);
				srv.start();
				
				Thread.sleep(500);
				
				Date reconnected_time = new Date();
				
				assertEquals(2, c.connect_count());
				assertEquals(0, c.receive_count());
				assertEquals(0, c.sent_count());
				assertTrue(!c.connection_time().before(connected_time));
				assertTrue(!c.connection_time().after(reconnected_time));
				
				assertEquals(5, bcl.m_state_changed.size());
				assertEquals(0, asq.m_values.size());
			}
		} finally {
			srv.close();
		}
		
		/*
		 * Wait for all threads to die...
		 */
		Thread.sleep(250);
	}
}
