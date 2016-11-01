package org.sa.rainbow.stitch2.visitor;


import org.sa.rainbow.stitch2.core.Expression;
import org.sa.rainbow.stitch2.core.IScope;
import org.sa.rainbow.stitch2.error.StitchProblemHandler;
import org.sa.rainbow.stitch2.util.KDebug;

import java.util.Stack;

/**
 * Created by schmerl on 10/3/2016.
 */
public class StitchState {

    private ThreadLocal<Stack<IScope>>     m_scopeStack;
    private ThreadLocal<Stack<Expression>> m_exprStack;
    private ThreadLocal<IScope> scope = null;
    private ThreadLocal<Expression> expr;  // expression "currently" in use for evaluation
    public  StitchProblemHandler    stitchProblemHandler;
    private Stitch                  m_stitch;

    private StitchState (Stitch stitch) {
        m_stitch = stitch;
        m_scopeStack = new ThreadLocal<Stack<IScope>> ();
        m_scopeStack.set (new Stack<IScope> ());
        m_exprStack = new ThreadLocal<Stack<Expression>> ();
        m_exprStack.set (new Stack<Expression> ());
        expr = new ThreadLocal<Expression> ();
        expr.set (null);
        scope = new ThreadLocal<IScope> ();
        scope.set (null);
        stitchProblemHandler = stitch.stitchProblemHandler;
    }

    public Stitch stitch () {
        return m_stitch;
    }

    /**
     * Pushes current scope in scope stack and set supplied scope as current
     * scope.
     *
     * @param s new scope to set as current scope
     */
    public void pushScope (IScope s) {
        Stack<IScope> ss = m_scopeStack.get ();
        if (ss == null) m_scopeStack.set (new Stack<IScope> ());
        m_scopeStack.get ().push (scope.get ());
        setScope (s);
//        System.out.println (sun.reflect.Reflection.getCallerClass(3).getName() + ".pushScope ()");

//        System.out.println ("pushScope(" + m_scopeStack.get ().size () + "): " + s.getClass ().getSimpleName ());
        System.out.println (KDebug.getCallTrace () + ", stack (" + System.identityHashCode (m_scopeStack.get ()) + ")" +
                                    " size=" +
                                    m_scopeStack
                                            .get ()
                                            .size () + ". Top of stack is " + s.toStringTree ());
    }

    /**
     * Pops last scope from stack and set as current scope.
     *
     * @return IScope  what was previously the current scope
     */
    public IScope popScope () {
        IScope prev = scope ();
        final IScope pop = m_scopeStack.get ().pop ();
        setScope (pop);
//        System.out.println ("popScope (" + m_scopeStack.get ().size () + "): " + pop.getClass ().getSimpleName ());
//        System.out.println (sun.reflect.Reflection.getCallerClass(3).getName() + ".popScope ()");
        System.out.println (KDebug.getCallTrace () + ", stack (" + System.identityHashCode (m_scopeStack.get ()) + ")" +
                                    "size=" +
                                    m_scopeStack
                                            .get ()
                                            .size () + ". Popped off stack is " + pop.toStringTree ());
        return prev;
    }

    /**
     * Stores the current expression and reset it for evaluation.
     */
    public void pushExpression () {
        Stack<Expression> expressionStack = m_exprStack.get ();
        if (expressionStack == null) m_exprStack.set (new Stack<Expression> ());
        m_exprStack.get ().push (expr.get ());
        expr.set (null);
    }

    /**
     * Pops the current expression and restore previous expression.
     */
    public void popExpression () {
        expr.set (m_exprStack.get ().pop ());
    }

    public Expression expr () {
        return this.expr.get ();
    }

    public void setExpr (Expression e) {
        this.expr.set (e);
    }

    public IScope scope () {
        return scope.get ();
    }

    public void setScope (IScope s) {
        scope.set (s);
    }

}
