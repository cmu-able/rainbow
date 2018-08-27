package org.sa.rainbow.stitch.visitor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.stitch.Ohana;
import org.sa.rainbow.stitch.core.Expression;
import org.sa.rainbow.stitch.core.IScope;
import org.sa.rainbow.stitch.core.Import;
import org.sa.rainbow.stitch.core.PostVar;
import org.sa.rainbow.stitch.core.ScopedEntity;
import org.sa.rainbow.stitch.core.Statement;
import org.sa.rainbow.stitch.core.StitchScript;
import org.sa.rainbow.stitch.core.Strategy;
import org.sa.rainbow.stitch.core.Strategy.ActionKind;
import org.sa.rainbow.stitch.core.Strategy.ConditionKind;
import org.sa.rainbow.stitch.core.Strategy.ExpressionKind;
import org.sa.rainbow.stitch.core.Strategy.ParseState;
import org.sa.rainbow.stitch.core.Strategy.StatementKind;
import org.sa.rainbow.stitch.core.StrategyNode;
import org.sa.rainbow.stitch.core.Tactic;
import org.sa.rainbow.stitch.core.Var;
import org.sa.rainbow.stitch.parser.StitchParser;
import org.sa.rainbow.stitch.util.Tool;
import org.sa.rainbow.util.Util;

/**
 * Created by schmerl on 10/3/2016.
 */
public class StitchScopeEstablisher extends BaseStitchBehavior {

    StitchBeginEndVisitor m_walker;

    public StitchScopeEstablisher (Stitch/*State*/ stitch) {
        super (stitch);
    }

    @Override
    public void setWalker (StitchBeginEndVisitor walker) {
        m_walker = walker;
    }

    @Override
    public void beginScript (IScope scriptScope) {
        pushScope (scriptScope); // set enclosing scope first

        debug ("== Begin Script ==");
        setScript (new StitchScript (scope (), null, m_stitch));
        pushScope (script ());
    }

    @Override
    public void endScript () {
        popScope ();
        debug ("^^ End Script ^^");

        // check to see if we have at least one tactic or one strategy
        // both list can't be empty in a script
        if (script ().tactics.size () == 0 && script ().strategies.size () == 0) {
            // ALI: MODIFIED
            Tool.warn (
                    "A script with no tactic nor strategy defined is not very useful!",
                    null, stitchProblemHandler ());
        }
        popScope ();
    }

    @Override
    public void createModule (String text) {
        script ().setName (text);

        debug ("Got module id \"" + script ().getName () + "\"");
    }

    @Override
    public Import createImport (StitchParser.ImportStContext ctx, Token path) {
        Import.Kind type = Import.Kind.UNKNOWN;
        if (ctx.MODEL () != null) type = Import.Kind.MODEL;
        else if (ctx.LIB () != null) type = Import.Kind.LIB;
        else if (ctx.OP () != null) type = Import.Kind.OP;
        String target = path.getText ();

        Import imp = new Import ();
        imp.scope = scope ();
        imp.type = type;
        imp.path = target.substring (1, target.length () - 1);
        imp.tree = ctx;
        script ().imports.add (imp);

        debug ("Importing resource at path \"" + imp.path + "\"");

        return imp;
    }

    @Override
    public void addImportRename (Token origName, Token renName) {
        if (script ().renames.containsKey (renName.getText ())) { // uh oh, warn of duplicate
            stitchProblemHandler ().setProblem (generateErrorFromToken (renName, MessageFormat
                    .format ("Import rename used a duplicate name '''{0}'''! Rename not valid.", renName
                            .getText ())));
        } else {
            script ().addRename (renName.getText (), origName.getText ());
        }
    }

    @Override
    public void doImports () {
        resolveImports ();
    }

