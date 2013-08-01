package edu.cmu.cs.able.typelib.enumeration;

import java.util.Arrays;

import org.junit.Test;

import edu.cmu.cs.able.parsec.ParsecFileReader;
import edu.cmu.cs.able.parsec.TextContainer;
import edu.cmu.cs.able.typelib.parser.DefaultTypelibParser;
import edu.cmu.cs.able.typelib.parser.TypelibParsingContext;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.scope.HierarchicalName;
import auxtestlib.DefaultTCase;

/**
 * Parses enumerations using the typelib parser.
 */
@SuppressWarnings("javadoc")
public class EnumerationParseTest extends DefaultTCase {
	@Test
	public void parse_simple_enumeration() throws Exception {
		String text = "namespace foo { enum bar { a; } }";
		TextContainer txt_cont = new ParsecFileReader().read_memory(text);
		PrimitiveScope pscope = new PrimitiveScope();
		TypelibParsingContext pctx = new TypelibParsingContext(pscope, pscope);
		DefaultTypelibParser p = DefaultTypelibParser.make();
		p.parse(txt_cont, pctx);
		
		HierarchicalName hn = new HierarchicalName(true, Arrays.asList("foo",
				"bar"));
		EnumerationType found = (EnumerationType) pscope.find(hn);
		assertNotNull(found);
		assertEquals("bar", found.name());
		assertTrue(found.has_value("a"));
	}
}
