package edu.cmu.cs.able.parsec;

import edu.cmu.cs.able.parsec.parser.ParseException;

/**
 * Interface with the actual parser callbacks. This is a public interface
 * because it needs to be used between different <code>parsec</code>
 * packages and is of no use to <code>parsec</code> users.
 */
public interface ParsecParserListener {
	/**
	 * A statement has been recognized.
	 * @param text the statement text without the trailing semicolon
	 * @param loc the location where the text was recognized
	 * @throws ParseException failed to parse the statement
	 */
	void statement_recognized(String text, LCCoord loc) throws ParseException;
	
	/**
	 * A block has been recognized.
	 * @param block_header text before block starts which may span more than
	 * one line;
	 * @param block_text block text itself
	 * @param loc the location where the text was recognized
	 * @throws ParseException failed to parse the statement
	 */
	void block_recognized(String block_header, String block_text, LCCoord loc)
			throws ParseException;
}
