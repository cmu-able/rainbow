package org.sa.rainbow.stitch.core;

import antlr.collections.AST;

/**
 * Represents an import statement.
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class Import {
	/**
	 * Defines the types of import statements.
	 */
	public enum Kind {
		UNKNOWN, LIB, MODEL, OP
	};

	public IScope scope = null;
	public Kind type = Kind.UNKNOWN; 
	public String path = null;
	public AST ast = null;

	public static Kind determineType (String str) {
		Kind t = Kind.UNKNOWN;
		if (str.equals("lib"))
			t = Kind.LIB;
		else if (str.equals("model"))
			t = Kind.MODEL;
		else if (str.equals("op"))
			t = Kind.OP;
		return t;
	}

	public String toString () {
		return "import: type " + type + ", path '" + path + "'";
	}
}