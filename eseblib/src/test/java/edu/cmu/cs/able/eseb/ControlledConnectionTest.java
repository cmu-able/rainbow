package edu.cmu.cs.able.eseb;

import java.io.IOException;

import incubator.dispatch.DispatchHelper;
import incubator.dispatch.DispatcherOp;
import incubator.wt.CloseableListener;

import org.junit.Before;
import org.junit.Test;

import auxtestlib.DefaultTCase;
import auxtestlib.TestHelper;
import auxtestlib.ThreadCountTestHelper;
import edu.cmu.cs.able.typelib.prim.Int32Value;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.prim.StringValue;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Test case that checks how the controlled connection works.
 */
@SuppressWarnings("javadoc")
public class ControlledConnectionTest extends DefaultTCase {
	private static final int TIME_UNIT_MS = 5;
	private static final int MULT = 10;
	
	@TestHelper
	public DispatchHelper m_dispatch_helper;
	
	@TestHelper
	public ThreadCountTestHelper m_thread_helper;
	
	private PrimitiveScope m_pscope;
	private TestDataTypeSocketConnection m_connection;
	private ControlledDataTypeSocketConnectionImpl m_controlled;
	
	@Before
	public void set_up() throws Exception {
		m_pscope = new PrimitiveScope();
		m_connection = new TestDataTypeSocketConnection();
		m_controlled = new ControlledDataTypeSocketConnectionImpl(m_pscope,
				m_connection);
	}
	
	@Test
	public void startup_shutdown_controlled_operates_on_connection()
			throws Exception {
		assertEquals(0, m_connection.m_start);
		assertEquals(0, m_connection.m_stop);
		assertEquals(0, m_connection.m_closed);
		
		m_controlled.start();
		
		assertEquals(1, m_connection.m_start);
		assertEquals(0, m_connection.m_stop);
		assertEquals(0, m_connection.m_closed);
		
		m_controlled.stop();
		
		assertEquals(1, m_connection.m_start);
		assertEquals(1, m_connection.m_stop);
		assertEquals(0, m_connection.m_closed);
		
		m_connection.close();
		
		assertEquals(1, m_connection.m_start);
		assertEquals(1, m_connection.m_stop);
		assertEquals(1, m_connection.m_closed);
	}
	
	@Test
	public void sends_pings_regularly_only_when_running() throws Exception {
		m_controlled.m_ping_check_interval_ms = TIME_UNIT_MS;
		m_controlled.m_ping_send_interval_ms = MULT * TIME_UNIT_MS;
		
		Thread.sleep(MULT * 3 / 2 * TIME_UNIT_MS);
		assertEquals(0, m_connection.m_written.size());
		
		m_controlled.start();
		
		Thread.sleep(MULT * 3 / 2 * TIME_UNIT_MS);
		assertEquals(1, m_connection.m_written.size());
		assertTrue(m_connection.m_written.get(0).value()
				instanceof StringValue);
		String v0 = ((StringValue)
				m_connection.m_written.get(0).value()).value();
		
		Thread.sleep(MULT * TIME_UNIT_MS);
		assertEquals(2, m_connection.m_written.size());
		assertTrue(m_connection.m_written.get(1).value()
				instanceof StringValue);
		String v1 = ((StringValue)
				m_connection.m_written.get(0).value()).value();
		assertEquals(v0, v1);
		
		m_controlled.stop();
		
		Thread.sleep(2 * MULT * TIME_UNIT_MS);
		assertEquals(2, m_connection.m_written.size());
	}
	
	@Test
	public void closes_connection_when_no_pings_received() throws Exception {
		m_controlled.m_ping_check_interval_ms = TIME_UNIT_MS;
		m_controlled.m_ping_send_interval_ms = MULT * TIME_UNIT_MS;
		m_controlled.m_ping_max_interval_ms = MULT * TIME_UNIT_MS;
		
		m_controlled.start();
		
		assertEquals(0, m_connection.m_closed);
		final StringValue sv = m_pscope.string().make(
				ControlledDataTypeSocketConnectionImpl.CMD_PREFIX
				+ ControlledDataTypeSocketConnectionImpl.CMD_PING);
		for (int i = 0; i < 5; i++) {
			m_connection.add_to_queue(new BusData(sv));
			
			Thread.sleep(MULT / 2 * TIME_UNIT_MS);
			assertEquals(0, m_connection.m_closed);
		}
		
		Thread.sleep(MULT * 3 / 2 * TIME_UNIT_MS);
		assertEquals(1, m_connection.m_closed);
		
		m_connection.m_written.clear();
		Thread.sleep(2 * MULT * TIME_UNIT_MS);
		assertEquals(1, m_connection.m_closed);
		assertEquals(0, m_connection.m_written.size());
	}
	
