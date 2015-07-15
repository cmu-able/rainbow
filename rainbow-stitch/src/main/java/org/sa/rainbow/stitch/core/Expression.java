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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.acmestudio.acme.core.type.IAcmeBooleanValue;
import org.acmestudio.acme.element.IAcmeElement;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.sa.rainbow.stitch.Ohana;
import org.sa.rainbow.stitch.util.Tool;
import org.sa.rainbow.stitch.visitor.ILiloBehavior;
import org.sa.rainbow.stitch.visitor.Stitch;

import antlr.RecognitionException;
import antlr.collections.AST;

/**
 * Represents an Expression, with its scope and AST stored for later
 * evaluation of this expression.  Originally, it also stored "type" defined in
 * the ExpressionState interface, but that has been removed as redundant.
 * Instead, any specialized expression type is distinguished at Tree Walk time
 * depending on the expression rule branch taken.
 * 
 * Inversion flag is used to faciliate expression negation, since we often need
 * to acquire a logical expression that is the inversion of another.
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class Expression extends ScopedEntity implements IEvaluableScope {

    /**
     * Declares the kind of Expression.  QUANTIFIED includes forall, exists,
     * exists unique, and select expressions.  LIST includes literal set,
     * method call (for its parameters), and tactic reference
     */
    public static enum Kind {
        UNKNOWN, NULL, BOOLEAN, INTEGER, FLOAT, CHAR, STRING, IDENTIFIER,
        ARITHMETIC, LOGICAL, RELATIONAL, UNARY, ASSIGNMENT,
        QUANTIFIED, LIST
    };

    public static final int LOP = 0;
    public static final int ROP = 1;
    public static final int OPS = 2;

    private static Expression m_trueExpression = null;
    private static Expression m_falseExpression = null;

    /**
     * @return  the "TRUE" expression, which always evaluates to true
     */
    public static Expression getTrueExpression () {
        if (m_trueExpression == null) {
            m_trueExpression = new Expression(Ohana.instance().getRootScope(), String.valueOf(Boolean.TRUE), Ohana.instance().getRootScope().stitch());
            m_trueExpression.m_result = true;
        }
        return m_trueExpression;
    }

    /**
     * @return  the "FALSE" expression, which always evaluates to false
     */
    public static Expression getFalseExpression () {
        if (m_falseExpression == null) {
            m_falseExpression = new Expression(Ohana.instance().getRootScope(), String.valueOf(Boolean.FALSE), Ohana.instance().getRootScope().stitch());
            m_falseExpression.m_result = false;
        }
        return m_falseExpression;
    }

    public int subLevel = 0;  // for tracking expression-local sublevel recursion
    public Kind kind = Kind.UNKNOWN;
    /**
     * Flag indicating whether to short-circuit quan predicate evaluation due
     * to tree-walk.  The way this flag should work is that, at the end of
     * the doQuantifiedExpression evaluation, turn this flag on (<code>true</code>),
     * and all expression eval methods should check that flag to determine
     * whether to skip.  The last endExpression (subLevel == 0) should turn
     * this back off (<code>false</code>).
     */
    public boolean skipQuanPredicate = false;

    /**
     * Stack used to track current working operands for evaluation.
     */
    public Stack[] lrOps = null;
    /**
     * Stack used to track current operand pointer, to either left or right.
     */
    public Stack<Integer> curOp = null;

    protected AST m_ast = null;
    /** Flag indicating whether to invert this expression, ONLY applicable if boolean! */
    protected boolean m_inverted = false;
    protected Object m_result = null;
    protected List<Var> m_refdVars = null;

    /**
     * Main Constructor for a new Expression object.
     * @param parent  the parent Scope of this scoped entity
     * @param name    the name for the scope of this expression
     * @param stitch  the Stitch evaluation context object
     */
    public Expression (IScope parent, String name, Stitch stitch) {
        super(parent, name, stitch);

        setDistinctScope(false);  // by default no distinct scope
        lrOps = new Stack[OPS];
        for (int i=0; i < OPS; i++) {
            lrOps[i] = new Stack();
        }
        curOp = new Stack<Integer>();
        m_refdVars = new ArrayList<Var>();
    }

    /**
     * Clones an Expression object, but without deep-copying the AST object.
     */
    @Override
    public Expression clone () {
        Expression clonedExpr = new Expression(parent(), getName(), stitch());
        copyState(clonedExpr);
        return clonedExpr;
    }

    @SuppressWarnings("unchecked")
    protected void copyState (Expression target) {
        super.copyState(target);
        target.subLevel = subLevel;
        target.kind = kind;
        target.skipQuanPredicate = skipQuanPredicate;
        target.lrOps = lrOps.clone();
        target.curOp = (Stack<Integer> )curOp.clone();
        target.m_ast = m_ast;
        target.m_inverted = m_inverted;
        target.m_result = m_result;
        target.m_refdVars = m_refdVars;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.ScopedEntity#toString()
     */
    @Override
    public String toString () {
        String invStr = m_inverted ? "!" : "";
        return "expression " + m_name + ": "
        + (m_result != null? m_result :
            m_ast != null? invStr + m_ast.toStringTree() : "don't know");
    }

    public AST ast () {
        return m_ast;
    }

    public void setAST (AST ast) {
        m_ast = ast;
    }

    public boolean isInverted () {
        return m_inverted;
    }
    public void setInverted (boolean b) {
        m_inverted = b;
    }

    public boolean isComplex () {
        return kind == Kind.QUANTIFIED || kind == Kind.LIST;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IEvaluable#evaluate(java.lang.Object[])
     */
    @Override
    public Object evaluate (Object[] argsIn) {
        // if called in an expression context, most often as boolean
        if (m_stitch == null || m_stitch.isCanceled()) return Boolean.FALSE;

        if (ast() != null) {
            resetResult();
            // proceed with evaluation
            m_stitch.pushScope(this);
            m_stitch.pushExpression();
            try {
                // set stitch to evaluate mode
                ILiloBehavior beh = m_stitch.getBehavior(Stitch.EVALUATOR_PASS);
                if (beh == null) return Boolean.FALSE;  // probably disposed

                m_stitch.walker.setBehavior(beh);
                m_stitch.walker.expr(ast());
                if (m_inverted) {
                    if (m_result instanceof Boolean) {
                        // we need to invert the result
                        m_result = !(Boolean )m_result;
                    }
                    else if (m_result instanceof IAcmeProperty) {
                        IAcmeProperty prop = (IAcmeProperty )m_result;
                        if (prop.getValue () instanceof IAcmeBooleanValue) {
                            m_result = !((IAcmeBooleanValue )prop.getValue ()).getValue ();
                        }

                    }
                }
            } catch (RecognitionException e) {
                Tool.logger().error("Unexpected Recognition Error evaluating Expression!\n", e);
            }
            m_stitch.popExpression();
            m_stitch.popScope();
        }

        return m_result;
    }


    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IEvaluable#estimateAvgTimeCost()
     */
    @Override
    public long estimateAvgTimeCost() {
        // TODO is there a need to estimate cost of expression?
        return 0L;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IEvaluable#modelElementsUsed()
     */
    @Override
    public Set<? extends IAcmeElement> modelElementsUsed() {
        Set<? extends IAcmeElement> resultSet = new HashSet<IAcmeElement>();
        // TODO implement an ILiloBehavior class to compute elements used/touched
        return resultSet;
    }

    /**
     * @param result the result to set
     */
    @SuppressWarnings("unchecked")
    public void setResult (Object result) {
        if (curOp.size() == 0) {
            // time to store final value
            this.m_result = result;
        } else {
            lrOps[curOp.pop().intValue()].push(result);
        }
    }
    /**
     * @return the result
     */
    public Object getResult () {
        return m_result;
    }

    public void resetResult () {
        if (m_ast != null /*&& m_expressions.size() > 0*/) {
            m_result = null;
        }
        // reset expression index
        curExprIdx = 0;
        // reset results in children expressions as well
        for (Expression e : m_expressions) {
            e.resetResult();
        }
    }

    /**
     * Method called to ensure that new result is evaluated during tree walking.
     */
    public void clearState () {
        // if identifier, clear state of this var in ancestor
        if (kind == Kind.IDENTIFIER) {
            Object o = lookup(getName());
            if (o instanceof Var) {
                ((Var )o).clearState();
            }
        }
        // clear states in children expressions as well
        for (Expression e : expressions()) {
            e.clearState();
        }
        for (Var v : m_vars.values()) {
            v.clearState();
        }
        for (Var v : m_refdVars) {  // clear state of referenced variables
            v.clearState();
        }
    }

    public void addRefdVar (Var v) {
        m_refdVars.add(v);
    }

}
