package incubator.jcodegen;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import auxtestlib.DefaultTCase;
import auxtestlib.FileContentWorker;
import auxtestlib.TemporaryFile;

/**
 * Unit tests for the {@link JavaClass} class.
 */
@SuppressWarnings("javadoc")
public class JavaClassTests extends DefaultTCase {
	/**
	 * A test package.
	 */
	private JavaPackage m_pkg;
	
	/**
	 * Temporary file.
	 */
	private TemporaryFile m_dir;
	
	/**
	 * The java code.
	 */
	private JavaCode m_jc;
	
	/**
	 * Prepares the test fixture.
	 * @throws Exception failed to set up
	 */
	@Before
	public void setup() throws Exception {
		m_dir = new TemporaryFile(true);
		m_jc = new JavaCode();
		m_pkg = m_jc.make_package("x");
	}
	
	/**
	 * Cleans up after the test case.
	 * @throws Exception failed to clean up
	 */
	@After
	public void tear_down() throws Exception {
		m_dir.delete();
	}
	
	@Test
	public void obtain_class_data() throws Exception {
		JavaClass c1 = m_pkg.make_class("c1", null);
		assertEquals("c1", c1.name());
		assertSame(m_pkg, c1.pkg());
		assertNull(c1.super_class());
	}
	
	@Test
	public void obtain_super_class() throws Exception {
		JavaClass c1 = m_pkg.make_class("c1", null);
		JavaClass c2 = m_pkg.make_class("c2", c1);
		assertEquals("c2", c2.name());
		assertSame(m_pkg, c2.pkg());
		assertSame(c1, c2.super_class());
	}
	
	@Test
	public void create_field() throws Exception {
		JavaClass c1 = m_pkg.make_class("c1", null);
		assertEquals(0, c1.field_names().size());
		JavaType jt = new JavaType("int");
		JavaField f1 = c1.make_field("f1", jt);
		assertNotNull(f1);
		assertEquals("f1", f1.name());
		assertSame(jt, f1.type());
		assertEquals(1, c1.field_names().size());
		assertTrue(c1.field_names().contains("f1"));
		f1 = c1.field("f1");
		assertNotNull(f1);
		assertEquals("f1", f1.name());
		assertSame(jt, f1.type());
	}
	
	@Test
	public void find_non_existing_field() throws Exception {
		JavaClass c1 = m_pkg.make_class("c1", null);
		assertNull(c1.field("foo"));
	}
	
	@Test
	public void create_method() throws Exception {
		JavaClass c1 = m_pkg.make_class("c1", null);
		assertEquals(0, c1.methods().size());
		JavaType xpto = new JavaType("xpto");
		JavaMethod m1 = c1.make_method("m1", xpto);
		assertEquals(1, c1.methods().size());
		assertSame(m1, c1.methods().get(0));
		assertEquals("m1", c1.methods().get(0).name());
		assertSame(xpto, c1.methods().get(0).type());
	}
	
	@Test
	public void create_method_with_arguments() throws Exception {
		JavaClass c1 = m_pkg.make_class("c1", null);
		assertEquals(0, c1.methods().size());
		JavaType xpto = new JavaType("xpto");
		JavaMethod m1 = c1.make_method("m1", xpto);
		assertEquals(0, m1.parameters().size());
		JavaType b = new JavaType("b");
		JavaMethodParameter p1 = m1.make_parameter("a", b);
		assertEquals(1, m1.parameters().size());
		assertSame(p1, m1.parameters().get(0));
		assertEquals("a", p1.name());
		assertSame(b, p1.type());
	}
	
	@Test
	public void add_interfaces_to_class() throws Exception {
		JavaClass c1 = m_pkg.make_class("c1", null);
		assertEquals(0, c1.interfaces().size());
		c1.add_implements("i1");
		assertEquals(1, c1.interfaces().size());
		assertEquals("i1", c1.interfaces().get(0));
		c1.add_implements("i2");
		assertEquals(2, c1.interfaces().size());
		assertEquals("i2", c1.interfaces().get(1));
	}
	
