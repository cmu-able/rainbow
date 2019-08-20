package org.sa.rainbow.stitch.visitor;

import java.text.MessageFormat;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.sa.rainbow.stitch.core.Expression;
import org.sa.rainbow.stitch.core.IScope;
import org.sa.rainbow.stitch.core.Import;
import org.sa.rainbow.stitch.core.Strategy;
import org.sa.rainbow.stitch.parser.StitchBaseVisitor;
import org.sa.rainbow.stitch.parser.StitchParser;
import org.sa.rainbow.stitch.parser.StitchParser.ExpressionsContext;

/**
 * Created by schmerl on 9/30/2016.
 */
public class StitchBeginEndVisitor extends StitchBaseVisitor<Boolean> {

    IStitchBehavior beh;
    IScope          parentScope;
    StitchVisitorData visitorData;
    
    class VisitorState {
    	Expression expr = null;
    	IScope scope = null;
    	int exprLevel = -1;
    	int scopeLevel = -1;
    	
    	VisitorState () {
    		expr = beh.stitch().expr();
    		scope = beh.stitch().scope();
    		exprLevel = expr!=null?expr.subLevel:-1;
    		scopeLevel = scope!=null?scope.getLevel():-1;
    	}
    }

    public StitchBeginEndVisitor (IStitchBehavior beh, IScope inScope) {
        this.beh = beh;
        this.parentScope = inScope;
        beh.setWalker (this);
    }

    public IStitchBehavior getBehavior () {
        return beh;
    }

    @Override
    public Boolean visitScript (@NotNull StitchParser.ScriptContext ctx) {
//        IScope preScope = beh.stitch ().scope ();
        beh.beginScript (parentScope);
        beh.createModule (ctx.IDENTIFIER ().getText ());
        for (int i = 0; i < ctx.importSt ().size (); i++) {
            visitImportSt (ctx.importSt (i));
        }
        beh.doImports ();
        visitFunctions (ctx.functions ());

        for (int i = 0; i < ctx.tactic ().size (); i++) {
            visitTactic (ctx.tactic (i));
        }

        for (int i = 0; i < ctx.strategy ().size (); i++) {
            visitStrategy (ctx.strategy (i));
        }

        beh.endScript ();
//        if (preScope != beh.stitch().scope ()) {
//            System.out.println ("Scopes don't match: " + this.toString ());
//        }
        return true;
    }

    @Override
    public Boolean visitImportSt (@NotNull StitchParser.ImportStContext ctx) {
        Import imp = null;
        String path = ctx.STRING_LIT ().getText ();
        imp = beh.createImport (ctx, ctx.STRING_LIT ().getSymbol ());
        for (int i = 0; i < ctx.importRename ().size (); i++) {
            visitImportRename (ctx.importRename (i));
        }
        return true;
    }

    @Override
    public Boolean visitImportRename (@NotNull StitchParser.ImportRenameContext ctx) {
        beh.addImportRename (ctx.id1, ctx.id2);
        return true;
    }

    @Override
    public Boolean visitFunctions (@NotNull StitchParser.FunctionsContext ctx) {
        beh.beginVarList ();
        for (int i = 0; i < ctx.var ().size (); i++) {
            visitVar (ctx.var (i));
        }
//        super.visitFunctions (ctx);
        beh.endVarList ();
        return true;
    }

    @Override
    public Boolean visitTactic (@NotNull StitchParser.TacticContext ctx) {
        beh.beginTactic (ctx.IDENTIFIER());
        super.visitTactic (ctx);
        beh.endTactic (ctx.IDENTIFIER());
        return true;
    }

    @Override
    public Boolean visitTacticParams (@NotNull StitchParser.TacticParamsContext ctx) {
        beh.beginParamList ();
        super.visitTacticParams (ctx);
        beh.endParamList ();
        return true;
    }

    @Override
    public Boolean visitTacticParam (@NotNull StitchParser.TacticParamContext ctx) {
        super.visitTacticParam (ctx);
        beh.createVar (ctx.dataType (), ctx.IDENTIFIER (), null, false, true);
        return true;
    }

    @Override
    public Boolean visitVars (@NotNull StitchParser.VarsContext ctx) {
//        IScope preScope = beh.stitch().scope ();
        beh.beginVarList ();
        super.visitVars (ctx);
        beh.endVarList ();
//        if (preScope != beh.stitch().scope ()) {
//            System.out.println ("Scopes don't match: " + this.toString ());
//        }
        return true;
    }

    @Override
    public Boolean visitVar (@NotNull StitchParser.VarContext ctx) {
        VisitorState preState = new VisitorState();
        beh.beginStatement (Strategy.StatementKind.VAR_DEF, ctx);
        visitExpression(ctx.expression());
        beh.createVar (ctx.t, ctx.IDENTIFIER (), ctx.expression (), ctx.DEFINE () != null || ctx.FUNCTION () != null, false);
        beh.endStatement (Strategy.StatementKind.VAR_DEF, ctx);
        checkPostState(preState);
        return true;
    }
    
    

