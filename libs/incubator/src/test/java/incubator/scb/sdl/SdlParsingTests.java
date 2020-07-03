package incubator.scb.sdl;

import incubator.jcodegen.JavaCode;
import incubator.jcodegen.JavaPackage;

import java.util.Map;

import org.junit.Test;

import auxtestlib.DefaultTCase;

/**
 * Test cases for parsing SDL.
 */
@SuppressWarnings("javadoc")
public class SdlParsingTests extends DefaultTCase {
	@Test
	public void empty_sdl() throws Exception {
		String text = "";
		SdlDefinition sd = new SdlParser(new GeneratorRegistry()).parse(text);
		assertNotNull(sd);
	}
	
	@Test
	public void single_package() throws Exception {
		String text = "package foo {}";
		SdlDefinition sd = new SdlParser(new GeneratorRegistry()).parse(text);
		assertNotNull(sd);
		assertEquals(1, sd.package_names().size());
		assertTrue(sd.package_names().contains("foo"));
		assertNotNull(sd.pkg("foo"));
		assertEquals("foo", sd.pkg("foo").name());
	}
	
	@Test
	public void multiple_packages() throws Exception {
		String text = "package foo {} package bar.glu {}";
		SdlDefinition sd = new SdlParser(new GeneratorRegistry()).parse(text);
		assertNotNull(sd);
		assertEquals(2, sd.package_names().size());
		assertTrue(sd.package_names().contains("foo"));
		assertTrue(sd.package_names().contains("bar.glu"));
		assertNotNull(sd.pkg("foo"));
		assertNotNull(sd.pkg("bar.glu"));
		assertEquals("foo", sd.pkg("foo").name());
		assertEquals("bar.glu", sd.pkg("bar.glu").name());
	}
	
	@Test
	public void empty_bean() throws Exception {
		String text = "package foo { bean Bar {} }";
		SdlDefinition sd = new SdlParser(new GeneratorRegistry()).parse(text);
		SdlPackage p = sd.pkg("foo");
		assertEquals(1, p.bean_names().size());
		assertTrue(p.bean_names().contains("Bar"));
		SdlBean b = p.bean("Bar");
		assertNotNull(b);
		assertEquals("Bar", b.name());
	}
	
	@Test
	public void multiple_beans() throws Exception {
		String text = "package foo { bean Bar {} bean Glu {} }";
		SdlDefinition sd = new SdlParser(new GeneratorRegistry()).parse(text);
		SdlPackage p = sd.pkg("foo");
		assertEquals(2, p.bean_names().size());
		assertTrue(p.bean_names().contains("Bar"));
		assertTrue(p.bean_names().contains("Glu"));
		SdlBean b = p.bean("Bar");
		assertNotNull(b);
		assertEquals("Bar", b.name());
		b = p.bean("Glu");
		assertNotNull(b);
		assertEquals("Glu", b.name());
	}
	
	@Test(expected = SdlParsingException.class)
	public void duplicate_bean_names() throws Exception {
		String text = "package x { bean A {} bean A {} }";
		new SdlParser(new GeneratorRegistry()).parse(text);
	}
	
	@Test
	public void bean_with_attributes() throws Exception {
		String text = "package x { bean A { attributes { a : string; b : int; "
				+ "} } }";
		SdlDefinition sd = new SdlParser(new GeneratorRegistry()).parse(text);
		SdlPackage p = sd.pkg("x");
		SdlBean b = p.bean("A");
		assertEquals(2, b.attribute_names().size());
		assertTrue(b.attribute_names().contains("a"));
		assertTrue(b.attribute_names().contains("b"));
		assertEquals("a", b.attribute("a").name());
		assertEquals("string", b.attribute("a").type().name());
		assertEquals("b", b.attribute("b").name());
		assertEquals("int", b.attribute("b").type().name());
	}
	
