package incubator.rcli;

import java.io.IOException;

/**
 * Interface of a session as seen by a command.
 */
public interface Session {
	/**
	 * Obtains the session ID (each session has a unique identifier).
	 * @return the ID
	 */
	public String sid();
	
	/**
	 * Writes a line of text to the session. A newline character will be
	 * added to the text.
	 * @param text the text to write
	 * @throws IOException failed to write; the command should exit ASAP
	 */
	public void output(String text) throws IOException;
	
	/**
	 * Reads a line of text. The trailing newline is removed from the input.
	 * This method will block until input is available.
	 * @return the line to read
	 * @throws IOException failed to read the line
	 */
	public String input() throws IOException;
	
	/**
	 * Forces the session to close. If invoked during command execution,
	 * the session will wait for the command to finish and will close
	 * afterwards.
	 */
	public void close_session();
}