    @Override
    public Boolean visitPathExpression (@NotNull StitchParser.PathExpressionContext ctx) {
    	VisitorState preState = new VisitorState();
        beh.beginPathExpression (ctx);
        VisitorState bpState = new VisitorState();
        visitNonLiteralIdExpression (ctx.nonLiteralIdExpression ());
        IStitchBehavior.TypeFilterT filter = IStitchBehavior.TypeFilterT.NONE;
        if (ctx.IDENTIFIER () != null) {
            filter = ctx.COLON () != null ? IStitchBehavior.TypeFilterT.SATISFIES : IStitchBehavior.TypeFilterT
                    .DECLARES;
        }
        boolean hasExpr = false;
        if (ctx.expression () != null) {
            hasExpr = true;
            beh.setupPathFilter (ctx.IDENTIFIER ());
//            beh
        }
        if (filter != IStitchBehavior.TypeFilterT.NONE || hasExpr) {
            hasExpr = !beh.pathExpressionFilter (filter, ctx.IDENTIFIER (), ctx.expression ());
        }
        if (hasExpr) {
        	VisitorState eState = new VisitorState();
        	visitExpression (ctx.expression ());
        	checkPostState(eState);
        }
        if (ctx.pathExpressionContinuation () != null) {
            visitPathExpressionContinuation (ctx.pathExpressionContinuation ());
        }
        checkPostState(bpState);
        beh.endPathExpression (ctx);
        checkPostState(preState);
        return true;
    }

    @Override
    public Boolean visitPathExpressionContinuation (@NotNull StitchParser.PathExpressionContinuationContext ctx) {
        IStitchBehavior.TypeFilterT filter = IStitchBehavior.TypeFilterT.NONE;
        if (ctx.IDENTIFIER ().size () > 1) {
            filter = ctx.COLON () != null ? IStitchBehavior.TypeFilterT.SATISFIES : IStitchBehavior.TypeFilterT
                    .DECLARES;
        }
        boolean hasExpr = false;
        if (ctx.expression () != null) {
            hasExpr = true;
            beh.setupPathFilter (filter != IStitchBehavior.TypeFilterT.NONE ? ctx.IDENTIFIER (0) : null);
            visitExpression (ctx.expression ());
        }
//        if (filter != IStitchBehavior.TypeFilterT.NONE || hasExpr) {
            boolean resultisSet = ctx.ELLIPSIS() == null;
			beh.continueExpressionFilter (filter, ctx.IDENTIFIER (0), ctx.IDENTIFIER (1), ctx.expression (), ctx.pathExpressionContinuation () != null, resultisSet);
//        }
        if (ctx.pathExpressionContinuation () != null) {
            visitPathExpressionContinuation (ctx.pathExpressionContinuation ());
        }
        return true;
    }

    @Override
    public Boolean visitCondition (@NotNull StitchParser.ConditionContext ctx) {
        beh.beginConditionBlock (ctx);
        for (int i = 0; i < ctx.expression ().size (); i++) {
        	beh.beginCondition(i);
            visitExpression (ctx.expression (i));
            beh.endCondition(i);
        }
        beh.endConditionBlock ();
        return true;
    }

    @Override
    public Boolean visitAction (@NotNull StitchParser.ActionContext ctx) {
        beh.beginActionBlock (ctx);
        for (int i = 0; i < ctx.statement().size(); i++) {
        	beh.beginAction(i);
        	visitStatement(ctx.statement(i));
        	beh.endAction (i);
        }
        beh.endActionBlock ();
        return true;
    }

    @Override
    public Boolean visitEffect (@NotNull StitchParser.EffectContext ctx) {
        beh.beginEffectBlock (ctx);
        int exprStartIndex = 0;
        if (ctx.AT () != null)
            exprStartIndex = 1;
        for (int i = exprStartIndex; i < ctx.expression ().size (); i++) {
            visitExpression (ctx.expression (i));
        }
        if (ctx.AT () != null) {
            visitExpression (ctx.expression (0));
            beh.doTacticDuration (ctx.expression (0));
        }
//        super.visitEffect (ctx);
        beh.endEffectBlock (ctx);
        return true;
    }

    @Override
    public Boolean visitStrategy (@NotNull StitchParser.StrategyContext ctx) {
//        IScope preScope = beh.stitch().scope ();
        TerminalNode identifier = ctx.IDENTIFIER ();
		beh.beginStrategy (identifier);
        visitExpression (ctx.expression ());
        beh.doStrategyCondition (Strategy.ConditionKind.APPLICABILITY, ctx);
        visitFunctions (ctx.functions ());
        for (int i = 0; i < ctx.strategyNode ().size (); i++) {
            visitStrategyNode (ctx.strategyNode (i));
        }
        beh.endStrategy ();
//        if (preScope != beh.stitch().scope ()) {
//            System.out.println ("Scopes don't match: " + this.toString ());
//        }
        return true;
    }