	@Test
	public void receives_nosend_and_does_not_publish_more_data_but_sends_ping()
			throws Exception {
		m_controlled.m_ping_check_interval_ms = TIME_UNIT_MS;
		m_controlled.m_ping_send_interval_ms = MULT * TIME_UNIT_MS;
		
		DataValue v = m_pscope.int32().make(33);
		
		m_controlled.start();
		
		m_controlled.write(v);
		assertEquals(1, m_connection.m_written.size());
		m_controlled.write(v);
		assertEquals(2, m_connection.m_written.size());
		
		Thread.sleep(MULT * 3 / 2 * TIME_UNIT_MS);
		assertEquals(3, m_connection.m_written.size());
		
		final StringValue sv = m_pscope.string().make(
				ControlledDataTypeSocketConnectionImpl.CMD_PREFIX
				+ ControlledDataTypeSocketConnectionImpl.CMD_NOSEND);
		m_connection.add_to_queue(new BusData(sv));
		
		Thread.sleep(MULT * TIME_UNIT_MS);
		assertEquals(4, m_connection.m_written.size());
		
		m_controlled.write(v);
		assertEquals(4, m_connection.m_written.size());
		m_controlled.write(v);
		assertEquals(4, m_connection.m_written.size());
		
		m_controlled.stop();
	}
	
	@Test
	public void sends_nosend_when_asked() throws Exception {
		m_controlled.start();
		
		m_controlled.publish_only();
		
		m_controlled.stop();
		
		assertEquals(1, m_connection.m_written.size());
		assertTrue(m_connection.m_written.get(0).value()
				instanceof StringValue);
		String v0 = ((StringValue)
				m_connection.m_written.get(0).value()).value();
		assertEquals(ControlledDataTypeSocketConnectionImpl.CMD_PREFIX
				+ ControlledDataTypeSocketConnectionImpl.CMD_NOSEND, v0);
	}
	
	@Test
	public void informs_connection_closed_does_not_send_pings()
			throws Exception {
		m_controlled.m_ping_check_interval_ms = TIME_UNIT_MS;
		m_controlled.m_ping_send_interval_ms = MULT * TIME_UNIT_MS;
		
		TestCloseableListener tcl = new TestCloseableListener();
		m_controlled.closeable_dispatcher().add(tcl);
		
		m_controlled.start();
		
		Thread.sleep(MULT * 3 / 2 * TIME_UNIT_MS);
		assertEquals(1, m_connection.m_written.size());
		assertEquals(0, tcl.m_closed.size());
		
		m_connection.m_cd.dispatch(new DispatcherOp<CloseableListener>() {
			@Override
			public void dispatch(CloseableListener l) {
				l.closed(new IOException("foo"));
			}
		});
		
		Thread.sleep(TIME_UNIT_MS);
		
		assertEquals(1, tcl.m_closed.size());
		assertEquals("foo", tcl.m_closed.get(0).getMessage());
		
		Thread.sleep(MULT * TIME_UNIT_MS);
		assertEquals(1, m_connection.m_written.size());
	}
	
	@Test
	public void dispatches_types_which_are_not_commands() throws Exception {
		TestArraySaveQueue asq = new TestArraySaveQueue();
		m_controlled.queue_group().add(asq);
		
		m_controlled.start();
		
		final Int32Value iv = m_pscope.int32().make(40);
		final StringValue sv = m_pscope.string().make("foo");
		final StringValue psv = m_pscope.string().make(
				ControlledDataTypeSocketConnectionImpl.CMD_PREFIX
				+ ControlledDataTypeSocketConnectionImpl.CMD_NOSEND);
		
		m_connection.add_to_queue(new BusData(iv));
		m_connection.add_to_queue(new BusData(sv));
		m_connection.add_to_queue(new BusData(psv));
		
		Thread.sleep(TIME_UNIT_MS);
		
		m_controlled.stop();
		
		assertEquals(2, asq.m_values.size());
		assertEquals(iv, asq.m_values.get(0));
		assertEquals(sv, asq.m_values.get(1));
	}
	
	@Test
	public void ignores_unknown_commands() throws Exception {
		TestArraySaveQueue asq = new TestArraySaveQueue();
		m_controlled.queue_group().add(asq);
		
		m_controlled.start();
		
		final StringValue cmd = m_pscope.string().make(
				ControlledDataTypeSocketConnectionImpl.CMD_PREFIX + "foo");
		
		m_connection.add_to_queue(new BusData(cmd));
		Thread.sleep(TIME_UNIT_MS);
		
		m_controlled.stop();
		
		assertEquals(0, asq.m_values.size());
	}
}
