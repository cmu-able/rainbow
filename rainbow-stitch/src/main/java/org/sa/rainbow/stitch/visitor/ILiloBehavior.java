package org.sa.rainbow.stitch.visitor;


import org.sa.rainbow.stitch.core.Expression;
import org.sa.rainbow.stitch.core.IScope;
import org.sa.rainbow.stitch.core.Import;
import org.sa.rainbow.stitch.core.Strategy;

import antlr.collections.AST;

public interface ILiloBehavior {

	public abstract void beginScript (IScope scriptScope);
	public abstract void endScript ();
	public abstract void createModule (AST modAST);

	public abstract Import createImport (AST impAST, AST pathAST);
	public abstract void addImportRename (AST origAST, AST renAST);
	public abstract void doImports ();

	public abstract void createVar (AST typeAST, AST nameAST);
	public abstract void beginVarList ();
	public abstract void endVarList ();
	public abstract void beginParamList ();
	public abstract void endParamList ();

	/** For expression, indicate that we're now processing the left operand */
	public abstract void lOp ();
	/** For expression, indicate that we're now processing the right operand */
	public abstract void rOp ();
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
	public abstract void beginExpression ();
	/**
	 * End walk of an expression node.  If sublevel count is non-zero, decrement
	 * count first before "popping" the current Expression object.
	 */
	public abstract void endExpression ();

	public abstract void beginQuantifiedExpression ();
	public abstract void doQuantifiedExpression ();
	public abstract void endQuantifiedExpression (AST quanAST);

	/**
	 * Although a method call doesn't really create a new scope, the params
	 * are going to be expressions, and for convenience, having a new "scope"
	 * localizes the list of expression within this method call.
	 * @param mcAST    the AST of the method call root
	 * @param idAST    the AST of the method identifier
	 */
	public abstract void beginMethodCallExpression ();
	public abstract void endMethodCallExpression (AST mcAST, AST idAST);
	public abstract void beginSetExpression ();
	public abstract void endSetExpression (AST setAST);

	public abstract void doExpression (AST exprAST);
	public abstract void doAssignExpression (AST opAST, AST lValAST);
	public abstract void doLogicalExpression (AST opAST);
	public abstract void doRelationalExpression (AST opAST);
	public abstract void doArithmeticExpression (AST opAST);
	public abstract void doUnaryExpression (AST opAST);
	public abstract void doIdentifierExpression (AST idAST, Expression.Kind kind);

	/**
	 * Begin walking a statement node, and end call to set the resulting AST.
	 * If statement is COMPOUND, IF, WHILE, or FOR, a new scope is created.
	 * NOTE:  Statement node has a special treatment of the AST being assigned
	 * _after_ the statement walk completes, due to the necessity to sometimes
	 * call begin and end outside of the statement rule.
	 * 
	 * @param stmtAST  the Statement AST node
	 */
	public abstract void beginStatement (AST stmtAST);
	public abstract void markForCondition ();
	public abstract void markForEach ();
	public abstract void endStatement ();

	public abstract void beginTactic (AST nameAST);
	public abstract void endTactic ();

	public abstract void beginConditionBlock (AST nameAST);
	public abstract void endConditionBlock ();
	public abstract void beginActionBlock (AST nameAST);
	public abstract void endActionBlock ();
	public abstract void beginEffectBlock (AST nameAST);
	public abstract void endEffectBlock ();

	public abstract void beginStrategy (AST nameAST);
	public abstract void endStrategy ();

	public abstract void beginBranching ();
	public abstract void endBranching ();
	public abstract void beginStrategyNode (AST labelAST);
	public abstract void endStrategyNode ();
	public abstract void doStrategyProbability (AST p1AST, AST p2AST, AST pLitAST);
	public abstract void doStrategyCondition (Strategy.ConditionKind type);
	public abstract void doStrategyDuration ();
	public abstract void beginReferencedTactic (AST labelAST);
	public abstract void endReferencedTactic ();
	public abstract void doStrategyAction (Strategy.ActionKind type);
	public abstract void doStrategyLoop (AST vAST, AST iAST, AST labelAST);

}
