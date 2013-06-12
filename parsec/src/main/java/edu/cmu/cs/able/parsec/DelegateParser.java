package edu.cmu.cs.able.parsec;


/**
 * Interface of a delegate parser. This interface should be implemented by
 * parsers that are invoked by {@link Parsec} to parse either statements or
 * blocks.
 * @param <T> the type of information passed to the delegate by the parser to
 * establish the parsing context
 */
public interface DelegateParser<T> {
	/**
	 * Invoked to parse a statement. The trailing semicolon has been removed
	 * from the statement text.
	 * @param statement statement text which may span more than one line;
	 * any leading or trailing whitespace has also been removed
	 * @param context the parsing context as provided to
	 * {@link Parsec#parse(TextContainer, Object)}
	 * @throws LocalizedParseException thrown if parsing fails; the location
	 * provided in the exception should be relative to this statement
	 * (meaning line 1, column 1 is the first character in
	 * <code>statement</code>)
	 */
	void parse_statement(String statement, T context)
			throws LocalizedParseException;
	
	/**
	 * Invoked to parse a block. The block is separated into the header (the
	 * part before the open brace) and the block text (the part between the
	 * braces). The braces have been removed.
	 * @param block_header text before block starts which may span more than
	 * one line; white space has already been trimmed
	 * @param block_text block text itself; white space has already been
	 * trimmed
	 * @param context the parsing context as provided to
	 * {@link Parsec#parse(TextContainer, Object)}
	 * @throws BlockHeaderParseException thrown if parsing of the block
	 * header fails; the location provided in the cause exception should be
	 * relative to this block header (meaning line 1, column 1 is the first
	 * character in <code>block_header</code>)
	 * @throws BlockTextParseException thrown if parsing of the block
	 * text fails; the location provided in the cause exception should be
	 * relative to this block text (meaning line 1, column 1 is the first
	 * character in <code>block_text</code>)
	 */
	void parse_block(String block_header, String block_text, T context)
			throws BlockHeaderParseException, BlockTextParseException;
}
