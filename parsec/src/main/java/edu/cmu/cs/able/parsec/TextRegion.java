package edu.cmu.cs.able.parsec;

import incubator.pval.Ensure;

import java.io.Reader;
import java.io.StringReader;

/**
 * A region is a part of the text being parsed which comes all from the same
 * file but is not necessarily the whole file. Regions are necessary to support
 * includes.
 */
class TextRegion {
	/**
	 * The text file where the region text comes from.
	 */
	private TextFile m_file;
	
	/**
	 * Index within the text file where this region starts.
	 */
	int m_start;
	
	/**
	 * The region's text.
	 */
	private String m_text;
	
	/**
	 * Creates a new region representing a whole file.
	 * @param file the file
	 */
	TextRegion(TextFile file) {
		Ensure.not_null(file);
		
		m_file = file;
		m_start = 0;
		m_text = file.text();
	}
	
	/**
	 * Creates a new region representing an in-memory string.
	 * @param text the text
	 */
	TextRegion(String text) {
		Ensure.not_null(text);
		
		m_file = null;
		m_start = 0;
		m_text = text;
	}
	
	/**
	 * Obtains the file from where the region text came.
	 * @return the file or <code>null</code> if parsing in-memory text
	 */
	public TextFile file() {
		return m_file;
	}
	
	/**
	 * Obtains the index within the text file where the region came from.
	 * @return the index
	 */
	public int start() {
		return m_start;
	}
	
	/**
	 * Obtains the index within the text file where this region ends.
	 * @return the index
	 */
	public int end() {
		return m_start + m_text.length();
	}
	
	/**
	 * Obtains the region's text.
	 * @return the text
	 */
	public String text() {
		return m_text;
	}
	
	/**
	 * Obtains a reader that reads the whole region's text.
	 * @return a reader
	 */
	public Reader reader() {
		return new StringReader(m_text);
	}
}
