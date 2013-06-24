package incubator.rcli;

import incubator.ExceptionSuppress;
import incubator.pval.Ensure;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.SocketTimeoutException;

/**
 * Implementation of a session.
 */
class SessionImpl implements Session, Closeable {
	/**
	 * The session ID.
	 */
	private String m_sid;
	
	/**
	 * The reader.
	 */
	private Reader m_reader;
	
	/**
	 * Buffered input reader.
	 */
	private BufferedReader m_b_reader;
	
	/**
	 * The writer.
	 */
	private Writer m_writer;
	
	/**
	 * Are we waiting for the session to close?
	 */
	private boolean m_closing;
	
	/**
	 * Creates a new session.
	 * @param sid the session ID
	 * @param r the reader where to read data from
	 * @param w the writer where to write data to
	 */
	SessionImpl(String sid, Reader r, Writer w) {
		Ensure.not_null(sid);
		Ensure.not_null(r);
		Ensure.not_null(w);
		
		m_sid = sid;
		m_reader = r;
		m_b_reader = new BufferedReader(r);
		m_writer = w;
		m_closing = false;
	}
	

	@Override
	public String sid() {
		return m_sid;
	}

	@Override
	public void output(String text) throws IOException {
		try {
			m_writer.write(text);
			m_writer.write('\n');
		} catch (IOException e) {
			throw e;
		}
	}

	@Override
	public String input() throws IOException {
		String i;
		do {
			i = maybe_input();
		} while (i == null);
		
		return i;
	}
	
	/**
	 * Reads a line from the input.
	 * @return the line read
	 * @throws IOException failed to read the line
	 * @throws EOFException end of file reached
	 */
	String maybe_input() throws IOException {
		IOException ioe = null;
		String r = null;
		try {
			r = m_b_reader.readLine();
		} catch (IOException e) {
			ioe = e;
		}
		
		if (ioe instanceof SocketTimeoutException) {
			return null;
		}
		
		if (ioe != null) {
			throw ioe;
		}
		
		return r;
	}

	@Override
	public void close_session() {
		m_closing = true;
	}
	
	@Override
	public void close() throws IOException {
		m_closing = true;
		
		ExceptionSuppress<IOException> esup = new ExceptionSuppress<>();
		try {
			m_reader.close();
		} catch (IOException e) {
			esup.add(e);
		}
		
		try {
			m_writer.close();
		} catch (IOException e) {
			esup.add(e);
		}
		
		esup.maybe_throw();
	}
	
	/**
	 * Checks whether {@link #close_session()} was invoked.
	 * @return was invoked?
	 */
	boolean closing() {
		return m_closing;
	}
}
