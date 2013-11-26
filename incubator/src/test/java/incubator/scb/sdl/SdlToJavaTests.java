package incubator.scb.sdl;

import incubator.jcodegen.JavaCode;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import auxtestlib.DefaultTCase;
import auxtestlib.FileContentWorker;
import auxtestlib.TemporaryFile;

/**
 * End-to-end tests from SDL to <em>java</em> code.
 */
@SuppressWarnings("javadoc")
public class SdlToJavaTests extends DefaultTCase {
	/**
	 * The SDL parser.
	 */
	private SdlParser m_sp;
	
	/**
	 * The java code generated.
	 */
	private JavaCode m_jc;
	
	/**
	 * The temporary directory where to generate source code.
	 */
	private TemporaryFile m_td;
	
	/**
	 * Prepares the text fixture.
	 * @throws Exception failed to set up
	 */
	@Before
	public void setup() throws Exception {
		m_sp = new SdlParser(new GeneratorRegistry());
		m_jc = new JavaCode();
		m_td = new TemporaryFile(true);
	}
	
	/**
	 * Cleans up after the text case.
	 * @throws Exception failed to clean up
	 */
	@After
	public void tear_down() throws Exception {
		m_td.delete();
	}
	
	@Test
	public void empty_simple_package() throws Exception {
		String text = "package foo {}";
		m_sp.parse(text).generate(m_jc);
		m_jc.generate(m_td.getFile());
		
		File foo_dir = new File(m_td.getFile(), "foo");
		assertTrue(foo_dir.isDirectory());
	}
	
	@Test
	public void multiple_complex_empty_packages() throws Exception {
		String text = "package foo {} package foo.bar.glu {}";
		m_sp.parse(text).generate(m_jc);
		m_jc.generate(m_td.getFile());
		
		File foo_dir = new File(m_td.getFile(), "foo");
		assertTrue(foo_dir.isDirectory());
		File bar_dir = new File(foo_dir, "bar");
		assertTrue(bar_dir.isDirectory());
		File glu_dir = new File(bar_dir, "glu");
		assertTrue(glu_dir.isDirectory());
	}
	
	@Test
	public void bean_without_generators() throws Exception {
		String text = "package x { bean y {} }";
		m_sp.parse(text).generate(m_jc);
		m_jc.generate(m_td.getFile());
		
		File x_dir = new File(m_td.getFile(), "x");
		File y_file = new File(x_dir, "y.java");
		assertFalse(y_file.exists());
	}
	
	@Test
	public void class_bean_generator() throws Exception {
		String text = "package x { bean y { generators { class } } }";
		m_sp.parse(text).generate(m_jc);
		m_jc.generate(m_td.getFile());
		
		File x_dir = new File(m_td.getFile(), "x");
		File y_file = new File(x_dir, "y.java");
		assertTrue(y_file.exists());
		String y_contents = FileContentWorker.readContents(y_file);
		assertEquals("package x;\npublic class y {\n}\n", y_contents);
	}
	
	@Test
	public void class_bean_with_attributes_as_fields() throws Exception {
		String text = "package x { bean y { attributes { a : String; "
				+ "b : int; } generators { class, attributes_as_fields } } }";
		m_sp.parse(text).generate(m_jc);
		m_jc.generate(m_td.getFile());
		
		File x_dir = new File(m_td.getFile(), "x");
		File y_file = new File(x_dir, "y.java");
		assertTrue(y_file.exists());
		String y_contents = FileContentWorker.readContents(y_file);
		assertEquals("package x;\npublic class y {\nprivate String m_a;\n"
				+ "private int m_b;\n}\n", y_contents);
	}
	
	@Test
	public void simple_constructor_generation() throws Exception {
		String text = "package x { bean y { attributes { a : String;"
				+ "b : int; } generators { class, attributes_as_fields, "
				+ "simple_constructor } } }";
		m_sp.parse(text).generate(m_jc);
		m_jc.generate(m_td.getFile());
		
		File x_dir = new File(m_td.getFile(), "x");
		File y_file = new File(x_dir, "y.java");
		assertTrue(y_file.exists());
		String y_contents = FileContentWorker.readContents(y_file);
		assertEquals("package x;\npublic class y {\nprivate String m_a;\n"
				+ "private int m_b;\npublic y(String a,int b) {\n"
				+ "this.m_a = a;\nthis.m_b = b;\n}\n}\n", y_contents);
	}
	
	@Test
	public void copy_constructor_generation() throws Exception {
		String text = "package x { bean y { attributes { a : String;"
				+ "b : int; } generators { class, attributes_as_fields, "
				+ "copy_constructor } } }";
		m_sp.parse(text).generate(m_jc);
		m_jc.generate(m_td.getFile());
		
		File x_dir = new File(m_td.getFile(), "x");
		File y_file = new File(x_dir, "y.java");
		assertTrue(y_file.exists());
		String y_contents = FileContentWorker.readContents(y_file);
		assertEquals("package x;\npublic class y {\nprivate String m_a;\n"
				+ "private int m_b;\npublic y(x.y src) {\n"
				+ "incubator.pval.Ensure.not_null(src, \"src == null\");\n"
				+ "this.m_a = src.m_a;\nthis.m_b = src.m_b;\n}\n}\n",
				y_contents);
	}
	
	@Test
	public void simple_accessor_generator_in_read_only_bean() throws Exception {
		String text = "package x { bean y { read_only; attributes { a : String;"
				+ "b : int; } generators { class, attributes_as_fields, "
				+ "simple_attribute_accessors } } }";
		m_sp.parse(text).generate(m_jc);
		m_jc.generate(m_td.getFile());
		
		File x_dir = new File(m_td.getFile(), "x");
		File y_file = new File(x_dir, "y.java");
		assertTrue(y_file.exists());
		String y_contents = FileContentWorker.readContents(y_file);
		assertEquals("package x;\npublic class y {\nprivate String m_a;\n"
				+ "private int m_b;\npublic String a() {\n"
				+ "return m_a;\n}\npublic int b() {\nreturn m_b;\n}\n}\n",
				y_contents);
	}
	
