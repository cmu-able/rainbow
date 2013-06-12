package edu.cmu.cs.able.parsec;

import java.io.Reader;

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
}
