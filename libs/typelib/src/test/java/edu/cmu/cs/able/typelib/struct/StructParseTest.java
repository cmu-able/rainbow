package edu.cmu.cs.able.typelib.struct;

import java.util.Arrays;

import org.junit.Test;

import auxtestlib.FileContentWorker;
import edu.cmu.cs.able.parsec.LocalizedParseException;
import edu.cmu.cs.able.parsec.ParsecFileReader;
import edu.cmu.cs.able.typelib.comp.MapDataType;
import edu.cmu.cs.able.typelib.comp.SetDataType;
import edu.cmu.cs.able.typelib.scope.HierarchicalName;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataTypeScope;

/**
 * Parses structures and checks their contents.
 */
@SuppressWarnings("javadoc")
public class StructParseTest extends StructureTestCase {
	@Test
	public void parse_empty_structure() throws Exception {
		StructureDataType sdt = parse_declaration("foo", "struct foo {}");
		assertEquals("foo", sdt.name());
		assertFalse(sdt.is_abstract());
		assertEquals(1, sdt.super_types().size());
		assertTrue(sdt.super_types().contains(m_pscope.any()));
		assertEquals(0, sdt.fields().size());
	}
	
	@Test
	public void parse_structure_with_fields() throws Exception {
		StructureDataType sdt = parse_declaration("bar", "struct bar {"
				+ "int32 f1; bool? optf;}");
		assertEquals("bar", sdt.name());
		assertFalse(sdt.is_abstract());
		assertEquals(1, sdt.super_types().size());
		assertTrue(sdt.super_types().contains(m_pscope.any()));
		assertEquals(2, sdt.fields().size());
		
		Field f1 = sdt.field("f1");
		assertNotNull(f1);
		assertEquals("f1", f1.name());
		assertEquals("::f1", f1.absolute_hname().toString());
		assertEquals("f1", f1.description().name());
		assertEquals(m_pscope.int32(), f1.description().type());
		assertEquals(sdt, f1.structure());
		
		Field optf = sdt.field("optf");
		assertNotNull(optf);
		assertEquals("optf", optf.name());
		assertEquals("::optf", optf.absolute_hname().toString());
		assertEquals("optf", optf.description().name());
		assertEquals(m_pscope.find("bool?"), optf.description().type());
		assertEquals(sdt, optf.structure());
	}
	
	@Test
	public void parse_structure_with_single_parent() throws Exception {
		FileContentWorker.setContents(m_tf.getFile(), "struct par {int32?? i;} "
				+ "struct chi : par {bool i; int64 j;}");
		m_general_parsec.parse(new ParsecFileReader().read(m_tf.getFile()),
				m_typelib_ctx);
		
		DataType par_dt = m_pscope.find("par");
		assertNotNull(par_dt);
		assertTrue(par_dt instanceof StructureDataType);
		
		DataType chi_dt = m_pscope.find("chi");
		assertNotNull(chi_dt);
		assertTrue(chi_dt instanceof StructureDataType);
		
		StructureDataType chi = (StructureDataType) chi_dt;
		assertEquals("chi", chi.name());
		assertFalse(chi.is_abstract());
		assertEquals(1, chi.super_types().size());
		assertTrue(chi.super_types().contains(par_dt));
		assertEquals(3, chi.fields().size());
		
		Field i_f = chi.field("i");
		assertNotNull(i_f);
		assertEquals("i", i_f.name());
		assertEquals("::i", i_f.absolute_hname().toString());
		assertEquals("i", i_f.description().name());
		assertEquals(m_pscope.bool(), i_f.description().type());
		assertEquals(chi, i_f.structure());
		
		Field j_f = chi.field("j");
		assertNotNull(j_f);
		assertEquals("j", j_f.name());
		assertEquals("::j", j_f.absolute_hname().toString());
		assertEquals("j", j_f.description().name());
		assertEquals(m_pscope.find("int64"), j_f.description().type());
		assertEquals(chi, j_f.structure());
		
		Field ip_f = chi.field(new HierarchicalName(false, "par", "i"));
		assertNotNull(ip_f);
		assertEquals("i", ip_f.name());
		assertEquals("::i", ip_f.absolute_hname().toString());
		assertEquals("i", ip_f.description().name());
		assertNotNull(m_pscope.find("int32??"));
		assertEquals(m_pscope.find("int32??"), ip_f.description().type());
		assertEquals(par_dt, ip_f.structure());
	}
	
