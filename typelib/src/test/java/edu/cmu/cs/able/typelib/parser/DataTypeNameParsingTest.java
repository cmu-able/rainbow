package edu.cmu.cs.able.typelib.parser;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import edu.cmu.cs.able.typelib.TestDataType;
import edu.cmu.cs.able.typelib.comp.BagDataType;
import edu.cmu.cs.able.typelib.comp.ListDataType;
import edu.cmu.cs.able.typelib.comp.MapDataType;
import edu.cmu.cs.able.typelib.comp.OptionalDataType;
import edu.cmu.cs.able.typelib.comp.SetDataType;
import edu.cmu.cs.able.typelib.comp.TupleDataType;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataTypeScope;
import auxtestlib.DefaultTCase;

/**
 * Tests parsing data type names.
 */
@SuppressWarnings("javadoc")
public class DataTypeNameParsingTest extends DefaultTCase {
	private DataTypeNameParser m_parser;
	private PrimitiveScope m_pscope;
	private DataTypeScope m_sub_scope;
	private TestDataType m_xx_in_pscope;
	private TestDataType m_xx_in_sub_scope;
	
	@Before
	public void set_up() throws Exception {
		m_parser = new DataTypeNameParser();
		m_pscope = new PrimitiveScope();
		m_sub_scope = new DataTypeScope("sub");
		m_pscope.add(m_sub_scope);
		m_xx_in_pscope = new TestDataType("xx");
		m_pscope.add(m_xx_in_pscope);
		m_xx_in_sub_scope = new TestDataType("xx");
		m_sub_scope.add(m_xx_in_sub_scope);
	}
	
	@Test
	public void parse_primitive_type() throws Exception {
		DataType dt = m_parser.parse("int32", m_pscope, m_pscope);
		assertEquals(m_pscope.int32(), dt);
	}
	
	@Test
	public void parse_qualified_primitive_type() throws Exception {
		DataType dt = m_parser.parse("::string", m_pscope, m_pscope);
		assertEquals(m_pscope.string(), dt);
	}
	
	@Test
	public void parse_data_type_in_pscope_with_no_qualification()
			throws Exception {
		DataType dt = m_parser.parse("xx", m_pscope, m_pscope);
		assertEquals(m_xx_in_pscope, dt);
	}
	
	@Test
	public void parse_data_type_in_sub_scope_with_no_qualification()
			throws Exception {
		DataType dt = m_parser.parse("xx", m_pscope, m_sub_scope);
		assertEquals(m_xx_in_sub_scope, dt);
	}
	
	@Test
	public void parse_data_type_in_sub_scope_with_qualification()
			throws Exception {
		DataType dt = m_parser.parse("::xx", m_pscope, m_sub_scope);
		assertEquals(m_xx_in_pscope, dt);
	}
	
	@Test
	public void parse_data_type_in_pscope_with_qualification()
			throws Exception {
		DataType dt = m_parser.parse("::sub::xx", m_pscope, m_pscope);
		assertEquals(m_xx_in_sub_scope, dt);
	}
	
	@Test
	public void parse_optional_data_type() throws Exception {
		DataType dt = m_parser.parse("xx?", m_pscope, m_pscope);
		assertTrue(dt instanceof OptionalDataType);
		dt = ((OptionalDataType) dt).inner_type();
		assertEquals(m_xx_in_pscope, dt);
	}
	
	@Test
	public void parse_double_optional_data_type() throws Exception {
		DataType dt = m_parser.parse("xx??", m_pscope, m_pscope);
		assertTrue(dt instanceof OptionalDataType);
		dt = ((OptionalDataType) dt).inner_type();
		assertTrue(dt instanceof OptionalDataType);
		dt = ((OptionalDataType) dt).inner_type();
		assertEquals(m_xx_in_pscope, dt);
	}
	
	@Test
	public void parse_optional_data_type_in_sub_scope() throws Exception {
		DataType dt = m_parser.parse("sub::xx?", m_pscope, m_pscope);
		assertTrue(dt instanceof OptionalDataType);
		assertEquals(m_sub_scope, dt.parent_dts());
		dt = ((OptionalDataType) dt).inner_type();
		assertEquals(m_xx_in_sub_scope, dt);
	}
	
	@Test
	public void parse_set_data_type_in_pscope() throws Exception {
		DataType dt = m_parser.parse("set<xx>", m_pscope, m_pscope);
		assertTrue(dt instanceof SetDataType);
		assertEquals(m_pscope, dt.parent_dts());
		dt = ((SetDataType) dt).inner_type();
		assertEquals(m_xx_in_pscope, dt);
	}
	
	@Test
	public void parse_set_data_type_in_qualified_sub_scope()
			throws Exception {
		DataType dt = m_parser.parse("set<::sub::xx>", m_pscope, m_pscope);
		assertTrue(dt instanceof SetDataType);
		assertEquals(m_sub_scope, dt.parent_dts());
		dt = ((SetDataType) dt).inner_type();
		assertEquals(m_xx_in_sub_scope, dt);
	}
	