    @Override
    public void doPostIdentifierExpression (StitchParser.PostIdExpressionContext identifier) {
        IScope scope = scope ();
        final Object preV = scope.lookup (identifier.IDENTIFIER ().getText ());
        if (!(preV instanceof Var)) {
            stitchProblemHandler ().setProblem (generateErrorFromToken (identifier.IDENTIFIER ().getSymbol (),
                                                                        "Variable " + identifier.IDENTIFIER ()
                                                                                .getText () +
                                                                                " is not defined as a variable in the" +
                                                                                " tactic, so can't calculate post " +
                                                                                "condition."));
            return;
        }


        IScope s = scope;
        while (!(s instanceof Tactic) && s != null)
            s = s.parent ();
        if (s instanceof Tactic) {
            Tactic t = ((Tactic) s);
            Tactic.ParseState preState = t.state;

            Var preVVar = (Var) preV;
            Var postV = new PostVar (preVVar);
            postV.scope = scope;
            postV.setType (preVVar.getType ());
            if (postV.computeClass () == null) {
                postV.setIsBasicType (false);
            }
            postV.name = "__post__" + identifier.IDENTIFIER ().getText ();
//            postV.valStmt = new Statement (s, postV.name + "_scope", stitch ());
//            postV.valStmt.setTree (preVVar.valStmt.tree ());
//            postV.scope = postV.valStmt;
//            StitchBeginEndVisitor v = new StitchBeginEndVisitor (this, postV.valStmt);
//            v.setBehavior (this);
//            beginStatement (StatementKind.VAR_DEF, (ParserRuleContext )preVVar.valStmt.tree ());
//            StitchParser.VarContext vc = (StitchParser.VarContext )preVVar.valStmt.tree ();
//            v.visitExpression (vc.expression ());
//            endStatement ();

            if (preState != Tactic.ParseState.IN_EFFECT) {
                stitchProblemHandler ().setProblem (generateErrorFromToken (identifier.IDENTIFIER ().getSymbol (),
                                                                            "Post condition variable " + postV
                                                                                    .name +
                                                                                    " can only be used in a tactic " +
                                                                                    "effect."));
            } else {
                if (!scope.parent ().addVar (postV.name, postV)) {
                    stitchProblemHandler ().setProblem (generateErrorFromToken (identifier.IDENTIFIER ().getSymbol (),
                                                                                "Variable " + postV
                                                                                        .name + "" +
                                                                                        " already defined."));
                }
            }
        } else
            stitchProblemHandler ().setProblem (generateErrorFromToken (identifier.IDENTIFIER ().getSymbol (),
                                                                        "Variable " + identifier.getText () + " is " +
                                                                                "not" +
                                                                                " being defined in an effect."));
        Expression expr = (Expression) scope ();
        storeExprTree (identifier);
        expr.setKind (translateExpressionKind (ExpressionKind.IDENTIFIER));
        expr.setName (identifier.getText ());
        // if (kind == Expression.Kind.IDENTIFIER) {
        // see if identifier is a var, if yes, add to list of vars
        String id = identifier.getText ();
        Object o = scope ().lookup (id);
        if (o != null && o instanceof Var) {
            expr.addRefdVar ((Var) o);
        }
    }

    @Override
    public void createVar (StitchParser.DataTypeContext type, TerminalNode id, StitchParser.ExpressionContext val,
                           boolean isFunction) {
        Var var = new Var ();
        var.scope = scope (); // var is declared in current scope
        var.setType (type.getText ());
        if (var.computeClass () == null) {
            var.setIsBasicType (false);
        }
        var.name = id.getText ();
        var.setFunction (isFunction);
        if (scope () instanceof Statement) {
            // store the statement of the var assignment
            var.valStmt = (Statement) scope ();
        }

        if (!scope ().addVar (var.name, var)) {
            stitchProblemHandler ().setProblem (generateErrorFromToken (id.getSymbol (), "Variable " + var.name + "" +
                    " already defined."));
        }

        debug ("Declaring var type \"" + var.getType () + "\", " + var.name
                       + " == " + var.valStmt);
    }

    @Override
    public void beginVarList () {
        if (scope () instanceof Tactic) {
            ((Tactic) scope ()).state = Tactic.ParseState.IN_VARS;
        } else if (scope () instanceof Strategy) {
            ((Strategy) scope ()).state = ParseState.IN_VARS;
        }
    }


    @Override
    public void beginParamList () {
        if (scope () instanceof Tactic) {
            ((Tactic) scope ()).state = Tactic.ParseState.IN_PARAMS;
        } else if (scope () instanceof Strategy) {
            ((Strategy) scope ()).state = ParseState.IN_PARAMS;
        }
        debug ("&> Begin param list...");
    }

    @Override
    public void endParamList () {
        debug ("<& End param list");
    }

    @Override
    public void lOp () {

    }

    @Override
    public void rOp () {

    }

    @Override
    public void beginExpression () {
        /*
         * Expression IR-construction design intent: - Expressions are held as
         * needed for evaluation to compute results. - Construct expression
         * objects only for (a) top-level expression, (b) complex expressions
         * like quantified/method/list, and (c) any expressions immediately
         * "below" the complex expressions. - Use scoping while tree walking to
         * maintain the additional info needed to achieve the above
         * construction.
         *
         * Algorithm: - At begin of an expression, check scope() to see if it's
         * Expression = if NOT an expression, construct an Expression and push
         * as scope = if it's an expression, see which kind it is + if
         * QUANTIFIED or LIST, construct an Expression and push as scope +
         * otherwise, just increment depth count and finish - At end of an
         * expression, get the scope() Expression E = if depth is non-zero,
         * decrement depth count of E and finish = if depth is ZERO, then +
         * store the AST in E + if parent scope is either (a) NOT an Expression
         * or (b) a complex Expression, also add E to parent' expression list +
         * pop scope - At begin of a "complex" expression, = Construct an
         * Expression CE marked with appropriate "complex" kind = Push CE as
         * scope - At end of a "complex" expression, = Store AST and add this
         * complex Expression to parent's list
         */
        boolean newExprScope = false;
        if (scope () instanceof Expression) {
            Expression expr = (Expression) scope ();
            if (expr.isComplex ()) {
                newExprScope = true;
            } else { // increment depth count
                expr.subLevel++;
            }
        } else {
            newExprScope = true;
        }

        if (newExprScope) {
            Expression expr = new Expression (scope (), "[expression]", m_stitch);
            debug ("#> Begin expression");
            pushScope (expr);
        }

    }