	@Test
	public void readonly_and_readwrite_bean() throws Exception {
		String text = "package x { bean A { read_only; } bean B {} }";
		SdlDefinition sd = new SdlParser(new GeneratorRegistry()).parse(text);
		SdlPackage p = sd.pkg("x");
		SdlBean ba = p.bean("A");
		assertTrue(ba.read_only());
		SdlBean bb = p.bean("B");
		assertFalse(bb.read_only());
	}
	
	@Test
	public void not_null_attribute_invariant() throws Exception {
		String text = "package x { bean A { attributes { b : string, not_null; "
				+ "} } }";
		SdlDefinition sd = new SdlParser(new GeneratorRegistry()).parse(text);
		SdlPackage p = sd.pkg("x");
		SdlBean ba = p.bean("A");
		SdlAttribute b = ba.attribute("b");
		assertNotNull(b);
		assertEquals(1, b.invariants().size());
		assertTrue(b.invariants().get(0)
				instanceof SdlAttributeNotNullInvariant);
	}
	
	@Test
	public void enumeration_attributes() throws Exception {
		String text = "package a { bean B { attributes { c : enum<D>; } } }";
		SdlDefinition sd = new SdlParser(new GeneratorRegistry()).parse(text);
		SdlPackage a = sd.pkg("a");
		SdlBean b = a.bean("B");
		SdlAttribute c = b.attribute("c");
		assertTrue(c.type() instanceof SdlEnumerationType);
		assertEquals("D", c.type().name());
	}
	
	@Test
	public void fqn_enumeration_attributes() throws Exception {
		String text = "package a { bean B { attributes { c : enum<d.E>; } } }";
		SdlDefinition sd = new SdlParser(new GeneratorRegistry()).parse(text);
		SdlPackage a = sd.pkg("a");
		SdlBean b = a.bean("B");
		SdlAttribute c = b.attribute("c");
		assertTrue(c.type() instanceof SdlEnumerationType);
		assertEquals("d.E", c.type().name());
	}
	
	@Test
	public void generator_attributes_no_properties() throws Exception {
		String text = "package a { bean B { generators { foo } } }";
		GeneratorRegistry reg = new GeneratorRegistry();
		reg.add_generator("foo", new SdlBeanGenerator() {
			@Override
			public GenerationInfo generate(SdlBean b, JavaCode jc,
					JavaPackage jp, Map<String, String> properties)
					throws SdlGenerationException {
				return new GenerationInfo(GenerationResult.NOTHING_TO_DO);
			}
		});
		
		new SdlParser(reg).parse(text);
	}
	
	@Test
	public void generator_flag_attribute() throws Exception {
		String text = "package a { bean B { generators { foo (x) } } }";
		GeneratorRegistry reg = new GeneratorRegistry();
		reg.add_generator("foo", new SdlBeanGenerator() {
			@Override
			public GenerationInfo generate(SdlBean b, JavaCode jc,
					JavaPackage jp, Map<String, String> properties)
					throws SdlGenerationException {
				return new GenerationInfo(GenerationResult.NOTHING_TO_DO);
			}
		});
		
		new SdlParser(reg).parse(text);
	}
	
	@Test
	public void generator_attributes_one_property() throws Exception {
		String text = "package a { bean B { generators { foo (a = b) } } }";
		GeneratorRegistry reg = new GeneratorRegistry();
		reg.add_generator("foo", new SdlBeanGenerator() {
			@Override
			public GenerationInfo generate(SdlBean b, JavaCode jc,
					JavaPackage jp, Map<String, String> properties)
					throws SdlGenerationException {
				return new GenerationInfo(GenerationResult.NOTHING_TO_DO);
			}
		});
		
		new SdlParser(reg).parse(text);
	}
	
	@Test
	public void generator_attributes_two_properties() throws Exception {
		String text = "package a { bean B { generators { foo (a = b, c = d) "
				+ "} } }";
		GeneratorRegistry reg = new GeneratorRegistry();
		reg.add_generator("foo", new SdlBeanGenerator() {
			@Override
			public GenerationInfo generate(SdlBean b, JavaCode jc,
					JavaPackage jp, Map<String, String> properties)
					throws SdlGenerationException {
				return new GenerationInfo(GenerationResult.NOTHING_TO_DO);
			}
		});
		
		new SdlParser(reg).parse(text);
	}
	