	@Test
	public void simple_accessor_generator_in_read_write_bean()
			throws Exception {
		String text = "package x { bean y { attributes { a : String;"
				+ "b : int; } generators { class, attributes_as_fields, "
				+ "simple_attribute_accessors } } }";
		m_sp.parse(text).generate(m_jc);
		m_jc.generate(m_td.getFile());
		
		File x_dir = new File(m_td.getFile(), "x");
		File y_file = new File(x_dir, "y.java");
		assertTrue(y_file.exists());
		String y_contents = FileContentWorker.readContents(y_file);
		assertEquals("package x;\n"
				+ "public class y {\n"
				+ "private String m_a;\n"
				+ "private int m_b;\n"
				+ "public String a() {\n"
				+ "return m_a;\n"
				+ "}\n"
				+ "public void a(String v) {\n"
				+ "if (org.apache.commons.lang.ObjectUtils.equals(m_a, v)) {\n"
				+ "return;\n"
				+ "}\n"
				+ "this.m_a = v;\n"
				+ "}\n"
				+ "public int b() {\n"
				+ "return m_b;"
				+ "\n"
				+ "}\n"
				+ "public void b(int v) {\n"
				+ "if (org.apache.commons.lang.ObjectUtils.equals(m_b, v)) {\n"
				+ "return;\n"
				+ "}\n"
				+ "this.m_b = v;\n"
				+ "}\n"
				+ "}\n",
				y_contents);
	}
	
	@Test
	public void not_null_invariant_simple_constructor() throws Exception {
		String text = "package x { bean Y { attributes { z : String, not_null;"
				+ "} generators { class, attributes_as_fields, "
				+ "simple_constructor } } }";
		m_sp.parse(text).generate(m_jc);
		m_jc.generate(m_td.getFile());
		
		File x_dir = new File(m_td.getFile(), "x");
		File y_file = new File(x_dir, "y.java");
		assertTrue(y_file.exists());
		String y_contents = FileContentWorker.readContents(y_file);
		assertEquals("package x;\npublic class Y {\n"
				+ "private String m_z;\n"
				+ "public Y(String z) {\n"
				+ "incubator.pval.Ensure.not_null(z, \"z == null\");\n"
				+ "this.m_z = z;\n"
				+ "}\n"
				+ "}\n",
				y_contents);
	}
	
	@Test
	public void not_null_invariant_copy_constructor() throws Exception {
		String text = "package x { bean Y { attributes { z : String, not_null;"
				+ "} generators { class, attributes_as_fields, "
				+ "copy_constructor } } }";
		m_sp.parse(text).generate(m_jc);
		m_jc.generate(m_td.getFile());
		
		File x_dir = new File(m_td.getFile(), "x");
		File y_file = new File(x_dir, "y.java");
		assertTrue(y_file.exists());
		String y_contents = FileContentWorker.readContents(y_file);
		assertEquals("package x;\npublic class Y {\n"
				+ "private String m_z;\n"
				+ "public Y(x.Y src) {\n"
				+ "incubator.pval.Ensure.not_null(src, \"src == null\");\n"
				+ "this.m_z = src.m_z;\n"
				+ "}\n"
				+ "}\n",
				y_contents);
	}
	
	@Test
	public void not_null_invariant_simple_accessors() throws Exception {
		String text = "package x { bean Y { attributes { z : String, not_null;"
				+ "} generators { class, attributes_as_fields, "
				+ "simple_attribute_accessors } } }";
		m_sp.parse(text).generate(m_jc);
		m_jc.generate(m_td.getFile());
		
		File x_dir = new File(m_td.getFile(), "x");
		File y_file = new File(x_dir, "y.java");
		assertTrue(y_file.exists());
		String y_contents = FileContentWorker.readContents(y_file);
		assertEquals("package x;\npublic class Y {\n"
				+ "private String m_z;\n"
				+ "public String z() {\n"
				+ "return m_z;\n"
				+ "}\n"
				+ "public void z(String v) {\n"
				+ "incubator.pval.Ensure.not_null(v, \"v == null\");\n"
				+ "if (org.apache.commons.lang.ObjectUtils.equals(m_z, v)) {\n"
				+ "return;\n"
				+ "}\n"
				+ "this.m_z = v;\n"
				+ "}\n"
				+ "}\n",
				y_contents);
	}
	
	@Test
	public void enumeration_attributes() throws Exception {
		String text = "package a { bean B { attributes { c : enum<D>; } "
				+ "generators { class, attributes_as_fields } } }";
		m_sp.parse(text).generate(m_jc);
		m_jc.generate(m_td.getFile());
		
		File a_dir = new File(m_td.getFile(), "a");
		File b_file = new File(a_dir, "B.java");
		assertTrue(b_file.exists());
		String b_contents = FileContentWorker.readContents(b_file);
		assertEquals("package a;\npublic class B {\n"
				+ "private D m_c;\n"
				+ "}\n",
				b_contents);
	}
	
	@Test
	public void scb_string_field() throws Exception {
		String text = "package a { bean B { attributes { c : String; } "
				+ "generators { class, attributes_as_fields, "
				+ "simple_constructor, simple_attribute_accessors, "
				+ "basic_scb } } }";
		m_sp.parse(text).generate(m_jc);
		m_jc.generate(m_td.getFile());
		
		File a_dir = new File(m_td.getFile(), "a");
		File b_file = new File(a_dir, "B.java");
		assertTrue(b_file.exists());
		String b_contents = FileContentWorker.readContents(b_file);
		assertEquals("package a;\npublic class B implements "
				+ "incubator.scb.Scb<B> {\n"
				+ "private String m_c;\n"
				+ "private incubator.dispatch.LocalDispatcher<"
				+ "incubator.scb.ScbUpdateListener<B>> m_update_dispatcher;\n"
				+ "public B(String c) {\n"
				+ "this.m_c = c;\n"
				+ "this.m_update_dispatcher = new incubator.dispatch."
				+ "LocalDispatcher<>();\n"
				+ "}\n"
				+ "public String c() {\n"
				+ "return m_c;\n"
				+ "}\n"
				+ "public void c(String v) {\n"
				+ "if (org.apache.commons.lang.ObjectUtils.equals(m_c, v)) {\n"
				+ "return;\n"
				+ "}\n"
				+ "this.m_c = v;\n"
				+ "notify_update();\n"
				+ "}\n"
				+ "public incubator.dispatch.Dispatcher<"
				+ "incubator.scb.ScbUpdateListener<B>> dispatcher() {\n"
				+ "return m_update_dispatcher;\n"
				+ "}\n"
				+ "protected void notify_update() {\n"
				+ "this.m_update_dispatcher.dispatch(new "
				+ "incubator.dispatch.DispatcherOp<"
				+ "incubator.scb.ScbUpdateListener<B>>() {\n"
				+ "@Override\n"
				+ "public void dispatch(incubator.scb.ScbUpdateListener<B>"
				+ " l) {\n"
				+ "incubator.pval.Ensure.not_null(l, \"l == null\");\n"
				+ "l.updated(B.this);\n"
				+ "}\n"
				+ "});\n"
				+ "}\n"
				+ "public static incubator.scb.ScbTextField<B> c_c() {\n"
				+ "return new incubator.scb.ScbTextField<B>(\"c\", "
				+ "true, null) {\n"
				+ "@Override\n"
				+ "public String get(B bean) {\n"
				+ "incubator.pval.Ensure.not_null(bean, \"bean == null\");\n"
				+ "return bean.c();\n"
				+ "}\n"
				+ "@Override\n"
				+ "public void set(B bean, String v) {\n"
				+ "incubator.pval.Ensure.not_null(bean, \"bean == null\");\n"
				+ "bean.c(v);\n"
				+ "}\n"
				+ "};\n"
				+ "}\n"
				+ "public static java.util.List<incubator.scb.ScbField<B, ?>> "
				+ "c_fields() {\n"
				+ "java.util.List<incubator.scb.ScbField<B, ?>> fields ="
				+ " new java.util.ArrayList<>();\n"
				+ "fields.add(c_c());\n"
				+ "return fields;\n"
				+ "}\n"
				+ "public java.util.List<incubator.scb.ScbField<B, ?>> "
				+ "fields() {\n"
				+ "return c_fields();\n"
				+ "}\n"
				+ "}\n",
				b_contents);
	}
	
