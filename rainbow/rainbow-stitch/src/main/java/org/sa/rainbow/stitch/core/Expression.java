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

import org.acmestudio.acme.core.type.IAcmeBooleanType;
import org.acmestudio.acme.core.type.IAcmeBooleanValue;
import org.acmestudio.acme.core.type.IAcmeDoubleType;
import org.acmestudio.acme.core.type.IAcmeFloatType;
import org.acmestudio.acme.core.type.IAcmeFloatingPointValue;
import org.acmestudio.acme.core.type.IAcmeIntType;
import org.acmestudio.acme.core.type.IAcmeIntValue;
import org.acmestudio.acme.core.type.IAcmeSequenceType;
import org.acmestudio.acme.core.type.IAcmeSetType;
import org.acmestudio.acme.core.type.IAcmeStringType;
import org.acmestudio.acme.core.type.IAcmeStringValue;
import org.acmestudio.acme.element.IAcmeElement;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.rule.node.IExpressionNode;
import org.acmestudio.standalone.resource.StandaloneLanguagePackHelper;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.sa.rainbow.stitch.Ohana;
import org.sa.rainbow.stitch.parser.StitchParser;
import org.sa.rainbow.stitch.util.Tool;
import org.sa.rainbow.stitch.visitor.IStitchBehavior;
import org.sa.rainbow.stitch.visitor.Stitch;
import org.sa.rainbow.stitch.visitor.StitchBeginEndVisitor;

import antlr.collections.AST;

