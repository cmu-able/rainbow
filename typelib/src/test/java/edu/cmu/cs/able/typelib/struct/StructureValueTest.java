package edu.cmu.cs.able.typelib.struct;

import org.junit.Test;

import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Tests for structure values.
 */
@SuppressWarnings("javadoc")
public class StructureValueTest extends StructureTestCase {
	@Test
	public void create_structure_value_get_field_values() throws Exception {
		StructureDataType x = parse_declaration("x", "struct x {int32 y;}");
		StructureDataValue x_v = make(x, "y", m_pscope.int32().make(8));
		assertEquals(m_pscope.int32().make(8), x_v.value(x.field("y")));
	}
	
	@Test
	public void create_structure_value_set_field_values() throws Exception {
		StructureDataType x = parse_declaration("x", "struct x {int32 y;}");
		StructureDataValue x_v = make(x, "y", m_pscope.int32().make(8));
		x_v.value(x.field("y"), m_pscope.int32().make(9));
		assertEquals(m_pscope.int32().make(9), x_v.value(x.field("y")));
	}
	
	@Test
	public void compare_structure_values() throws Exception {
		StructureDataType x = parse_declaration("x", "struct x {int32 y;}");
		StructureDataValue x_v1 = make(x, "y", m_pscope.int32().make(8));
		StructureDataValue x_v2 = make(x, "y", m_pscope.int32().make(9));
		StructureDataValue x_v3 = make(x, "y", m_pscope.int32().make(9));
		
		assertFalse(x_v1.equals(x_v2));
		assertTrue(x_v2.equals(x_v3));
		assertTrue(x_v1.hashCode() != x_v2.hashCode());
		assertTrue(x_v2.hashCode() == x_v3.hashCode());
	}
	
	@Test
	public void compare_to_itself() throws Exception {
		StructureDataType x = parse_declaration("x", "struct x {int32 y;}");
		StructureDataValue x_v1 = make(x, "y", m_pscope.int32().make(8));
		assertTrue(x_v1.equals(x_v1));
	}
	
	@Test
	public void compare_to_null() throws Exception {
		StructureDataType x = parse_declaration("x", "struct x {int32 y;}");
		StructureDataValue x_v1 = make(x, "y", m_pscope.int32().make(8));
		assertFalse(x_v1.equals((Object) null));
	}
	
	@Test
	public void compare_to_non_object() throws Exception {
		StructureDataType x = parse_declaration("x", "struct x {int32 y;}");
		StructureDataValue x_v1 = make(x, "y", m_pscope.int32().make(8));
		assertFalse(x_v1.equals(new Integer(5)));
	}
	
	@Test
	public void string_description_contains_names_and_values()
			throws Exception {
		DataValue pval8 = m_pscope.int32().make(8);
		DataValue pval9 = m_pscope.int32().make(9);
		StructureDataType x = parse_declaration("x",
				"struct x {int32 y; int32 z;}");
		StructureDataValue x_v1 = make(x, "y", pval8, "z", pval9);
		assertEquals("x{x::y=" + pval8.toString() + ",x::z=" + pval9.toString()
				+ "}", x_v1.toString());
	}
	
	@Test
	public void cloning_values() throws Exception {
		StructureDataType x1 = parse_declaration("x1", "struct x1 {}");
		StructureDataValue x1_v1 = make(x1);
		StructureDataType x2 = parse_declaration("x2", "struct x2 {x1 z;}");
		StructureDataValue x2_v1 = make(x2, "z", x1_v1);
		
		StructureDataValue x2_v2 = x2_v1.clone();
		assertEquals(x2_v2, x2_v1);
		assertNotSame(x2_v2, x2_v1);
		
		StructureDataValue x1_v2 = (StructureDataValue) x2_v2.value(
				x2.field("z"));
		assertEquals(x1_v1, x1_v2);
		assertNotSame(x1_v1, x1_v2);
	}
}