	@Test
	public void scb_read_only_string_field() throws Exception {
		String text = "package a { bean B { read_only; attributes "
				+ "{ c : String; } "
				+ "generators { class, attributes_as_fields, "
				+ "simple_constructor, simple_attribute_accessors, "
				+ "basic_scb } } }";
		m_sp.parse(text).generate(m_jc);
		m_jc.generate(m_td.getFile());
		
		File a_dir = new File(m_td.getFile(), "a");
		File b_file = new File(a_dir, "B.java");
		assertTrue(b_file.exists());
		String b_contents = FileContentWorker.readContents(b_file);
		assertEquals("package a;\npublic class B implements "
				+ "incubator.scb.Scb<B> {\n"
				+ "private String m_c;\n"
				+ "private incubator.dispatch.LocalDispatcher<"
				+ "incubator.scb.ScbUpdateListener<B>> m_update_dispatcher;\n"
				+ "public B(String c) {\n"
				+ "this.m_c = c;\n"
				+ "this.m_update_dispatcher = new incubator.dispatch."
				+ "LocalDispatcher<>();\n"
				+ "}\n"
				+ "public String c() {\n"
				+ "return m_c;\n"
				+ "}\n"
				+ "public incubator.dispatch.Dispatcher<"
				+ "incubator.scb.ScbUpdateListener<B>> dispatcher() {\n"
				+ "return m_update_dispatcher;\n"
				+ "}\n"
				+ "protected void notify_update() {\n"
				+ "this.m_update_dispatcher.dispatch(new "
				+ "incubator.dispatch.DispatcherOp<"
				+ "incubator.scb.ScbUpdateListener<B>>() {\n"
				+ "@Override\n"
				+ "public void dispatch(incubator.scb.ScbUpdateListener<B>"
				+ " l) {\n"
				+ "incubator.pval.Ensure.not_null(l, \"l == null\");\n"
				+ "l.updated(B.this);\n"
				+ "}\n"
				+ "});\n"
				+ "}\n"
				+ "public static incubator.scb.ScbTextField<B> c_c() {\n"
				+ "return new incubator.scb.ScbTextField<B>(\"c\", "
				+ "false, null) {\n"
				+ "@Override\n"
				+ "public String get(B bean) {\n"
				+ "incubator.pval.Ensure.not_null(bean, \"bean == null\");\n"
				+ "return bean.c();\n"
				+ "}\n"
				+ "@Override\n"
				+ "public void set(B bean, String v) {\n"
				+ "incubator.pval.Ensure.unreachable(\"Cannot set value "
				+ "in read only field\");\n"
				+ "}\n"
				+ "};\n"
				+ "}\n"
				+ "public static java.util.List<incubator.scb.ScbField<B, ?>> "
				+ "c_fields() {\n"
				+ "java.util.List<incubator.scb.ScbField<B, ?>> fields ="
				+ " new java.util.ArrayList<>();\n"
				+ "fields.add(c_c());\n"
				+ "return fields;\n"
				+ "}\n"
				+ "public java.util.List<incubator.scb.ScbField<B, ?>> "
				+ "fields() {\n"
				+ "return c_fields();\n"
				+ "}\n"
				+ "}\n",
				b_contents);
	}
	
	@Test
	public void scb_enum_field() throws Exception {
		String text = "package a { bean B { attributes { c : enum<D>; } "
				+ "generators { class, attributes_as_fields, "
				+ "simple_constructor, simple_attribute_accessors, "
				+ "basic_scb } } }";
		m_sp.parse(text).generate(m_jc);
		m_jc.generate(m_td.getFile());
		
		File a_dir = new File(m_td.getFile(), "a");
		File b_file = new File(a_dir, "B.java");
		assertTrue(b_file.exists());
		String b_contents = FileContentWorker.readContents(b_file);
		assertEquals("package a;\npublic class B implements "
				+ "incubator.scb.Scb<B> {\n"
				+ "private D m_c;\n"
				+ "private incubator.dispatch.LocalDispatcher<"
				+ "incubator.scb.ScbUpdateListener<B>> m_update_dispatcher;\n"
				+ "public B(D c) {\n"
				+ "this.m_c = c;\n"
				+ "this.m_update_dispatcher = new incubator.dispatch."
				+ "LocalDispatcher<>();\n"
				+ "}\n"
				+ "public D c() {\n"
				+ "return m_c;\n"
				+ "}\n"
				+ "public void c(D v) {\n"
				+ "if (org.apache.commons.lang.ObjectUtils.equals(m_c, v)) {\n"
				+ "return;\n"
				+ "}\n"
				+ "this.m_c = v;\n"
				+ "notify_update();\n"
				+ "}\n"
				+ "public incubator.dispatch.Dispatcher<"
				+ "incubator.scb.ScbUpdateListener<B>> dispatcher() {\n"
				+ "return m_update_dispatcher;\n"
				+ "}\n"
				+ "protected void notify_update() {\n"
				+ "this.m_update_dispatcher.dispatch(new "
				+ "incubator.dispatch.DispatcherOp<"
				+ "incubator.scb.ScbUpdateListener<B>>() {\n"
				+ "@Override\n"
				+ "public void dispatch(incubator.scb.ScbUpdateListener<B>"
				+ " l) {\n"
				+ "incubator.pval.Ensure.not_null(l, \"l == null\");\n"
				+ "l.updated(B.this);\n"
				+ "}\n"
				+ "});\n"
				+ "}\n"
				+ "public static incubator.scb.ScbEnumField<B, D> c_c() {\n"
				+ "return new incubator.scb.ScbEnumField<B, D>(\"c\", "
				+ "true, null, D.class) {\n"
				+ "@Override\n"
				+ "public D get(B bean) {\n"
				+ "incubator.pval.Ensure.not_null(bean, \"bean == null\");\n"
				+ "return bean.c();\n"
				+ "}\n"
				+ "@Override\n"
				+ "public void set(B bean, D v) {\n"
				+ "incubator.pval.Ensure.not_null(bean, \"bean == null\");\n"
				+ "bean.c(v);\n"
				+ "}\n"
				+ "};\n"
				+ "}\n"
				+ "public static java.util.List<incubator.scb.ScbField<B, ?>> "
				+ "c_fields() {\n"
				+ "java.util.List<incubator.scb.ScbField<B, ?>> fields ="
				+ " new java.util.ArrayList<>();\n"
				+ "fields.add(c_c());\n"
				+ "return fields;\n"
				+ "}\n"
				+ "public java.util.List<incubator.scb.ScbField<B, ?>> "
				+ "fields() {\n"
				+ "return c_fields();\n"
				+ "}\n"
				+ "}\n",
				b_contents);
	}
	