    @Override
    public Boolean visitStrategyNode (@NotNull StitchParser.StrategyNodeContext ctx) {
//        IScope preScope = beh.stitch().scope ();
        beh.beginStrategyNode (ctx.IDENTIFIER (), ctx);
        visitStrategyCond(ctx.strategyCond());
        visitTacticRef(ctx.tacticRef());
        beh.endStrategyNode ();
//        if (preScope != beh.stitch().scope ()) {
//            System.out.println ("Scopes don't match: " + this.toString ());
//        }
        return true;
    }

    @Override
    public Boolean visitStrategyCond (@NotNull StitchParser.StrategyCondContext ctx) {
        int exprIdx = 0;
        if (ctx.HASH () != null) {
            visitExpression (ctx.expression (0));
            exprIdx = 1;
            beh.doStrategyProbability (ctx);
        }
        if (ctx.SUCCESS () != null) beh.doStrategyCondition (Strategy.ConditionKind.SUCCESS, ctx);
        if (ctx.FAILURE () != null) beh.doStrategyCondition (Strategy.ConditionKind.FAILURE, ctx);
        if (ctx.DEFAULT () != null) beh.doStrategyCondition (Strategy.ConditionKind.DEFAULT, ctx);
        if (ctx.expression () != null && ctx.expression ().size () == exprIdx + 1) {
            visitExpression (ctx.expression (exprIdx));
            beh.doStrategyCondition (Strategy.ConditionKind.EXPRESSION, ctx);
        }
        return true;
    }

    @Override
    public Boolean visitTacticRef (@NotNull StitchParser.TacticRefContext ctx) {
        if (ctx.DONE () != null) beh.doStrategyAction (Strategy.ActionKind.DONE);
        else if (ctx.NULLTACTIC () != null) beh.doStrategyAction (Strategy.ActionKind.NULL);
        else if (ctx.DO () != null) {
            beh.doStrategyLoop (ctx.v, ctx.i, ctx.t2);
        } else {
            beh.beginReferencedTactic (ctx.IDENTIFIER (0));
            int exprEndIdx = ctx.expression ().size ();
            if (ctx.AT () != null)
                exprEndIdx--;
            for (int i = 0; i < exprEndIdx; i++) {
                visitExpression (ctx.expression (i));
            }
            beh.endReferencedTactic (ctx.IDENTIFIER (0));
            if (ctx.AT () != null) {
                visitExpression (ctx.expression (exprEndIdx));
                beh.doStrategyDuration (ctx.expression (exprEndIdx),ctx.IDENTIFIER(0));
            }
            if (!ctx.strategyBranch ().isEmpty ()) {
                visitStrategyBranch (ctx.strategyBranch ());
            }

        }
        return true;
    }

    @Override
    public Boolean visitStrategyBranch (@NotNull StitchParser.StrategyBranchContext ctx) {
        beh.beginBranching ();
        super.visitStrategyBranch (ctx);
        beh.endBranching ();
        return true;
    }

    @Override
    public Boolean visitStatement (@NotNull StitchParser.StatementContext ctx) {
    	VisitorState preState = new VisitorState();
        if (!ctx.statement ().isEmpty ()) {
            beh.beginStatement (Strategy.StatementKind.STMT_LIST, ctx);
            for (int i = 0; i < ctx.statement ().size (); i++) {
                visitStatement (ctx.statement (i));
            }
            if (ctx.errorHandler () != null) {
                visitErrorHandler (ctx.errorHandler ());
            }
            beh.endStatement (Strategy.StatementKind.STMT_LIST, ctx);
        } else if (ctx.expression () != null) {
            beh.beginStatement (Strategy.StatementKind.EXPRESSION, ctx);
            visitExpression (ctx.expression ());
            beh.endStatement (Strategy.StatementKind.EXPRESSION, ctx);
        } else if (ctx.ifStmt () != null) {
            visitIfStmt (ctx.ifStmt ());
        } else if (ctx.whileStmt () != null) {
            visitWhileStmt (ctx.whileStmt ());
        } else if (ctx.forStmt () != null) {
            visitForStmt (ctx.forStmt ());
        } else if (ctx.var () != null) {
            visitVar (ctx.var ());
        } else {
            beh.beginStatement (Strategy.StatementKind.EMPTY_STMT, ctx);
            beh.endStatement (Strategy.StatementKind.EMPTY_STMT, ctx);
        }
        checkPostState(preState);
        return true;
    }

    @Override
    public Boolean visitErrorHandler (@NotNull StitchParser.ErrorHandlerContext ctx) {
        beh.beginStatement (Strategy.StatementKind.ERROR, ctx);
        super.visitErrorHandler (ctx);
        beh.endStatement (Strategy.StatementKind.ERROR, ctx);
        return true;
    }

