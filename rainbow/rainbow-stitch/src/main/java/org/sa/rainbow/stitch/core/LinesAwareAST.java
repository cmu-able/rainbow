/*
 * The MIT License
 *
 * Copyright 2014 CMU ABLE Group.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public int getColumn() {
        return col;
    }

    @Override
    public void initialize(Token tok) {
        super.initialize(tok);
        line = tok.getLine();
        col = tok.getColumn();

        //Tool.debug("[LinesAwareAST] Line for this node is: "+line);
    }

    @Override
    public void initialize (AST ast) {	
        super.initialize(ast);
        if (ast instanceof LinesAwareAST){
            col = ast.getColumn ();
            line = ast.getLine ();
        }
        //Tool.debug("[LinesAwareAST] Line for this node is: "+line);
    }
}