	@Test
	public void scb_int_field() throws Exception {
		String text = "package a { bean B { attributes { c : int; } "
				+ "generators { class, attributes_as_fields, "
				+ "simple_constructor, simple_attribute_accessors, "
				+ "basic_scb } } }";
		m_sp.parse(text).generate(m_jc);
		m_jc.generate(m_td.getFile());
		
		File a_dir = new File(m_td.getFile(), "a");
		File b_file = new File(a_dir, "B.java");
		assertTrue(b_file.exists());
		String b_contents = FileContentWorker.readContents(b_file);
		assertEquals("package a;\npublic class B implements "
				+ "incubator.scb.Scb<B> {\n"
				+ "private int m_c;\n"
				+ "private incubator.dispatch.LocalDispatcher<"
				+ "incubator.scb.ScbUpdateListener<B>> m_update_dispatcher;\n"
				+ "public B(int c) {\n"
				+ "this.m_c = c;\n"
				+ "this.m_update_dispatcher = new incubator.dispatch."
				+ "LocalDispatcher<>();\n"
				+ "}\n"
				+ "public int c() {\n"
				+ "return m_c;\n"
				+ "}\n"
				+ "public void c(int v) {\n"
				+ "if (org.apache.commons.lang.ObjectUtils.equals(m_c, v)) {\n"
				+ "return;\n"
				+ "}\n"
				+ "this.m_c = v;\n"
				+ "notify_update();\n"
				+ "}\n"
				+ "public incubator.dispatch.Dispatcher<"
				+ "incubator.scb.ScbUpdateListener<B>> dispatcher() {\n"
				+ "return m_update_dispatcher;\n"
				+ "}\n"
				+ "protected void notify_update() {\n"
				+ "this.m_update_dispatcher.dispatch(new "
				+ "incubator.dispatch.DispatcherOp<"
				+ "incubator.scb.ScbUpdateListener<B>>() {\n"
				+ "@Override\n"
				+ "public void dispatch(incubator.scb.ScbUpdateListener<B>"
				+ " l) {\n"
				+ "incubator.pval.Ensure.not_null(l, \"l == null\");\n"
				+ "l.updated(B.this);\n"
				+ "}\n"
				+ "});\n"
				+ "}\n"
				+ "public static incubator.scb.ScbIntegerField<B> c_c() {\n"
				+ "return new incubator.scb.ScbIntegerField<B>(\"c\", "
				+ "true, null) {\n"
				+ "@Override\n"
				+ "public Integer get(B bean) {\n"
				+ "incubator.pval.Ensure.not_null(bean, \"bean == null\");\n"
				+ "return bean.c();\n"
				+ "}\n"
				+ "@Override\n"
				+ "public void set(B bean, Integer v) {\n"
				+ "incubator.pval.Ensure.not_null(bean, \"bean == null\");\n"
				+ "bean.c(v);\n"
				+ "}\n"
				+ "};\n"
				+ "}\n"
				+ "public static java.util.List<incubator.scb.ScbField<B, ?>> "
				+ "c_fields() {\n"
				+ "java.util.List<incubator.scb.ScbField<B, ?>> fields ="
				+ " new java.util.ArrayList<>();\n"
				+ "fields.add(c_c());\n"
				+ "return fields;\n"
				+ "}\n"
				+ "public java.util.List<incubator.scb.ScbField<B, ?>> "
				+ "fields() {\n"
				+ "return c_fields();\n"
				+ "}\n"
				+ "}\n",
				b_contents);
	}
	
	@Test
	public void scb_boolean_field() throws Exception {
		String text = "package a { bean B { attributes { c : boolean; } "
				+ "generators { class, attributes_as_fields, "
				+ "simple_constructor, simple_attribute_accessors, "
				+ "basic_scb } } }";
		m_sp.parse(text).generate(m_jc);
		m_jc.generate(m_td.getFile());
		
		File a_dir = new File(m_td.getFile(), "a");
		File b_file = new File(a_dir, "B.java");
		assertTrue(b_file.exists());
		String b_contents = FileContentWorker.readContents(b_file);
		assertEquals("package a;\npublic class B implements "
				+ "incubator.scb.Scb<B> {\n"
				+ "private boolean m_c;\n"
				+ "private incubator.dispatch.LocalDispatcher<"
				+ "incubator.scb.ScbUpdateListener<B>> m_update_dispatcher;\n"
				+ "public B(boolean c) {\n"
				+ "this.m_c = c;\n"
				+ "this.m_update_dispatcher = new incubator.dispatch."
				+ "LocalDispatcher<>();\n"
				+ "}\n"
				+ "public boolean c() {\n"
				+ "return m_c;\n"
				+ "}\n"
				+ "public void c(boolean v) {\n"
				+ "if (org.apache.commons.lang.ObjectUtils.equals(m_c, v)) {\n"
				+ "return;\n"
				+ "}\n"
				+ "this.m_c = v;\n"
				+ "notify_update();\n"
				+ "}\n"
				+ "public incubator.dispatch.Dispatcher<"
				+ "incubator.scb.ScbUpdateListener<B>> dispatcher() {\n"
				+ "return m_update_dispatcher;\n"
				+ "}\n"
				+ "protected void notify_update() {\n"
				+ "this.m_update_dispatcher.dispatch(new "
				+ "incubator.dispatch.DispatcherOp<"
				+ "incubator.scb.ScbUpdateListener<B>>() {\n"
				+ "@Override\n"
				+ "public void dispatch(incubator.scb.ScbUpdateListener<B>"
				+ " l) {\n"
				+ "incubator.pval.Ensure.not_null(l, \"l == null\");\n"
				+ "l.updated(B.this);\n"
				+ "}\n"
				+ "});\n"
				+ "}\n"
				+ "public static incubator.scb.ScbBooleanField<B> c_c() {\n"
				+ "return new incubator.scb.ScbBooleanField<B>(\"c\", "
				+ "true, null) {\n"
				+ "@Override\n"
				+ "public Boolean get(B bean) {\n"
				+ "incubator.pval.Ensure.not_null(bean, \"bean == null\");\n"
				+ "return bean.c();\n"
				+ "}\n"
				+ "@Override\n"
				+ "public void set(B bean, Boolean v) {\n"
				+ "incubator.pval.Ensure.not_null(bean, \"bean == null\");\n"
				+ "bean.c(v);\n"
				+ "}\n"
				+ "};\n"
				+ "}\n"
				+ "public static java.util.List<incubator.scb.ScbField<B, ?>> "
				+ "c_fields() {\n"
				+ "java.util.List<incubator.scb.ScbField<B, ?>> fields ="
				+ " new java.util.ArrayList<>();\n"
				+ "fields.add(c_c());\n"
				+ "return fields;\n"
				+ "}\n"
				+ "public java.util.List<incubator.scb.ScbField<B, ?>> "
				+ "fields() {\n"
				+ "return c_fields();\n"
				+ "}\n"
				+ "}\n",
				b_contents);
	}
	
