package incubator.jcodegen;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import auxtestlib.DefaultTCase;
import auxtestlib.FileContentWorker;
import auxtestlib.TemporaryFile;

/**
 * Test cases for the {@link JavaCode} class.
 */
@SuppressWarnings("javadoc")
public class JavaCodeTests extends DefaultTCase {
	/**
	 * A temporary directory.
	 */
	private TemporaryFile m_dir;
	
	/**
	 * Prepares the test fixture.
	 * @throws Exception failed to prepare
	 */
	@Before
	public void setup() throws Exception {
		m_dir = new TemporaryFile(true);
	}
	
	/**
	 * Cleans up the test fixture.
	 * @throws Exception failed to clean up
	 */
	@After
	public void tear_down() throws Exception {
		m_dir.delete();
	}
	
	@Test
	public void generates_cleans_directory_recursively() throws Exception {
		File d = new File(m_dir.getFile(), "foo");
		d.mkdir();
		File dd = new File(d, "bar");
		FileContentWorker.set_contents(dd, "foo");
		JavaCode jc = new JavaCode();
		
		assertTrue(d.exists());
		assertTrue(dd.exists());
		jc.generate(m_dir.getFile());
		assertFalse(d.exists());
		assertFalse(dd.exists());
	}
	
	@Test
	public void create_and_find_root_package() throws Exception {
		JavaCode jc = new JavaCode();
		JavaPackage p = jc.make_package("foo");
		assertSame(p, jc.pkg("foo"));
	}
	
	@Test
	public void find_non_existent_root_package() throws Exception {
		JavaCode jc = new JavaCode();
		assertNull(jc.pkg("foo"));
	}
	
	@Test
	public void create_and_find_root_sub_package() throws Exception {
		JavaCode jc = new JavaCode();
		JavaPackage p = jc.make_package("foo");
		p = p.make_child("bar");
		assertSame(p, jc.pkg("foo.bar"));
	}
	
	@Test
	public void find_non_existent_root_sub_package() throws Exception {
		JavaCode jc = new JavaCode();
		assertNull(jc.pkg("foo.bar"));
		jc.make_package("foo");
		assertNull(jc.pkg("foo.bar"));
	}
}