    @Override
    public void endExpression (ParserRuleContext ctx) {
        Expression expr = (Expression) scope ();
        if (expr.subLevel > 0) {
            expr.subLevel--;
        } else { // we're at a top-level expression
            // AST would be stored by now
            // determine whether to add Expression to parent's list
//            if (!(expr.parent () instanceof Expression) && !expr.hasAcmeExpression ()) {
//                int a = ctx.start.getStartIndex ();
//                int b = ctx.stop.getStopIndex ();
//                String text = ctx.getStart ().getInputStream ().getText (new Interval (a, b));
//
////                String text = ctx.getStart ().getInputStream ().getText (ctx.getSourceInterval ());
//                text = text.replaceAll ("&&", "and");
//                text = text.replaceAll ("\\|\\|", "or");
//                try {
//                    final IExpressionNode exprNode = StandaloneLanguagePackHelper.defaultLanguageHelper ()
//                            .designRuleExpressionFromString (text,
//                                                             new RegionManager ());
//                    expr.setAcmeExpression (exprNode);
//                } catch (Exception e) {
//
//                }
//            }
            boolean addToParentList = false;
            if (expr.parent () instanceof Expression) {
                Expression pExpr = (Expression) expr.parent ();
                if (pExpr.isComplex ()) {
                    addToParentList = true;
                }
            } else {
                addToParentList = true;
            }
            if (addToParentList) {
                debug ("=# add expression " + expr.getName () + " to parent "
                               + expr.parent ().getName ());
                expr.parent ().addExpression (expr);
            }
            if (expr.tree () == null) expr.setTree (ctx);

            popScope ();
            debug ("<# End expression");
        }
    }

    @Override
    public void beginQuantifiedExpression () {
        debug ("#Q> Begin quantified expression");
        // true on distinct scope to make sure vars are declared in this scope
        doBeginComplexExpr (null, Expression.Kind.QUANTIFIED, true);
    }

    @Override
    public void beginPathExpression () {
        debug ("#P> Begin path expression");
        doBeginComplexExpr (null, Expression.Kind.PATH, true);

    }

    @Override
    public void endQuantifiedExpression (ExpressionKind quantKind, StitchParser.QuantifiedExpressionContext
            quant) {
        Expression expr = doEndComplexExpr ();
        expr.setName (quant.getText ());
        expr.setTree (quant);
        debug ("<Q# End quantified expression");

        storeExprTree (quant);
    }

    @Override
    public void endPathExpression (StitchParser.PathExpressionContext pathExpr) {
        Expression expr = doEndComplexExpr ();
        expr.setName (pathExpr.getText ());
        expr.setTree (pathExpr);
        debug ("<P# End path expression");
        storeExprTree (pathExpr);
    }

    @Override
    public void beginMethodCallExpression () {
        debug ("=> begin method call expression");
        doBeginComplexExpr (null, Expression.Kind.LIST, false);
    }

    @Override
    public void endMethodCallExpression (TerminalNode mc, StitchParser.MethodCallContext id) {
        Expression expr = doEndComplexExpr ();
        expr.setName (id.getText ());
        debug ("=> end method call expression");

        storeExprTree (id);
    }

    @Override
    public void beginSetExpression () {
        debug ("=> begin set expression");
        doBeginComplexExpr ("set", Expression.Kind.LIST, false);
    }

    @Override
    public void endSetExpression (StitchParser.SetExpressionContext setAST) {
        doEndComplexExpr ();
        debug ("=> end set expression");

        storeExprTree (setAST);
    }

    @Override
    public void doExpression (ParserRuleContext exprAST) {
        debug ("=> recurse expression");

        storeExprTree (exprAST);
    }

    @Override
    public void doAssignExpression (ParserRuleContext identifier, ParserRuleContext expression) {
        debug ("=> assign expression");
        storeExprTree (expression);

        ((Expression) scope ()).setKind (Expression.Kind.ASSIGNMENT);
        scope ().setName (identifier.getText () + expression.getText ());
    }

    @Override
    public void doLogicalExpression (ExpressionKind opAST, ParserRuleContext ctx) {
        debug ("=> logical expression");
        storeExprTree (ctx);

        ((Expression) scope ()).setKind (Expression.Kind.LOGICAL);
        scope ().setName (ctx.getText ());
    }

    @Override
    public void doRelationalExpression (ExpressionKind opAST, ParserRuleContext ctx) {
        debug ("=> relational expression");
        storeExprTree (ctx);

        ((Expression) scope ()).setKind (Expression.Kind.RELATIONAL);
        scope ().setName (ctx.getText ());
    }

    @Override
    public void doArithmeticExpression (ExpressionKind opAST, ParserRuleContext ctx) {
        debug ("=> arithmetid expression");
        storeExprTree (ctx);

        ((Expression) scope ()).setKind (Expression.Kind.RELATIONAL);
        scope ().setName (ctx.getText ());
    }

    @Override
    public void doUnaryExpression (ExpressionKind opAST, StitchParser.UnaryExpressionContext ctx) {
        debug ("=> unary expression");
        storeExprTree (ctx);

        ((Expression) scope ()).setKind (Expression.Kind.UNARY);
        scope ().setName (ctx.getText ());
    }

