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
 * 
 */
package org.sa.rainbow.stitch.visitor;


import org.sa.rainbow.stitch.core.Expression;
import org.sa.rainbow.stitch.core.Expression.Kind;
import org.sa.rainbow.stitch.core.IScope;
import org.sa.rainbow.stitch.core.Import;
import org.sa.rainbow.stitch.core.StitchScript;
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
    @Override
    public void beginScript(IScope scriptScope) {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IStitchBehavior#endScript()
     */
    @Override
    public void endScript() {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IStitchBehavior#createModule(antlr.collections.AST)
     */
    @Override
    public void createModule(AST modAST) {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IStitchBehavior#createImport(antlr.collections.AST, antlr.collections.AST)
     */
    @Override
    public Import createImport(AST impAST, AST pathAST) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.visitor.IStitchBehavior#addImportRename(antlr.collections.AST, antlr.collections.AST)
     */
    @Override
    public void addImportRename(AST origAST, AST renAST) {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IStitchBehavior#doImports()
     */
    @Override
    public void doImports() {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IStitchBehavior#createVar(antlr.collections.AST, antlr.collections.AST)
     */
    @Override
    public void createVar(AST typeAST, AST nameAST) {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IStitchBehavior#beginVarList()
     */
    @Override
    public void beginVarList() {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IStitchBehavior#endVarList()
     */
    @Override
    public void endVarList() {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IStitchBehavior#beginParamList()
     */
    @Override
    public void beginParamList() {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IStitchBehavior#endParamList()
     */
    @Override
    public void endParamList() {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.visitor.IStitchBehavior#lOp()
     */
    @Override
    public void lOp() {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.visitor.IStitchBehavior#rOp()
     */
    @Override
    public void rOp() {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IStitchBehavior#beginExpression()
     */
    @Override
    public void beginExpression() {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IStitchBehavior#endExpression()
     */
    @Override
    public void endExpression() {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IStitchBehavior#beginQuantifiedExpression()
     */
    @Override
    public void beginQuantifiedExpression() {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.visitor.IStitchBehavior#doQuantifiedExpression()
     */
    @Override
    public void doQuantifiedExpression() {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IStitchBehavior#endQuantifiedExpression(antlr.collections.AST)
     */
    @Override
    public void endQuantifiedExpression(AST quanAST) {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.visitor.IStitchBehavior#beginMethodCallExpression()
     */
    @Override
    public void beginMethodCallExpression() {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IStitchBehavior#endMethodCallExpression(antlr.collections.AST, antlr.collections.AST)
     */
    @Override
    public void endMethodCallExpression(AST mcAST, AST idAST) {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.visitor.IStitchBehavior#beginSetExpression()
     */
    @Override
    public void beginSetExpression() {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IStitchBehavior#endSetExpression(antlr.collections.AST)
     */
    @Override
    public void endSetExpression(AST setAST) {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IStitchBehavior#doExpression(antlr.collections.AST)
     */
    @Override
    public void doExpression(AST exprAST) {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IStitchBehavior#doAssignExpression(antlr.collections.AST, antlr.collections.AST)
     */
    @Override
    public void doAssignExpression(AST opAST, AST lValAST) {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IStitchBehavior#doLogicalExpression(antlr.collections.AST)
     */
    @Override
    public void doLogicalExpression(AST opAST) {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IStitchBehavior#doRelationalExpression(antlr.collections.AST)
     */
    @Override
    public void doRelationalExpression(AST opAST) {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IStitchBehavior#doArithmeticExpression(antlr.collections.AST)
     */
    @Override
    public void doArithmeticExpression(AST opAST) {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IStitchBehavior#doUnaryExpression(antlr.collections.AST)
     */
    @Override
    public void doUnaryExpression(AST opAST) {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.visitor.ILiloBehavior#doIdentifierExpression(antlr.collections.AST, org.sa.rainbow.stitch.core.Expression.Kind)
     */
    @Override
    public void doIdentifierExpression(AST idAST, Kind kind) {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IStitchBehavior#beginStatement(antlr.collections.AST)
     */
    @Override
    public void beginStatement(AST stmtAST) {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.visitor.IStitchBehavior#markForCondition()
     */
    @Override
    public void markForCondition() {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.visitor.IStitchBehavior#markForEach()
     */
    @Override
    public void markForEach() {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IStitchBehavior#endStatement(org)
     */
    @Override
    public void endStatement() {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IStitchBehavior#beginTactic(antlr.collections.AST)
     */
    @Override
    public void beginTactic(AST nameAST) {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IStitchBehavior#endTactic()
     */
    @Override
    public void endTactic() {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IStitchBehavior#beginActionBlock(antlr.collections.AST)
     */
    @Override
    public void beginActionBlock(AST nameAST) {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IStitchBehavior#endActionBlock()
     */
    @Override
    public void endActionBlock() {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IStitchBehavior#beginConditionBlock(antlr.collections.AST)
     */
    @Override
    public void beginConditionBlock(AST nameAST) {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IStitchBehavior#endConditionBlock()
     */
    @Override
    public void endConditionBlock() {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IStitchBehavior#beginEffectBlock(antlr.collections.AST)
     */
    @Override
    public void beginEffectBlock(AST nameAST) {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IStitchBehavior#endEffectBlock()
     */
    @Override
    public void endEffectBlock() {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IStitchBehavior#beginStrategy(antlr.collections.AST)
     */
    @Override
    public void beginStrategy(AST nameAST) {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IStitchBehavior#endStrategy()
     */
    @Override
    public void endStrategy() {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.visitor.ILiloBehavior#beginBranching()
     */
    @Override
    public void beginBranching() {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.visitor.ILiloBehavior#endBranching()
     */
    @Override
    public void endBranching() {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.visitor.ILiloBehavior#beginStrategyNode(antlr.collections.AST)
     */
    @Override
    public void beginStrategyNode(AST labelAST) {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.visitor.ILiloBehavior#endStrategyNode()
     */
    @Override
    public void endStrategyNode() {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.visitor.ILiloBehavior#doStrategyProbability(antlr.collections.AST, antlr.collections.AST, antlr.collections.AST)
     */
    @Override
    public void doStrategyProbability () {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.visitor.ILiloBehavior#doStrategyCondition(org.sa.rainbow.stitch.core.Strategy.ConditionKind)
     */
    @Override
    public void doStrategyCondition(ConditionKind type) {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.visitor.ILiloBehavior#doStrategyDuration()
     */
    @Override
    public void doStrategyDuration() {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.visitor.ILiloBehavior#beginReferencedTactic(antlr.collections.AST)
     */
    @Override
    public void beginReferencedTactic(AST labelAST) {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.visitor.ILiloBehavior#endReferencedTactic()
     */
    @Override
    public void endReferencedTactic() {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.visitor.ILiloBehavior#doStrategyAction(org.sa.rainbow.stitch.core.Strategy.ActionKind)
     */
    @Override
    public void doStrategyAction(ActionKind type) {
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.visitor.ILiloBehavior#doStrategyLoop(antlr.collections.AST, antlr.collections.AST, antlr.collections.AST)
     */
    @Override
    public void doStrategyLoop(AST vAST, AST iAST, AST labelAST) {
    }

}