	@Test
	public void scb_long_field() throws Exception {
		String text = "package a { bean B { attributes { c : long; } "
				+ "generators { class, attributes_as_fields, "
				+ "simple_constructor, simple_attribute_accessors, "
				+ "basic_scb } } }";
		m_sp.parse(text).generate(m_jc);
		m_jc.generate(m_td.getFile());
		
		File a_dir = new File(m_td.getFile(), "a");
		File b_file = new File(a_dir, "B.java");
		assertTrue(b_file.exists());
		String b_contents = FileContentWorker.readContents(b_file);
		assertEquals("package a;\npublic class B implements "
				+ "incubator.scb.Scb<B> {\n"
				+ "private long m_c;\n"
				+ "private incubator.dispatch.LocalDispatcher<"
				+ "incubator.scb.ScbUpdateListener<B>> m_update_dispatcher;\n"
				+ "public B(long c) {\n"
				+ "this.m_c = c;\n"
				+ "this.m_update_dispatcher = new incubator.dispatch."
				+ "LocalDispatcher<>();\n"
				+ "}\n"
				+ "public long c() {\n"
				+ "return m_c;\n"
				+ "}\n"
				+ "public void c(long v) {\n"
				+ "if (org.apache.commons.lang.ObjectUtils.equals(m_c, v)) {\n"
				+ "return;\n"
				+ "}\n"
				+ "this.m_c = v;\n"
				+ "notify_update();\n"
				+ "}\n"
				+ "public incubator.dispatch.Dispatcher<"
				+ "incubator.scb.ScbUpdateListener<B>> dispatcher() {\n"
				+ "return m_update_dispatcher;\n"
				+ "}\n"
				+ "protected void notify_update() {\n"
				+ "this.m_update_dispatcher.dispatch(new "
				+ "incubator.dispatch.DispatcherOp<"
				+ "incubator.scb.ScbUpdateListener<B>>() {\n"
				+ "@Override\n"
				+ "public void dispatch(incubator.scb.ScbUpdateListener<B>"
				+ " l) {\n"
				+ "incubator.pval.Ensure.not_null(l, \"l == null\");\n"
				+ "l.updated(B.this);\n"
				+ "}\n"
				+ "});\n"
				+ "}\n"
				+ "public static incubator.scb.ScbLongField<B> c_c() {\n"
				+ "return new incubator.scb.ScbLongField<B>(\"c\", "
				+ "true, null) {\n"
				+ "@Override\n"
				+ "public Long get(B bean) {\n"
				+ "incubator.pval.Ensure.not_null(bean, \"bean == null\");\n"
				+ "return bean.c();\n"
				+ "}\n"
				+ "@Override\n"
				+ "public void set(B bean, Long v) {\n"
				+ "incubator.pval.Ensure.not_null(bean, \"bean == null\");\n"
				+ "bean.c(v);\n"
				+ "}\n"
				+ "};\n"
				+ "}\n"
				+ "public static java.util.List<incubator.scb.ScbField<B, ?>> "
				+ "c_fields() {\n"
				+ "java.util.List<incubator.scb.ScbField<B, ?>> fields ="
				+ " new java.util.ArrayList<>();\n"
				+ "fields.add(c_c());\n"
				+ "return fields;\n"
				+ "}\n"
				+ "public java.util.List<incubator.scb.ScbField<B, ?>> "
				+ "fields() {\n"
				+ "return c_fields();\n"
				+ "}\n"
				+ "}\n",
				b_contents);
	}
	
	@Test
	public void scb_date_field() throws Exception {
		String text = "package a { bean B { attributes { c : java.util.Date; } "
				+ "generators { class, attributes_as_fields, "
				+ "simple_constructor, simple_attribute_accessors, "
				+ "basic_scb } } }";
		m_sp.parse(text).generate(m_jc);
		m_jc.generate(m_td.getFile());
		
		File a_dir = new File(m_td.getFile(), "a");
		File b_file = new File(a_dir, "B.java");
		assertTrue(b_file.exists());
		String b_contents = FileContentWorker.readContents(b_file);
		assertEquals("package a;\npublic class B implements "
				+ "incubator.scb.Scb<B> {\n"
				+ "private java.util.Date m_c;\n"
				+ "private incubator.dispatch.LocalDispatcher<"
				+ "incubator.scb.ScbUpdateListener<B>> m_update_dispatcher;\n"
				+ "public B(java.util.Date c) {\n"
				+ "this.m_c = c;\n"
				+ "this.m_update_dispatcher = new incubator.dispatch."
				+ "LocalDispatcher<>();\n"
				+ "}\n"
				+ "public java.util.Date c() {\n"
				+ "return m_c;\n"
				+ "}\n"
				+ "public void c(java.util.Date v) {\n"
				+ "if (org.apache.commons.lang.ObjectUtils.equals(m_c, v)) {\n"
				+ "return;\n"
				+ "}\n"
				+ "this.m_c = v;\n"
				+ "notify_update();\n"
				+ "}\n"
				+ "public incubator.dispatch.Dispatcher<"
				+ "incubator.scb.ScbUpdateListener<B>> dispatcher() {\n"
				+ "return m_update_dispatcher;\n"
				+ "}\n"
				+ "protected void notify_update() {\n"
				+ "this.m_update_dispatcher.dispatch(new "
				+ "incubator.dispatch.DispatcherOp<"
				+ "incubator.scb.ScbUpdateListener<B>>() {\n"
				+ "@Override\n"
				+ "public void dispatch(incubator.scb.ScbUpdateListener<B>"
				+ " l) {\n"
				+ "incubator.pval.Ensure.not_null(l, \"l == null\");\n"
				+ "l.updated(B.this);\n"
				+ "}\n"
				+ "});\n"
				+ "}\n"
				+ "public static incubator.scb.ScbDateField<B> c_c() {\n"
				+ "return new incubator.scb.ScbDateField<B>(\"c\", "
				+ "true, null) {\n"
				+ "@Override\n"
				+ "public java.util.Date get(B bean) {\n"
				+ "incubator.pval.Ensure.not_null(bean, \"bean == null\");\n"
				+ "return bean.c();\n"
				+ "}\n"
				+ "@Override\n"
				+ "public void set(B bean, java.util.Date v) {\n"
				+ "incubator.pval.Ensure.not_null(bean, \"bean == null\");\n"
				+ "bean.c(v);\n"
				+ "}\n"
				+ "};\n"
				+ "}\n"
				+ "public static java.util.List<incubator.scb.ScbField<B, ?>> "
				+ "c_fields() {\n"
				+ "java.util.List<incubator.scb.ScbField<B, ?>> fields ="
				+ " new java.util.ArrayList<>();\n"
				+ "fields.add(c_c());\n"
				+ "return fields;\n"
				+ "}\n"
				+ "public java.util.List<incubator.scb.ScbField<B, ?>> "
				+ "fields() {\n"
				+ "return c_fields();\n"
				+ "}\n"
				+ "}\n",
				b_contents);
	}
	
