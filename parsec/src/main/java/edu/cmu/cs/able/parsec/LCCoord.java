package edu.cmu.cs.able.parsec;

import incubator.pval.Ensure;

/**
 * Line/column coordinate. Lines and columns start at 1 and they are used in
 * error reporting as users prefer, for some weird reason, to know line/column
 * instead of file offset. Instances of <code>LCCoord</code> are immutable.
 */
public class LCCoord {
	/**
	 * The line number.
	 */
	private int m_line;
	
	/**
	 * The column number.
	 */
	private int m_column;
	
	/**
	 * Creates a new coordinate.
	 * @param line the line number which must be greater than or equal to
	 * <code>0</code>; of <code>0</code>, it will be interpreted as
	 * <code>1</code> (this is included to simplify code from
	 * <em>javacc</em>)
	 * @param column the column number which must be greater than or equal to
	 * <code>0</code>; of <code>0</code>, it will be interpreted as
	 * <code>1</code> (this is included to simplify code from
	 * <em>javacc</em>)
	 */
	public LCCoord(int line, int column) {
		Ensure.greater_equal(line, 0);
		Ensure.greater_equal(column, 0);
		
		m_line = line;
		m_column = column;
		
		if (m_line == 0) {
			m_line = 1;
		}
		
		if (m_column == 0) {
			m_column = 1;
		}
	}
	
	/**
	 * Obtains the line number.
	 * @return the line number
	 */
	public int line() {
		return m_line;
	}
	
	/**
	 * Obtains the column number.
	 * @return the column number
	 */
	public int column() {
		return m_column;
	}
	
	@Override
	public String toString() {
		return m_line + ":" + m_column;
	}
}