    Expression.Kind translateExpressionKind (ExpressionKind kind) {
        switch (kind) {
            case AND:
            case OR:
            case IMPLIES:
            case IFF:
                return Expression.Kind.LOGICAL;
            case UNKNOWN:
                return Expression.Kind.UNKNOWN;
            case BOOLEAN:
                return Expression.Kind.BOOLEAN;
            case INTEGER:
                return Expression.Kind.INTEGER;
            case FLOAT:
                return Expression.Kind.FLOAT;
            case CHAR:
                return Expression.Kind.CHAR;
            case STRING:
                return Expression.Kind.STRING;
            case IDENTIFIER:
                return Expression.Kind.IDENTIFIER;
            case PLUS:
            case MINUS:
            case MULTIPLY:
            case DIVIDE:
            case MOD:
                return Expression.Kind.ARITHMETIC;
            case EQ:
            case NE:
            case LE:
            case LT:
            case GE:
            case GT:
                return Expression.Kind.RELATIONAL;
            case UNARY_PLUS:
            case UNARY_MINUS:
            case INCR:
            case DECR:
            case NOT:
                return Expression.Kind.UNARY;
            case FORALL:
            case EXISTS:
            case EXISTS_UNIQUE:
            case SELECT:
                return Expression.Kind.QUANTIFIED;
        }
        return Expression.Kind.UNKNOWN;
    }

    @Override
    public void doIdentifierExpression (ParserRuleContext idAST, ExpressionKind kind) {
        debug ("=> identifier expression");
        storeExprTree (idAST);
        // do nothing more; [2006-05-07] original use of Ref removed

        Expression expr = (Expression) scope ();
        expr.setKind (translateExpressionKind (kind));
        expr.setName (idAST.getText ());
        // if (kind == Expression.Kind.IDENTIFIER) {
        // see if identifier is a var, if yes, add to list of vars
        String id = idAST.getText ();
        Object o = scope ().lookup (id);
        if (o != null && o instanceof Var) {
            expr.addRefdVar ((Var) o);
        }
        // }
    }

    @Override
    public void beginStatement (StatementKind stmtAST, ParserRuleContext ctx) {
        String name = "[statement] " + ctx.getText ();
        Statement stmt = new Statement (scope (), name, m_stitch);
        stmt.setTree (ctx); // causes Statement type to be set
        // determine what to do with different stmt types
        if (stmt.type == Statement.Kind.IF || stmt.type == Statement.Kind.FOR
                || stmt.type == Statement.Kind.WHILE
                || stmt.type == Statement.Kind.ERROR) {
            scope ().setHasErrorHandler (true);
            // we don't want the child statement to establish separate scope
            stmt.suppressScope = true;
        } else if (stmt.type == Statement.Kind.DECLARATION
                || stmt.type == Statement.Kind.EXPRESSION
                || stmt.type == Statement.Kind.EMPTY) {
            // no separate scope needed
            stmt.setDistinctScope (false);
        } else if (stmt.type == Statement.Kind.COMPOUND) {
            if (scope () instanceof Statement
                    && ((Statement) scope ()).suppressScope) {

                stmt.setDistinctScope (false);
            }
        } else { // may be condition, action, or effect blocks
            stmt.suppressScope = false;
            stmt.setDistinctScope (true);
        }
        if (scope ().parent () instanceof Tactic) {
            // store ONLY top-level statements in tactic's scope
            scope ().parent ().addStatement (stmt);
        } else {
            // store statement if parent scope is distinct
            if (scope () instanceof Statement) {
                scope ().addStatement (stmt);
            }
        }

        if (stmt.isDistinctScope ()) {
            debug ("$> Begin statement \"" + name + "\"");
        } else {
            debug ("Creating statement \"" + ctx.toStringTree () + "\"");
        }
        pushScope (stmt); // still need to move down on the scope stack...
    }

    @Override
    public void markForCondition () {
        // assume we're in the for statement scope
        // set the for condition expression index to the size of current
        // expression list, since the next expression will be the condition
        ((Statement) scope ()).forCondIdx = scope ().expressions ().size ();
    }

    @Override
    public void markForEach () {
        // assume we're in the for statement scope
        // set the statement type to for each
        ((Statement) scope ()).type = Statement.Kind.FOREACH;
        // designate the loop var by name, which should be the only var
        // currently in scope
        ((Statement) scope ()).forEachVar = scope ().vars ().keySet ().iterator ()
                .next ();
    }

    @Override
    public void endStatement () {
        IScope prevScope = scope ();
        // had new scope, so move scope up one level
        popScope ();
        // check if scope closing occurs now
        if (prevScope instanceof Statement
                && prevScope.isDistinctScope ()) {
            debug ("<$ End statement \"" + prevScope.getName () + "\"");
        }
    }

    @Override
    public void beginTactic (Token nameAST) {
        String name = nameAST.getText ();
        Tactic tactic = new Tactic (scope (), name, m_stitch);
        script ().tactics.add (tactic);

        debug (">T> Begin tactic \"" + name + "\"");
        pushScope (tactic);
    }

    @Override
    public void endTactic () {
        if (scope () instanceof Tactic) {
            ((Tactic) scope ()).state = Tactic.ParseState.PARSED;
        }
        String name = scope ().getName ();
        popScope ();
        debug ("<T< End tactic \"" + name + "\"");
    }

    @Override
    public void beginConditionBlock (StitchParser.ConditionContext nameAST) {
        if (scope () instanceof Tactic) {
            ((Tactic) scope ()).state = Tactic.ParseState.IN_CONDITION;
            debug ("--- Begin CONDITION ---");
        }
    }

    @Override
    public void endConditionBlock () {
        if (scope () instanceof Tactic) {
            debug ("--- End CONDITION ---");
        }
    }