	@Test
	public void scb_object_field() throws Exception {
		String text = "package a { bean B { attributes { c : Foo; } "
				+ "generators { class, attributes_as_fields, "
				+ "simple_constructor, simple_attribute_accessors, "
				+ "basic_scb } } }";
		m_sp.parse(text).generate(m_jc);
		m_jc.generate(m_td.getFile());
		
		File a_dir = new File(m_td.getFile(), "a");
		File b_file = new File(a_dir, "B.java");
		assertTrue(b_file.exists());
		String b_contents = FileContentWorker.readContents(b_file);
		assertEquals("package a;\npublic class B implements "
				+ "incubator.scb.Scb<B> {\n"
				+ "private Foo m_c;\n"
				+ "private incubator.dispatch.LocalDispatcher<"
				+ "incubator.scb.ScbUpdateListener<B>> m_update_dispatcher;\n"
				+ "public B(Foo c) {\n"
				+ "this.m_c = c;\n"
				+ "this.m_update_dispatcher = new incubator.dispatch."
				+ "LocalDispatcher<>();\n"
				+ "}\n"
				+ "public Foo c() {\n"
				+ "return m_c;\n"
				+ "}\n"
				+ "public void c(Foo v) {\n"
				+ "if (org.apache.commons.lang.ObjectUtils.equals(m_c, v)) {\n"
				+ "return;\n"
				+ "}\n"
				+ "this.m_c = v;\n"
				+ "notify_update();\n"
				+ "}\n"
				+ "public incubator.dispatch.Dispatcher<"
				+ "incubator.scb.ScbUpdateListener<B>> dispatcher() {\n"
				+ "return m_update_dispatcher;\n"
				+ "}\n"
				+ "protected void notify_update() {\n"
				+ "this.m_update_dispatcher.dispatch(new "
				+ "incubator.dispatch.DispatcherOp<"
				+ "incubator.scb.ScbUpdateListener<B>>() {\n"
				+ "@Override\n"
				+ "public void dispatch(incubator.scb.ScbUpdateListener<B>"
				+ " l) {\n"
				+ "incubator.pval.Ensure.not_null(l, \"l == null\");\n"
				+ "l.updated(B.this);\n"
				+ "}\n"
				+ "});\n"
				+ "}\n"
				+ "public static incubator.scb.ScbField<B,Foo> c_c() {\n"
				+ "return new incubator.scb.ScbField<B,Foo>(\"c\", "
				+ "true, null, Foo.class) {\n"
				+ "@Override\n"
				+ "public Foo get(B bean) {\n"
				+ "incubator.pval.Ensure.not_null(bean, \"bean == null\");\n"
				+ "return bean.c();\n"
				+ "}\n"
				+ "@Override\n"
				+ "public void set(B bean, Foo v) {\n"
				+ "incubator.pval.Ensure.not_null(bean, \"bean == null\");\n"
				+ "bean.c(v);\n"
				+ "}\n"
				+ "};\n"
				+ "}\n"
				+ "public static java.util.List<incubator.scb.ScbField<B, ?>> "
				+ "c_fields() {\n"
				+ "java.util.List<incubator.scb.ScbField<B, ?>> fields ="
				+ " new java.util.ArrayList<>();\n"
				+ "fields.add(c_c());\n"
				+ "return fields;\n"
				+ "}\n"
				+ "public java.util.List<incubator.scb.ScbField<B, ?>> "
				+ "fields() {\n"
				+ "return c_fields();\n"
				+ "}\n"
				+ "}\n",
				b_contents);
	}
	
	@Test
	public void protected_setters_in_read_write_bean() throws Exception {
		String text = "package a { bean B { attributes { c : String;"
				+ "} generators { class, attributes_as_fields, "
				+ "simple_attribute_accessors (setter_protection = protected) "
				+ "} } }";
		m_sp.parse(text).generate(m_jc);
		m_jc.generate(m_td.getFile());
		
		File a_dir = new File(m_td.getFile(), "a");
		File b_file = new File(a_dir, "B.java");
		assertTrue(b_file.exists());
		String y_contents = FileContentWorker.readContents(b_file);
		assertEquals("package a;\n"
				+ "public class B {\n"
				+ "private String m_c;\n"
				+ "public String c() {\n"
				+ "return m_c;\n"
				+ "}\n"
				+ "protected void c(String v) {\n"
				+ "if (org.apache.commons.lang.ObjectUtils.equals(m_c, v)) {\n"
				+ "return;\n"
				+ "}\n"
				+ "this.m_c = v;\n"
				+ "}\n"
				+ "}\n",
				y_contents);
	}
	
