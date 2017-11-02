package edu.cmu.cs.able.typelib.parser;

import java.io.StringReader;

import edu.cmu.cs.able.parsec.BlockHeaderParseException;
import edu.cmu.cs.able.parsec.BlockTextParseException;
import edu.cmu.cs.able.parsec.DelegateParser;
import edu.cmu.cs.able.parsec.LCCoord;
import edu.cmu.cs.able.parsec.LocalizedParseException;

/**
 * Delegation parser that parses the contents of an enumeration.
 */
public class EnumerationDelParser
		implements DelegateParser<EnumerationParsingContext> {
	/**
	 * Creates a new parser.
	 */
	public EnumerationDelParser() {
	}

	@Override
	public void parse_statement(String statement, EnumerationParsingContext ctx)
			throws LocalizedParseException {
		EnumerationJjParser parser = new EnumerationJjParser(new StringReader(
				statement));
		String name;
		try {
			name = parser.EnumerationValueName();
		} catch (ParseException e) {
			throw new LocalizedParseException(e.getMessage(), new LCCoord(
					e.currentToken.beginLine, e.currentToken.beginColumn));
		}
		
		ctx.enumeration_declaration().add(name);
	}

	@Override
	public void parse_block(String block_header, String block_text,
			EnumerationParsingContext ctx) throws BlockHeaderParseException,
			BlockTextParseException {
		throw new BlockTextParseException(new LocalizedParseException(
				"Unexpected block.", new LCCoord(1, 1)));
	}
}
