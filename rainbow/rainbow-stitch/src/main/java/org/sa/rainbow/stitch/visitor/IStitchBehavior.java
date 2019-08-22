package org.sa.rainbow.stitch.visitor;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.sa.rainbow.stitch.core.IScope;
import org.sa.rainbow.stitch.core.Import;
import org.sa.rainbow.stitch.core.Strategy;
import org.sa.rainbow.stitch.parser.StitchParser;

/**
 * Created by schmerl on 9/28/2016.
 */
public interface IStitchBehavior {

    Stitch/*State*/ stitch ();

    void beginScript (IScope scriptScope);

    void endScript ();

    void createModule (String text);

    Import createImport (StitchParser.ImportStContext imp, Token path);

    void addImportRename (Token origName, Token renName);

    void doImports ();

    void createVar (StitchParser.DataTypeContext type, TerminalNode id, StitchParser.ExpressionContext val, boolean
            isFunction, boolean isFormalParam);

    void beginVarList ();

    void endVarList ();

    void beginParamList ();

    void endParamList ();

    void lOp ();

    void rOp ();

    boolean beginExpression (ParserRuleContext ctx);

    void endExpression (ParserRuleContext ctx, boolean pushed);

    void beginQuantifiedExpression (ParserRuleContext ctx);

    void doQuantifiedExpression (Strategy.ExpressionKind quantifierKind, StitchParser.QuantifiedExpressionContext ctx);

    void endQuantifiedExpression (Strategy.ExpressionKind quant, StitchParser.QuantifiedExpressionContext
            quantifiedExpressionContext);

    void beginMethodCallExpression (ParserRuleContext ctx);

    void endMethodCallExpression (TerminalNode mc, StitchParser.MethodCallContext id);

    void beginSetExpression (ParserRuleContext ctx);

    void endSetExpression (StitchParser.SetExpressionContext setAST);

    void doExpression (ParserRuleContext exprAST);

    void doAssignExpression (ParserRuleContext identifier, ParserRuleContext expression);

    void doLogicalExpression (Strategy.ExpressionKind opAST, ParserRuleContext ctx);

    void doRelationalExpression (Strategy.ExpressionKind opAST, ParserRuleContext ctx);

    void doArithmeticExpression (Strategy.ExpressionKind opAST, ParserRuleContext ctx);

    void doUnaryExpression (Strategy.ExpressionKind opAST, StitchParser.UnaryExpressionContext ctx);

    void doIdentifierExpression (ParserRuleContext idAST, Strategy.ExpressionKind kind);

    void doPostIdentifierExpression (StitchParser.PostIdExpressionContext identifier);

    /**
     * Begin walking a statement node, and end call to set the resulting AST.
     * If statement is COMPOUND, IF, WHILE, or FOR, a new scope is created.
     * NOTE:  Statement node has a special treatment of the AST being assigned
     * _after_ the statement walk completes, due to the necessity to sometimes
     * call begin and end outside of the statement rule.
     *
     * @param stmtAST the Statement AST node
     * @param ctx
     */
    void beginStatement (Strategy.StatementKind stmtAST, ParserRuleContext ctx);

    void endStatement (Strategy.StatementKind stmtAST, ParserRuleContext ctx);
    void markForCondition ();

    void markForEach ();


    void beginTactic (TerminalNode nameAST);

    void endTactic (TerminalNode nameAST);

    void beginConditionBlock (StitchParser.ConditionContext nameAST);

    void endConditionBlock ();

    void beginActionBlock (StitchParser.ActionContext nameAST);

    void endActionBlock ();

    void beginEffectBlock (StitchParser.EffectContext nameAST);

    void endEffectBlock (StitchParser.EffectContext nameAST);

    void beginStrategy (TerminalNode nameAST);

    void endStrategy ();

    void beginBranching ();

    void endBranching ();

    void beginStrategyNode (TerminalNode identifier, ParserRuleContext ctx);

    void endStrategyNode ();

    void doStrategyProbability (StitchParser.StrategyCondContext ctx);

    void doStrategyCondition (Strategy.ConditionKind type, ParserRuleContext ctx);

    void doStrategyDuration (ParserRuleContext ctx, TerminalNode labelAST);

    void beginReferencedTactic (TerminalNode labelAST);

    void endReferencedTactic (TerminalNode labelAST);

    void doStrategyAction (Strategy.ActionKind type);


    void doStrategyLoop (Token vAST, Token iAST, Token labelAST);

    void setWalker (StitchBeginEndVisitor walker);

    void beginPathExpression (ParserRuleContext ctx);

    boolean pathExpressionFilter (TypeFilterT filter, TerminalNode identifier, StitchParser.ExpressionContext expression);

    void endPathExpression (StitchParser.PathExpressionContext ctx);

    void continueExpressionFilter (TypeFilterT filter, TerminalNode setIdentidfier, TerminalNode typeIdentifier,
                                   StitchParser.ExpressionContext
            expression, boolean mustBeSet, boolean resultisSet);

    void setupPathFilter (TerminalNode identifier);

    void doTacticDuration (ParserRuleContext expression);

    /**
     * Created by schmerl on 10/20/2016.
     */
    enum TypeFilterT {
        NONE, SATISFIES, DECLARES
    }

	void processParameter();

	void beginCondition(int i);

	void endCondition(int i);

	void beginAction(int i);

	void endAction(int i);

}
