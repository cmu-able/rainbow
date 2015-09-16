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
 * Created March 15, 2006, separated from class Stitch April 4, 2006.
 */
package org.sa.rainbow.stitch.core;

import antlr.RecognitionException;
import antlr.collections.AST;
import org.acmestudio.acme.element.IAcmeElement;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.stitch.parser.StitchTreeWalkerTokenTypes;
import org.sa.rainbow.stitch.util.Tool;
import org.sa.rainbow.stitch.visitor.Stitch;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Statement extends ScopedEntity implements IEvaluableScope {

    /**
     * Declares types of Statements that exist in script.
     */
    public enum Kind {
        UNKNOWN, COMPOUND, ERROR, DECLARATION, EXPRESSION,
        IF, WHILE, FOR, FOREACH, EMPTY
    }

    public Kind type = Kind.UNKNOWN;
    /**
     * This flag, if set to true, tells the stmt rule to suppress this AND its
     * next statement list block from starting a new scope, but will be reset
     * within that rule to allow usage in any internal statements.
     */
    public boolean suppressScope = false;  // true means suppress child scope
    public int forCondIdx = 0;  // used in for loop parsing to indicate condition expression index
    public String forEachVar = null;  // used in foreach loop parsing to designate loop var

    protected AST m_ast = null;

    private long m_avgExecutionTime = 0L;

    public static Kind determineType (AST ast) {
        Kind t = Kind.UNKNOWN;
        switch (ast.getType()) {
        case StitchTreeWalkerTokenTypes.STMT_LIST:
            t = Kind.COMPOUND;
            break;
        case StitchTreeWalkerTokenTypes.ERROR:
            t = Kind.ERROR;
            break;
        case StitchTreeWalkerTokenTypes.VAR_DEF:
            t = Kind.DECLARATION;
            break;
        case StitchTreeWalkerTokenTypes.EXPR:
            t = Kind.EXPRESSION;
            break;
        case StitchTreeWalkerTokenTypes.IF:
            t = Kind.IF;
            break;
        case StitchTreeWalkerTokenTypes.WHILE:
            t = Kind.WHILE;
            break;
        case StitchTreeWalkerTokenTypes.FOR:
            t = Kind.FOR;
            // FOREACH type is determined later by scope establisher
            break;
        case StitchTreeWalkerTokenTypes.EMPTY_STMT:
            t = Kind.EMPTY;
            break;
        }
        return t;
    }

    /**
     * Main Constructor.
     * @param parent  the parent scope of this scoped entity
     * @param name    the name of this scope
     * @param stitch  the Stitch evaluation context object
     */
    public Statement (IScope parent, String name, Stitch stitch) {
        super(parent, name, stitch);
        setDistinctScope(true);
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.ScopedEntity#toString()
     */
    @Override
    public String toString () {
        return "statement: type " + type + ", { " + m_ast.toStringTree() + " }";
    }

    public AST ast () {
        return m_ast;
    }

    public void setAST (AST ast) {
        m_ast = ast;
        type = Statement.determineType(ast);
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IEvaluable#evaluate(java.lang.Object[])
     */
    @Override
    public Object evaluate(Object[] argsIn) {
        m_hasError = false;  // initially no error!
        long startTime = System.currentTimeMillis();
        switch (type) {
        case EMPTY:  // do nothing
            break;
        case DECLARATION:
        case EXPRESSION:
            resetResult();
            m_stitch.pushScope(this);
            m_stitch.pushExpression();
            try {
                // set stitch to evaluate mode
                m_stitch.walker.setBehavior(m_stitch.getBehavior(Stitch.EVALUATOR_PASS));
                m_stitch.walker.statement(ast());
            } catch (RecognitionException e) {
                Tool.logger().error("Unexpected Recognition Error evaluating Statement!\n", e);
            }
            m_stitch.popExpression();
            m_stitch.popScope();
            break;
        case COMPOUND:
            // evaluate all if no error block
            int cnt = statements().size();
            for (Statement stmt : statements()) {
                --cnt;
                if (cnt == 0 && stmt.type == Kind.ERROR) {
                    // TODO: treat error block specially, by tracking execution error...
                    if (m_hasError) {  // execute error block
                        stmt.evaluate(null);
                    } else {  // no error, short-circuit FOR loop
                        break;
                    }
                } else {
                    if (m_hasError) {
                        continue;  // skip execution since encountered errors
                    } else {
                        stmt.evaluate(null);
                        m_hasError |= stmt.hasError();
                    }
                }
            }
            break;
        case IF:
            if (expressions().size() < 1
                    || statements().size() < 1) {  // not enough things 
                Tool.logger().error("If statement lacks expression or statement or both!");
            }
            // get the expression to evaluate
            Expression expr = expressions().get(0);
            Statement stmt = statements().get(0);
            if ((Boolean )expr.evaluate(null)) {
                // descend into THEN statement block
                stmt.evaluate(null);
            } else {
                // descend into ELSE statement block, if exists
                if (statements().size() > 1) {
                    statements().get(1).evaluate(null);
                }
            }
            break;
        case WHILE:
            if (expressions().size() < 1
                    || statements().size() < 1) {  // not enough things 
                Tool.logger().error("While statement lacks expression or statement or both!");
            }
            expr = expressions().get(0);
            stmt = statements().get(0);
            while ((Boolean )expr.evaluate(null)) {
                stmt.evaluate(null);
            }
            break;
        case FOR:
            if (expressions().size() < forCondIdx
                    || statements().size() < 1) {  // not enough things 
                Tool.logger().error("For statement lacks expression or statement or both!");
            }
            // evaluate the initialization statements, meaning all but last element
            // - if no var declaration, then this loop won't apply
            for (int i=0; i < statements().size() - 1; ++i) {
                statements().get(i).evaluate(null);
            }
            // evaluate any expressions before the condition expr
            // - this is when no var declaration occurred in the for init
            for (int i=0; i < forCondIdx; ++i) {
                Expression condE = expressions().get(i);
                condE.evaluate(null);
            }
            // now the for-loop cycle:
            // (1) check loop condition, (2) execute stmt body, (3) next values
            expr = expressions().get(forCondIdx);
            stmt = statements().get(statements().size()-1);
            if (!(expr.evaluate(null) instanceof Boolean)) {
                Tool.logger().error("For condition expression not boolean!! " + expr.ast().toStringTree());
                break;
            }
            while ((Boolean )expr.getResult()) {
                stmt.evaluate(null);  /*(2)*/
                /*(3)*/
                for (int i = forCondIdx+1; i < expressions().size(); ++i) {
                    Expression nextE = expressions().get(i);
                    nextE.evaluate(null);
                }
                expr.evaluate(null);  // re-evaluate for (1)
            }
            break;
        case FOREACH:
            if (vars().size() < 1 || expressions().size() < 1
                    || statements().size() < 1) {  // not enough things 
                Tool.logger().error("For Each statement lacks variable or expression or statement or ALL!");
            }
            Var v = vars().get(forEachVar);
            expr = expressions().get(0);
            stmt = statements().get(0);
            if (!(expr.evaluate(null) instanceof Set)) {
                Tool.logger().error("For Each set needs to be a set type! " + expr.ast().toStringTree());
                break;
            }
            Set set = (Set )expr.getResult();
            // verify that variable is of right type
            if (set.size() > 0) {
                if (! Tool.typeMatches(v, set.iterator().next())) {
                    Tool.logger().error("Type mismatch between loop variable " + v.name
                            + " and Set " + set + " in for each statement " + ast().toStringList());
                    break;
                }
            }
            for (Object o : set) {
                v.setValue(o);
                stmt.evaluate(null);
            }
            break;
        case ERROR:
            // error block consists of condition -> statements
            // so, check for paired expression and statement counts
            if (expressions().size() != statements().size()) {
                Tool.logger().error("Error block doesn't have matching condition and statements!!");
                // but keep going
            }
            int maxCnt = Math.min(expressions().size(), statements().size());
            for (int i=0; i < maxCnt; ++i) {
                expr = expressions().get(i);
                stmt = statements().get(i);
                if ((Boolean )expr.evaluate(null)) {
                    stmt.evaluate(null);
                }
            }
            break;
        default:
            Tool.logger().error("Statement type " + type + " unknown! " + ast().toStringTree());
            break;
        }
        // track time elapsed and store exponential avg
        long estTime = System.currentTimeMillis() - startTime;
        double alpha = Double.parseDouble (Rainbow.getProperty (RainbowConstants.PROPKEY_MODEL_ALPHA));
        m_avgExecutionTime = (long) ((1-alpha) * m_avgExecutionTime + alpha * estTime);
        return null;  // statement doesn't return result
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IEvaluable#estimateAvgTimeCost()
     */
    @Override
    public long estimateAvgTimeCost() {
        if (m_avgExecutionTime > 0) return m_avgExecutionTime;

        // estimate cost based on type of statemnt
        long t = 0L;
        Random rand = new Random();
        switch (type) {
        case EMPTY:  // use no time
            break;
        case DECLARATION:
        case EXPRESSION:
            t = rand.nextInt(400)+100;  // a random of 100-500 ms for an expression
            break;
        case COMPOUND:
            // accumulate time across all statements
            for (Statement stmt : statements()) {
                t += stmt.estimateAvgTimeCost();
            }
            break;
        case IF:
            // assume equally likely to do then vs. else
            if (statements().size() > 1) {
                Statement elseStmt = statements().get(1);
                t += 0.5 * elseStmt.estimateAvgTimeCost();
            }
            if (statements().size() > 0) {
                Statement thenStmt = statements().get(0);
                t += 0.5 * thenStmt.estimateAvgTimeCost();
            }
            break;
        case WHILE:  // is there any way to tell loop count without executing??
        case FOR:
        case FOREACH:
            if (statements().size() > 0) {
                Statement whileStmt = statements().get(0);
                t += whileStmt.estimateAvgTimeCost();
            }
            break;
        case ERROR:
            // error block consists of condition -> statements
            // so, estimate based on the num of applicable statements
            int maxCnt = Math.min(expressions().size(), statements().size());
            for (int i=0; i < maxCnt; ++i) {
                Statement stmt = statements().get(i);
                t += stmt.estimateAvgTimeCost();
            }
            break;
        }

        return t;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IEvaluable#modelElementsUsed()
     */
    @Override
    public Set<? extends IAcmeElement> modelElementsUsed() {
        Set<IAcmeElement> resultSet = new HashSet<IAcmeElement>();
        // indiscriminantly accumulate used from sub statements and expressions
        for (Expression expr : expressions()) {
            // TODO hmmm, expression AST may not have an expression object, may need to implement an ILiloBehavior?
            resultSet.addAll(expr.modelElementsUsed());
        }
        for (Statement stmt : statements()) {
            resultSet.addAll(stmt.modelElementsUsed());
        }
        return resultSet;
    }

    public void resetResult () {
        // prepare to evaluate var def or expression using Treewalker
        curExprIdx = 0;  // reset expression counter
        // clear results in children expressions
        for (Expression e : expressions()) {
            e.resetResult();
        }
    }

    /**
     * Method called to ensure that new result is evaluated during tree walking.
     */
    public void clearState () {
        if (m_ast == null) return;

        // clear state in children expressions
        for (Expression e : expressions()) {
            e.clearState();
        }
        for (Var v : m_vars.values()) {
            v.clearValue();
        }
    }

}