    @Override
    public Boolean visitIfStmt (@NotNull StitchParser.IfStmtContext ctx) {
        beh.beginStatement (Strategy.StatementKind.IF, ctx);
        super.visitIfStmt (ctx);
        beh.endStatement (Strategy.StatementKind.IF, ctx);
        return true;
    }

    @Override
    public Boolean visitWhileStmt (@NotNull StitchParser.WhileStmtContext ctx) {
        beh.beginStatement (Strategy.StatementKind.WHILE, ctx);
        super.visitWhileStmt (ctx);
        beh.endStatement (Strategy.StatementKind.WHILE, ctx);
        return true;
    }

    @Override
    public Boolean visitForStmt (@NotNull StitchParser.ForStmtContext ctx) {
        beh.beginStatement (Strategy.StatementKind.FOR, ctx);
        if (ctx.forInit () != null) {
            visitForInit (ctx.forInit ());
            beh.markForCondition ();
            visitForCond (ctx.forCond ());
            visitForIter (ctx.forIter ());
        } else {
            visitTacticParam (ctx.tacticParam ());
            beh.markForEach ();
            visitExpression (ctx.expression ());
        }
        visitStatement (ctx.statement ());
        beh.endStatement (Strategy.StatementKind.FOR, ctx);
        return true;
    }

    @Override
    public Boolean visitExpression (@NotNull StitchParser.ExpressionContext ctx) {
    	VisitorState preState = new VisitorState();
        boolean pushed = beh.beginExpression (ctx);
        VisitorState state1 = new VisitorState();
        visitAssignmentExpression(ctx.assignmentExpression());
        checkPostState(state1);
        beh.doExpression (ctx);
        checkPostState(state1);
        beh.endExpression (ctx, pushed);
        checkPostState(preState);
        return true;
    }
    
    protected boolean checkPostState(VisitorState pre) {
    	IScope scope = beh.stitch().scope();
    	Expression expr = beh.stitch().expr();
    	boolean ret = true;
    	if (pre.scope != scope) {
    		ret =  false;
    	}
    	else if (pre.expr != expr) {
    		ret =  false;
    	}
    	else if (scope != null && pre.scopeLevel != scope.getLevel()) {
    		ret =  false;
    	}
    	else if (expr != null && pre.expr != null && pre.exprLevel != expr.subLevel) {
    		ret =  false;
    	}
    	if (!ret) {
    		String msg = MessageFormat.format("PreExpr=''{2}'', current=''{3}''", pre.scope.toStringTree(), scope.toStringTree(), ParserUtils.formatTree(pre.expr!=null?pre.expr.tree():null), ParserUtils.formatTree(expr!=null?expr.tree():null));
//    		System.out.println(msg);
    	}
    	return ret;
    }

//	protected void checkScopeMatch(IScope preScope, int level) {
//		IScope scope = beh.stitch().scope ();
//		if (preScope != scope) {
//            System.out.println ("Scopes don't match: " + this.toString ());
//        }
//		else {
//			if (preScope instanceof Expression) {
//				Expression postExpression = (Expression )scope;
//				if (postExpression.subLevel != level) {
//					System.out.println("sublevels don't match");
//				}
//			}
//		}
//	}


    //    @Override
//    public Boolean visitExpression (@NotNull StitchParser.ExpressionContext ctx) {
//        IScope preScope = beh.stitch().scope ();
//        String type = "unknown";
//        beh.beginExpression ();
//        if (ctx.e != null) {
//            type = "nested";
//            visitExpression (ctx.expression (0));
//            beh.doExpression (ctx.e);
//        }
//        else if (ctx.assignmentExpression () != null) {
//            type = "assignment";
//            visitAssignmentExpression (ctx.assignmentExpression ());
//        } else if (isArithmetic (ctx) || isRelational (ctx) || isLogical (ctx)) {
//            beh.lOp ();
//            visitExpression (ctx.expression (0));
//            beh.rOp ();
//            visitExpression (ctx.expression (1));
//            if (isArithmetic (ctx)) {
//                type = "arithmetic";
//                beh.doArithmeticExpression (getExpressionKind (ctx), ctx);
//            }
//            else if (isRelational (ctx)) {
//                type = "relational";
//                beh.doRelationalExpression (getExpressionKind (ctx), ctx);
//            }
//            else if (isLogical (ctx)) {
//                type = "logical";
//                beh.doLogicalExpression (getExpressionKind (ctx), ctx);
//            }
//        } else if (ctx.quantifiedExpression () != null) {
//            type = "quantified";
//            visitQuantifiedExpression (ctx.quantifiedExpression ());
//        }
//        else if (ctx.setExpression () != null) {
//            type = "set";
//            visitSetExpression (ctx.setExpression ());
//        }
//        else if (ctx.unaryExpression () != null) {
//            type="unary";
//            visitUnaryExpression (ctx.unaryExpression ());
//        }
//        else if (ctx.idExpression () != null) {
//            type="id";
//            visitIdExpression (ctx.idExpression ());
//        }
//        else if (ctx.postIdExpression () != null) visitPostIdExpression (ctx.postIdExpression ());
//        beh.endExpression ();
//        if (preScope != beh.stitch().scope ()) {
//            System.out.println ("visitExpression(" + type + "): Scopes don't match: " + this.toString ());
//        }
//        return true;
//    }