	@Test
	public void private_setters_in_read_only_bean() throws Exception {
		String text = "package a { bean B { read_only; attributes { c : String;"
				+ "} generators { class, attributes_as_fields, "
				+ "simple_attribute_accessors (setter_protection = private) "
				+ "} } }";
		m_sp.parse(text).generate(m_jc);
		m_jc.generate(m_td.getFile());
		
		File a_dir = new File(m_td.getFile(), "a");
		File b_file = new File(a_dir, "B.java");
		assertTrue(b_file.exists());
		String y_contents = FileContentWorker.readContents(b_file);
		assertEquals("package a;\n"
				+ "public class B {\n"
				+ "private String m_c;\n"
				+ "public String c() {\n"
				+ "return m_c;\n"
				+ "}\n"
				+ "private void c(String v) {\n"
				+ "if (org.apache.commons.lang.ObjectUtils.equals(m_c, v)) {\n"
				+ "return;\n"
				+ "}\n"
				+ "this.m_c = v;\n"
				+ "}\n"
				+ "}\n",
				y_contents);
	}
	
	@Test
	public void cow_method() throws Exception {
		String text = "package a { bean B { attributes { c : int; } "
				+ "generators { class, attributes_as_fields, "
				+ "simple_attribute_accessors, copy_constructor, "
				+ "cow_setters } } }";
		m_sp.parse(text).generate(m_jc);
		m_jc.generate(m_td.getFile());
		
		File a_dir = new File(m_td.getFile(), "a");
		File b_file = new File(a_dir, "B.java");
		assertTrue(b_file.exists());
		String y_contents = FileContentWorker.readContents(b_file);
		assertEquals("package a;\n"
				+ "public class B {\n"
				+ "private int m_c;\n"
				+ "public int c() {\n"
				+ "return m_c;\n"
				+ "}\n"
				+ "public void c(int v) {\n"
				+ "if (org.apache.commons.lang.ObjectUtils.equals(m_c, v)) {\n"
				+ "return;\n"
				+ "}\n"
				+ "this.m_c = v;\n"
				+ "}\n"
				+ "public B(a.B src) {\n"
				+ "incubator.pval.Ensure.not_null(src, \"src == null\");\n"
				+ "this.m_c = src.m_c;\n"
				+ "}\n"
				+ "public a.B cow_c(int v) {\n"
				+ "B copy = new B(this);\n"
				+ "copy.c(v);\n"
				+ "return copy;\n"
				+ "}\n"
				+ "}\n",
				y_contents);
	}
	
	@Test
	public void mergeable_scb() throws Exception {
		String text = "package a { bean B { attributes { c : int; } "
				+ "generators { class, attributes_as_fields, "
				+ "simple_attribute_accessors, copy_constructor, "
				+ "mergeable_scb, simple_constructor } } }";
		m_sp.parse(text).generate(m_jc);
		m_jc.generate(m_td.getFile());
		
		File a_dir = new File(m_td.getFile(), "a");
		File b_file = new File(a_dir, "B.java");
		assertTrue(b_file.exists());
		String y_contents = FileContentWorker.readContents(b_file);
		assertEquals("package a;\n"
				+ "public class B implements "
				+ "incubator.scb.MergeableIdScb<a.B> {\n"
				+ "private int m_c;\n"
				+ "private int m_id;\n"
				+ "public int c() {\n"
				+ "return m_c;\n"
				+ "}\n"
				+ "public void c(int v) {\n"
				+ "if (org.apache.commons.lang.ObjectUtils.equals(m_c, v)) {\n"
				+ "return;\n"
				+ "}\n"
				+ "this.m_c = v;\n"
				+ "}\n"
				+ "public B(a.B src) {\n"
				+ "incubator.pval.Ensure.not_null(src, \"src == null\");\n"
				+ "this.m_c = src.m_c;\n"
				+ "this.m_id = src.m_id;\n"
				+ "}\n"
				+ "public int id() {\n"
				+ "return m_id;\n"
				+ "}\n"
				+ "public void merge(a.B v) {\n"
				+ "incubator.pval.Ensure.not_null(v, \"v == null\");\n"
				+ "incubator.pval.Ensure.equals(m_id, v.m_id, "
				+ "\"Objects to merge do not have the same ID.\");\n"
				+ "if (!org.apache.commons.lang.ObjectUtils.equals(m_c, "
				+ "v.m_c)) {\n"
				+ "c(v.m_c);\n"
				+ "}\n"
				+ "}\n"
				+ "public B(int c,int id) {\n"
				+ "this.m_c = c;\n"
				+ "incubator.pval.Ensure.greater(id, 0, \"id <= 0\");\n"
				+ "this.m_id = id;\n"
				+ "}\n"
				+ "}\n",
				y_contents);
	}
	
	@Test
	public void hashcode_equals() throws Exception {
		String text = "package a { bean B { attributes { c : int; } "
				+ "generators { class,attributes_as_fields,hashcode_equals}}}";
		m_sp.parse(text).generate(m_jc);
		m_jc.generate(m_td.getFile());
		
		File a_dir = new File(m_td.getFile(), "a");
		File b_file = new File(a_dir, "B.java");
		assertTrue(b_file.exists());
		String y_contents = FileContentWorker.readContents(b_file);
		assertEquals("package a;\n"
				+ "public class B {\n"
				+ "private int m_c;\n"
				+ "public int hashCode() {\n"
				+ "final int prime = 31;\n"
				+ "int result = 1;\n"
				+ "result = prime * result + org.apache.commons.lang."
						+ "ObjectUtils.hashCode(m_c);\n"
				+ "return result;\n"
				+ "}\n"
				+ "public boolean equals(Object obj) {\n"
				+ "if (this == obj) return true;\n"
				+ "if (obj == null) return false;\n"
				+ "if (getClass() != obj.getClass()) return false;\n"
				+ "a.B other = (a.B) obj;\n"
				+ "if (!org.apache.commons.lang.ObjectUtils.equals(m_c, "
				+		"other.m_c)) return false;\n"
				+ "return true;\n"
				+ "}\n"
				+ "}\n",
				y_contents);
	}
	
