package edu.cmu.cs.able.eseb;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import auxtestlib.AbstractControlledThread;
import auxtestlib.DefaultTCase;
import auxtestlib.TestPropertiesDefinition;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.txtenc.typelib.DefaultTextEncoding;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Sends and receives data types through socket connections.
 */
@SuppressWarnings("javadoc")
public class SocketTest extends DefaultTCase {
	private PrimitiveScope m_scope;
	
	@Before
	public void set_up() {
		m_scope = new PrimitiveScope();
	}
	
	@Test
	public void send_receive_sockets() throws Exception {
		int port = TestPropertiesDefinition.getInt("free-port-zone-start");
		@SuppressWarnings("resource")
		final ServerSocket ss = new ServerSocket(port);
		AbstractControlledThread act = new AbstractControlledThread() {
			@Override
			public Object myRun() throws Exception {
				return ss.accept();
			}
		};
		
		act.start();
		
		@SuppressWarnings("resource")
		Socket cli_side = new Socket("localhost", port);
		act.waitForEnd();
		@SuppressWarnings("resource")
		Socket srv_side = (Socket) act.getResult();
		
		ss.close();
		
		@SuppressWarnings("resource")
		DataTypeSocketConnectionImpl cli_conn =
				new DataTypeSocketConnectionImpl("cli", cli_side,
				new DefaultTextEncoding(), m_scope);
		@SuppressWarnings("resource")
		DataTypeSocketConnectionImpl srv_conn =
				new DataTypeSocketConnectionImpl("srv", srv_side,
				new DefaultTextEncoding(), m_scope);
		
		cli_conn.thread_group().start();
		srv_conn.thread_group().start();
		
		TestArraySaveQueue cli_recv = new TestArraySaveQueue();
		cli_conn.queue_group().add(cli_recv);
		
		TestCloseableListener cli_close = new TestCloseableListener();
		cli_conn.closeable_dispatcher().add(cli_close);
		
		TestArraySaveQueue srv_recv = new TestArraySaveQueue();
		srv_conn.queue_group().add(srv_recv);
		
		TestCloseableListener srv_close = new TestCloseableListener();
		srv_conn.closeable_dispatcher().add(srv_close);
		
		List<DataValue> send_at_cli = new ArrayList<>();
		List<DataValue> send_at_srv = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			send_at_cli.add(m_scope.string().make(
					RandomStringUtils.randomAlphabetic(450)));
			cli_conn.write(send_at_cli.get(i));
			send_at_srv.add(m_scope.string().make(
					RandomStringUtils.randomAlphabetic(450)));
			srv_conn.write(send_at_srv.get(i));
		}
		
		Thread.sleep(500);
		
		assertEquals(send_at_cli.size(), srv_recv.m_values.size());
		for (int i = 0; i < send_at_cli.size(); i++) {
			assertEquals(send_at_cli.get(i), srv_recv.m_values.get(i));
		}
		
		assertEquals(send_at_srv.size(), cli_recv.m_values.size());
		for (int i = 0; i < send_at_srv.size(); i++) {
			assertEquals(send_at_srv.get(i), cli_recv.m_values.get(i));
		}
		
		cli_conn.close();
		
		Thread.sleep(250);
		assertEquals(1, cli_close.m_closed.size());
		assertNull(cli_close.m_closed.get(0));
		
		srv_conn.close();
		assertEquals(1, srv_close.m_closed.size());
		assertNotNull(srv_close.m_closed.get(0));
		
		cli_conn.thread_group().stop();
		srv_conn.thread_group().stop();
	}
}
