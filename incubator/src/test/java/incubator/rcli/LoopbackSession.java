package incubator.rcli;

import java.util.LinkedList;
import java.util.Queue;

import org.junit.Assert;

/**
 * Class that creates a client session and a server session that send data to
 * each other directly.
 */
public class LoopbackSession {
	/**
	 * Data sent by the client, pending server read.
	 */
	private Queue<String> m_pending_cs;
	
	/**
	 * Data sent by the server, pending client read.
	 */
	private Queue<String> m_pending_sc;
	
	/**
	 * The client-side session.
	 */
	private Session m_client;
	
	/**
	 * The server-side session.
	 */
	private Session m_server;
	
	/**
	 * Creates a new loopback session.
	 */
	public LoopbackSession() {
		m_pending_cs = new LinkedList<>();
		m_pending_sc = new LinkedList<>();
		m_client = new Session() {
			@Override
			public String sid() {
				Assert.fail();
				return null;
			}

			@Override
			public void output(String text) {
				Assert.assertNotNull(text);
				m_pending_cs.add(text);
			}

			@Override
			public String input() {
				Assert.assertTrue(m_pending_sc.size() > 0);
				return m_pending_sc.poll();
			}

			@Override
			public void close_session() {
				Assert.fail();
			}
		};
		
		m_server = new Session() {
			@Override
			public String sid() {
				Assert.fail();
				return null;
			}

			@Override
			public void output(String text) {
				Assert.assertNotNull(text);
				m_pending_sc.add(text);
			}

			@Override
			public String input() {
				Assert.assertTrue(m_pending_cs.size() > 0);
				return m_pending_cs.poll();
			}

			@Override
			public void close_session() {
				Assert.fail();
			}
		};
	}
	
	/**
	 * Obtains the client-side session.
	 * @return the session
	 */
	public Session client_session() {
		return m_client;
	}
	
	/**
	 * Obtains the server-side session.
	 * @return the session
	 */ 
	public Session server_session() {
		return m_server;
	}
	
	/**
	 * Invoked when finished processing to ensure all sent data was read.
	 */
	public void done() {
		Assert.assertEquals(m_pending_cs.size(), 0);
		Assert.assertEquals(m_pending_sc.size(), 0);
	}
}