    @Override
    public void beginActionBlock (StitchParser.ActionContext nameAST) {
        if (scope () instanceof Tactic) {
            ((Tactic) scope ()).state = Tactic.ParseState.IN_ACTION;
            debug ("--- Begin ACTION ---");
            beginStatement (StatementKind.ACTION, nameAST);
        }
    }

    @Override
    public void endActionBlock () {
        if (scope ().parent () instanceof Tactic) {
            endStatement ();
            debug ("--- End ACTION ---");
        }
    }

    @Override
    public void beginEffectBlock (StitchParser.EffectContext nameAST) {
        if (scope () instanceof Tactic) {
            ((Tactic) scope ()).state = Tactic.ParseState.IN_EFFECT;
            debug ("--- Begin EFFECT ---");
        }
    }

    @Override
    public void endEffectBlock () {
        if (scope () instanceof Tactic) {
            debug ("--- End EFFECT ---");
        }
    }

    private ThreadLocal<Strategy>     curStrategy = new ThreadLocal<> ();
    private ThreadLocal<StrategyNode> curNode     = new ThreadLocal<> ();
    private ThreadLocal<StrategyNode> parentNode  = new ThreadLocal<> ();

    @Override
    public void beginStrategy (TerminalNode nameAST) {
        String name = nameAST.getText ();
        curStrategy.set (new Strategy (scope (), name, m_stitch));
        script ().strategies.add (curStrategy.get ());
        curNode.set (curStrategy.get ().createRootNode ());
        parentNode.set (curNode.get ()); // root node serves as parent of all top-level
        // strategy nodes

        debug (">S> Begin strategy \"" + name + "\"");
        pushScope (curStrategy.get ());
    }

    @Override
    public void endStrategy () {
        if (scope () instanceof Strategy) {
            ((Strategy) scope ()).state = ParseState.PARSED;
        }
        String name = scope ().getName ();
        popScope ();
        debug ("<S< End strategy \"" + name + "\"");
    }

    @Override
    public void beginBranching () {
        parentNode.set (curNode.get ());
        debug ("{| Begin Strategy branch");
    }

    @Override
    public void endBranching () {
        if (parentNode.get () == null) {
            Tool.error (
                    "Error! The parent node is null when completing children branch!",
                    null, stitchProblemHandler ());
            return;
        }

        curNode.set (parentNode.get ());
        parentNode.set (parentNode.get ().getParent ());
        debug ("|} End Strategy branch");
    }

    @Override
    public void beginStrategyNode (TerminalNode identifier, ParserRuleContext ctx) {
        // create new Strategy tree node and add node to Strategy
        String label = "";
        if (identifier != null)
            label = identifier.getText ();
        else
            label = ctx.getText ();
        curNode.set (new StrategyNode (scope ().stitchState (), label));
        curStrategy.get ().addNode (curNode.get ());

        curNode.get ().setParent (parentNode.get ());
        if (parentNode.get () != null) {
            parentNode.get ().addBranch (curNode.get ());
        }

        debug ("-> Begin strategy node \"" + label + "\"");
        pushScope (new ScopedEntity (scope (), label, m_stitch));
    }

    @Override
    public void endStrategyNode () {
        if (curStrategy.get () == null) {
            Tool.error ("Unexpected error!  No current Strategy is set?!", null,
                        stitchProblemHandler ());
            return;
        }

        curNode.set (null);
        String name = scope ().getName ();
        popScope ();
        debug ("<- End strategy node \"" + name + "\"");
    }

    @Override
    public void doStrategyProbability () {
        if (curNode.get () == null) {
            Tool.error (
                    "Expected to be processing condition part of a strategy tree node, but null curNode encountered!!",
                    null, stitchProblemHandler ());
            return;
        }
        curNode.get ().setHasProbability (true);
        Expression expr = scope ().expressions ().get (scope ().expressions ().size () - 1);
        curNode.get ().setProbabilityExpr (expr);
        debug ("* Probability gathered: has it? " + curNode.get ().hasDuration () + ", expr == "
                       + curNode.get ().getProbabilityExpr ().tree ().toStringTree ());
//        // optional probability clause exists, process it
//        if (p1AST != null) { // an identifier that needs later substitution
//            curNode.setHasProbability(true);
//            String pKey = p1AST.getText();
//            if (p2AST != null) { // append "{subkey}"
//                pKey += "{" + p2AST.getText() + "}";
//            }
//            curNode.setProbKey(pKey);
//        } else if (pLitAST != null) { // a literal for probability
//            curNode.setHasProbability(true);
//            curNode.setProbKey(null); // just to be sure it'll be treated as
//            // literal
//            curNode.setProbability(Double.valueOf(pLitAST.getText()));
//        } else {
//            curNode.setHasProbability(false);
//        }
//        debug("* Probability gathered:  has it? " + curNode.hasProbability()
//                + ", key == " + curNode.getProbKey() + ", lit == "
//                + curNode.getProbability());
    }

