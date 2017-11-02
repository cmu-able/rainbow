package edu.cmu.cs.able.typelib.parser;

import java.io.StringReader;

import edu.cmu.cs.able.parsec.BlockHeaderParseException;
import edu.cmu.cs.able.parsec.BlockTextParseException;
import edu.cmu.cs.able.parsec.DelegateParser;
import edu.cmu.cs.able.parsec.LCCoord;
import edu.cmu.cs.able.parsec.LocalizedParseException;
import edu.cmu.cs.able.typelib.struct.FieldDescription;
import edu.cmu.cs.able.typelib.type.DataType;

/**
 * Delegation parser that parses the contents of a structure.
 */
public class StructureDelParser
		implements DelegateParser<StructureParsingContext> {
	/**
	 * Creates a new parser.
	 */
	public StructureDelParser() {
	}

	@Override
	public void parse_statement(String statement, StructureParsingContext ctx)
			throws LocalizedParseException {
		StructureJjParser parser = new StructureJjParser(new StringReader(
				statement));
		FieldDeclaration fd;
		try {
			fd = parser.Field();
		} catch (ParseException e) {
			throw new LocalizedParseException(e.getMessage(), new LCCoord(
					e.currentToken.beginLine, e.currentToken.beginColumn));
		}
		
		DataTypeNameParser dtnp = new DataTypeNameParser();
		DataType type;
		
		try {
			type = dtnp.parse(fd.type_name(),
					ctx.typelib_ctx().primitive_scope(),
					ctx.typelib_ctx().scope());
		} catch (ParseException e) {
			throw new LocalizedParseException(e.getMessage(),
					new LCCoord(1, 1));
		}
		
		if (type == null) {
			throw new LocalizedParseException("Cannot find type '"
					+ fd.type_name() + "'.", new LCCoord(1, 1));
		}
		
		ctx.structure_declaration().add(new FieldDescription(fd.name(), type));
	}

	@Override
	public void parse_block(String block_header, String block_text,
			StructureParsingContext ctx) throws BlockHeaderParseException,
			BlockTextParseException {
		throw new BlockTextParseException(new LocalizedParseException(
				"Unexpected block.", new LCCoord(1, 1)));
	}
}
