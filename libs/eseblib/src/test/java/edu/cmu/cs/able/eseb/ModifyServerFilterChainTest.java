package edu.cmu.cs.able.eseb;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import auxtestlib.BooleanEvaluation;
import auxtestlib.TestPropertiesDefinition;
import edu.cmu.cs.able.eseb.bus.EventBus;
import edu.cmu.cs.able.eseb.bus.EventBusAcceptPreprocessor;
import edu.cmu.cs.able.eseb.bus.EventBusConnectionData;
import edu.cmu.cs.able.eseb.bus.EventBusListener;
import edu.cmu.cs.able.eseb.conn.BusConnection;
import edu.cmu.cs.able.eseb.filter.EventFilter;
import edu.cmu.cs.able.typelib.prim.Int32Value;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;

/**
 * Test case that modifies the server event filter chains, both incoming and
 * outgoing.
 */
@SuppressWarnings("javadoc")
public class ModifyServerFilterChainTest extends EsebTestCase {
	/**
	 * The primitive scope.
	 */
	private PrimitiveScope m_pscope;
	
	/**
	 * The event bus.
	 */
	private EventBus m_bus;
	
	/**
	 * A bus connection.
	 */
	private BusConnection m_conn_1;
	
	/**
	 * A bus connection.
	 */
	private BusConnection m_conn_2;
	
	/**
	 * Save queue for connection 1.
	 */
	private TestArraySaveQueue m_save_1;
	
	/**
	 * Save queue for connection 2.
	 */
	private TestArraySaveQueue m_save_2;
	
	/**
	 * Server ID for client 1.
	 */
	private int m_id_1;
	
	/**
	 * Server ID for client 2.
	 */
	private int m_id_2;
	
	@Before
	public void set_up() throws Exception {
		m_pscope = new PrimitiveScope();
		short port = (short) TestPropertiesDefinition.getInt(
				"free-port-zone-start");
		m_bus = new EventBus(port, m_pscope);
		m_bus.start();
		
		m_conn_1 = new BusConnection("localhost", port, m_pscope);
		m_save_1 = new TestArraySaveQueue();
		m_conn_1.queue_group().add(m_save_1);
		
		final Integer[] ids = new Integer[1];
		m_bus.add_listener(new EventBusListener() {
			@Override
			public void distributed(BusData v, EventBusConnectionData source) {
				/* */
			}
			
			@Override
			public void connection_disconnected(EventBusConnectionData data) {
				/* */
			}
			
			@Override
			public void connection_accepted(EventBusConnectionData data) {
				ids[0] = data.id();
			}
		});
		
		m_conn_1.start();
		
		wait_for_true(new BooleanEvaluation() {
			@Override
			public boolean evaluate() throws Exception {
				return ids[0] != null;
			}
		});
		
		m_id_1 = ids[0];
		
		ids[0] = null;
		
		m_conn_2 = new BusConnection("localhost", port, m_pscope);
		m_conn_2.start();
		m_save_2 = new TestArraySaveQueue();
		m_conn_2.queue_group().add(m_save_2);
		
		wait_for_true(new BooleanEvaluation() {
			@Override
			public boolean evaluate() throws Exception {
				return ids[0] != null;
			}
		});
		
		m_id_2 = ids[0];
	}
	
	@After
	public void tear_down() throws Exception {
		if (m_bus != null) {
			m_bus.close();
		}
		
		if (m_conn_1 != null) {
			m_conn_1.close();
		}
		
		if (m_conn_2 != null) {
			m_conn_2.close();
		}
	}
	
	@Test
	public void modify_server_input_chain() throws Exception {
		m_bus.incoming_chain(m_id_1).add_filter(new EventFilter() {
			@Override
			public void sink(BusData data) throws IOException {
				forward(new BusData(m_pscope.int32().make(
						((Int32Value) data.value()).value() + 1)));
			}
		});
		
		m_conn_1.send(m_pscope.int32().make(5));
		wait_for_true(new BooleanEvaluation() {
			@Override
			public boolean evaluate() throws Exception {
				return m_save_1.m_values.size() == 1
						&& m_save_2.m_values.size() == 1;
			}
		});
		
		assertEquals(m_pscope.int32().make(6), m_save_1.m_values.get(0));
		assertEquals(m_pscope.int32().make(6), m_save_2.m_values.get(0));
	}
	
	@Test
	public void modify_server_output_chain() throws Exception {
		m_bus.outgoing_chain(m_id_2).add_filter(new EventFilter() {
			@Override
			public void sink(BusData data) throws IOException {
				forward(new BusData(m_pscope.int32().make(
						((Int32Value) data.value()).value() + 1)));
			}
		});
		
		m_conn_1.send(m_pscope.int32().make(5));
		wait_for_true(new BooleanEvaluation() {
			@Override
			public boolean evaluate() throws Exception {
				return m_save_1.m_values.size() == 1
						&& m_save_2.m_values.size() == 1;
			}
		});
		
		assertEquals(m_pscope.int32().make(5), m_save_1.m_values.get(0));
		assertEquals(m_pscope.int32().make(6), m_save_2.m_values.get(0));
	}
	
	@Test
	public void modify_server_chain_using_preprocessor() throws Exception {
		final boolean[] ran = new boolean[1];
		ran[0] = false;
		m_bus.add_preprocessor(new EventBusAcceptPreprocessor() {
			@Override
			public boolean preprocess(ControlledDataTypeSocketConnection
					connection) {
				connection.outgoing_chain().add_filter(new EventFilter() {
					@Override
					public void sink(BusData data) throws IOException {
						forward(new BusData(m_pscope.int32().make(
								((Int32Value) data.value()).value() + 1)));
					}
				});
				ran[0] = true;
				return true;
			}
		});
		
		try (BusConnection c = new BusConnection("localhost", m_bus.port(),
				m_pscope)) {
			c.start();
			wait_for_true(new BooleanEvaluation() {
				@Override
				public boolean evaluate() throws Exception {
					return ran[0];
				}
			});
			
			m_conn_1.send(m_pscope.int32().make(7));
			wait_for_true(new BooleanEvaluation() {
				@Override
				public boolean evaluate() throws Exception {
					return m_save_1.m_values.size() == 1
							&& m_save_2.m_values.size() == 1;
				}
			});
			
			assertEquals(m_pscope.int32().make(7), m_save_1.m_values.get(0));
			
			c.send(m_pscope.int32().make(8));
			wait_for_true(new BooleanEvaluation() {
				@Override
				public boolean evaluate() throws Exception {
					return m_save_1.m_values.size() == 2
							&& m_save_2.m_values.size() == 2;
				}
			});
			
			assertEquals(m_pscope.int32().make(8), m_save_1.m_values.get(1));
		}
	}
}
