package edu.cmu.cs.able.typelib.jconv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;

import auxtestlib.DefaultTCase;
import edu.cmu.cs.able.typelib.comp.ListDataType;
import edu.cmu.cs.able.typelib.comp.ListDataValue;
import edu.cmu.cs.able.typelib.comp.MapDataType;
import edu.cmu.cs.able.typelib.comp.MapDataValue;
import edu.cmu.cs.able.typelib.comp.SetDataType;
import edu.cmu.cs.able.typelib.comp.SetDataValue;
import edu.cmu.cs.able.typelib.prim.Int32Type;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;

/**
 * Tests conversion of complex data types.
 */
@SuppressWarnings("javadoc")
public class TypelibJavaComplexConversionTest extends DefaultTCase {
	/**
	 * The primitive type scope.
	 */
	private PrimitiveScope m_pscope;
	
	/**
	 * The int32 data type.
	 */
	private Int32Type m_i32;
	
	/**
	 * Data type converter.
	 */
	private TypelibJavaConverter m_converter;
	
	@Before
	public void set_up() throws Exception {
		m_pscope = new PrimitiveScope();
		m_i32 = m_pscope.int32();
		m_converter = DefaultTypelibJavaConverter.make(m_pscope);
	}
	
	@Test
	public void set_to_java_with_class() throws Exception {
		Set<Integer> j = new TreeSet<>();
		j.add(new Integer(0));
		SetDataType vt = SetDataType.set_of(m_i32, m_pscope);
		SetDataValue v = vt.make();
		v.add(m_i32.make(0));
		
		assertEquals(j, m_converter.to_java(v, TreeSet.class));
	}
	
	@Test
	public void set_to_java_with_interface() throws Exception {
		Set<Integer> j = new HashSet<>();
		j.add(new Integer(0));
		SetDataType vt = SetDataType.set_of(m_i32, m_pscope);
		SetDataValue v = vt.make();
		v.add(m_i32.make(0));
		
		assertEquals(j, m_converter.to_java(v, Set.class));
	}
	
	@Test(expected = ValueConversionException.class)
	public void set_to_java_with_bad_class() throws Exception {
		SetDataType vt = SetDataType.set_of(m_i32, m_pscope);
		SetDataValue v = vt.make();
		v.add(m_i32.make(0));
		
		m_converter.to_java(v, Object.class);
	}
	
	@Test
	public void set_to_java_without_type() throws Exception {
		Set<Integer> j = new HashSet<>();
		j.add(new Integer(0));
		SetDataType vt = SetDataType.set_of(m_i32, m_pscope);
		SetDataValue v = vt.make();
		v.add(m_i32.make(0));
		
		assertEquals(j, m_converter.to_java(v, null));
	}
	
	@Test
	public void set_from_java_with_type() throws Exception {
		Set<Integer> j = new HashSet<>();
		j.add(new Integer(0));
		SetDataType vt = SetDataType.set_of(m_i32, m_pscope);
		SetDataValue v = SetDataType.set_of(m_i32, m_pscope).make();
		v.add(m_i32.make(0));
		
		assertEquals(v, m_converter.from_java(j, vt));
	}
	
	@Test(expected = ValueConversionException.class)
	public void set_from_java_without_type() throws Exception {
		Set<Integer> j = new HashSet<>();
		j.add(new Integer(0));
		
		m_converter.from_java(j, null);
	}
	
	@Test(expected = ValueConversionException.class)
	public void set_from_java_with_wrong_typelib_type() throws Exception {
		Set<Integer> j = new HashSet<>();
		j.add(new Integer(0));
		
		m_converter.from_java(j, m_pscope.int32());
	}
	
	@Test(expected = ValueConversionException.class)
	public void wrong_set_from_java() throws Exception {
		SetDataType vt = SetDataType.set_of(m_i32, m_pscope);
		m_converter.from_java(new Object(), vt);
	}
	
	@Test
	public void list_to_java_with_class() throws Exception {
		List<Integer> j = new LinkedList<>();
		j.add(new Integer(0));
		ListDataType vt = ListDataType.list_of(m_i32, m_pscope);
		ListDataValue v = vt.make();
		v.add(m_i32.make(0));
		
		assertEquals(j, m_converter.to_java(v, LinkedList.class));
	}
	
