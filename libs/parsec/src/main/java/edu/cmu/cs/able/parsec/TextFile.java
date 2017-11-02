package edu.cmu.cs.able.parsec;

import incubator.pval.Ensure;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Class representing a file.
 */
class TextFile {
	/**
	 * Buffer size.
	 */
	private static final int BUFFER_SIZE = 1024;
	
	/**
	 * The file itself.
	 */
	private File m_file;
	
	/**
	 * The file's text.
	 */
	private String m_text;
	
	/**
	 * Creates a new file.
	 * @param file the file
	 * @throws IOException failed to read the file
	 */
	TextFile(File file) throws IOException {
		Ensure.not_null(file);
		m_file = file;
		
		StringBuilder builder = new StringBuilder();
		try (FileReader fr = new FileReader(file)) {
			char[] buf = new char[BUFFER_SIZE];
			int r;
			while ((r = fr.read(buf)) >= 0) {
				builder.append(buf, 0, r);
			}
		}
		
		m_text = builder.toString();
	}
	
	/**
	 * Obtains the file system file from which this file was read. 
	 * @return the file
	 */
	public File file() {
		return m_file;
	}
	
	/**
	 * Obtains the file's text.
	 * @return the text
	 */
	public String text() {
		return m_text;
	}
}