	@Test
	public void mergeable_inside_mergeable() throws Exception {
		String text = "package a { bean B { generators { class, "
				+ "mergeable_scb } } bean C { generators { class, "
				+ "attributes_as_fields, simple_attribute_accessors, "
				+ "mergeable_scb} attributes "
				+ "{ b : bean<B>; } } }";
		m_sp.parse(text).generate(m_jc);
		m_jc.generate(m_td.getFile());
		
		File a_dir = new File(m_td.getFile(), "a");
		File b_file = new File(a_dir, "B.java");
		assertTrue(b_file.exists());
		String b_contents = FileContentWorker.readContents(b_file);
		assertEquals("package a;\n"
				+ "public class B implements "
				+ "incubator.scb.MergeableIdScb<a.B> {\n"
				+ "private int m_id;\n"
				+ "public int id() {\n"
				+ "return m_id;\n"
				+ "}\n"
				+ "public void merge(a.B v) {\n"
				+ "incubator.pval.Ensure.not_null(v, \"v == null\");\n"
				+ "incubator.pval.Ensure.equals(m_id, v.m_id, "
				+ "\"Objects to merge do not have the same ID.\");\n"
				+ "}\n"
				+ "}\n",
				b_contents);
		File c_file = new File(a_dir, "C.java");
		assertTrue(c_file.exists());
		String c_contents = FileContentWorker.readContents(c_file);
		assertEquals("package a;\n"
				+ "public class C implements "
				+ "incubator.scb.MergeableIdScb<a.C> {\n"
				+ "private B m_b;\n"
				+ "private int m_id;\n"
				+ "public B b() {\n"
				+ "return m_b;\n"
				+ "}\n"
				+ "public void b(B v) {\n"
				+ "if (org.apache.commons.lang.ObjectUtils.equals(m_b, v)) {\n"
				+ "return;\n"
				+ "}\n"
				+ "this.m_b = v;\n"
				+ "}\n"
				+ "public int id() {\n"
				+ "return m_id;\n"
				+ "}\n"
				+ "public void merge(a.C v) {\n"
				+ "incubator.pval.Ensure.not_null(v, \"v == null\");\n"
				+ "incubator.pval.Ensure.equals(m_id, v.m_id, "
				+ "\"Objects to merge do not have the same ID.\");\n"
				+ "if (!org.apache.commons.lang.ObjectUtils.equals(m_b, "
				+ "v.m_b)) {\n"
				+ "if (m_b == null || v.m_b == null) {\n"
				+ "b(v.m_b);\n"
				+ "} else {\n"
				+ "m_b.merge(v.m_b);\n"
				+ "}\n"
				+ "}\n"
				+ "}\n"
				+ "}\n",
				c_contents);
	}
	
	@Test
	public void mergeable_without_id() throws Exception {
		String text = "package a { bean B { generators { class, "
				+ "mergeable_scb (no_id) } } }";
		m_sp.parse(text).generate(m_jc);
		m_jc.generate(m_td.getFile());
		
		File a_dir = new File(m_td.getFile(), "a");
		File b_file = new File(a_dir, "B.java");
		assertTrue(b_file.exists());
		String b_contents = FileContentWorker.readContents(b_file);
		assertEquals("package a;\n"
				+ "public class B implements "
				+ "incubator.scb.MergeableScb<a.B> {\n"
				+ "public void merge(a.B v) {\n"
				+ "incubator.pval.Ensure.not_null(v, \"v == null\");\n"
				+ "}\n"
				+ "}\n",
				b_contents);
	}
	
	@Test
	public void bean_inside_bean_simple_constructor()
			throws Exception {
		String text = "package a { bean B { generators { class, "
				+ "copy_constructor } } bean C { attributes { d : bean<B>; } "
				+ "generators { class, attributes_as_fields, "
				+ "simple_constructor } } }";
		m_sp.parse(text).generate(m_jc);
		m_jc.generate(m_td.getFile());
		
		File a_dir = new File(m_td.getFile(), "a");
		File b_file = new File(a_dir, "B.java");
		assertTrue(b_file.exists());
		String b_contents = FileContentWorker.readContents(b_file);
		assertEquals("package a;\n"
				+ "public class B {\n"
				+ "public B(a.B src) {\n"
				+ "incubator.pval.Ensure.not_null(src, \"src == null\");\n"
				+ "}\n"
				+ "}\n",
				b_contents);
		
		File c_file = new File(a_dir, "C.java");
		assertTrue(c_file.exists());
		String c_contents = FileContentWorker.readContents(c_file);
		assertEquals("package a;\n"
				+ "public class C {\n"
				+ "private B m_d;\n"
				+ "public C(B d) {\n"
				+ "this.m_d = (d == null? null : new B(d));\n"
				+ "}\n"
				+ "}\n",
				c_contents);
	}
	
	@Test
	public void bean_inside_bean_simple_getters_and_setters()
			throws Exception {
		String text = "package a { bean B { generators { class, "
				+ "copy_constructor } } bean C { attributes { d : bean<B>; } "
				+ "generators { class, attributes_as_fields, "
				+ "simple_attribute_accessors } } }";
		m_sp.parse(text).generate(m_jc);
		m_jc.generate(m_td.getFile());
		
		File a_dir = new File(m_td.getFile(), "a");
		File b_file = new File(a_dir, "B.java");
		assertTrue(b_file.exists());
		String b_contents = FileContentWorker.readContents(b_file);
		assertEquals("package a;\n"
				+ "public class B {\n"
				+ "public B(a.B src) {\n"
				+ "incubator.pval.Ensure.not_null(src, \"src == null\");\n"
				+ "}\n"
				+ "}\n",
				b_contents);
		
		File c_file = new File(a_dir, "C.java");
		assertTrue(c_file.exists());
		String c_contents = FileContentWorker.readContents(c_file);
		assertEquals("package a;\n"
				+ "public class C {\n"
				+ "private B m_d;\n"
				+ "public B d() {\n"
				+ "return (m_d == null? null : new B(m_d));\n"
				+ "}\n"
				+ "public void d(B v) {\n"
				+ "if (org.apache.commons.lang.ObjectUtils.equals(m_d, v)) {\n"
				+ "return;\n"
				+ "}\n"
				+ "this.m_d = (v == null? null : new B(v));\n"
				+ "}\n"
				+ "}\n",
				c_contents);
	}
	
	@Test
	public void bean_inside_bean_copy_constructor()
			throws Exception {
		String text = "package a { bean B { generators { class, "
				+ "copy_constructor } } bean C { attributes { d : bean<B>; } "
				+ "generators { class, attributes_as_fields, "
				+ "copy_constructor } } }";
		m_sp.parse(text).generate(m_jc);
		m_jc.generate(m_td.getFile());
		
		File a_dir = new File(m_td.getFile(), "a");
		File b_file = new File(a_dir, "B.java");
		assertTrue(b_file.exists());
		String b_contents = FileContentWorker.readContents(b_file);
		assertEquals("package a;\n"
				+ "public class B {\n"
				+ "public B(a.B src) {\n"
				+ "incubator.pval.Ensure.not_null(src, \"src == null\");\n"
				+ "}\n"
				+ "}\n",
				b_contents);
		
		File c_file = new File(a_dir, "C.java");
		assertTrue(c_file.exists());
		String c_contents = FileContentWorker.readContents(c_file);
		assertEquals("package a;\n"
				+ "public class C {\n"
				+ "private B m_d;\n"
				+ "public C(a.C src) {\n"
				+ "incubator.pval.Ensure.not_null(src, \"src == null\");\n"
				+ "this.m_d = (src.m_d == null? null : new B(src.m_d));\n"
				+ "}\n"
				+ "}\n",
				c_contents);
	}
}
