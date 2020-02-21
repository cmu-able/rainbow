package incubator.scb.filter;

import incubator.pval.Ensure;
import incubator.scb.Scb;
import incubator.scb.ScbField;
import incubator.scb.filter.parser.ParseException;
import incubator.scb.filter.parser.ScbFilterJjParser;
import incubator.scb.filter.parser.TokenMgrError;

import java.io.StringReader;
import java.util.Collection;

/**
 * Parser that constructs an {@link ScbFilter} from a text description.
 * @param <T> the type of SCB
 */
public class ScbFilterParser<T extends Scb<T>> {
	/**
	 * The SCB's fields.
	 */
	private Collection<ScbField<T, ?>> m_fields;
	
	/**
	 * Creates a new parser.
	 * @param fields the fields of the SCB that can be used for parsing
	 */
	public ScbFilterParser(Collection<ScbField<T, ?>> fields) {
		Ensure.not_null(fields, "fields == null");
		m_fields = fields;
	}
	
	/**
	 * Parses a text with a filter.
	 * @param text the text to parse
	 * @return the filter represented by the text
	 * @throws FilterParserException failed to parse the text
	 */
	public ScbFilter<T> parse_filter(String text) throws FilterParserException {
		Ensure.not_null(text, "text == null");
		
		if (text.trim().length() == 0) {
			return new ScbTrueFilter<>();
		}
		
		ScbFilterJjParser<T> parser = new ScbFilterJjParser<>(
				new StringReader(text));
		try {
			return parser.Filter(m_fields);
		} catch (ParseException|TokenMgrError e) {
			throw new FilterParserException(e);
		}
	}
}