    @Override
    public void doStrategyCondition (ConditionKind type, ParserRuleContext ctx) {
        StrategyNode curNodeL = curNode.get ();
		if (curNodeL == null) {
            Tool.error (
                    "Expected to be processing condition part of a strategy tree node, but null curNode encountered!!",
                    null, stitchProblemHandler ());
            return;
        }

        // check for condition expression
        curNodeL.setCondFlag (type);
        Expression expr = null;
        switch (type) {
            case APPLICABILITY: // in this case, should be the only expression in
                // strategy scope
            	expr = scope().expressions().get(0);
            	break;
            case EXPRESSION: // proper expression
                // retrieve the latest expression from the current transient scope
                expr = scope ().expressions ().get (scope ().expressions ().size () - 1);
                expr.setTree (ctx);
                break;
            case SUCCESS:
                // set using the TRUE expression
                expr = Expression.getTrueExpression ();
                // intentional fall-thru
            case FAILURE:
                if (parentNode.get () == null || parentNode.get ().getTactic () == null) {
                    // neither top-level node nor node whose parent does not invoke
                    // a tactic can just have SUCCESS as condition
                    Tool.error (
                            "The "
                                    + type.name ()
                                    + " conditon is only applicable if there's a parent node that invokes a tactic!",
                            null, stitchProblemHandler ());
                    return;
                } else {
                    if (expr == null) {
                        // set using the FALSE expression
                        expr = Expression.getFalseExpression ();
                    }
                }
                break;
            case DEFAULT: // set using the TRUE expr
                expr = Expression.getTrueExpression ();
                break;
        }

        curNodeL.setCondExpr (expr);
        debug ("* Condition expr:  " + expr);
    }

    @Override
    public void doStrategyDuration (ParserRuleContext ctx) {
        if (curNode.get () == null) {
            Tool.error (
                    "Expected to be processing condition part of a strategy tree node, but null curNode encountered!!",
                    null, stitchProblemHandler ());
            return;
        }

        // optional duration clause exists, process it
        curNode.get ().setHasDuration (true);
        // retrieve the latest expression from the current transient scope
        Expression expr = scope ().expressions ().get (
                scope ().expressions ().size () - 1);
        expr.setTree (ctx);
        curNode.get ().setDurationExpr (expr);
        debug ("* Duration gathered:  has it? " + curNode.get ().hasDuration ()
                       + ", expr == " + curNode.get ().getDurationExpr ().tree ().toStringTree ());
    }

    public void doTacticDuration (ParserRuleContext ctx) {
        if (!(scope () instanceof Tactic)) {
            Tool.error ("Unexpectedly processing tactic duration outside of a tactic", null, stitchProblemHandler ());
            return;
        }

        Tactic tactic = (Tactic) scope ();
        tactic.setHasDuration (true);
        // retrieve the first expression from the current scoep
        Expression expr = tactic.expressions ().get (0);
        expr.setTree (ctx);
        tactic.setDurationExpr (expr);
        debug ("* Duration gathered: has it? " + tactic.hasDuration () + ", expr = " + tactic.getDurationExpr ().tree
                ().toStringTree ());
    }

    @Override
    public void beginReferencedTactic (TerminalNode labelAST) {
        if (curNode.get () == null) {
            Tool.error (
                    "Expected to be processing action part of a strategy tree node, but null curNode encountered!!",
                    null, stitchProblemHandler ());
            return;
        }

        // action refers to a tactic, find the tactic
        String tacticName = labelAST.getText ();
        Object obj = scope ().lookup (tacticName);
        if (obj instanceof Tactic) { // good, found tactic, store it
            curNode.get ().setActionFlag (ActionKind.TACTIC);
            curNode.get ().setTactic ((Tactic) obj);
        } else { // tactic not found
            Tool.warn ("Referenced tactic not yet defined? " + tacticName,
                       null, stitchProblemHandler ());
            // keep going though
        }
        debug ("*> Begin Tactic reference:  " + tacticName);
        // start a complex expression to group argument expressions together
        doBeginComplexExpr (null, Expression.Kind.LIST, false);
    }

    @Override
    public void endReferencedTactic () {
        if (curNode == null) {
            Tool.error (
                    "Processing tactic reference in a strategy tree node, but null curNode encountered!!",
                    null, stitchProblemHandler ());
            return;
        }

        // retrieve the collected expressions and store them in tactic
        Expression expr = doEndComplexExpr ();
        for (Expression e : expr.expressions ()) {
            if (e.tree () != null)
                debug ("- argument expression: " + e.tree ().toStringTree ());
            curNode.get ().addTacticArgExpr (e);
        } // complex expression is disregarded after this
        debug ("*> End Tactic reference");
    }

    @Override
    public void doStrategyAction (ActionKind type) {
        if (curNode == null) {
            Tool.error (
                    "Expected to be processing action part of a strategy tree node, but null curNode encountered!!",
                    null, stitchProblemHandler ());
            return;
        }

        // action is not a tactic, set the proper type
        curNode.get ().setActionFlag (type);
        debug ("* Set Strategy action type " + type);
    }