	@Test
	public void make_static_method() throws Exception {
		JavaClass c1 = m_pkg.make_class("c1", null);
		JavaMethod m1 = c1.make_method("foo", new JavaType("bar"));
		assertFalse(m1.is_static());
		m1.set_static();
		assertTrue(m1.is_static());
	}
	
	@Test
	public void generate_simple_class() throws Exception {
		m_pkg.make_class("c1", null);
		m_jc.generate(m_dir.getFile());
		File c1f = new File(new File(m_dir.getFile(), "x"), "c1.java");
		String c1c = FileContentWorker.read_contents(c1f);
		assertEquals("package x;\npublic class c1 {\n}\n", c1c);
	}
	
	@Test
	public void generate_class_with_two_fields() throws Exception {
		JavaClass c1 = m_pkg.make_class("c1", null);
		c1.make_field("foo", new JavaType("char"));
		c1.make_field("bar", new JavaType("long"));
		
		m_jc.generate(m_dir.getFile());
		File c1f = new File(new File(m_dir.getFile(), "x"), "c1.java");
		String c1c = FileContentWorker.read_contents(c1f);
		assertEquals("package x;\npublic class c1 {\nprivate char foo;\n"
				+ "private long bar;\n}\n", c1c);
		
	}
	
	@Test
	public void generate_class_with_method() throws Exception {
		JavaClass c1 = m_pkg.make_class("c1", null);
		c1.make_method("foo", new JavaType("bar"));
		
		m_jc.generate(m_dir.getFile());
		File c1f = new File(new File(m_dir.getFile(), "x"), "c1.java");
		String c1c = FileContentWorker.read_contents(c1f);
		assertEquals("package x;\npublic class c1 {\npublic bar foo() {\n}\n"
				+ "}\n", c1c);
	}
	
	@Test
	public void generate_class_with_method_with_2_parameters()
			throws Exception {
		JavaClass c1 = m_pkg.make_class("c1", null);
		JavaMethod m = c1.make_method("foo", new JavaType("bar"));
		m.make_parameter("a", new JavaType("b"));
		m.make_parameter("c", new JavaType("d"));
		
		m_jc.generate(m_dir.getFile());
		File c1f = new File(new File(m_dir.getFile(), "x"), "c1.java");
		String c1c = FileContentWorker.read_contents(c1f);
		assertEquals("package x;\npublic class c1 {\npublic bar foo("
				+ "b a,d c) {\n}\n}\n", c1c);
	}
	
	@Test
	public void generate_class_with_method_with_contents() throws Exception {
		JavaClass c1 = m_pkg.make_class("c1", null);
		JavaMethod m = c1.make_method("foo", new JavaType("bar"));
		m.append_contents("// This is stuff.\n");
		
		m_jc.generate(m_dir.getFile());
		File c1f = new File(new File(m_dir.getFile(), "x"), "c1.java");
		String c1c = FileContentWorker.read_contents(c1f);
		assertEquals("package x;\npublic class c1 {\npublic bar foo() {\n"
				+ "// This is stuff.\n}\n}\n", c1c);
	}
	
	@Test
	public void generate_constructor() throws Exception {
		JavaClass c1 = m_pkg.make_class("c1", null);
		c1.make_method("c1", null);
		
		m_jc.generate(m_dir.getFile());
		File c1f = new File(new File(m_dir.getFile(), "x"), "c1.java");
		String c1c = FileContentWorker.read_contents(c1f);
		assertEquals("package x;\npublic class c1 {\npublic c1() {\n"
				+ "}\n}\n", c1c);
	}
	
	@Test
	public void generate_one_interface() throws Exception {
		JavaClass c1 = m_pkg.make_class("c1", null);
		c1.add_implements("foo");
		
		m_jc.generate(m_dir.getFile());
		File c1f = new File(new File(m_dir.getFile(), "x"), "c1.java");
		String c1c = FileContentWorker.read_contents(c1f);
		assertEquals("package x;\npublic class c1 implements foo {\n"
				+ "}\n", c1c);
	}
	