    @Override
    public Boolean visitAssignmentExpression (@NotNull StitchParser.AssignmentExpressionContext ctx) {
        if (ctx.assignmentExpression () != null) {
            boolean pushed = beh.beginExpression (ctx);
            beh.lOp ();
            visitBooleanExpression (ctx.booleanExpression ());
            beh.rOp ();
            visitAssignmentExpression (ctx.assignmentExpression ());
            beh.doAssignExpression (ctx.booleanExpression (), ctx.assignmentExpression ());
            beh.endExpression (ctx, pushed);
        } else {
            visitBooleanExpression (ctx.booleanExpression ());
        }

        return true;
    }

    @Override
    public Boolean visitBooleanExpression (@NotNull StitchParser.BooleanExpressionContext ctx) {
    	VisitorState preState = new VisitorState();

        boolean pushed = beh.beginExpression (ctx);
        if (ctx.impliesExpression() != null)
        	visitImpliesExpression(ctx.impliesExpression());
        else 
        	visitQuantifiedExpression(ctx.quantifiedExpression());
        beh.endExpression (ctx, pushed);
        checkPostState(preState);
        return true;
    }

    @Override
    public Boolean visitImpliesExpression (@NotNull StitchParser.ImpliesExpressionContext ctx) {
        if (ctx.impliesExpression () != null) {
            boolean pushed = beh.beginExpression (ctx);
            beh.lOp ();
            visitIffExpression (ctx.iffExpression ());
            beh.rOp ();
            visitImpliesExpression (ctx.impliesExpression ());
            beh.doLogicalExpression (Strategy.ExpressionKind.IMPLIES, ctx);
            beh.endExpression (ctx, pushed);
        } else {
            visitIffExpression (ctx.iffExpression ());
        }
        return true;
    }

    @Override
    public Boolean visitIffExpression (@NotNull StitchParser.IffExpressionContext ctx) {
        if (ctx.iffExpression () != null) {
            boolean pushed = beh.beginExpression (ctx);
            beh.lOp ();
            visitLogicalOrExpression (ctx.logicalOrExpression ());
            beh.rOp ();
            visitIffExpression (ctx.iffExpression ());
            beh.doLogicalExpression (Strategy.ExpressionKind.IFF, ctx);
            beh.endExpression (ctx, pushed);
        } else {
        	visitLogicalOrExpression(ctx.logicalOrExpression());
        }
        return true;
    }

    @Override
    public Boolean visitLogicalOrExpression (@NotNull StitchParser.LogicalOrExpressionContext ctx) {
        if (ctx.logicalOrExpression () != null) {

        	VisitorState preState = new VisitorState();
            boolean pushed = beh.beginExpression (ctx);
            beh.lOp ();
            visitLogicalAndExpression (ctx.logicalAndExpression ());
            beh.rOp ();
            visitLogicalOrExpression (ctx.logicalOrExpression ());
            beh.doLogicalExpression (Strategy.ExpressionKind.OR, ctx);
            beh.endExpression (ctx, pushed);
            checkPostState(preState);
        } else {
        	visitLogicalAndExpression(ctx.logicalAndExpression());
        }
        return true;
    }

    @Override
    public Boolean visitLogicalAndExpression (@NotNull StitchParser.LogicalAndExpressionContext ctx) {
    	try {
        if (ctx.logicalAndExpression () == null) {
        	visitEqualityExpression(ctx.equalityExpression());
        } else {
        	VisitorState preState = new VisitorState();

            boolean pushed = beh.beginExpression (ctx);
            beh.lOp ();
            visitEqualityExpression (ctx.equalityExpression ());
            beh.rOp ();
            visitLogicalAndExpression (ctx.logicalAndExpression ());
            beh.doLogicalExpression (Strategy.ExpressionKind.AND, ctx);
            beh.endExpression (ctx,pushed);
            checkPostState(preState);
        }
        return true;
    	}
    	catch (Throwable t) {
    		System.out.println("Throwing exception for " + ParserUtils.formatTree(ctx));
    		throw t;
    	}
    }

    @Override
    public Boolean visitEqualityExpression (@NotNull StitchParser.EqualityExpressionContext ctx) {
        if (ctx.equalityExpression () == null) {
        	visitRelationalExpression(ctx.relationalExpression());
        } else {
        	VisitorState pre = new VisitorState();

            boolean pushed = beh.beginExpression (ctx);
            beh.lOp ();
            visitRelationalExpression (ctx.relationalExpression ());
            beh.rOp ();
            visitEqualityExpression (ctx.equalityExpression ());
            beh.doRelationalExpression (ctx.EQ () != null ? Strategy.ExpressionKind.EQ : Strategy.ExpressionKind.NE,
                                        ctx);
            beh.endExpression (ctx, pushed);
            checkPostState(pre);
        }
        return true;
    }

