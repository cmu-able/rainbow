package org.sa.rainbow.stitch2.visitor;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.sa.rainbow.stitch2.core.*;
import org.sa.rainbow.stitch2.error.IStitchProblem;
import org.sa.rainbow.stitch2.error.RecognitionException;
import org.sa.rainbow.stitch2.error.StitchProblem;
import org.sa.rainbow.stitch2.error.StitchProblemHandler;
import org.sa.rainbow.stitch2.parser.StitchParser;
import org.sa.rainbow.stitch2.util.Tool;

/**
 * Created by schmerl on 9/28/2016.
 */
public abstract class BaseStitchBehavior implements IStitchBehavior {

    protected final Stitch m_stitch;

    protected BaseStitchBehavior (Stitch/*State*/ stitch) {
        m_stitch = stitch;
    }

    protected StitchScript script () {
        return m_stitch/*.stitch()*/.script;
    }

    protected void setScript (StitchScript script) {
        m_stitch/*.stitch()*/.script = script;
    }

    protected IScope scope () {
        return m_stitch.scope ();
    }

    public Stitch/*State*/ stitch () {
        return m_stitch;
    }

    protected void pushScope (IScope newScope) {
        m_stitch.pushScope (newScope);
    }

    protected void popScope () {
        m_stitch.popScope ();
    }

    public Expression expr () {
        return m_stitch.expr ();
    }

    public void setExpression (Expression expr) {
        m_stitch.setExpr (expr);
    }

    protected StitchProblemHandler stitchProblemHandler () {
        return m_stitch.stitchProblemHandler;
    }

    protected StitchProblem generateErrorFromToken (Token id, String msg) {
        return generateProblemFromToken (id, msg, IStitchProblem.ERROR);
    }

    protected StitchProblem generateProblemFromToken (Token id, String msg, int errorClass) {
        return new StitchProblem (new RecognitionException (msg, "",
                                                            id.getLine (), id
                                                                    .getCharPositionInLine ()), errorClass);
    }

    protected void debug (String s) {
        if (!Tool.logger ().isDebugEnabled ()) return;
        String pad = scope () == null ? "" : scope ().leadPadding ("..");
        Tool.logger ().debug (pad + s);
    }


    @Override
    public void beginScript (IScope scriptScope) {

    }

    @Override
    public void endScript () {

    }

    @Override
    public void createModule (String text) {

    }

    @Override
    public Import createImport (StitchParser.ImportStContext imp, Token path) {
        return null;
    }

    @Override
    public void addImportRename (Token origName, Token renName) {

    }

    @Override
    public void doImports () {

    }

    @Override
    public void createVar (StitchParser.DataTypeContext type, TerminalNode id, StitchParser.ExpressionContext val,
                           boolean isFunction) {

    }

    @Override
    public void beginVarList () {

    }

    @Override
    public void endVarList () {

    }

    @Override
    public void beginParamList () {

    }

    @Override
    public void endParamList () {

    }

    @Override
    public void lOp () {

    }

    @Override
    public void rOp () {

    }

    @Override
    public void beginExpression () {

    }

    @Override
    public void endExpression (ParserRuleContext ctx) {

    }

    @Override
    public void beginQuantifiedExpression () {

    }

    @Override
    public void doQuantifiedExpression (Strategy.ExpressionKind quantifierKind, StitchParser
            .QuantifiedExpressionContext ctx) {

    }

    @Override
    public void endQuantifiedExpression (Strategy.ExpressionKind quant, StitchParser.QuantifiedExpressionContext
            quantifiedExpressionContext) {

    }

    @Override
    public void beginMethodCallExpression () {

    }

    @Override
    public void endMethodCallExpression (TerminalNode mc, StitchParser.MethodCallContext id) {

    }

    @Override
    public void beginSetExpression () {

    }

    @Override
    public void endSetExpression (StitchParser.SetExpressionContext setAST) {

    }

    @Override
    public void doExpression (ParserRuleContext exprAST) {

    }

    @Override
    public void doAssignExpression (ParserRuleContext identifier, ParserRuleContext expression) {

    }

    @Override
    public void doLogicalExpression (Strategy.ExpressionKind opAST, ParserRuleContext ctx) {

    }

    @Override
    public void doRelationalExpression (Strategy.ExpressionKind opAST, ParserRuleContext ctx) {

    }

    @Override
    public void doArithmeticExpression (Strategy.ExpressionKind opAST, ParserRuleContext ctx) {

    }

    @Override
    public void doUnaryExpression (Strategy.ExpressionKind opAST, StitchParser.UnaryExpressionContext ctx) {

    }

    @Override
    public void doIdentifierExpression (ParserRuleContext idAST, Strategy.ExpressionKind kind) {

    }

    @Override
    public void doPostIdentifierExpression (StitchParser.PostIdExpressionContext identifier) {

    }

    @Override
    public void beginStatement (Strategy.StatementKind stmtAST, ParserRuleContext ctx) {

    }

    @Override
    public void markForCondition () {

    }

    @Override
    public void markForEach () {

    }

    @Override
    public void endStatement () {

    }

    @Override
    public void beginTactic (Token nameAST) {

    }

    @Override
    public void endTactic () {

    }

    @Override
    public void beginConditionBlock (StitchParser.ConditionContext nameAST) {

    }

    @Override
    public void endConditionBlock () {

    }

    @Override
    public void beginActionBlock (StitchParser.ActionContext nameAST) {

    }

    @Override
    public void endActionBlock () {

    }

    @Override
    public void beginEffectBlock (StitchParser.EffectContext nameAST) {

    }

    @Override
    public void endEffectBlock () {

    }

    @Override
    public void beginStrategy (TerminalNode nameAST) {

    }

    @Override
    public void endStrategy () {

    }

    @Override
    public void beginBranching () {

    }

    @Override
    public void endBranching () {

    }

    @Override
    public void beginStrategyNode (TerminalNode identifier, ParserRuleContext ctx) {

    }

    @Override
    public void endStrategyNode () {

    }

    @Override
    public void doStrategyProbability () {

    }

    @Override
    public void doStrategyCondition (Strategy.ConditionKind type, ParserRuleContext ctx) {

    }

    @Override
    public void doStrategyDuration (ParserRuleContext ctx) {

    }

    @Override
    public void beginReferencedTactic (TerminalNode labelAST) {

    }

    @Override
    public void endReferencedTactic () {

    }

    @Override
    public void doStrategyAction (Strategy.ActionKind type) {

    }

    @Override
    public void doStrategyLoop (Token vAST, Token iAST, Token labelAST) {

    }

    @Override
    public void beginPathExpression () {

    }

    @Override
    public void endPathExpression (StitchParser.PathExpressionContext ctx) {

    }

    @Override
    public void pathExpressionFilter (TypeFilterT filter, TerminalNode identifier, StitchParser.ExpressionContext
            expression) {

    }

    @Override
    public void continueExpressionFilter (TypeFilterT filter, TerminalNode setIdentidfier, TerminalNode
            typeIdentifier, StitchParser.ExpressionContext expression) {

    }

    @Override
    public void setupPathFilter (TerminalNode identifier) {

    }

    @Override
    public void doTacticDuration (StitchParser.ExpressionContext expression) {

    }
}