	@Test
	public void parse_optional_set_data_type() throws Exception {
		DataType dt = m_parser.parse("set<bool>?", m_pscope, m_pscope);
		assertTrue(dt instanceof OptionalDataType);
		assertEquals(m_pscope, dt.parent_dts());
		dt = ((OptionalDataType) dt).inner_type();
		assertTrue(dt instanceof SetDataType);
		assertEquals(m_pscope, dt.parent_dts());
		dt = ((SetDataType) dt).inner_type();
		assertEquals(m_pscope.bool(), dt);
	}
	
	@Test
	public void parse_set_optional_data_type() throws Exception {
		DataType dt = m_parser.parse("set<bool?>", m_pscope, m_pscope);
		assertTrue(dt instanceof SetDataType);
		assertEquals(m_pscope, dt.parent_dts());
		dt = ((SetDataType) dt).inner_type();
		assertTrue(dt instanceof OptionalDataType);
		assertEquals(m_pscope, dt.parent_dts());
		dt = ((OptionalDataType) dt).inner_type();
		assertEquals(m_pscope.bool(), dt);
	}
	
	@Test
	public void parse_set_set_data_type() throws Exception {
		DataType dt = m_parser.parse("set<set<sub::xx>>", m_pscope, m_pscope);
		assertTrue(dt instanceof SetDataType);
		assertEquals(m_sub_scope, dt.parent_dts());
		dt = ((SetDataType) dt).inner_type();
		assertTrue(dt instanceof SetDataType);
		assertEquals(m_sub_scope, dt.parent_dts());
		dt = ((SetDataType) dt).inner_type();
		assertEquals(m_xx_in_sub_scope, dt);
	}
	
	@Test
	public void parse_list_data_type_in_pscope() throws Exception {
		DataType dt = m_parser.parse("list<xx>", m_pscope, m_pscope);
		assertTrue(dt instanceof ListDataType);
		assertEquals(m_pscope, dt.parent_dts());
		dt = ((ListDataType) dt).inner_type();
		assertEquals(m_xx_in_pscope, dt);
	}
	
	@Test
	public void parse_list_data_type_in_qualified_sub_scope()
			throws Exception {
		DataType dt = m_parser.parse("list<::sub::xx>", m_pscope, m_pscope);
		assertTrue(dt instanceof ListDataType);
		assertEquals(m_sub_scope, dt.parent_dts());
		dt = ((ListDataType) dt).inner_type();
		assertEquals(m_xx_in_sub_scope, dt);
	}
	
	@Test
	public void parse_bag_data_type_in_pscope() throws Exception {
		DataType dt = m_parser.parse("bag<xx>", m_pscope, m_pscope);
		assertTrue(dt instanceof BagDataType);
		assertEquals(m_pscope, dt.parent_dts());
		dt = ((BagDataType) dt).inner_type();
		assertEquals(m_xx_in_pscope, dt);
	}
	
	@Test
	public void parse_bag_data_type_in_qualified_sub_scope()
			throws Exception {
		DataType dt = m_parser.parse("bag<::sub::xx>", m_pscope, m_pscope);
		assertTrue(dt instanceof BagDataType);
		assertEquals(m_sub_scope, dt.parent_dts());
		dt = ((BagDataType) dt).inner_type();
		assertEquals(m_xx_in_sub_scope, dt);
	}
	
	@Test
	public void parse_tuple_data_type_in_pscope() throws Exception {
		DataType dt = m_parser.parse("tuple<xx>", m_pscope, m_pscope);
		assertTrue(dt instanceof TupleDataType);
		assertEquals(m_pscope, dt.parent_dts());
		List<DataType> its = ((TupleDataType) dt).inner_types();
		assertEquals(1, its.size());
		assertEquals(m_xx_in_pscope, its.get(0));
	}
	
	@Test
	public void parse_tuple_data_type_in_sub_scope() throws Exception {
		DataType dt = m_parser.parse("tuple<xx,::xx,::sub::xx>", m_pscope,
				m_sub_scope);
		assertTrue(dt instanceof TupleDataType);
		assertEquals(m_pscope, dt.parent_dts());
		List<DataType> its = ((TupleDataType) dt).inner_types();
		assertEquals(3, its.size());
		assertEquals(m_xx_in_sub_scope, its.get(0));
		assertEquals(m_xx_in_pscope, its.get(1));
		assertEquals(m_xx_in_sub_scope, its.get(2));
	}
	
	@Test
	public void parse_map_data_type_in_pscope() throws Exception {
		DataType dt = m_parser.parse("map<xx,int32>", m_pscope, m_pscope);
		assertTrue(dt instanceof MapDataType);
		assertEquals(m_pscope, dt.parent_dts());
		assertEquals(m_xx_in_pscope, ((MapDataType) dt).key_type());
		assertEquals(m_pscope.int32(), ((MapDataType) dt).value_type());
	}
	
	@Test
	public void parse_map_data_type_in_sub_scope() throws Exception {
		DataType dt = m_parser.parse("map<xx,::xx>", m_pscope, m_sub_scope);
		assertTrue(dt instanceof MapDataType);
		assertEquals(m_pscope, dt.parent_dts());
		assertEquals(m_xx_in_sub_scope, ((MapDataType) dt).key_type());
		assertEquals(m_xx_in_pscope, ((MapDataType) dt).value_type());
	}
}