    @Override
    public Boolean visitRelationalExpression (@NotNull StitchParser.RelationalExpressionContext ctx) {
        if (ctx.relationalExpression () == null)
        	visitAdditiveExpression(ctx.additiveExpression());
        else {
        	VisitorState pre = new VisitorState();
            boolean pushed = beh.beginExpression (ctx);
            beh.lOp ();
            VisitorState lState = new VisitorState();
            visitAdditiveExpression (ctx.additiveExpression ());
            checkPostState(lState);
            beh.rOp ();
            VisitorState rState = new VisitorState();
            visitRelationalExpression (ctx.relationalExpression ());
            checkPostState(rState);
            beh.doRelationalExpression (getExpressionKind (ctx), ctx);
            beh.endExpression (ctx, pushed);
            checkPostState(pre);
        }
        return true;
    }

    private Strategy.ExpressionKind getExpressionKind (StitchParser.RelationalExpressionContext ctx) {
        if (ctx.GE () != null) return Strategy.ExpressionKind.GE;
        if (ctx.GT () != null) return Strategy.ExpressionKind.GT;
        if (ctx.LT () != null) return Strategy.ExpressionKind.LT;
        if (ctx.LE () != null) return Strategy.ExpressionKind.LE;
        return Strategy.ExpressionKind.UNKNOWN;
    }

    @Override
    public Boolean visitAdditiveExpression (@NotNull StitchParser.AdditiveExpressionContext ctx) {
        if (ctx.additiveExpression () == null)
            return visitMultiplicativeExpression(ctx.multiplicativeExpression());
        else {
            boolean pushed = beh.beginExpression (ctx);
            beh.lOp ();
            visitMultiplicativeExpression (ctx.multiplicativeExpression ());
            beh.rOp ();
            visitAdditiveExpression (ctx.additiveExpression ());
            beh.doArithmeticExpression (ctx.MINUS () == null ? Strategy.ExpressionKind.PLUS : Strategy.ExpressionKind
                    .MINUS, ctx);
            beh.endExpression (ctx, pushed);
        }
        return true;
    }

    @Override
    public Boolean visitMultiplicativeExpression (@NotNull StitchParser.MultiplicativeExpressionContext ctx) {
        if (ctx.multiplicativeExpression () == null) {
        	visitUnaryExpression(ctx.unaryExpression());
        } else {
            boolean pushed = beh.beginExpression (ctx);
            beh.lOp ();
            visitUnaryExpression (ctx.unaryExpression ());
            beh.rOp ();
            visitMultiplicativeExpression (ctx.multiplicativeExpression ());
            beh.doArithmeticExpression (getExpressionKind (ctx), ctx);
            beh.endExpression (ctx, pushed);
        }
        return true;
    }

    private Strategy.ExpressionKind getExpressionKind (StitchParser.MultiplicativeExpressionContext ctx) {
        if (ctx.SLASH () != null) return Strategy.ExpressionKind.DIVIDE;
        if (ctx.STAR () != null) return Strategy.ExpressionKind.MULTIPLY;
        if (ctx.MOD () != null) return Strategy.ExpressionKind.MOD;
        return Strategy.ExpressionKind.UNKNOWN;
    }

    @Override
    public Boolean visitUnaryExpression (@NotNull StitchParser.UnaryExpressionContext ctx) {
        if (ctx.unaryExpression () != null) {
        	
            boolean pushed = beh.beginExpression (ctx);
            beh.lOp ();
            visitUnaryExpression (ctx.unaryExpression ());
            beh.doUnaryExpression (getUnaryExpressionKind (ctx), ctx);
            beh.endExpression (ctx, pushed);
        } else
        	visitPrimaryExpression(ctx.primaryExpression());
        return true;
    }

    //    @Override
//    public Boolean visitAssignmentExpression (@NotNull StitchParser.AssignmentExpressionContext ctx) {
//        IScope preScope = beh.stitch().scope ();
//        beh.lOp ();
//        beh.rOp ();
//        visitExpression (ctx.expression ());
//        beh.doAssignExpression (ctx.IDENTIFIER (), ctx.expression ());
//        if (preScope != beh.stitch().scope ()) {
//            System.out.println ("visitAssignmentExpression: Scopes don't match: " + this.toString ());
//        }
//        return true;
//    }
//
//    @Override
//    public Boolean visitUnaryExpression (@NotNull StitchParser.UnaryExpressionContext ctx) {
//        IScope preScope = beh.stitch().scope ();
//        beh.lOp ();
//        visitExpression (ctx.expression ());
//        beh.doUnaryExpression (getUnaryExpressionKind (ctx), ctx);
//        if (preScope != beh.stitch().scope ()) {
//            System.out.println ("visitUnaryExpression: Scopes don't match: " + this.toString ());
//        }
//        return true;
//    }

