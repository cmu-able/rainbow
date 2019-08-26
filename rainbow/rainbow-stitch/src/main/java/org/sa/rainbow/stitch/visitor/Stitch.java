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
package org.sa.rainbow.stitch.visitor;

import org.sa.rainbow.stitch.Ohana;
import org.sa.rainbow.stitch.adaptation.StitchExecutor;
import org.sa.rainbow.stitch.core.*;
import org.sa.rainbow.stitch.error.StitchProblemHandler;

import java.util.List;
import java.util.Stack;

/**
 * The main purpose of this class is to serve as the factory for generating the
 * treewalker behaviors.
 * <p/>
 * History: - "Maquette," taken from sculpting, denotes a small clay model
 * created to as a guide to a larger sculpture. This class serves that purpose,
 * and provides the intermediate data to form a parsed unit of adaptation
 * script, as well as utility functions. - As of July 5, 2006, class renamed to
 * Stitch, due to Stitch & Lilo theme, and the fact that Stitch is "all-capable"
 * to appear anywhere and has the weaponry necessary to evaluate a script. :-)
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class Stitch {

	public static final int SCOPER_PASS = 0;
	public static final int TYPECHECKER_PASS = 1;
	public static final int EVALUATOR_PASS = 2;
	public static final int NUM_PASS = 3;
	public static final Stitch NULL_STITCH = new Stitch(null, null);
	private IStitchBehavior[] m_behaviors;

	private final ThreadLocal<Stack<IScope>> m_scopeStack = new ThreadLocal<>();
	private final ThreadLocal<Stack<Expression>> m_exprStack = new ThreadLocal<>();
	private final ThreadLocal<IScope> scope = new ThreadLocal<IScope>() {
		@Override
		protected IScope initialValue() {
			return null;
		}
	};
	private final ThreadLocal<Expression> expr = new ThreadLocal<>(); // expression "currently" in
	// use for evaluation
	private final ThreadLocal<StitchExecutor> executor = new ThreadLocal<>();
	public String path = null;
	public StitchScript script = null;
	public StitchProblemHandler stitchProblemHandler = null; // ALI: ADDED; SWC: made public

	private boolean m_keepRunning = true; // flag used to cancel execution
	private Stack<Boolean> m_executing;

	/**
	 * Protected constructor used by static instance method to instantiate.
	 */
	protected Stitch(String scriptPath, StitchProblemHandler handler) {
		// [From ALI] incorporated stitchProblemHandler into tree walker
		stitchProblemHandler = handler;
		path = scriptPath;
		m_behaviors = new IStitchBehavior[NUM_PASS];
		m_executing = new Stack<Boolean>();
		m_scopeStack.set(new Stack<IScope>());
		m_exprStack.set(new Stack<Expression>());
		expr.set(null);
		scope.set(null);
	}

	@Override
	public Stitch clone() {
		StitchProblemHandler stitchProblemHandlerClone = stitchProblemHandler.clone();

		Stitch s = new Stitch(path, stitchProblemHandlerClone);
//        s.script = this.script;
//        s.scope = this.scope == null? (this.scope.clone (s.scope.parent ()));
//        s.expr = this.expr;
		return s;
	}

	/**
	 * Instantiates a new Stitch object to aid adaptation script parsing. Path to
	 * the script should be supplied.
	 *
	 * @param scriptPath script file path used as key to store stitchState
	 * @return new instance of Stitch
	 */
	public static Stitch newInstance(String scriptPath, StitchProblemHandler stitchProblemHandler) {
		Stitch stitch = Ohana.instance().findStitch(scriptPath);
		if (stitch == null) {
			stitch = new Stitch(scriptPath, stitchProblemHandler);
			Ohana.instance().storeStitch(scriptPath, stitch);
		}
		return stitch;
	}

	public static Stitch newInstance(String scriptPath, StitchProblemHandler stitchProblemHandler, boolean force) {
		if (!force)
			return newInstance(scriptPath, stitchProblemHandler);
		Stitch stitch = new Stitch(scriptPath, stitchProblemHandler);
		Ohana.instance().storeStitch(scriptPath, stitch);
		return stitch;
	}

	/**
	 * @return list of stored stitches
	 * @deprecated Use {@link Ohana2.instance().listStitches()} instead.
	 */
	@Deprecated
	public static List<Stitch> list() {
		return Ohana.instance().listStitches();
	}

	/**
	 * @deprecated Use {@link Ohana2.instance().findStitch()} instead.
	 */
	@Deprecated
	public static Stitch find(String key) {
		return Ohana.instance().findStitch(key);
	}

	/**
	 * @deprecated Use {@link Ohana2.instance().storeStitch()} instead.
	 */
	@Deprecated
	public static Stitch store(String key, Stitch m) {
		return Ohana.instance().storeStitch(key, m);
	}

	/**
	 * @deprecated Use {@link Ohana2.instance().removeStitch()} instead.
	 */
	@Deprecated
	public static Stitch remove(String key) {
		return Ohana.instance().removeStitch(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String str = "\n********\n" + "  The stitchState holds " + script.toString() + "\n" + "  Base scope "
				+ (script.parent() == null ? "NULL" : script.parent().toString()) + "\n" + "  Imports [";
		for (Import i : script.imports) {
			str += "\n    " + i.toString();
		}
		str += "\n  ]\n  Tactics [";
		for (Tactic t : script.tactics) {
			str += "\n\t" + t.toString();
		}
		str += "\n  ]\n  Strategies [";
		for (Strategy s : script.strategies) {
			str += "\n\t" + s.toString();
		}
		str += "\n  ]\n********";
		str += "\n========\n" + script.toStringTree() + "\n========";
		return str;
	}

	/**
	 * This dispose method should only be called when cleaning up for application
	 * shutdown, not during, for example, building (reconciling) cycles.
	 */
	public void dispose() {
		m_behaviors = new IStitchBehavior[NUM_PASS];
		m_executing = new Stack<Boolean>();
		m_scopeStack.set(new Stack<IScope>());
		m_exprStack.set(new Stack<Expression>());
		expr.set(null);
		scope.set(null);

	}

	/**
	 * Instantiates a new IStitchBehavior for adaptation script parsing.
	 *
	 * @param phase the phase of behavior needed
	 * @return IStitchBehavior new instance of IStitchBehavior
	 */
	public IStitchBehavior getBehavior(int phase) {
		if (m_behaviors == null)
			return null; // probably disposed

		IStitchBehavior beh = null;

		switch (phase) {
		case SCOPER_PASS:
			// ALI: MODIFIED
			if (m_behaviors[SCOPER_PASS] == null)
				beh = new StitchScopeEstablisher(this);
			else
				return m_behaviors[SCOPER_PASS];
			break;
		case TYPECHECKER_PASS:
			if (m_behaviors[TYPECHECKER_PASS] == null)
				beh = new StitchTypechecker(this);
			else
				return m_behaviors[TYPECHECKER_PASS];
			break;
		case EVALUATOR_PASS:
			// ALI: MODIFIED
			if (m_behaviors[EVALUATOR_PASS] == null)
				beh = new StitchScriptEvaluator(this);
			else
				return m_behaviors[EVALUATOR_PASS];
			break;
		}

		return beh;
	}

	/**
	 * Returns whether evaluation has been canceled, which is also <code>true</code>
	 * if the Ohana central instance has been disposed.
	 *
	 * @return boolean <code>true</code> if operation was canceled or Ohana got
	 *         disposed, <code>false</code> otherwise.
	 */
	public boolean isCanceled() {
		return !m_keepRunning || Ohana.isDisposed();
	}

	/**
	 * Cancels the evaluation operation, which should eventually interrupt
	 * execution.
	 */
	public void cancel() {
		m_keepRunning = false;
	}

	public Tactic findTactic(String iden) {
		Object o = script.lookup(iden);
		if (o instanceof Tactic)
			return (Tactic) o;
		else
			return null;
	}

	public synchronized void markExecuting(boolean executing) {
		if (executing)
			m_executing.push(executing);
		else if (!m_executing.isEmpty())
			m_executing.pop();
	}

	public synchronized boolean isExecuting() {
		return !m_executing.empty();
	}

	/**
	 * Pushes current scope in scope stack and set supplied scope as current scope.
	 *
	 * @param s new scope to set as current scope
	 */
	public void pushScope(IScope s) {
		if (m_scopeStack.get() == null)
			m_scopeStack.set(new Stack<IScope>());
		m_scopeStack.get().push(scope.get());
		setScope(s);
//        System.out.println (sun.reflect.Reflection.getCallerClass(3).getName() + ".pushScope ()");

//        System.out.println ("pushScope(" + m_scopeStack.get ().size () + "): " + s.getClass ().getSimpleName ());
//        System.out.println (KDebug.getCallTrace () + ", stack (" + System.identityHashCode (m_scopeStack.get ()) +
// ")" +
//                                    " size=" +
//                                    m_scopeStack.get ()
//
//                                            .size () + ". Top of stack is " + s.toStringTree ());
	}

	/**
	 * Pops last scope from stack and set as current scope.
	 *
	 * @return IScope what was previously the current scope
	 */
	public IScope popScope() {
		IScope prev = scope();
		final IScope pop = m_scopeStack.get().pop();
		setScope(pop);
//        System.out.println ("popScope (" + m_scopeStack.get ().size () + "): " + pop.getClass ().getSimpleName ());
//        System.out.println (sun.reflect.Reflection.getCallerClass(3).getName() + ".popScope ()");
//        System.out.println (KDebug.getCallTrace () + ", stack (" + System.identityHashCode (m_scopeStack.get ()) +
// ")" +
//                                    "size=" +
//                                    m_scopeStack.get ()
//
//                                            .size () + ". Popped off stack is " + (pop == null ? "(null)" : pop
//                .toStringTree
//                        ()));
		return prev;
	}

	/**
	 * Stores the current expression and reset it for evaluation.
	 */
	public void pushExpression() {
		if (m_exprStack.get() == null)
			m_exprStack.set(new Stack<Expression>());
		m_exprStack.get().push(expr.get());
		setExpr(null);
	}

	/**
	 * Pops the current expression and restore previous expression.
	 */
	public void popExpression() {
		expr.set(m_exprStack.get().pop());
	}

	public Expression expr() {
		return this.expr.get();
	}

	public void setExpr(Expression e) {
		this.expr.set(e);
	}

	public IScope scope() {
		return scope.get();
	}

	public void setScope(IScope s) {
		scope.set(s);
	}

	public void setExecutor(StitchExecutor executor) {
		this.executor.set(executor);
	}

	public StitchExecutor executor() {
		return this.executor.get();
	}

	public void setBehavior(int scoperPass, StitchScopeEstablisher behavior) {
		m_behaviors[scoperPass] = behavior;
	}
}
