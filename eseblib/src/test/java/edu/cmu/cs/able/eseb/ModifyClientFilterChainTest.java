package edu.cmu.cs.able.eseb;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import auxtestlib.BooleanEvaluation;
import auxtestlib.TestPropertiesDefinition;
import edu.cmu.cs.able.eseb.bus.EventBus;
import edu.cmu.cs.able.eseb.conn.BusConnection;
import edu.cmu.cs.able.eseb.filter.EventFilter;
import edu.cmu.cs.able.typelib.prim.Int32Value;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;

/**
 * Modifies the client's incoming and outgoing filter chains.
 */
@SuppressWarnings("javadoc")
public class ModifyClientFilterChainTest extends EsebTestCase {
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
	
	@Before
	public void set_up() throws Exception {
		m_pscope = new PrimitiveScope();
		short port = (short) TestPropertiesDefinition.getInt(
				"free-port-zone-start");
		m_bus = new EventBus(port, m_pscope);
		m_bus.start();
		
		m_conn_1 = new BusConnection("localhost", port, m_pscope);
		m_conn_1.start();
		m_save_1 = new TestArraySaveQueue();
		m_conn_1.queue_group().add(m_save_1);
		
		m_conn_2 = new BusConnection("localhost", port, m_pscope);
		m_conn_2.start();
		m_save_2 = new TestArraySaveQueue();
		m_conn_2.queue_group().add(m_save_2);
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
	public void modify_incoming_chain() throws Exception {
		m_conn_1.incoming_chain().add_filter(new EventFilter() {
			@Override
			public void sink(BusData data) throws IOException {
				if (data.value() instanceof Int32Value) {
					forward(new BusData(m_pscope.int32().make(
							((Int32Value) data.value()).value() + 1)));
				}
			}
		});
		
		m_conn_1.send(m_pscope.int32().make(3));
		wait_for_true(new BooleanEvaluation() {
			@Override
			public boolean evaluate() throws Exception {
				return m_save_1.m_values.size() == 1
						&& m_save_2.m_values.size() == 1;
			}
		});
		
		assertEquals(m_pscope.int32().make(4), m_save_1.m_values.get(0));
		assertEquals(m_pscope.int32().make(3), m_save_2.m_values.get(0));
		
		m_conn_2.send(m_pscope.int32().make(-3));
		wait_for_true(new BooleanEvaluation() {
			@Override
			public boolean evaluate() throws Exception {
				return m_save_1.m_values.size() == 2
						&& m_save_2.m_values.size() == 2;
			}
		});
		
		assertEquals(m_pscope.int32().make(-2), m_save_1.m_values.get(1));
		assertEquals(m_pscope.int32().make(-3), m_save_2.m_values.get(1));
	}
	
	@Test
	public void modify_outgoing_chain() throws Exception {
		m_conn_1.outgoing_chain().add_filter(new EventFilter() {
			@Override
			public void sink(BusData data) throws IOException {
				if (data.value() instanceof Int32Value) {
					forward(new BusData(m_pscope.int32().make(
							((Int32Value) data.value()).value() + 1)));
				}
			}
		});
		
		m_conn_1.send(m_pscope.int32().make(3));
		wait_for_true(new BooleanEvaluation() {
			@Override
			public boolean evaluate() throws Exception {
				return m_save_1.m_values.size() == 1
						&& m_save_2.m_values.size() == 1;
			}
		});
		
		assertEquals(m_pscope.int32().make(4), m_save_1.m_values.get(0));
		assertEquals(m_pscope.int32().make(4), m_save_2.m_values.get(0));
		
		m_conn_2.send(m_pscope.int32().make(-3));
		wait_for_true(new BooleanEvaluation() {
			@Override
			public boolean evaluate() throws Exception {
				return m_save_1.m_values.size() == 2
						&& m_save_2.m_values.size() == 2;
			}
		});
		
		assertEquals(m_pscope.int32().make(-3), m_save_1.m_values.get(1));
		assertEquals(m_pscope.int32().make(-3), m_save_2.m_values.get(1));
	}
}
