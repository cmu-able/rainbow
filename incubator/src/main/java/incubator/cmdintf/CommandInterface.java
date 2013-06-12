package incubator.cmdintf;

/**
 * Interface provided by clients that connect to a command manager.
 */
public interface CommandInterface {
	/**
	 * Writes text to the client.
	 * 
	 * @param text the text to write
	 */
	void write(String text);

	/**
	 * Writes text and a line terminator to the client.
	 * 
	 * @param text the text to write
	 */
	void writeLine(String text);

	/**
	 * Writes a line terminator to the client.
	 */
	void writeLine();

	/**
	 * Reads a line of text from the client.
	 * 
	 * @return the text read
	 */
	String readLine();

	/**
	 * Determines if this interface hasn't yet been closed.
	 * 
	 * @return is it alive? (<code>true</code> means it hasn't been closed)
	 */
	boolean isAlive();

	/**
	 * Closes this interface.
	 */
	void close();
}
