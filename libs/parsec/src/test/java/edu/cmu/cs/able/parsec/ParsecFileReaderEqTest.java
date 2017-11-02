package edu.cmu.cs.able.parsec;

import java.io.Reader;

import org.apache.commons.lang.SystemUtils;
import org.junit.Test;

import auxtestlib.DefaultTCase;
import auxtestlib.FileContentWorker;
import auxtestlib.TemporaryFile;

/**
 * Equivalence class tests for the parsed file reader.
 */
@SuppressWarnings("javadoc")
public class ParsecFileReaderEqTest extends DefaultTCase {
	@Test
	public void read_file() throws Exception {
		String text = "Text";
		TemporaryFile tf = new TemporaryFile(false);
		FileContentWorker.setContents(tf.getFile(), text);
		
		ParsecFileReader reader = new ParsecFileReader();
		TextContainer container = reader.read(tf.getFile());
		try (Reader rd = container.reader()) {
			assertEquals('T', rd.read());
			assertEquals('e', rd.read());
			assertEquals('x', rd.read());
			assertEquals('t', rd.read());
			assertEquals(-1, rd.read());
		}
	}
	
	@Test
	public void locate_in_file() throws Exception {
		String text = "Some" + SystemUtils.LINE_SEPARATOR + "Text.";
		TemporaryFile tf = new TemporaryFile(false);
		FileContentWorker.setContents(tf.getFile(), text);
		
		ParsecFileReader reader = new ParsecFileReader();
		TextContainer container = reader.read(tf.getFile());
		TextRegionMatch m = container.locate(new LCCoord(2, 3));
		assertEquals("" + tf.getFile().getAbsolutePath() + ":2:3",
				m.toString());
	}
	
	@Test
	public void locate_in_memory() throws Exception {
		String text = "Some" + SystemUtils.LINE_SEPARATOR + "Text.";
		ParsecFileReader reader = new ParsecFileReader();
		TextContainer container = reader.read_memory(text);
		TextRegionMatch m = container.locate(new LCCoord(2, 3));
		assertEquals(TextRegionMatch.NO_FILE_NAME + ":2:3", m.toString());
	}
}
