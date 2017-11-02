package edu.cmu.cs.able.typelib.parser;

import org.junit.Before;
import org.junit.Test;

import auxtestlib.DefaultTCase;
import edu.cmu.cs.able.parsec.ParsecFileReader;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.scope.HierarchicalName;
import edu.cmu.cs.able.typelib.struct.Field;
import edu.cmu.cs.able.typelib.struct.StructureDataType;
import edu.cmu.cs.able.typelib.type.DataType;

/**
 * Test case for the default typelib parser test.
 */
@SuppressWarnings("javadoc")
public class DefaultTypelibParserTest extends DefaultTCase {
	/**
	 * The parser.
	 */
	private DefaultTypelibParser m_parser;
	
	/**
	 * The primitive type scope.
	 */
	private PrimitiveScope m_pscope;
	
	/**
	 * The parsing context.
	 */
	private TypelibParsingContext m_pctx;
	
	@Before
	public void set_up() throws Exception {
		m_parser = DefaultTypelibParser.make();
		m_pscope = new PrimitiveScope();
		m_pctx = new TypelibParsingContext(m_pscope, m_pscope);
	}
	
	@Test
	public void parse_structure() throws Exception {
		m_parser.parse(new ParsecFileReader().read_memory("struct x{}"),
				m_pctx);
		DataType dt = m_pscope.find("x");
		assertNotNull(dt);
		assertTrue(dt instanceof StructureDataType);
	}
	
	@Test
	public void parse_namespace() throws Exception {
		m_parser.parse(new ParsecFileReader().read_memory("namespace foo{}"),
				m_pctx);
		assertNotNull(m_pscope.find_scope("foo"));
	}
	
	@Test
	public void find_types_in_super_namespaces() throws Exception {
		m_parser.parse(new ParsecFileReader().read_memory("struct x{} "
				+ "namespace a{struct y{x f;}}"), m_pctx);
		StructureDataType x = (StructureDataType) m_pscope.find("x");
		assertNotNull(x);
		StructureDataType y = (StructureDataType) m_pscope.find(
				new HierarchicalName(true, "a", "y"));
		assertNotNull(y);
		Field f = y.field("f");
		assertNotNull(f);
		assertSame(x, f.description().type());
	}
	
	@Test
	public void find_types_in_super_super_namespaces() throws Exception {
		m_parser.parse(new ParsecFileReader().read_memory("struct x{} "
				+ "namespace a{namespace b{struct y{x f;}}}"), m_pctx);
		StructureDataType x = (StructureDataType) m_pscope.find("x");
		assertNotNull(x);
		StructureDataType y = (StructureDataType) m_pscope.find(
				new HierarchicalName(true, "a", "b", "y"));
		assertNotNull(y);
		Field f = y.field("f");
		assertNotNull(f);
		assertSame(x, f.description().type());
	}
}
