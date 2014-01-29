package incubator;

import java.io.File;
import java.util.Set;

import org.junit.Test;

import auxtestlib.DefaultTCase;
import auxtestlib.FileContentWorker;
import auxtestlib.TemporaryFile;

/**
 * Test case for the directory reader class.
 */
public class DirectoryReaderTest extends DefaultTCase {
	/**
	 * Reads the contents of an empty directory.
	 * @throws Exception test failed
	 */
	@Test
	public void readEmptyDirectory() throws Exception {
		TemporaryFile tf = new TemporaryFile(true);
		Set<File> f = DirectoryReader.listAllRecursively(tf.getFile());
		assertEquals(0, f.size());
		tf.delete();
	}
	
	/**
	 * Reads the contents of a directory that has 2 files. 
	 * @throws Exception test failed
	 */
	@Test
	public void readDirectoryWithFiles() throws Exception {
		TemporaryFile tf = new TemporaryFile(true);
		File f1 = new File(tf.getFile(), "x");
		FileContentWorker.set_contents(f1, "x");
		File f2 = new File(tf.getFile(), "y");
		FileContentWorker.set_contents(f2, "y");
		Set<File> f = DirectoryReader.listAllRecursively(tf.getFile());
		assertEquals(2, f.size());
		assertTrue(f.contains(f1));
		assertTrue(f.contains(f2));
		tf.delete();
	}
	
	/**
	 * Reads the contents of a directory with a subdirectory with one
	 * file.
	 * @throws Exception test failed
	 */
	@Test
	public void readDirectoryWithSubdirectory() throws Exception {
		TemporaryFile tf = new TemporaryFile(true);
		File f1 = new File(tf.getFile(), "x");
		FileContentWorker.set_contents(f1, "x");
		File d = new File(tf.getFile(), "z");
		d.mkdir();
		File f2 = new File(d, "y");
		FileContentWorker.set_contents(f2, "y");
		Set<File> f = DirectoryReader.listAllRecursively(tf.getFile());
		assertEquals(2, f.size());
		assertTrue(f.contains(f1));
		assertTrue(f.contains(f2));
		tf.delete();
	}
}