	@Test
	public void list_to_java_with_interface() throws Exception {
		List<Integer> j = new ArrayList<>();
		j.add(new Integer(0));
		ListDataType vt = ListDataType.list_of(m_i32, m_pscope);
		ListDataValue v = vt.make();
		v.add(m_i32.make(0));
		
		assertEquals(j, m_converter.to_java(v, List.class));
	}
	
	@Test(expected = ValueConversionException.class)
	public void list_to_java_with_bad_class() throws Exception {
		ListDataType vt = ListDataType.list_of(m_i32, m_pscope);
		ListDataValue v = vt.make();
		v.add(m_i32.make(0));
		
		m_converter.to_java(v, Object.class);
	}
	
	@Test
	public void list_to_java_without_type() throws Exception {
		List<Integer> j = new ArrayList<>();
		j.add(new Integer(0));
		ListDataType vt = ListDataType.list_of(m_i32, m_pscope);
		ListDataValue v = vt.make();
		v.add(m_i32.make(0));
		
		assertEquals(j, m_converter.to_java(v, null));
	}
	
	@Test
	public void list_from_java_with_type() throws Exception {
		List<Integer> j = new ArrayList<>();
		j.add(new Integer(0));
		ListDataType vt = ListDataType.list_of(m_i32, m_pscope);
		ListDataValue v = vt.make();
		v.add(m_i32.make(0));
		
		assertEquals(v, m_converter.from_java(j, vt));
	}
	
	@Test(expected = ValueConversionException.class)
	public void list_from_java_without_type() throws Exception {
		List<Integer> j = new ArrayList<>();
		j.add(new Integer(0));
		
		m_converter.from_java(j, null);
	}
	
	@Test(expected = ValueConversionException.class)
	public void list_from_java_with_wrong_typelib_type() throws Exception {
		List<Integer> j = new ArrayList<>();
		j.add(new Integer(0));
		
		m_converter.from_java(j, m_pscope.int32());
	}
	
	@Test(expected = ValueConversionException.class)
	public void wrong_list_from_java() throws Exception {
		ListDataType vt = ListDataType.list_of(m_i32, m_pscope);
		m_converter.from_java(new Object(), vt);
	}
	
	@Test
	public void map_to_java_with_class() throws Exception {
		Map<Integer, String> j = new TreeMap<>();
		j.put(new Integer(0), "foo");
		MapDataType vt = MapDataType.map_of(m_i32, m_pscope.string(),
				m_pscope);
		MapDataValue v = vt.make();
		v.put(m_i32.make(0), m_pscope.string().make("foo"));
		
		assertEquals(j, m_converter.to_java(v, TreeMap.class));
	}
	
	@Test
	public void map_to_java_with_interface() throws Exception {
		Map<Integer, String> j = new HashMap<>();
		j.put(new Integer(0), "foo");
		MapDataType vt = MapDataType.map_of(m_i32, m_pscope.string(),
				m_pscope);
		MapDataValue v = vt.make();
		v.put(m_i32.make(0), m_pscope.string().make("foo"));
		
		assertEquals(j, m_converter.to_java(v, Map.class));
	}
	
	@Test(expected = ValueConversionException.class)
	public void map_to_java_with_bad_class() throws Exception {
		MapDataType vt = MapDataType.map_of(m_i32, m_pscope.string(),
				m_pscope);
		MapDataValue v = vt.make();
		v.put(m_i32.make(0), m_pscope.string().make("foo"));
		
		m_converter.to_java(v, Object.class);
	}
	
	@Test
	public void map_to_java_without_type() throws Exception {
		Map<Integer, String> j = new HashMap<>();
		j.put(new Integer(0), "foo");
		MapDataType vt = MapDataType.map_of(m_i32, m_pscope.string(),
				m_pscope);
		MapDataValue v = vt.make();
		v.put(m_i32.make(0), m_pscope.string().make("foo"));
		
		assertEquals(j, m_converter.to_java(v, null));
	}
	
	@Test
	public void map_from_java_with_type() throws Exception {
		Map<Integer, String> j = new HashMap<>();
		j.put(new Integer(0), "foo");
		MapDataType vt = MapDataType.map_of(m_i32, m_pscope.string(),
				m_pscope);
		MapDataValue v = vt.make();
		v.put(m_i32.make(0), m_pscope.string().make("foo"));
		
		assertEquals(v, m_converter.from_java(j, vt));
	}
	
