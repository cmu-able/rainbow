package incubator.jcodegen;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import auxtestlib.DefaultTCase;
import auxtestlib.TemporaryFile;

/**
 * Tests for the {@link JavaPackage} class.
 */
@SuppressWarnings("javadoc")
public class JavaPackageTests extends DefaultTCase {
	/**
	 * The java code.
	 */
	private JavaCode m_jc;
	
	/**
	 * A temporary directory.
	 */
	private TemporaryFile m_dir;
	
	/**
	 * A package.
	 */
	private JavaPackage m_p;
	
	/**
	 * Prepares the code fixture.
	 * @throws Exception preparation failed
	 */
	@Before
	public void setup() throws Exception {
		m_jc = new JavaCode();
		m_dir = new TemporaryFile(true);
		m_p = m_jc.make_package("p");
	}
	
	/**
	 * Tears down the code fixture.
	 * @throws Exception failed to tear down
	 */
	@After
	public void tear_down() throws Exception {
		m_dir.delete();
	}
	
	@Test
	public void create_find_existing_child_package() throws Exception {
		JavaPackage p = m_p.make_child("foo");
		assertSame(p, m_p.child("foo"));
	}
	
	@Test
	public void find_non_existing_child_package() throws Exception {
		assertNull(m_p.child("foo"));
	}
	
	@Test
	public void create_find_existing_child_sub_package() throws Exception {
		JavaPackage p = m_p.make_child("foo");
		JavaPackage pp = p.make_child("bar");
		assertSame(pp, m_p.child("foo.bar"));
	}
	
	@Test
	public void find_non_existing_child_sub_package() throws Exception {
		assertNull(m_p.child("foo.bar"));
		m_p.make_child("foo");
		assertNull(m_p.child("foo.bar"));
	}
	
	@Test
	public void get_package_name() throws Exception {
		assertEquals("p", m_p.name());
	}
	
	@Test
	public void get_child_package_name() throws Exception {
		JavaPackage p = m_p.make_child("q");
		assertEquals("p", m_p.name());
		assertEquals("q", p.name());
	}
	
	@Test
	public void get_package_fqn() throws Exception {
		assertEquals("p", m_p.fqn());
	}
	
	@Test
	public void get_child_package_fqn() throws Exception {
		JavaPackage p = m_p.make_child("q");
		assertEquals("p", m_p.fqn());
		assertEquals("p.q", p.fqn());
	}
	
	@Test
	public void get_root_parent() throws Exception {
		assertNull(m_p.parent());
	}
	
	@Test
	public void get_child_parent() throws Exception {
		JavaPackage p = m_p.make_child("x");
		assertSame(m_p, p.parent());
	}
	
	@Test
	public void find_existing_class() throws Exception {
		JavaPackage p = m_p.make_child("x");
		p.make_class("foo");
		assertNotNull(p.child_class("foo"));
		assertEquals("foo", p.child_class("foo").name());
	}
	
	@Test
	public void find_non_existing_class() throws Exception {
		JavaPackage p = m_p.make_child("x");
		assertNull(p.child_class("foo"));
	}
	
	@Test
	public void generate_with_children() throws Exception {
		m_p.make_child("x");
		m_jc.generate(m_dir.getFile());
		assertTrue(new File(m_dir.getFile(), "p").exists());
		assertTrue(new File(m_dir.getFile(), "p").isDirectory());
		assertTrue(new File(new File(m_dir.getFile(), "p"), "x").exists());
		assertTrue(new File(new File(m_dir.getFile(), "p"), "x").isDirectory());
	}
}
