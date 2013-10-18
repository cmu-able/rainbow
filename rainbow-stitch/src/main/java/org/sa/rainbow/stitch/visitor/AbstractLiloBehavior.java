/**
 * 
 */
package org.sa.rainbow.stitch.visitor;


import org.sa.rainbow.stitch.core.Expression;
import org.sa.rainbow.stitch.core.IScope;
import org.sa.rainbow.stitch.core.Import;
import org.sa.rainbow.stitch.core.StitchScript;
import org.sa.rainbow.stitch.core.Expression.Kind;
import org.sa.rainbow.stitch.core.Strategy.ActionKind;
import org.sa.rainbow.stitch.core.Strategy.ConditionKind;
import org.sa.rainbow.stitch.error.StitchProblemHandler;
import org.sa.rainbow.stitch.util.Tool;

import antlr.collections.AST;

/**
 * Base behavior class which allows the subclass to implement only the wanted
 * methods.
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public abstract class AbstractLiloBehavior implements ILiloBehavior {

	protected Stitch m_stitch = null;

	/**
	 * Contructor is protected since the Stitch class serves as the factory for
	 * all behavior objects.
	 * Constructor takes in an aid (Stitch) for parsing and walking the script.
	 * @param stitch  the script molding scaffold
	 */
	protected AbstractLiloBehavior (Stitch stitch) {
		m_stitch = stitch;
	}

	protected StitchScript script () {
		return m_stitch.script;
	}
	protected void setScript (StitchScript script) {
		m_stitch.script = script;
	}

	protected IScope scope () {
		return m_stitch.scope;
	}
	protected void pushScope (IScope newScope) {
		m_stitch.pushScope(newScope);
	}
	protected void popScope () {
		m_stitch.popScope();
	}

	protected Expression expr() {
		return m_stitch.expr;
	}
	protected void setExpression(Expression expr) {
		m_stitch.expr = expr;
	}

	protected StitchProblemHandler stitchProblemHandler() {
		return m_stitch.stitchProblemHandler;
	}

	protected void debug (String s) {
		if (! Tool.logger().isDebugEnabled()) return;
		String pad = scope() == null ? "" : scope().leadPadding("..");
		Tool.logger().debug(pad + s);
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IStitchBehavior#beginScript(org.sa.rainbow.stitch.core.IScope)
	 */
	public void beginScript(IScope scriptScope) {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IStitchBehavior#endScript()
	 */
	public void endScript() {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IStitchBehavior#createModule(antlr.collections.AST)
	 */
	public void createModule(AST modAST) {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IStitchBehavior#createImport(antlr.collections.AST, antlr.collections.AST)
	 */
	public Import createImport(AST impAST, AST pathAST) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.visitor.IStitchBehavior#addImportRename(antlr.collections.AST, antlr.collections.AST)
	 */
	public void addImportRename(AST origAST, AST renAST) {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IStitchBehavior#doImports()
	 */
	public void doImports() {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IStitchBehavior#createVar(antlr.collections.AST, antlr.collections.AST)
	 */
	public void createVar(AST typeAST, AST nameAST) {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IStitchBehavior#beginVarList()
	 */
	public void beginVarList() {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IStitchBehavior#endVarList()
	 */
	public void endVarList() {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IStitchBehavior#beginParamList()
	 */
	public void beginParamList() {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IStitchBehavior#endParamList()
	 */
	public void endParamList() {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.visitor.IStitchBehavior#lOp()
	 */
	public void lOp() {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.visitor.IStitchBehavior#rOp()
	 */
	public void rOp() {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IStitchBehavior#beginExpression()
	 */
	public void beginExpression() {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IStitchBehavior#endExpression()
	 */
	public void endExpression() {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IStitchBehavior#beginQuantifiedExpression()
	 */
	public void beginQuantifiedExpression() {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.visitor.IStitchBehavior#doQuantifiedExpression()
	 */
	public void doQuantifiedExpression() {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IStitchBehavior#endQuantifiedExpression(antlr.collections.AST)
	 */
	public void endQuantifiedExpression(AST quanAST) {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.visitor.IStitchBehavior#beginMethodCallExpression()
	 */
	public void beginMethodCallExpression() {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IStitchBehavior#endMethodCallExpression(antlr.collections.AST, antlr.collections.AST)
	 */
	public void endMethodCallExpression(AST mcAST, AST idAST) {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.visitor.IStitchBehavior#beginSetExpression()
	 */
	public void beginSetExpression() {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IStitchBehavior#endSetExpression(antlr.collections.AST)
	 */
	public void endSetExpression(AST setAST) {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IStitchBehavior#doExpression(antlr.collections.AST)
	 */
	public void doExpression(AST exprAST) {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IStitchBehavior#doAssignExpression(antlr.collections.AST, antlr.collections.AST)
	 */
	public void doAssignExpression(AST opAST, AST lValAST) {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IStitchBehavior#doLogicalExpression(antlr.collections.AST)
	 */
	public void doLogicalExpression(AST opAST) {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IStitchBehavior#doRelationalExpression(antlr.collections.AST)
	 */
	public void doRelationalExpression(AST opAST) {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IStitchBehavior#doArithmeticExpression(antlr.collections.AST)
	 */
	public void doArithmeticExpression(AST opAST) {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IStitchBehavior#doUnaryExpression(antlr.collections.AST)
	 */
	public void doUnaryExpression(AST opAST) {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.visitor.ILiloBehavior#doIdentifierExpression(antlr.collections.AST, org.sa.rainbow.stitch.core.Expression.Kind)
	 */
	public void doIdentifierExpression(AST idAST, Kind kind) {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IStitchBehavior#beginStatement(antlr.collections.AST)
	 */
	public void beginStatement(AST stmtAST) {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.visitor.IStitchBehavior#markForCondition()
	 */
	public void markForCondition() {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.visitor.IStitchBehavior#markForEach()
	 */
	public void markForEach() {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IStitchBehavior#endStatement(org)
	 */
	public void endStatement() {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IStitchBehavior#beginTactic(antlr.collections.AST)
	 */
	public void beginTactic(AST nameAST) {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IStitchBehavior#endTactic()
	 */
	public void endTactic() {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IStitchBehavior#beginActionBlock(antlr.collections.AST)
	 */
	public void beginActionBlock(AST nameAST) {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IStitchBehavior#endActionBlock()
	 */
	public void endActionBlock() {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IStitchBehavior#beginConditionBlock(antlr.collections.AST)
	 */
	public void beginConditionBlock(AST nameAST) {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IStitchBehavior#endConditionBlock()
	 */
	public void endConditionBlock() {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IStitchBehavior#beginEffectBlock(antlr.collections.AST)
	 */
	public void beginEffectBlock(AST nameAST) {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IStitchBehavior#endEffectBlock()
	 */
	public void endEffectBlock() {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IStitchBehavior#beginStrategy(antlr.collections.AST)
	 */
	public void beginStrategy(AST nameAST) {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IStitchBehavior#endStrategy()
	 */
	public void endStrategy() {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.visitor.ILiloBehavior#beginBranching()
	 */
	public void beginBranching() {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.visitor.ILiloBehavior#endBranching()
	 */
	public void endBranching() {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.visitor.ILiloBehavior#beginStrategyNode(antlr.collections.AST)
	 */
	public void beginStrategyNode(AST labelAST) {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.visitor.ILiloBehavior#endStrategyNode()
	 */
	public void endStrategyNode() {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.visitor.ILiloBehavior#doStrategyProbability(antlr.collections.AST, antlr.collections.AST, antlr.collections.AST)
	 */
	public void doStrategyProbability(AST p1AST, AST p2AST, AST pLitAST) {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.visitor.ILiloBehavior#doStrategyCondition(org.sa.rainbow.stitch.core.Strategy.ConditionKind)
	 */
	public void doStrategyCondition(ConditionKind type) {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.visitor.ILiloBehavior#doStrategyDuration()
	 */
	public void doStrategyDuration() {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.visitor.ILiloBehavior#beginReferencedTactic(antlr.collections.AST)
	 */
	public void beginReferencedTactic(AST labelAST) {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.visitor.ILiloBehavior#endReferencedTactic()
	 */
	public void endReferencedTactic() {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.visitor.ILiloBehavior#doStrategyAction(org.sa.rainbow.stitch.core.Strategy.ActionKind)
	 */
	public void doStrategyAction(ActionKind type) {
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.visitor.ILiloBehavior#doStrategyLoop(antlr.collections.AST, antlr.collections.AST, antlr.collections.AST)
	 */
	public void doStrategyLoop(AST vAST, AST iAST, AST labelAST) {
	}

}
