package edu.cmu.cs.able.typelib.txtenc.typelib;

import incubator.pval.Ensure;

import java.io.IOException;
import java.io.Reader;

import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;

/**
 * Utility class to read information.
 */
public class ReaderUtils {
	/**
	 * Utility class: no constructor.
	 */
	private ReaderUtils() {
	}
	
	/**
	 * Reads characters from a reader until a string is read.
	 * @param r the reader to read from
	 * @param limit the last string to read
	 * @return the characters read without the limit string
	 * @throws IOException failed to read from the reader
	 * @throws InvalidEncodingException EOF reached before the limit string was
	 * found
	 */
	public static String read_until(Reader r, String limit) throws IOException,
			InvalidEncodingException {
		Ensure.not_null(r);
		Ensure.not_null(limit);
		Ensure.greater(limit.length(), 0);
		
		StringBuilder s = new StringBuilder();
		while (!(s.toString().endsWith(limit))) {
			int c = r.read();
			if (c == -1) {
				throw new InvalidEncodingException("EOF reached before '"
						+ limit + "' was read.");
			}
			
			s.append((char) c);
		}
		
		return s.toString().substring(0, s.length() - limit.length());
	}
}
