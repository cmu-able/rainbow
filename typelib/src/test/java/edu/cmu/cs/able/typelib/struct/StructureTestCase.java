package edu.cmu.cs.able.typelib.struct;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;

import edu.cmu.cs.able.parsec.Parsec;
import edu.cmu.cs.able.parsec.ParsecFileReader;
import edu.cmu.cs.able.typelib.parser.StructureDelParser;
import edu.cmu.cs.able.typelib.parser.StructureParsingContext;
import edu.cmu.cs.able.typelib.parser.TypelibDelParser;
import edu.cmu.cs.able.typelib.parser.TypelibParsingContext;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.type.DataValue;
import auxtestlib.DefaultTCase;
import auxtestlib.TemporaryFile;

/**
 * Abstract test case for structures.
 */
@SuppressWarnings("javadoc")
public class StructureTestCase extends DefaultTCase {
	protected TemporaryFile m_tf;
	protected Parsec<StructureParsingContext> m_structure_parsec;
	protected Parsec<TypelibParsingContext> m_general_parsec;
	protected PrimitiveScope m_pscope;
	protected TypelibParsingContext m_typelib_ctx;
	protected ParsecFileReader m_reader;
	
	@Before
	public void set_up() throws Exception {
		m_pscope = new PrimitiveScope();
		m_tf = new TemporaryFile(false);
		m_typelib_ctx = new TypelibParsingContext(m_pscope, m_pscope);
		m_structure_parsec = new Parsec<>();
		m_structure_parsec.add(new StructureDelParser());
		m_general_parsec = new Parsec<>();
		m_general_parsec.add(new TypelibDelParser(m_general_parsec,
				m_structure_parsec));
		m_reader = new ParsecFileReader();
	}

	protected void parse_declaration(String code)
			throws Exception {
		m_general_parsec.parse(m_reader.read_memory(code), m_typelib_ctx);
	}

	protected StructureDataType parse_declaration(String name, String code)
			throws Exception {
		parse_declaration(code);
		StructureDataType sdt = (StructureDataType) m_pscope.find(name);
		assertNotNull(sdt);
		return sdt;
	}
	
	protected StructureDataValue make(StructureDataType dt, Object...fv)
			throws Exception {
		Map<Field, DataValue> v = new HashMap<>();
		for (int i = 0; i < fv.length; i++) {
			String fname = (String) fv[i];
			i++;
			DataValue val = (DataValue) fv[i];
			v.put(dt.field(fname), val);
		}
		
		return dt.make(v);
	}
}