/**
 * Represents an Expression, with its scope and AST stored for later evaluation
 * of this expression. Originally, it also stored "type" defined in the
 * ExpressionState interface, but that has been removed as redundant. Instead,
 * any specialized expression type is distinguished at Tree Walk time depending
 * on the expression rule branch taken.
 * <p/>
 * Inversion flag is used to faciliate expression negation, since we often need
 * to acquire a logical expression that is the inversion of another.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class Expression extends ScopedEntity implements IEvaluableScope, StitchTypes {

	private IExpressionNode m_acmeExpression;

	public Kind getKind() {
		return kind;
	}

	public void setKind(Kind kind) {
		this.kind = kind;
	}

	public boolean hasAcmeExpression() {
		return m_acmeExpression != null;
	}

	public void setAcmeExpression(IExpressionNode acmeExpression) {
		m_acmeExpression = acmeExpression;
	}

	public IExpressionNode getAcmeExpression() {
		return m_acmeExpression;
	}

	/**
	 * Declares the kind of Expression. QUANTIFIED includes forall, exists, exists
	 * unique, and select expressions. LIST includes literal set, method call (for
	 * its parameters), and tactic reference
	 */
	public enum Kind {
		UNKNOWN, NULL, BOOLEAN, INTEGER, FLOAT, CHAR, STRING, IDENTIFIER, ARITHMETIC, LOGICAL, RELATIONAL, UNARY,
		ASSIGNMENT, QUANTIFIED, LIST, PATH
	}

	public static final int LOP = 0;
	public static final int ROP = 1;
	public static final int OPS = 2;

	private static Expression m_trueExpression = null;
	private static Expression m_falseExpression = null;

	/**
	 * @return the "TRUE" expression, which always evaluates to true
	 */
	public static Expression getTrueExpression() {
		if (m_trueExpression == null) {
			m_trueExpression = new Expression(Ohana.instance().getRootScope(), String.valueOf(Boolean.TRUE),
					Ohana.instance().getRootScope().stitchState());
			m_trueExpression.m_result = true;
		}
		return m_trueExpression;
	}

	/**
	 * @return the "FALSE" expression, which always evaluates to false
	 */
	public static Expression getFalseExpression() {
		if (m_falseExpression == null) {
			m_falseExpression = new Expression(Ohana.instance().getRootScope(), String.valueOf(Boolean.FALSE),
					Ohana.instance().getRootScope().stitchState());
			m_falseExpression.m_result = false;
		}
		return m_falseExpression;
	}

	public int subLevel = 0; // for tracking expression-local sublevel recursion
	private Kind kind = Kind.UNKNOWN;
	protected String type = null;
	/**
	 * Flag indicating whether to short-circuit quan predicate evaluation due to
	 * tree-walk. The way this flag should work is that, at the end of the
	 * doQuantifiedExpression evaluation, turn this flag on (<code>true</code>), and
	 * all expression eval methods should check that flag to determine whether to
	 * skip. The last endExpression (subLevel == 0) should turn this back off
	 * (<code>false</code>).
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
	protected ParseTree m_tree = null;
	/**
	 * Flag indicating whether to invert this expression, ONLY applicable if
	 * boolean!
	 */
	protected boolean m_inverted = false;
	protected Object m_result = Strategy.Outcome.UNKNOWN;
	protected List<Var> m_refdVars = null;

	/**
	 * Main Constructor for a new Expression object.
	 *
	 * @param parent the parent Scope of this scoped entity
	 * @param name   the name for the scope of this expression
	 * @param stitch the Stitch evaluation context object
	 */
	public Expression(IScope parent, String name, Stitch/* State */ stitch) {
		super(parent, name, stitch);

		setDistinctScope(false); // by default no distinct scope
		lrOps = new Stack[OPS];
		for (int i = 0; i < OPS; i++) {
			lrOps[i] = new Stack();
		}
		curOp = new Stack<Integer>();
		m_refdVars = new ArrayList<Var>();
	}

	/**
	 * Clones an Expression object, but without deep-copying the AST object.
	 */
	@Override
	public Expression clone(IScope parent) {
		Expression clonedExpr = new Expression(parent, getName(), stitchState());
		copyState(clonedExpr);
		return clonedExpr;
	}

	@SuppressWarnings("unchecked")
	protected void copyState(Expression target) {
		super.copyState(target);
		target.subLevel = subLevel;
		target.setKind(getKind());
		target.skipQuanPredicate = skipQuanPredicate;
		target.lrOps = lrOps.clone();
		target.curOp = (Stack<Integer>) curOp.clone();
		target.m_ast = m_ast;
		target.m_tree = m_tree;
		target.m_inverted = m_inverted;
		target.m_result = m_result;
		target.m_refdVars = m_refdVars;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sa.rainbow.stitchState.core.ScopedEntity#toString()
	 */
	@Override
	public String toString() {
		String invStr = m_inverted ? "!" : "";
		return "expression " + m_name + ": "
				+ (m_result != null ? m_result
						: m_ast != null ? invStr + m_ast.toStringTree()
								: m_tree != null ? invStr + m_tree.toStringTree() : "don't " + "know");
	}

	public ParseTree tree() {
		return m_tree;
	}

	public void setTree(ParseTree tree) {
		m_tree = tree;
	}

	public boolean isInverted() {
		return m_inverted;
	}

	public void setInverted(boolean b) {
		m_inverted = b;
	}

	public boolean isComplex() {
		return getKind() == Kind.QUANTIFIED || getKind() == Kind.LIST || getKind() == Kind.PATH;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sa.rainbow.stitchState.core.IEvaluable#evaluate(java.lang.Object[])
	 */
	@Override
	public Object evaluate(Object[] argsIn) {
		IStitchBehavior beh = m_stitch./* stitch (). */getBehavior(Stitch.EVALUATOR_PASS);
		if (beh == null) {
			System.out.println("Could not find a Stitch.EVALUATOR_PASS behavior");
			return Boolean.FALSE; // probably disposed
		}
		StitchBeginEndVisitor walker = new StitchBeginEndVisitor(beh, this/* m_stitch.scope () */);
		walker.setBehavior(beh);
		return evaluate(argsIn, walker);
	}

	public Object evaluate(Object[] argsIn, StitchBeginEndVisitor walker) {
		// if called in an expression context, most often as boolean
		if (m_stitch == null || m_stitch./* stitch (). */isCanceled())
			return Boolean.FALSE;

		IScope preScope = m_stitch.scope();

		if (tree() != null) {
			resetResult();
			// proceed with evaluation
			m_stitch.pushScope(this);
			m_stitch.pushExpression();
			try {
				// set stitchState to evaluate mode

				walker.getBehavior().stitch().setScope(this);
				if (!(tree() instanceof StitchParser.ExpressionContext) || (((StitchParser.ExpressionContext) tree())
						.getParent()) instanceof StitchParser.ExpressionContext) {
					walker.getBehavior().beginExpression();
					walker.visit(tree());
					walker.getBehavior().endExpression((ParserRuleContext) tree());
				} else {
					walker.visit(tree());
				}
				if (m_inverted) {
					if (m_result instanceof Boolean) {
						// we need to invert the result
						m_result = !(Boolean) m_result;
					} else if (m_result instanceof IAcmeProperty) {
						IAcmeProperty prop = (IAcmeProperty) m_result;
						if (prop.getValue() instanceof IAcmeBooleanValue) {
							m_result = !((IAcmeBooleanValue) prop.getValue()).getValue();
						}

					}
				}
			} catch (Exception e) {
				Tool.logger().error("Unexpected Recognition Error evaluating Expression!\n", e);
			}
			m_stitch.popExpression();
			m_stitch.popScope();
		}

		if (preScope != m_stitch.scope()) {
			System.out.println("Expression::evaluate: Scopes don't match: " + this.toString());
		}

		return m_result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sa.rainbow.stitchState.core.IEvaluable#estimateAvgTimeCost()
	 */
	@Override
	public long estimateAvgTimeCost() {
		// TODO is there a need to estimate cost of expression?
		return 0L;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sa.rainbow.stitchState.core.IEvaluable#modelElementsUsed()
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
	public void setResult(Object result) {
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
	public Object getResult() {
		return m_result;
	}

	public void resetResult() {
		if (m_ast != null || m_tree != null/* && m_expressions.size() > 0 */) {
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
	public void clearState() {
		// if identifier, clear state of this var in ancestor
		if (getKind() == Kind.IDENTIFIER) {
			Object o = lookup(getName());
			if (o instanceof Var) {
				((Var) o).clearState();
			}
		}
		// clear states in children expressions as well
		for (Expression e : expressions()) {
			e.clearState();
		}
		for (Var v : m_vars.values()) {
			v.clearState();
		}
		for (Var v : m_refdVars) { // clear state of referenced variables
			if (v.isFunction())
				v.clearState();
		}
	}

	public void addRefdVar(Var v) {
		m_refdVars.add(v);
	}

	public String getType() {
		if (type == null) {
			switch (getKind()) {
			case ASSIGNMENT:
				type = "void";
				break;
			case BOOLEAN:
			case LOGICAL:
			case QUANTIFIED:
			case RELATIONAL:
				type = BOOLEAN;
				break;
			case CHAR:
				type = CHAR;
				break;
			case STRING:
				type = STRING;
				break;
			case FLOAT:
				type = FLOAT;
				break;
			case INTEGER:
				type = INTEGER;
				break;
			case ARITHMETIC:
				type = UNKNOWN;
				break;
			case IDENTIFIER: {
				Object o = lookup(tree().getText());
				if (o == null)
					type = UNKNOWN;
				else if (o instanceof Var) {
					type = ((Var) o).getType();
				} else if (o instanceof IAcmeProperty) {
					type = getTypeFromAcme((IAcmeProperty) o);

				} else
					type = UNKNOWN;
			}

			}
		}
		return type;
	}

	private String getTypeFromAcme(IAcmeProperty o) {
		if (o.getType() instanceof IAcmeIntType || o.getValue() instanceof IAcmeIntValue) {
			return INTEGER;
		}
		if (o.getType() instanceof IAcmeStringType || o.getValue() instanceof IAcmeStringValue) {
			return STRING;
		}
		if (o.getType() instanceof IAcmeFloatType || o.getValue() instanceof IAcmeFloatingPointValue
				|| o.getType() instanceof IAcmeDoubleType)
			return FLOAT;
		if (o.getType() instanceof IAcmeBooleanType || o.getValue() instanceof IAcmeBooleanValue)
			return BOOLEAN;
		else if (o.getType() instanceof IAcmeSetType) {
			return SET;
		} else if (o.getType() instanceof IAcmeSequenceType) {
			return SEQ;
		}
		return UNKNOWN;
	}

	public void setType(String type) {
		this.type = type;
	}

}