	@Test
	public void parse_multiline_comments() throws Exception {
		String text = "package a { } /* package b \n{ } */";
		SdlDefinition sd = new SdlParser(new GeneratorRegistry()).parse(text);
		assertNotNull(sd);
		assertEquals(1, sd.package_names().size());
		assertTrue(sd.package_names().contains("a"));
		assertNotNull(sd.pkg("a"));
	}
	
	@Test
	public void parse_singleline_comments() throws Exception {
		String text = "// package a { } \n package b \n{ } ";
		SdlDefinition sd = new SdlParser(new GeneratorRegistry()).parse(text);
		assertNotNull(sd);
		assertEquals(1, sd.package_names().size());
		assertTrue(sd.package_names().contains("b"));
		assertNotNull(sd.pkg("b"));
	}
	
	@Test
	public void reference_to_bean() throws Exception {
		String text = "package a { bean B {} } package c { bean D { "
				+ "attributes { e : bean<a.B>; } } }";
		SdlDefinition sd = new SdlParser(new GeneratorRegistry()).parse(text);
		assertNotNull(sd);
		assertEquals(2, sd.package_names().size());
		SdlPackage a = sd.pkg("a");
		assertNotNull(a);
		SdlBean b = a.bean("B");
		assertNotNull(b);
		SdlPackage c = sd.pkg("c");
		assertNotNull(b);
		SdlBean d = c.bean("D");
		assertNotNull(d);
		assertEquals(1, d.attribute_names().size());
		SdlAttribute e = d.attribute("e");
		assertNotNull(e);
		assertTrue(e.type() instanceof SdlBeanType);
		assertSame(b, ((SdlBeanType) e.type()).bean());
	}
	
	@Test
	public void reference_to_same_package() throws Exception {
		String text = "package a { bean B {} bean C { "
				+ "attributes { d : bean<B>; } } }";
		
		SdlDefinition sd = new SdlParser(new GeneratorRegistry()).parse(text);
		assertNotNull(sd);
		assertEquals(1, sd.package_names().size());
		SdlPackage a = sd.pkg("a");
		assertNotNull(a);
		SdlBean b = a.bean("B");
		assertNotNull(b);
		SdlBean c = a.bean("C");
		assertNotNull(c);
		assertEquals(1, c.attribute_names().size());
		SdlAttribute d = c.attribute("d");
		assertNotNull(d);
		assertTrue(d.type() instanceof SdlBeanType);
		assertSame(b, ((SdlBeanType) d.type()).bean());
	}
	
	@Test
	public void parent_bean() throws Exception {
		String text = "package a { bean B {} bean C extends B {} }";
		SdlDefinition sd = new SdlParser(new GeneratorRegistry()).parse(text);
		assertNotNull(sd);
		assertEquals(1, sd.package_names().size());
		SdlPackage a = sd.pkg("a");
		assertNotNull(a);
		SdlBean b = a.bean("B");
		assertNotNull(b);
		assertNull(b.parent());
		SdlBean c = a.bean("C");
		assertNotNull(c);
		assertSame(b, c.parent());
	}
	
	@Test
	public void bean_with_set_attribute() throws Exception {
		String text = "package x{bean A{attributes{a:set<string>;}}}";
		SdlDefinition sd = new SdlParser(new GeneratorRegistry()).parse(text);
		SdlPackage p = sd.pkg("x");
		SdlBean b = p.bean("A");
		assertEquals(1, b.attribute_names().size());
		assertTrue(b.attribute_names().contains("a"));
		assertEquals("a", b.attribute("a").name());
		assertEquals("set<string>", b.attribute("a").type().name());
	}
}