	@Test
	public void parse_structure_with_multiple_parents() throws Exception {
		FileContentWorker.setContents(m_tf.getFile(), "struct par1 {bool x;} "
				+ "struct par2 {bool y;} struct chi : par1, par2 {}");
		m_general_parsec.parse(new ParsecFileReader().read(m_tf.getFile()),
				m_typelib_ctx);
		
		DataType par1_dt = m_pscope.find("par1");
		assertNotNull(par1_dt);
		assertTrue(par1_dt instanceof StructureDataType);
		
		DataType par2_dt = m_pscope.find("par2");
		assertNotNull(par2_dt);
		assertTrue(par2_dt instanceof StructureDataType);
		
		DataType chi_dt = m_pscope.find("chi");
		assertNotNull(chi_dt);
		assertTrue(chi_dt instanceof StructureDataType);
		
		StructureDataType chi = (StructureDataType) chi_dt;
		assertEquals("chi", chi.name());
		assertFalse(chi.is_abstract());
		assertEquals(2, chi.super_types().size());
		assertTrue(chi.super_types().contains(par1_dt));
		assertTrue(chi.super_types().contains(par2_dt));
		assertEquals(2, chi.fields().size());
		
		Field x_f = chi.field("x");
		assertNotNull(x_f);
		assertEquals("x", x_f.name());
		assertEquals("::x", x_f.absolute_hname().toString());
		assertEquals("x", x_f.description().name());
		assertEquals(m_pscope.bool(), x_f.description().type());
		assertEquals(par1_dt, x_f.structure());
		
		Field y_f = chi.field("y");
		assertNotNull(y_f);
		assertEquals("y", y_f.name());
		assertEquals("::y", y_f.absolute_hname().toString());
		assertEquals("y", y_f.description().name());
		assertEquals(m_pscope.bool(), y_f.description().type());
		assertEquals(par2_dt, y_f.structure());
	}
	
	@Test
	public void parse_abstract_structure() throws Exception {
		StructureDataType sdt = parse_declaration("foo",
				"abstract struct foo {}");
		assertEquals("foo", sdt.name());
		assertTrue(sdt.is_abstract());
		assertEquals(1, sdt.super_types().size());
		assertTrue(sdt.super_types().contains(m_pscope.any()));
		assertEquals(0, sdt.fields().size());
	}
	
	@Test(expected = LocalizedParseException.class)
	public void parse_structure_with_repeated_fields() throws Exception {
		parse_declaration("foo", "abstract struct foo {bool x; int32 x;}");
	}
	
	@Test(expected = UnknownFieldException.class)
	public void access_non_existing_field() throws Exception {
		StructureDataType sdt = parse_declaration("foo",
				"abstract struct foo {}");
		sdt.field("g");
	}
	
	@Test
	public void parse_structure_in_namespace() throws Exception {
		parse_declaration("namespace foo { struct bar {} }");
		
		assertNull(m_pscope.find("foo"));
		DataTypeScope foo = (DataTypeScope) m_pscope.find_scope("foo");
		assertNotNull(foo);
		
		StructureDataType sdt = (StructureDataType) m_pscope.find(
				new HierarchicalName(false, Arrays.asList("foo", "bar")));
		
		assertSame(sdt, foo.find("bar"));
		assertNotSame(foo, m_pscope);
	}
	
	@Test
	public void parse_structure_with_complex_field() throws Exception {
		StructureDataType bar = parse_declaration("foo",
				"struct foo { set<string> bar; }");
		assertEquals(1, bar.fields().size());
		Field f = bar.fields().iterator().next();
		assertEquals("bar", f.description().name());
		assertTrue(f.description().type() instanceof SetDataType);
	}
	
	@Test
	public void parse_structure_with_complex_field_2() throws Exception {
		StructureDataType bar = parse_declaration("foo",
				"struct foo { map<string,int32> bar; }");
		assertEquals(1, bar.fields().size());
		Field f = bar.fields().iterator().next();
		assertEquals("bar", f.description().name());
		assertTrue(f.description().type() instanceof MapDataType);
	}
}