    @Override
    public void doStrategyLoop (Token vAST, Token iAST, Token labelAST) {
        if (curNode.get () == null) {
            stitchProblemHandler ().setProblem (generateErrorFromToken (labelAST, "Expected to be processing " +
                    "condition part of a strategy tree not, but the current node is null."));

            return;
        }

        // action is a do loop, store the parts
        curNode.get ().setActionFlag (ActionKind.DOLOOP);
        String doTarget = labelAST.getText ();
        if (curStrategy.get ().nodes.containsKey (doTarget)) { // check if target has
            // tactic action
            StrategyNode tgtNode = curStrategy.get ().nodes.get (doTarget);
            if (tgtNode == null || tgtNode.getActionFlag () != ActionKind.TACTIC) {
                stitchProblemHandler ().setProblem (generateErrorFromToken (labelAST, "Invalid target " +
                        "node '" +
                        doTarget +
                        "'... target node must have " +
                        "a tactic action"));
                return;
            }
        } else {
            stitchProblemHandler ().setProblem (generateErrorFromToken (labelAST, "Do target does not " +
                    "refer to an existing label: " + doTarget));
            return;
        }
        // store target
        curNode.get ().setDoTarget (doTarget);
        // get loop count, if any
        if (vAST == null && iAST != null) { // literal
            curNode.get ().setNumDoTrials (Integer.parseInt (iAST.getText ()));
        } else if (vAST != null && iAST == null) { // identifier, look it up
            Object vObj = scope ().lookup (vAST.getText ());
            // make sure we match type expectation, report error and return
            // in case of any mismatch
            if (vObj instanceof Var) { // check to make sure type is correct
                Var v = (Var) vObj;
                v.computeValue ();
                Object val = v.getValue ();
                if (val instanceof Integer) {
                    curNode.get ().setNumDoTrials ((Integer) val);
                } else {
                    Tool.error (
                            "Mismatched type in loop max count:  "
                                    + v.getType () + " instead of expected int",
                            null, stitchProblemHandler ());
                    return;
                }
            } else { // not expected var
                Tool.error (
                        "Identifier for loop max count should refer to a declared variable!",
                        null, stitchProblemHandler ());
                return;
            }
        } else { // warning, neither a literal nor an identifier provided
            curNode.get ().setNumDoTrials (StrategyNode.DEFAULT_LOOP_MAX);
            Tool.warn (
                    "No loop max count provided for DO loop, using default of "
                            + StrategyNode.DEFAULT_LOOP_MAX,
                    null, stitchProblemHandler ());
            // no returning since we're using a default value
        }
        debug ("* Strategy DO loop, max count == " + curNode.get ().getNumDoTrials ());
    }


    private Expression doBeginComplexExpr (String name, Expression.Kind kind,
                                           boolean distinctScope) {
        Expression expr = new Expression (scope (), name, m_stitch);
        expr.setKind (kind);
        expr.setDistinctScope (distinctScope);
        pushScope (expr);

        return expr;
    }

    private Expression doEndComplexExpr () {
        Expression expr = (Expression) scope ();
        if (expr.parent () != null) {
            // store complex expr to the prev/parent expression's list
            expr.parent ().addExpression (expr);
        } else {
            Tool.error ("Parent expression of complex expression is NULL?!",
                        null, stitchProblemHandler ());
        }
        popScope ();

        return expr;
    }

    private void storeExprTree (ParseTree ast) {
        Expression expr = (Expression) scope ();
        if (expr.subLevel == 0 && expr.tree () == null) {
            // store if we're at the outermost expr of current subclause
            expr.setTree (ast);
            debug (" * stored [" + expr.subLevel + "]: " + ast.toStringTree ());
        }
    }