	@Test(expected = ValueConversionException.class)
	public void map_from_java_without_type() throws Exception {
		Map<Integer, String> j = new HashMap<>();
		j.put(new Integer(0), "foo");
		
		m_converter.from_java(j, null);
	}
	
	@Test(expected = ValueConversionException.class)
	public void map_from_java_with_wrong_typelib_type() throws Exception {
		Map<Integer, String> j = new HashMap<>();
		j.put(new Integer(0), "foo");
		
		m_converter.from_java(j, m_pscope.int32());
	}
	
	@Test(expected = ValueConversionException.class)
	public void wrong_map_from_java() throws Exception {
		MapDataType vt = MapDataType.map_of(m_i32, m_pscope.string(),
				m_pscope);
		m_converter.from_java(new Object(), vt);
	}
	
	@Test
	public void very_complex_to_java_with_class() throws Exception {
		Map<List<Set<Integer>>, Set<List<String>>> j = new HashMap<>();
		j.put(Arrays.asList((Set<Integer>) new HashSet<>(
				Arrays.asList((Integer) 0))),
				new HashSet<>(Arrays.asList(Arrays.asList("foo"))));
		
		SetDataType is = SetDataType.set_of(m_i32, m_pscope);
		ListDataType il = ListDataType.list_of(is, m_pscope);
		ListDataType sl = ListDataType.list_of(m_pscope.string(), m_pscope);
		SetDataType ss = SetDataType.set_of(sl, m_pscope);
		MapDataType vt = MapDataType.map_of(il, ss, m_pscope);
		
		MapDataValue v = vt.make();
		SetDataValue is1 = is.make();
		is1.add(m_i32.make(0));
		ListDataValue il1 = il.make();
		il1.add(is1);
		ListDataValue sl1 = sl.make();
		sl1.add(m_pscope.string().make("foo"));
		SetDataValue ss1 = ss.make();
		ss1.add(sl1);
		v.put(il1, ss1);
		
		assertEquals(j, m_converter.to_java(v, HashMap.class));
	}
	
	@Test
	public void very_complex_to_java_with_interface() throws Exception {
		Map<List<Set<Integer>>, Set<List<String>>> j = new HashMap<>();
		j.put(Arrays.asList((Set<Integer>) new HashSet<>(
				Arrays.asList((Integer) 0))),
				new HashSet<>(Arrays.asList(Arrays.asList("foo"))));
		
		SetDataType is = SetDataType.set_of(m_i32, m_pscope);
		ListDataType il = ListDataType.list_of(is, m_pscope);
		ListDataType sl = ListDataType.list_of(m_pscope.string(), m_pscope);
		SetDataType ss = SetDataType.set_of(sl, m_pscope);
		MapDataType vt = MapDataType.map_of(il, ss, m_pscope);
		
		MapDataValue v = vt.make();
		SetDataValue is1 = is.make();
		is1.add(m_i32.make(0));
		ListDataValue il1 = il.make();
		il1.add(is1);
		ListDataValue sl1 = sl.make();
		sl1.add(m_pscope.string().make("foo"));
		SetDataValue ss1 = ss.make();
		ss1.add(sl1);
		v.put(il1, ss1);
		
		assertEquals(j, m_converter.to_java(v, Map.class));
	}
	
	@Test
	public void very_complex_from_java_with_type() throws Exception {
		Map<List<Set<Integer>>, Set<List<String>>> j = new HashMap<>();
		j.put(Arrays.asList((Set<Integer>) new HashSet<>(
				Arrays.asList((Integer) 0))),
				new HashSet<>(Arrays.asList(Arrays.asList("foo"))));
		
		SetDataType is = SetDataType.set_of(m_i32, m_pscope);
		ListDataType il = ListDataType.list_of(is, m_pscope);
		ListDataType sl = ListDataType.list_of(m_pscope.string(), m_pscope);
		SetDataType ss = SetDataType.set_of(sl, m_pscope);
		MapDataType vt = MapDataType.map_of(il, ss, m_pscope);
		
		MapDataValue v = vt.make();
		SetDataValue is1 = is.make();
		is1.add(m_i32.make(0));
		ListDataValue il1 = il.make();
		il1.add(is1);
		ListDataValue sl1 = sl.make();
		sl1.add(m_pscope.string().make("foo"));
		SetDataValue ss1 = ss.make();
		ss1.add(sl1);
		v.put(il1, ss1);
		
		assertEquals(v, m_converter.from_java(j, vt));
	}
}
