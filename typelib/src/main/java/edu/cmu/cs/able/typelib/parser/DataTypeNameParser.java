package edu.cmu.cs.able.typelib.parser;

import incubator.pval.Ensure;

import java.io.StringReader;

import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataTypeScope;

/**
 * Class that is able to parse a data type name from a string. Parsing is
 * done in the context of a data type scope. If the type is a composite type
 * based on another type, such as an optional data type, then the composite
 * data type will be created if needed.
 */
public class DataTypeNameParser {
	/**
	 * Creates a new parser.
	 */
	public DataTypeNameParser() {
	}
	
	/**
	 * Parses text and returns the data type it refers to.
	 * @param text the text to parse
	 * @param pscope the primitive scope
	 * @param scope the scope used to find data types
	 * @return the data type or <code>null</code> if the data type was
	 * not found and could not be created
	 * @throws ParseException parsing of the text has failed
	 */
	public DataType parse(String text, PrimitiveScope pscope,
			DataTypeScope scope) throws ParseException {
		Ensure.not_null(text);
		Ensure.not_null(pscope);
		Ensure.not_null(scope);
		
		TypeNameJjParser p = new TypeNameJjParser(new StringReader(text));
		DataTypeName dtn;
		dtn = p.TypeName();
		
		return dtn.find_in_scope(scope, pscope);
	}
}
