package incubator.scb.sdl;

import java.io.StringReader;

import incubator.pval.Ensure;
import incubator.scb.sdl.parser.ParseException;
import incubator.scb.sdl.parser.SdlJjParser;
import incubator.scb.sdl.parser.TokenMgrError;

/**
 * Class that parses an SDL description and processes the java code.
 */
public class SdlParser {
	/**
	 * Registry for generators.
	 */
	private GeneratorRegistry m_registry;
	
	/**
	 * Creates a new parser.
	 * @param reg the generator registry
	 */
	public SdlParser(GeneratorRegistry reg) {
		Ensure.not_null(reg, "reg == null");
		m_registry = reg;
	}
	
	/**
	 * Parses an SDL description.
	 * @param sdl the SDL to parse
	 * @return the parsed definition
	 * @throws SdlParsingException failed to parse the SDL text
	 */
	public SdlDefinition parse(String sdl) throws SdlParsingException {
		Ensure.not_null(sdl, "sdl == null");
		SdlJjParser parser = new SdlJjParser(new StringReader(sdl));
		
		try {
			return parser.SDL(m_registry);
		} catch (ParseException|TokenMgrError e) {
			throw new SdlParsingException("Failed to parse SDL text.", e);
		}
	}
}