	@Test
	public void generate_two_interfaces() throws Exception {
		JavaClass c1 = m_pkg.make_class("c1", null);
		c1.add_implements("foo");
		c1.add_implements("bar");
		
		m_jc.generate(m_dir.getFile());
		File c1f = new File(new File(m_dir.getFile(), "x"), "c1.java");
		String c1c = FileContentWorker.read_contents(c1f);
		assertEquals("package x;\npublic class c1 implements foo, bar {\n"
				+ "}\n", c1c);
	}
	
	@Test
	public void generate_static_method() throws Exception {
		JavaClass c1 = m_pkg.make_class("c1", null);
		JavaMethod m1 = c1.make_method("foo", new JavaType("bar"));
		m1.set_static();
		
		m_jc.generate(m_dir.getFile());
		File c1f = new File(new File(m_dir.getFile(), "x"), "c1.java");
		String c1c = FileContentWorker.read_contents(c1f);
		assertEquals("package x;\npublic class c1 {\npublic static bar "
				+ "foo() {\n}\n}\n", c1c);
	}
	
	@Test
	public void generate_private_method() throws Exception {
		JavaClass c1 = m_pkg.make_class("c1", null);
		JavaMethod m1 = c1.make_method("foo", new JavaType("bar"));
		m1.protection(ProtectionLevel.PRIVATE);
		
		m_jc.generate(m_dir.getFile());
		File c1f = new File(new File(m_dir.getFile(), "x"), "c1.java");
		String c1c = FileContentWorker.read_contents(c1f);
		assertEquals("package x;\npublic class c1 {\nprivate bar "
				+ "foo() {\n}\n}\n", c1c);
	}
	
	@Test
	public void generate_protected_method() throws Exception {
		JavaClass c1 = m_pkg.make_class("c1", null);
		JavaMethod m1 = c1.make_method("foo", new JavaType("bar"));
		m1.protection(ProtectionLevel.PROTECTED);
		
		m_jc.generate(m_dir.getFile());
		File c1f = new File(new File(m_dir.getFile(), "x"), "c1.java");
		String c1c = FileContentWorker.read_contents(c1f);
		assertEquals("package x;\npublic class c1 {\nprotected bar "
				+ "foo() {\n}\n}\n", c1c);
	}
	
	@Test
	public void generate_package_method() throws Exception {
		JavaClass c1 = m_pkg.make_class("c1", null);
		JavaMethod m1 = c1.make_method("foo", new JavaType("bar"));
		m1.protection(ProtectionLevel.PACKAGE);
		
		m_jc.generate(m_dir.getFile());
		File c1f = new File(new File(m_dir.getFile(), "x"), "c1.java");
		String c1c = FileContentWorker.read_contents(c1f);
		assertEquals("package x;\npublic class c1 {\n bar "
				+ "foo() {\n}\n}\n", c1c);
	}
	
	@Test
	public void generate_class_with_parent() throws Exception {
		JavaClass c1 = m_pkg.make_class("c1", null);
		m_pkg.make_class("c2", c1);
		
		m_jc.generate(m_dir.getFile());
		File c1f = new File(new File(m_dir.getFile(), "x"), "c1.java");
		String c1c = FileContentWorker.read_contents(c1f);
		assertEquals("package x;\npublic class c1 {\n"
				+ "}\n", c1c);
		File c2f = new File(new File(m_dir.getFile(), "x"), "c2.java");
		String c2c = FileContentWorker.read_contents(c2f);
		assertEquals("package x;\npublic class c2 extends x.c1 {\n"
				+ "}\n", c2c);
	}
	
	@Test
	public void generate_class_with_method_with_added_contents()
			throws Exception {
		JavaClass c1 = m_pkg.make_class("c1", null);
		JavaMethod m = c1.make_method("foo", new JavaType("bar"));
		m.append_contents("// This is stuff.\n");
		m.append_contents("foo\n");
		assertTrue(m.append_contents_before("bar\n", "^foo.*"));
		assertFalse(m.append_contents_before("bar\n", "^fooo.*"));
		
		m_jc.generate(m_dir.getFile());
		File c1f = new File(new File(m_dir.getFile(), "x"), "c1.java");
		String c1c = FileContentWorker.read_contents(c1f);
		assertEquals("package x;\npublic class c1 {\npublic bar foo() {\n"
				+ "// This is stuff.\nbar\nfoo\n}\n}\n", c1c);
	}
}
