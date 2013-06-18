package incubator.rcli;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import incubator.pval.Ensure;

/**
 * Class that defines the cli-encoding. The cli-encoding is the way command
 * line text is encoded. There are two levels of encoding: strict and loose
 * encoding. Encoding done by this class is always strict, but decoding will
 * decode loose as well as strict. Strict encoding is encoding
 * done by Java's <code>URLEncoder</code> class. Loose encoding allows
 * encoding of percent signs and backslashes by quoting them with a backslash.
 * Loose encoding is useful for command-line human interface. Additionally,
 * backslashed double quote characters inside double quotes are not changed. 
 */
public class CliEncoding {
	/**
	 * Encodes a string using strict encoding.
	 * @param s the string to encode (cannot be <code>null</code>)
	 * @return the encoded string
	 */
	public static String encode(String s) {
		Ensure.not_null(s);
		try {
			return URLEncoder.encode(s, "utf-8");
		} catch (UnsupportedEncodingException e) {
			Ensure.never_thrown(e);
			return null;
		}
	}
	
	/**
	 * Decodes a string.
	 * @param s the string to decode (cannot be <code>null</code>)
	 * @return the decoded string or <code>null</code> if the string is
	 * invalid
	 */
	public static String decode(String s) {
		Ensure.not_null(s);
		
		int bs_idx = 0;
		while ((bs_idx = s.indexOf('\\', bs_idx)) != -1) {
			if (bs_idx == s.length() - 1) {
				return null;
			}
			
			String repl;
			if (s.charAt(bs_idx + 1) == '%') {
				repl = "%5c%25";
			} else {
				repl = "%5c" + s.charAt(bs_idx + 1);
			}
			
			String prefix = s.substring(0, bs_idx);
			String suffix = "";
			if (bs_idx + 1 < s.length() - 1) {
				suffix = s.substring(bs_idx + 2);
			}
			
			s = prefix + repl + suffix;
			bs_idx += repl.length();
		}
		
		try {
			return URLDecoder.decode(s, "utf-8");
		} catch (UnsupportedEncodingException e) {
			Ensure.never_thrown(e);
			return null;
		}
	}
}
