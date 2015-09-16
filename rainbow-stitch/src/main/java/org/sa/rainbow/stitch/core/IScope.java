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
 * Created March 15, 2006
 */
package org.sa.rainbow.stitch.core;

import org.sa.rainbow.stitch.visitor.Stitch;

import java.util.List;
import java.util.Map;


/**
 * Interface to a scope object, allowing the scope to be queried, parent of the
 * scope to be traversed...  The root scope should be the only scope to return
 * <code>true</code> to the isRoot() query.
 * 
 * Design note:  As a reference is created, it should be accounted for in
 * the scope (tracked within the tree parser) in which it resides, that way,
 * when it is "accesed" elsewhere, the reference can be found with a scope search...
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public interface IScope {

    /**
     * Returns the name of this scope, if any, <code>null</code> if unnamed
     * @return  the name string
     */
    String getName ();

    /**
     * Returns the fully qualified name of this scope, if any, <code>null</code>
     * if this scope is unnamed.  This is equivalent to dot-concatenating
     * stitch().script.getName() with this.getName(), unless this IScope is
     * the script itself, in which case this is equivalent to this.getName().
     * @return  the name string
     */
    String getQualifiedName ();

    /**
     * Sets the name of this scope.
     * @param name  the new name of the scope
     */
    void setName (String name);

    /**
     * Returns the level number of this scope.
     * @return  the level number
     */
    int getLevel ();

    /**
     * Sets the level number of this scope, used primarily for indenting. 
     * @param l  the new level number to set
     */
    void setLevel (int l);

    /**
     * Returns whether this scope is the mother of all scopes.
     * @return  <code>true</code> if this scope is the root scope, <code>false</code> otherwise
     */
    boolean isRoot ();

    /**
     * Returns whether this scope is a distinct scope.
     * @return  <code>true</code> if this scope is a distinctive scope, <code>false</code> otherwise
     */
    boolean isDistinctScope ();

    /**
     * Sets flag of whether this scope is a distinctive scope 
     * @param b  boolean flag
     */
    void setDistinctScope (boolean b);

    /**
     * Returns whether an error has occurred within this scope
     * @return  <code>true</code> if this scope has error, <code>false</code> otherwise
     */
    boolean hasError ();

    /**
     * Sets flag of whether this scope has error
     * @param b  boolean flag
     */
    void setError (boolean b);

    /**
     * Returns whether scope contains a error handler
     * @return  <code>true</code> if this scope has error, <code>false</code> otherwise
     */
    boolean hasErrorHandler ();

    /**
     * Sets flag of whether this scope has error handler
     * @param b  boolean flag
     */
    void setHasErrorHandler (boolean b);

    /**
     * Returns the Stitch parsing and evaluation context.
     * @return  the <code>Stitch</code> object
     */
    Stitch stitch ();

    /**
     * Sets a new Stitch context object
     * @param s  the new <code>Stitch</code> object to use for evaluation
     */
    void setStitch (Stitch s);

    /**
     * Returns the parent scope of this scope
     * @return  the parent scope
     */
    IScope parent ();

    /**
     * Returns the hash of variables declared in this scope
     * @return  the variables hash map
     */
    Map<String, Var> vars ();

    /**
     * Returns the list of expressions defined in this scope
     * @return  the list of expressions
     */
    List<Expression> expressions ();

    /**
     * Returns the list of statements defined in this scope
     * @return  the list of statements
     */
    List<Statement> statements ();

    /**
     * Adds a child scope to the list of known scopes in this scope.
     * @param child  the child scope to add
     */
    void addChildScope (IScope child);

    /**
     * Adds a variable declaration to the list of known vars in this scope,
     * if it is distinct, or parent scope otherwise.
     * If duplicate variable name, then return false and no add happens.
     * @param id   the variable identifier
     * @param var  the created Var object to add to list
     * @return boolean  <code>true</code> if addition successful, <code>false</code> otherwise
     */
    boolean addVar (String id, Var var);

    /**
     * Adds an expression to the sequential list of expressions in this scope
     * @param expr  the Expression to add
     */
    void addExpression (Expression expr);

    /**
     * Adds a statement to the sequential list of statements in this scope
     * @param stmt  the Statement to add
     */
    void addStatement (Statement stmt);

    /**
     * Searches for a reference within this scope, moving up if necessary
     * @param  string identifier of the sought reference
     * @return  the IScope within which the reference was first declared, or
     *   <code>null</code> if not found anywhere
     */
    Object lookup (String name);

    /**
     * Returns a string representation of the tree of scopes rooted at this node.
     * @return String representing the tree of scopes
     */
    String toStringTree ();

    /**
     * Returns a string reflecting the scoping depth for output purposes.
     * @param padder  the padder string to use
     * @return String of lead padding reflecting scope depth.
     */
    String leadPadding (String padder);

}