    @Override
    public Boolean visitQuantifiedExpression (@NotNull StitchParser.QuantifiedExpressionContext ctx) {
    	VisitorState pre = new VisitorState();
        beh.beginQuantifiedExpression (ctx);
        visitParams (ctx.params ());
        if (ctx.setExpression () != null) {
            boolean pushed = beh.beginExpression (ctx.setExpression());
            visitSetExpression (ctx.setExpression ());
            beh.endExpression (ctx, pushed);
        } else if (ctx.idExpression () != null) {
            boolean pushed = beh.beginExpression (ctx.idExpression());
            visitIdExpression (ctx.idExpression ());
            beh.endExpression (ctx, pushed);
        }
        beh.doQuantifiedExpression (getQuantifierKind (ctx), ctx);
        visitExpression (ctx.expression ());
        beh.endQuantifiedExpression (getQuantifierKind (ctx), ctx);
        checkPostState(pre);
        return true;
    }

    @Override
    public Boolean visitPrimaryExpression (@NotNull StitchParser.PrimaryExpressionContext ctx) {
        if (ctx.setExpression () != null)
        	visitSetExpression(ctx.setExpression());
        else if (ctx.assignmentExpression () != null) {
            boolean pushed = beh.beginExpression (ctx.assignmentExpression());
            visitAssignmentExpression (ctx.assignmentExpression ());
            beh.endExpression (ctx, pushed);
        } else {
        	VisitorState pre = new VisitorState();
            boolean pushed = beh.beginExpression (ctx);
            VisitorState p = new VisitorState();
            if (ctx.idExpression() != null)
            	visitIdExpression(ctx.idExpression());
            else if (ctx.postIdExpression() != null)
            	visitPostIdExpression(ctx.postIdExpression());
            else if (ctx.pathExpression() != null)
            	visitPathExpression(ctx.pathExpression());
            checkPostState(p);
            beh.doExpression (ctx);
            beh.endExpression (ctx,pushed);
            checkPostState(pre);
        }
        return true;
    }

    @Override
    public Boolean visitSetExpression (@NotNull StitchParser.SetExpressionContext ctx) {
    	VisitorState pre = new VisitorState();

        beh.beginSetExpression (ctx);
        visitLiteralSet(ctx.literalSet());
        beh.endSetExpression (ctx);
        checkPostState(pre);
        return true;
    }

    @Override
    public Boolean visitNonLiteralIdExpression (@NotNull StitchParser.NonLiteralIdExpressionContext ctx) {
        if (ctx.methodCall () != null) {
            visitMethodCall (ctx.methodCall ());
        } else {
            boolean pushed = beh.beginExpression (ctx);
            beh.doIdentifierExpression (ctx, Strategy.ExpressionKind.IDENTIFIER);
            beh.endExpression (ctx, pushed);
        }
        return true;
    }

    @Override
    public Boolean visitIdExpression (@NotNull StitchParser.IdExpressionContext ctx) {
    	VisitorState pre = new VisitorState();

        if (ctx.methodCall () != null) {
            visitMethodCall (ctx.methodCall ());
        } else {
            beh.doIdentifierExpression (ctx, getIdExpressionKind (ctx));
        }
        checkPostState(pre);
        return true;
    }

    @Override
    public Boolean visitPostIdExpression (@NotNull StitchParser.PostIdExpressionContext ctx) {
        beh.doPostIdentifierExpression (ctx);
        return true;
    }

    @Override
    public Boolean visitMethodCall (@NotNull StitchParser.MethodCallContext ctx) {
    	VisitorState pre = new VisitorState();
    	ExpressionsContext expr = ctx.expressions();
        beh.beginMethodCallExpression (ctx);
        if (ctx.expressions() != null)  {
        	for (ParseTree e : expr.children) {
        		if (e instanceof StitchParser.ExpressionContext) {
        			VisitorState eState = new VisitorState();
        			this.visitExpression ((StitchParser.ExpressionContext )e);
        			checkPostState(eState);
        			beh.processParameter();
        		}
        		else if (!(e instanceof TerminalNode)) {
        			throw new NullPointerException();
        		}
        	}
        }
        beh.endMethodCallExpression (ctx.IDENTIFIER (), ctx);
        checkPostState(pre);
        return true;
    }

    @Override
    public Boolean visitParams (@NotNull StitchParser.ParamsContext ctx) {
//        IScope preScope = beh.stitch().scope ();
        beh.beginParamList ();
        super.visitParams (ctx);
        beh.endParamList ();
//        if (preScope != beh.stitch().scope ()) {
//            System.out.println ("visitParams: Scopes don't match: " + this.toString ());
//        }
        return true;
    }

