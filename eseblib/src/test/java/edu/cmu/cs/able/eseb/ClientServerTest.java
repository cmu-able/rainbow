package edu.cmu.cs.able.eseb;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Date;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import auxtestlib.BooleanEvaluation;
import auxtestlib.TestHelper;
import auxtestlib.TestPropertiesDefinition;
import auxtestlib.ThreadCountTestHelper;
import edu.cmu.cs.able.eseb.bus.EventBus;
import edu.cmu.cs.able.eseb.bus.EventBusAcceptPreprocessor;
import edu.cmu.cs.able.eseb.bus.EventBusConnectionData;
import edu.cmu.cs.able.eseb.conn.BusConnection;
import edu.cmu.cs.able.eseb.conn.BusConnectionState;
import edu.cmu.cs.able.typelib.TestDataType;
import edu.cmu.cs.able.typelib.TestDataValue;
import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;
import edu.cmu.cs.able.typelib.prim.Int64Value;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.txtenc.DelegateTextEncoding;
import edu.cmu.cs.able.typelib.txtenc.TextEncoding;
import edu.cmu.cs.able.typelib.txtenc.typelib.DefaultTextEncoding;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataTypeScope;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Test case that connects clients to servers and checks that publishing and
 * subscribing work.
 */
@SuppressWarnings("javadoc")
public class ClientServerTest extends EsebTestCase {
	@TestHelper
	private ThreadCountTestHelper m_thread_count_helper;
	
	private static final long ENOUGH_TIME_TO_CONNECT_IF_WE_COULD_MS = 5000;
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
			try (BusConnection c = new BusConnection("localhost", m_port,
					m_scope)) {
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
			try (BusConnection c = new BusConnection("localhost", m_port,
					m_scope)) {
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
			
			try (BusConnection c = new BusConnection("localhost", m_port,
					m_scope)) {
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
	
	@Test
	public void server_preprocessor_can_deny_connection() throws Exception {
		try (EventBus srv = new EventBus(m_port, m_scope)) {
			srv.start();
			
			srv.add_preprocessor(new EventBusAcceptPreprocessor() {
				@Override
				public boolean preprocess(ControlledDataTypeSocketConnection
						connection) {
					return false;
				}
			});
			
			try (BusConnection c = new BusConnection("localhost", m_port,
					m_scope)) {
				c.start();
				
				Thread.sleep(ENOUGH_TIME_TO_CONNECT_IF_WE_COULD_MS);
				
				assertNotSame(BusConnectionState.CONNECTED, c.state());
 			}
		}
	}
	
	@Test
	public void can_send_non_understandable_data() throws Exception {
		final PrimitiveScope ps = new PrimitiveScope();
		final TestDataType tdt = new TestDataType("foo",
				new HashSet<DataType>());
		ps.add(tdt);
		
		DefaultTextEncoding dte = new DefaultTextEncoding(ps);
		dte.add(new DelegateTextEncoding() {
			@Override
			public boolean supports(DataType t) {
				return t instanceof TestDataType;
			}
			
			@Override
			public void encode(DataValue v, Writer w, TextEncoding enc)
					throws IOException {
				DataValue dv = ps.int64().make(((TestDataValue) v).m_val);
				enc.encode(dv, w);
			}
			
			@Override
			public DataValue decode(Reader r, DataType type, DataTypeScope dts,
					TextEncoding enc) throws IOException,
					InvalidEncodingException {
				Int64Value i64 = (Int64Value) enc.decode(r, dts);
				return new TestDataValue(tdt, i64.value());
			}
		});
		
		try (EventBus srv = new EventBus(m_port, m_scope);
				final BusConnection c1 = new BusConnection("localhost", m_port,
				ps, dte);
				final BusConnection c2 = new BusConnection("localhost", m_port,
				m_scope)) {
			srv.start();
			c1.start();
			c2.start();
			wait_for_true(new BooleanEvaluation() {
				@Override
				public boolean evaluate() throws Exception {
					return c1.state() == BusConnectionState.CONNECTED
							&& c2.state() == BusConnectionState.CONNECTED;
				}
			});
			
			final TestArraySaveQueue tasq = new TestArraySaveQueue();
			c2.queue_group().add(tasq);
			
			c1.send(new TestDataValue(tdt, 8));
			
			wait_for_true(new BooleanEvaluation() {
				@Override
				public boolean evaluate() throws Exception {
					return tasq.m_bdata.size() > 0;
				}
			});
			
			assertEquals(1, tasq.m_values.size());
			assertEquals(1, tasq.m_bdata.size());
			assertNull(tasq.m_values.get(0));
			assertNotNull(tasq.m_bdata.get(0));
			assertTrue(tasq.m_bdata.get(0).length > 0);
			assertNotNull(tasq.m_ex.get(0));
		}
	}
	
	@Test
	public void send_receive_data_bus_does_not_understand() throws Exception {
		final PrimitiveScope ps = new PrimitiveScope();
		final TestDataType tdt = new TestDataType("foo",
				new HashSet<DataType>());
		ps.add(tdt);
		
		DefaultTextEncoding dte = new DefaultTextEncoding(ps);
		dte.add(new DelegateTextEncoding() {
			@Override
			public boolean supports(DataType t) {
				return t instanceof TestDataType;
			}
			
			@Override
			public void encode(DataValue v, Writer w, TextEncoding enc)
					throws IOException {
				DataValue dv = ps.int64().make(((TestDataValue) v).m_val);
				enc.encode(dv, w);
			}
			
			@Override
			public DataValue decode(Reader r, DataType type, DataTypeScope dts,
					TextEncoding enc) throws IOException,
					InvalidEncodingException {
				Int64Value i64 = (Int64Value) enc.decode(r, dts);
				return new TestDataValue(tdt, i64.value());
			}
		});
		
		try (EventBus srv = new EventBus(m_port, m_scope);
				final BusConnection c1 = new BusConnection("localhost", m_port,
				ps, dte);
				final BusConnection c2 = new BusConnection("localhost", m_port,
				ps, dte)) {
			srv.start();
			c1.start();
			c2.start();
			wait_for_true(new BooleanEvaluation() {
				@Override
				public boolean evaluate() throws Exception {
					return c1.state() == BusConnectionState.CONNECTED
							&& c2.state() == BusConnectionState.CONNECTED;
				}
			});
			
			final TestArraySaveQueue tasq = new TestArraySaveQueue();
			c2.queue_group().add(tasq);
			
			c1.send(new TestDataValue(tdt, 8));
			
			wait_for_true(new BooleanEvaluation() {
				@Override
				public boolean evaluate() throws Exception {
					return tasq.m_bdata.size() > 0;
				}
			});
			
			assertEquals(1, tasq.m_values.size());
			assertEquals(1, tasq.m_bdata.size());
			assertNotNull(tasq.m_values.get(0));
			assertTrue(tasq.m_values.get(0) instanceof TestDataValue);
			assertEquals(8, ((TestDataValue) tasq.m_values.get(0)).m_val);
			assertNotNull(tasq.m_bdata.get(0));
			assertTrue(tasq.m_bdata.get(0).length > 0);
			assertNull(tasq.m_ex.get(0));
		}
	}
}
