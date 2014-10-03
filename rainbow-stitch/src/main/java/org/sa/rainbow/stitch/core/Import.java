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
        if (str.equals("lib")) {
            t = Kind.LIB;
        }
        else if (str.equals("model")) {
            t = Kind.MODEL;
        }
        else if (str.equals("op")) {
            t = Kind.OP;
        }
        return t;
    }

    @Override
    public String toString () {
        return "import: type " + type + ", path '" + path + "'";
    }
}