    @Override
    public Boolean visitParam (@NotNull StitchParser.ParamContext ctx) {
//        IScope preScope = beh.stitch().scope ();
        super.visitParam (ctx);
        beh.createVar (ctx.dataType (), ctx.IDENTIFIER (), null, false, false);
//        if (preScope != beh.stitch().scope ()) {
//            System.out.println ("visitParam: Scopes don't match: " + this.toString ());
//        }
        return true;
    }

    private Strategy.ExpressionKind getIdExpressionKind (StitchParser.IdExpressionContext ctx) {
        if (ctx.IDENTIFIER () != null) return Strategy.ExpressionKind.IDENTIFIER;
        if (ctx.INTEGER_LIT () != null) return Strategy.ExpressionKind.INTEGER;
        if (ctx.FLOAT_LIT () != null) return Strategy.ExpressionKind.FLOAT;
        if (ctx.STRING_LIT () != null) return Strategy.ExpressionKind.STRING;
        if (ctx.CHAR_LIT () != null) return Strategy.ExpressionKind.CHAR;
        if (ctx.TRUE () != null) return Strategy.ExpressionKind.BOOLEAN;
        if (ctx.FALSE () != null) return Strategy.ExpressionKind.BOOLEAN;
        if (ctx.NULL () != null) return Strategy.ExpressionKind.NULL;
        return Strategy.ExpressionKind.UNKNOWN;

    }

    private Strategy.ExpressionKind getQuantifierKind (StitchParser.QuantifiedExpressionContext ctx) {
        if (ctx.FORALL () != null) return Strategy.ExpressionKind.FORALL;
        if (ctx.EXISTS () != null && ctx.UNIQUE () != null) return Strategy.ExpressionKind.EXISTS_UNIQUE;
        if (ctx.EXISTS () != null) return Strategy.ExpressionKind.EXISTS;
        if (ctx.SELECT () != null) return Strategy.ExpressionKind.SELECT;
        return Strategy.ExpressionKind.UNKNOWN;

    }

    private Strategy.ExpressionKind getUnaryExpressionKind (StitchParser.UnaryExpressionContext ctx) {
        if (ctx.INCR () != null) return Strategy.ExpressionKind.INCR;
        if (ctx.DECR () != null) return Strategy.ExpressionKind.DECR;
        if (ctx.MINUS () != null) return Strategy.ExpressionKind.UNARY_MINUS;
        if (ctx.PLUS () != null) return Strategy.ExpressionKind.UNARY_PLUS;
        if (ctx.LOGICAL_NOT () != null) return Strategy.ExpressionKind.NOT;
        return Strategy.ExpressionKind.UNKNOWN;
    }


//    private boolean isLogical (StitchParser.ExpressionContext ctx) {
//        return ctx.IMPLIES () != null || ctx.IFF () != null || ctx.LOGICAL_AND () != null || ctx.LOGICAL_OR () !=
// null;
//    }
//
//    private boolean isRelational (StitchParser.ExpressionContext ctx) {
//        return ctx.NE () != null || ctx.EQ () != null || ctx.LT () != null || ctx.LE () != null || ctx.GE () != null
//                || ctx.GT () != null;
//    }
//
//    private boolean isArithmetic (@NotNull StitchParser.ExpressionContext ctx) {
//        return ctx.STAR () != null || ctx.SLASH () != null || ctx.MOD () != null || ctx.PLUS () != null || ctx
//                .MINUS () != null;
//    }
//
//    private Strategy.ExpressionKind getExpressionKind (StitchParser.ExpressionContext ctx) {
//        if (ctx.SLASH () != null) return Strategy.ExpressionKind.DIVIDE;
//        if (ctx.PLUS () != null) return Strategy.ExpressionKind.PLUS;
//        if (ctx.STAR () != null) return Strategy.ExpressionKind.MULTIPLY;
//        if (ctx.MOD () != null) return Strategy.ExpressionKind.MOD;
//        if (ctx.MINUS () != null) return Strategy.ExpressionKind.MINUS;
//        if (ctx.IMPLIES () != null) return Strategy.ExpressionKind.IMPLIES;
//        if (ctx.IFF () != null) return Strategy.ExpressionKind.IFF;
//        if (ctx.LOGICAL_AND () != null) return Strategy.ExpressionKind.AND;
//        if (ctx.LOGICAL_OR () != null) return Strategy.ExpressionKind.OR;
//        if (ctx.NE () != null) return Strategy.ExpressionKind.NE;
//        if (ctx.EQ () != null) return Strategy.ExpressionKind.EQ;
//        if (ctx.LT () != null) return Strategy.ExpressionKind.LT;
//        if (ctx.GE () != null) return Strategy.ExpressionKind.GE;
//        if (ctx.LE () != null) return Strategy.ExpressionKind.LE;
//        if (ctx.GT () != null) return Strategy.ExpressionKind.GT;
//        return Strategy.ExpressionKind.UNKNOWN;
//    }

    public void setBehavior (IStitchBehavior behavior) {
        beh = behavior;
    }
}
