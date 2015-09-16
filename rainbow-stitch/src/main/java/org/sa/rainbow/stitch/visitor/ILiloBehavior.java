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
package org.sa.rainbow.stitch.visitor;


import antlr.collections.AST;
import org.sa.rainbow.stitch.core.Expression;
import org.sa.rainbow.stitch.core.IScope;
import org.sa.rainbow.stitch.core.Import;
import org.sa.rainbow.stitch.core.Strategy;

public interface ILiloBehavior {

    void beginScript (IScope scriptScope);

    void endScript ();

    void createModule (AST modAST);

    Import createImport (AST impAST, AST pathAST);

    void addImportRename (AST origAST, AST renAST);

    void doImports ();

    void createVar (AST typeAST, AST nameAST);

    void beginVarList ();

    void endVarList ();

    void beginParamList ();

    void endParamList ();

    /** For expression, indicate that we're now processing the left operand */
    void lOp ();
    /** For expression, indicate that we're now processing the right operand */
    void rOp ();
    /**
     * Begin walking an expression node, processing intermediate expressions.
     * If expression is a quantified one, a new scope is created.
     * If expression recurses, don't create sublevel Expression objects, but
     * keep track of level count until the AST object is set by an intermediate
     * "do" action.  AST object can only be set on the current Expression object
     * when the reference is still null AND the sublevel count is zero.  This
     * ensures that only the outermost, and most interesting Expression clause
     * gets saved for later evaluation.
     */
    void beginExpression ();
    /**
     * End walk of an expression node.  If sublevel count is non-zero, decrement
     * count first before "popping" the current Expression object.
     */
    void endExpression ();

    void beginQuantifiedExpression ();

    void doQuantifiedExpression ();

    void endQuantifiedExpression (AST quanAST);

    /**
     * Although a method call doesn't really create a new scope, the params
     * are going to be expressions, and for convenience, having a new "scope"
     * localizes the list of expression within this method call.
     * @param mcAST    the AST of the method call root
     * @param idAST    the AST of the method identifier
     */
    void beginMethodCallExpression ();

    void endMethodCallExpression (AST mcAST, AST idAST);

    void beginSetExpression ();

    void endSetExpression (AST setAST);

    void doExpression (AST exprAST);

    void doAssignExpression (AST opAST, AST lValAST);

    void doLogicalExpression (AST opAST);

    void doRelationalExpression (AST opAST);

    void doArithmeticExpression (AST opAST);

    void doUnaryExpression (AST opAST);

    void doIdentifierExpression (AST idAST, Expression.Kind kind);

    /**
     * Begin walking a statement node, and end call to set the resulting AST.
     * If statement is COMPOUND, IF, WHILE, or FOR, a new scope is created.
     * NOTE:  Statement node has a special treatment of the AST being assigned
     * _after_ the statement walk completes, due to the necessity to sometimes
     * call begin and end outside of the statement rule.
     * 
     * @param stmtAST  the Statement AST node
     */
    void beginStatement (AST stmtAST);

    void markForCondition ();

    void markForEach ();

    void endStatement ();

    void beginTactic (AST nameAST);

    void endTactic ();

    void beginConditionBlock (AST nameAST);

    void endConditionBlock ();

    void beginActionBlock (AST nameAST);

    void endActionBlock ();

    void beginEffectBlock (AST nameAST);

    void endEffectBlock ();

    void beginStrategy (AST nameAST);

    void endStrategy ();

    void beginBranching ();

    void endBranching ();

    void beginStrategyNode (AST labelAST);

    void endStrategyNode ();

    void doStrategyProbability (/*AST p1AST, AST p2AST, AST pLitAST*/);

    void doStrategyCondition (Strategy.ConditionKind type);

    void doStrategyDuration ();

    void beginReferencedTactic (AST labelAST);

    void endReferencedTactic ();

    void doStrategyAction (Strategy.ActionKind type);

    void doStrategyLoop (AST vAST, AST iAST, AST labelAST);

}
