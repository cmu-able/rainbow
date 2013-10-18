/**
 * Stitch Editor
 * @author Ali Almossawi <aalossaw@cs.cmu.edu>
 * @version 0.1
 * @created July 4, 2006
 * 
 * This class extends CommonAST in order to override its getLine(), getColumn()
 * and initialize() methods and therefore be able to keep track of line numbers.
 */

package org.sa.rainbow.stitch.core;

/* ANTLR Translator Generator
 * See JavaDoc above for description
 */

import antlr.CommonAST;
import antlr.Token;
import antlr.collections.AST;

/**
 * @author Ali Almossawi
 */
public class LinesAwareAST extends CommonAST {
	private static final long serialVersionUID = -5857321948532135644L;

	private int col = 0;
	private int line = 0;
	
	public int getLine() {
		return line;
	}
	
	public int getColumn() {
		return col;
	}
	
	public void initialize(Token tok) {
		super.initialize(tok);
		line = tok.getLine();
		col = tok.getColumn();
		
		//Tool.debug("[LinesAwareAST] Line for this node is: "+line);
	}
	
	public void initialize (AST ast) {	
		super.initialize(ast);
		if (ast instanceof LinesAwareAST){
			col = ((LinesAwareAST)ast).getColumn();
			line = ((LinesAwareAST)ast).getLine();
		}
		//Tool.debug("[LinesAwareAST] Line for this node is: "+line);
	}
}
