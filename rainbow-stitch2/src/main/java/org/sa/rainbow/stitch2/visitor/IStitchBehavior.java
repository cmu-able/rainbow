package org.sa.rainbow.stitch2.visitor;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.sa.rainbow.stitch2.core.IScope;
import org.sa.rainbow.stitch2.core.Import;
import org.sa.rainbow.stitch2.core.Strategy;
import org.sa.rainbow.stitch2.parser.StitchParser;

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
            isFunction);

    void beginVarList ();

    void endVarList ();

    void beginParamList ();

    void endParamList ();

    void lOp ();

    void rOp ();

    void beginExpression ();

    void endExpression (ParserRuleContext ctx);

    void beginQuantifiedExpression ();

    void doQuantifiedExpression (Strategy.ExpressionKind quantifierKind, StitchParser.QuantifiedExpressionContext ctx);

    void endQuantifiedExpression (Strategy.ExpressionKind quant, StitchParser.QuantifiedExpressionContext
            quantifiedExpressionContext);

    void beginMethodCallExpression ();

    void endMethodCallExpression (TerminalNode mc, StitchParser.MethodCallContext id);

    void beginSetExpression ();

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

    void markForCondition ();

    void markForEach ();

    void endStatement ();

    void beginTactic (Token nameAST);

    void endTactic ();

    void beginConditionBlock (StitchParser.ConditionContext nameAST);

    void endConditionBlock ();

    void beginActionBlock (StitchParser.ActionContext nameAST);

    void endActionBlock ();

    void beginEffectBlock (StitchParser.EffectContext nameAST);

    void endEffectBlock ();

    void beginStrategy (TerminalNode nameAST);

    void endStrategy ();

    void beginBranching ();

    void endBranching ();

    void beginStrategyNode (TerminalNode identifier, ParserRuleContext ctx);

    void endStrategyNode ();

    void doStrategyProbability (/*AST p1AST, AST p2AST, AST pLitAST*/);

    void doStrategyCondition (Strategy.ConditionKind type, ParserRuleContext ctx);

    void doStrategyDuration (ParserRuleContext ctx);

    void beginReferencedTactic (TerminalNode labelAST);

    void endReferencedTactic ();

    void doStrategyAction (Strategy.ActionKind type);


    void doStrategyLoop (Token vAST, Token iAST, Token labelAST);

    void setWalker (StitchBeginEndVisitor walker);

    void beginPathExpression ();

    void pathExpressionFilter (TypeFilterT filter, TerminalNode identifier, StitchParser.ExpressionContext expression);

    void endPathExpression (StitchParser.PathExpressionContext ctx);

    void continueExpressionFilter (TypeFilterT filter, TerminalNode setIdentidfier, TerminalNode typeIdentifier,
                                   StitchParser.ExpressionContext
            expression);

    void setupPathFilter (TerminalNode identifier);

    void doTacticDuration (StitchParser.ExpressionContext expression);

    /**
     * Created by schmerl on 10/20/2016.
     */
    enum TypeFilterT {
        NONE, SATISFIES, DECLARES
    }
}
