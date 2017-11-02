package edu.cmu.cs.able.typelib;

import incubator.pval.Ensure;

import java.nio.charset.Charset;

/**
 * Class that supports ASCII encoding of strings.
 */
public class AsciiEncoding {
	/**
	 * Checks whether a string is an ASCII string or not.
	 * @param s the string
	 * @return does it have only ASCII characters?
	 */
	public static boolean is_ascii(String s) {
		Ensure.not_null(s);
		Charset cs = Charset.forName("US-ASCII");
		return cs.newEncoder().canEncode(s);
	}
}
