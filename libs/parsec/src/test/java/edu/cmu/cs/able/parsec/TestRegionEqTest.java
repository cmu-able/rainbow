package edu.cmu.cs.able.parsec;

import java.io.Reader;

import org.apache.commons.lang.SystemUtils;
import org.junit.Before;
import org.junit.Test;

import auxtestlib.DefaultTCase;
import auxtestlib.FileContentWorker;
import auxtestlib.TemporaryFile;

/**
 * Equivalence class tests for the test region.
 */
@SuppressWarnings("javadoc")
public class TestRegionEqTest extends DefaultTCase {
	private TemporaryFile m_temporary;
	
	@Before
	public void set_up() throws Exception {
		m_temporary = new TemporaryFile(false);
	}
	
	@Test
	public void get_basic_region_data() throws Exception {
		FileContentWorker.setContents(m_temporary.getFile(), "");
		TextFile file = new TextFile(m_temporary.getFile());
		TextRegion reg = new TextRegion(file);
		assertEquals(file, reg.file());
		assertEquals(0, reg.start());
	}
	
	@Test
	public void obtain_region_end_empty_text() throws Exception {
		FileContentWorker.setContents(m_temporary.getFile(), "");
		TextRegion reg = new TextRegion(new TextFile(m_temporary.getFile()));
		assertEquals(0, reg.end());
	}
	
	@Test
	public void obtain_region_end_text_no_new_lines() throws Exception {
		FileContentWorker.setContents(m_temporary.getFile(), "6chars");
		TextRegion reg = new TextRegion(new TextFile(m_temporary.getFile()));
		assertEquals(6, reg.end());
	}
	
	@Test
	public void obtain_region_end_text_new_lines() throws Exception {
		FileContentWorker.setContents(m_temporary.getFile(), "6chars"
				+ SystemUtils.LINE_SEPARATOR + "baz");
		TextRegion r1 = new TextRegion(new TextFile(m_temporary.getFile()));
		assertEquals(10, r1.end());
	}
	
	@Test
	public void obtain_text_from_region() throws Exception {
		FileContentWorker.setContents(m_temporary.getFile(), "foobar");
		TextRegion r1 = new TextRegion(new TextFile(m_temporary.getFile()));
		try (Reader r = r1.reader()) {
			assertEquals('f', r.read());
			assertEquals('o', r.read());
			assertEquals('o', r.read());
			assertEquals('b', r.read());
			assertEquals('a', r.read());
			assertEquals('r', r.read());
			assertEquals(-1, r.read());
		}
	}
}