    /**
     * Resolve all the imports that have been created, by iterating through the
     * list and seeking out the import targets. For each import, depennding on
     * the type, resolution occurs differently. Each import brings in a
     * namespace to which references can later go to resolve references. The
     * different types of import causes the target to be treated differently: <br>
     * <br>
     * <ul>
     * <li> <code>Import.LIB</code>: another script to pull in
     * <li> <code>Import.MODEL</code>: an architectural model file (default:
     * Acme)
     * <li> <code>Import.OP</code>: an operator file, could be style operator or
     * utility operators like the system Query interface
     * </ul>
     */
    private void resolveImports () {
        for (Import imp : m_stitch/*.stitch()*/.script.imports) {
            if (imp.type == Import.Kind.LIB) {
                // read in the script and add its imports, tactics
                // and strategies into this script's scope
                File f = determinePath (imp.path);
                try {
                    Stitch stitch = Ohana.instance ().findStitch (
                            f.getAbsolutePath ());
                    if (stitch == null) { // imported script file not previously
                        // parsed
                        stitch = Stitch.newInstance (f.getAbsolutePath (),
                                                     stitchProblemHandler ());
                        Ohana.instance ().parseFile (stitch);
                        // BRS: Added so that tactics aren't imported twice
                        Ohana.instance ().storeStitch (f.getAbsolutePath (), stitch);
                    }
                    m_stitch/*.stitch()*/.script.renames.putAll (stitch.script.renames);
                    m_stitch/*.stitch()*/.script.tactics.addAll (stitch.script.tactics);
                    m_stitch/*.stitch()*/.script.strategies.addAll (stitch.script.strategies);
                    m_stitch./*stitch().*/script.models.addAll (stitch.script.models);
                    m_stitch./*stitch().*/script.ops.addAll (stitch.script.ops);
                    m_stitch.stitchProblemHandler
                            .addAll (stitch.stitchProblemHandler
                                             .unreportedProblems ());
                } catch (IOException e) {
                    // ALI: Modified
                    Tool.error ("Cannot find library file '" + imp.path
                                        + "' to import!", e, null, stitchProblemHandler ());
                    e.printStackTrace ();
                }
            } else if (imp.type == Import.Kind.MODEL) {
                if (Rainbow.instance ().getRainbowMaster ().modelsManager () != null) {
                    ModelReference model = Util.decomposeModelReference (imp.path);
                    if (model.getModelType () == null) {

                        model = new ModelReference (imp.path.split ("\\.")[0], "Acme");
                    }
                    Object o = Rainbow.instance ().getRainbowMaster ().modelsManager ()
                            .getModelInstance (model);
                    if (o instanceof AcmeModelInstance) {
                        AcmeModelInstance ami = (AcmeModelInstance) o;
                        m_stitch./*stitch().*/script.models.add (ami);
                    } else {
                        Tool.warn ("Could not import unknown model " + imp.path, null, stitchProblemHandler ());
                    }
                }
                /*else if (Ohana2.instance ().modelRepository () != null) {
                    // TODO: Assumes that we run on same VM as the models manager

                    try {
                        Object o = Rainbow.instance ().getRainbowMaster ().modelsManager ()
                                .getModelInstanceByResource (determinePath (imp.path).getCanonicalPath ());

                        if (o instanceof IAcmeModel) {
                            IAcmeModel model = (IAcmeModel )o;
//                            m_stitch.script.models.add (model);
                            throw new NotImplementedException ("Need to implement this for standard files");
                        }
                        else if (o instanceof AcmeModelInstance) {
                            AcmeModelInstance s = (AcmeModelInstance )o;
                            m_stitch.script.models.add (s);
                        }
                        else {
                            Tool.warn ("No AcmeModel loaded, perhaps a dummy repository is used?!", null,
                                       stitchProblemHandler ());
                        }
                    }
                    catch (IOException e) {
                        Tool.warn ("Could not import model from '" + imp.path + "'", null, stitchProblemHandler ());
                    }
                }*/
                else {
                    Tool.error (
                            "Model repository not set yet! Unable to load model file "
                                    + imp.path, null, stitchProblemHandler ());
                }
            } else if (imp.type == Import.Kind.OP) {
                // import a file of operator, or a path to a package of classes,
                // as in Java's import statement: import java.util.*;
                // TODO: support JAR file later

                Class<?>[] classes = null;
                // see if name is a package, i.e., ends in *
                if (imp.path.endsWith (".*")) { // yes, list classes in package
                    String pkgname = imp.path.substring (0,
                                                         imp.path.length () - 2);
                    try {
                        classes = Util.getClasses (pkgname);
                    } catch (ClassNotFoundException e) {
                        Tool.warn ("Package name in OP import appears invalid: "
                                           + pkgname, e, imp.tree, stitchProblemHandler ());
                        continue;
                    }
                } else {

                    // Treat imported name as name of class
                    classes = new Class<?>[1];
                    String className = imp.path;
                    if (className.endsWith (".class")) { // truncate
                        className = className.substring (0, className.length () - 6);
                    }
                    String packageName = imp.path.substring (0, imp.path.lastIndexOf ("."));
                    List<ClassLoader> classLoaderList = new LinkedList<ClassLoader> ();
                    classLoaderList.add (ClasspathHelper.contextClassLoader ());
                    classLoaderList.add (ClasspathHelper.staticClassLoader ());
                    Reflections reflections = new Reflections (new ConfigurationBuilder ()
                                                                       .setScanners (new SubTypesScanner (false), new
                                                                               ResourcesScanner ())
                                                                       .setUrls (ClasspathHelper.forClassLoader
                                                                               (classLoaderList.toArray (new ClassLoader[0])))
                                                                       .filterInputsBy (new FilterBuilder ().include
                                                                               (FilterBuilder.prefix (packageName))));
                    Set<Class<?>> foundClasses = reflections.getSubTypesOf (Object.class);
                    for (Class<?> candidate : foundClasses) {
                        String name = candidate.getName ();
                        if (name.equals (className)) {
                            classes[0] = candidate;
                            break;
                        }
                    }
                    if (classes[0] == null) {
                        Tool.warn ("Class name in OP import appears invalid: " + className, null, imp.tree,
                                   stitchProblemHandler ());
                        continue;
                    }
                }
                StringBuffer mStr = new StringBuffer ("[ "); // for debug
                if (classes != null) {
                    for (Class<?> clazz : classes) {
                        // store class object
                        m_stitch./*stitch().*/script.ops.add (clazz);
                        // build list of accessible methods and method meta info
                        Method[] methods = clazz.getDeclaredMethods ();
                        for (Method m : methods) {
                            mStr.append (m.toString ()).append (" ");
                        }
                    }
                }
                Tool.logger ().debug (
                        "Imported library has methods: " + mStr.append ("]"));
            }
        }
    }

    private File determinePath (String path) {
        File f = new File (path);
        if (!f.exists ()) {
            // search in path of current script
            f = new File (m_stitch/*.stitch ()*/.path);
            String parentDirname = f.getParent ();
            String newPath = parentDirname + File.separator + path;
            f = new File (newPath);

            if (!f.exists ()) { // do some more path mangling
                // look inside "model" subdir in parent dir
                String modelPath = parentDirname + File.separator + ".."
                        + File.separator + "model" + File.separator + path;
                f = new File (modelPath);
            }
        }
        return f;
    }


}
