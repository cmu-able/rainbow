package edu.cmu.cs.able.typelib.txtenc;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

import incubator.pval.Ensure;
import edu.cmu.cs.able.typelib.scope.HierarchicalName;

/**
 * Class that is capable to encoding a hierarchical name in an ASCII string.
 */
public class HNameAsciiEncoding {
	/**
	 * Divider between individual elements in the path.
	 */
	public static final String TYPE_NAME_DIVIDER = "/";
	
	/**
	 * Creates a new encoding.
	 */
	public HNameAsciiEncoding() {
	}
	
	/**
	 * Encodes a hierarchical name.
	 * @param hn the name
	 * @return a string representation of the hierarchical name
	 */
	public String encode(HierarchicalName hn) {
		Ensure.not_null(hn);
		
		StringBuilder sb = new StringBuilder();
		if (hn.absolute()) {
			sb.append(TYPE_NAME_DIVIDER);
		}
		
		for (; hn != null; hn = hn.pop_first()) {
			String p = hn.peek();
			Ensure.is_false(p.contains(TYPE_NAME_DIVIDER));
			
			sb.append(hn.peek());
			if (!hn.leaf()) {
				sb.append(TYPE_NAME_DIVIDER);
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * Decodes a hierarchical name.
	 * @param n the string representation of the hierarchical name
	 * @return the decoded hierarchical name
	 */
	public HierarchicalName decode(String n) {
		Ensure.not_null(n);
		Ensure.greater(n.length(), 0);
		
		boolean abs = false;
		if (n.startsWith(TYPE_NAME_DIVIDER)) {
			abs = true;
			n = n.substring(TYPE_NAME_DIVIDER.length());
			Ensure.greater(n.length(), 0);
		}
		
		String[] split = StringUtils.splitByWholeSeparatorPreserveAllTokens(n,
				TYPE_NAME_DIVIDER);
		return new HierarchicalName(abs, Arrays.asList(split));
	}
}
