package incubator.rcli;

import incubator.pval.Ensure;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;

/**
 * Class that implements a command processor: receives requests from users
 * and invokes the commands.
 */
public class CommandProcessor implements Closeable {
	/**
	 * Size of a session identifier.
	 */
	private static final int SESSION_ID_SIZE = 5;
	
	/**
	 * Maps command names to commands.
	 */
	private Map<String, Command> m_commands;
	
	/**
	 * Maps session IDs to sessions.
	 */
	private Map<String, SessionThread> m_sessions;
	
	/**
	 * Creates a new processor.
	 */
	public CommandProcessor() {
		m_commands = new HashMap<>();
		m_sessions = new HashMap<>();
	}
	
	/**
	 * Adds a command with the given name.
	 * @param name the command name
	 * @param cmd the command
	 */
	public synchronized void add(String name, Command cmd) {
		Ensure.not_null(name);
		Ensure.not_null(cmd);
		Ensure.is_false(m_commands.containsKey(name));
	}
	
	/**
	 * Adds an abstract command (which has a name).
	 * @param cmd the command
	 */
	public synchronized void add(AbstractCommand cmd) {
		Ensure.not_null(cmd);
		add(cmd.name(), cmd);
	}
	
	/**
	 * Starts a new interactive session.
	 * @param r the reader where commands are read from
	 * @param w the writer where command outputs are written to
	 */
	public synchronized void start_session(Reader r, Writer w) {
		Ensure.not_null(r);
		Ensure.not_null(w);
		
		String sid;
		do {
			sid = RandomStringUtils.randomAlphanumeric(SESSION_ID_SIZE);
		} while (m_sessions.containsKey(sid));
		
		@SuppressWarnings("resource")
		SessionImpl si = new SessionImpl(sid, r, w);
		
		SessionThread st = new SessionThread(si, this);
		m_sessions.put(sid, st);
		
		st.start();
	}

	@Override
	public void close() throws IOException {
		boolean empty;
		do {
			empty = true;
			Map<String, SessionThread> threads;
			synchronized (this) {
				threads = new HashMap<>(m_sessions);
			}
			
			if (threads.size() > 0) {
				empty = false;
			}
			
			for (SessionThread st : threads.values()) {
				st.session().close();
				st.stop();
			}
		} while (!empty);
	}
}
