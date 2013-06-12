package edu.cmu.cs.able.parsec;

import edu.cmu.cs.able.parsec.parser.ParseException;

/**
 * Listener of the parser which is invoked from {@link Parsec}. See
 * {@link Parsec#parse_i(TextContainer, ParsecParserPostListener)}.
 * This class is used to decouple delegate invocation from the parser
 * recognition.
 */
abstract class ParsecParserPostListener {
	/**
	 * A statement has been recognized.
	 * @param text the text of the statement
	 * @param loc the match location
	 * @throws ParseException parsing of the statement failed
	 */
	abstract void statement_recognized(String text, TextRegionMatch loc)
			throws ParseException;
	
	/**
	 * A block has been recognized.
	 * @param block_header the text of the header of the block
	 * @param h_loc the match location of the header of the block
	 * @param block_text the text of the block
	 * @param t_loc the match location of the text of the block
	 * @throws ParseException parsing of the block failed
	 */
	abstract void block_recognized(String block_header, TextRegionMatch h_loc,
			String block_text, TextRegionMatch t_loc) throws ParseException;
}
