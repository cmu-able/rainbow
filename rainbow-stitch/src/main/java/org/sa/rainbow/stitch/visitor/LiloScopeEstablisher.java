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
 * Created March 15, 2006, separated from class Stitch April 4, 2006. 
 */
package org.sa.rainbow.stitch.visitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.acmestudio.acme.model.IAcmeModel;
import org.apache.commons.lang.NotImplementedException;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.stitch.Ohana;
import org.sa.rainbow.stitch.core.Expression;
import org.sa.rainbow.stitch.core.IScope;
import org.sa.rainbow.stitch.core.Import;
import org.sa.rainbow.stitch.core.ScopedEntity;
import org.sa.rainbow.stitch.core.Statement;
import org.sa.rainbow.stitch.core.StitchScript;
import org.sa.rainbow.stitch.core.Strategy;
import org.sa.rainbow.stitch.core.Strategy.ActionKind;
import org.sa.rainbow.stitch.core.Strategy.ConditionKind;
import org.sa.rainbow.stitch.core.StrategyNode;
import org.sa.rainbow.stitch.core.Tactic;
import org.sa.rainbow.stitch.core.Var;
import org.sa.rainbow.stitch.util.Tool;
import org.sa.rainbow.util.Util;

import antlr.collections.AST;

/**
 * The main purpose of this class is to serve as the behavior of the treewalker
 * (for practical programming, to continue to be able to tap into Java IDE while
 * doing grammar stuff). So almost all the actions that needs to happen inside
 * the tree walker gets done here.
 * 
 * As of April 4, 2006, this class is renamed to ScopeEstablisher, whose purpose
 * is to:
 * <ol>
 * <li>establish scope
 * <li>setup symbol tables in scope
 * <li>resolve imports
 * <li>resolve references
 * </ol>
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class LiloScopeEstablisher extends AbstractLiloBehavior implements
ILiloBehavior {

    /**
     * @param stitch
     *            the script molding scaffold
     */
    protected LiloScopeEstablisher(Stitch stitch) {
        super(stitch);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sa.rainbow.stitch.visitor.AbstractLiloBehavior#beginScript(org.sa
     * .rainbow.stitch.core.IScope)
     */
    @Override
    public void beginScript(IScope scriptScope) {
        pushScope(scriptScope); // set enclosing scope first

        debug("== Begin Script ==");
        setScript(new StitchScript(scope(), null, m_stitch));
        pushScope(script());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sa.rainbow.stitch.visitor.AbstractLiloBehavior#endScript()
     */
    @Override
    public void endScript() {
        popScope();
        debug("^^ End Script ^^");

        // check to see if we have at least one tactic or one strategy
        // both list can't be empty in a script
        if (script().tactics.size() == 0 && script().strategies.size() == 0) {
            // ALI: MODIFIED
            Tool.warn(
                    "A script with no tactic nor strategy defined is not very useful!",
                    null, stitchProblemHandler());
        }
        popScope();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sa.rainbow.stitch.core.AbstractLiloBehavior#createModule(antlr.
     * collections.AST)
     */
    @Override
    public void createModule(AST modAST) {
        script().setName(modAST.getText());

        debug("Got module id \"" + script().getName() + "\"");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sa.rainbow.stitch.core.AbstractLiloBehavior#createImport(antlr.
     * collections.AST, antlr.collections.AST)
     */
    @Override
    public Import createImport(AST impAST, AST pathAST) {
        String type = impAST.getText();
        String target = pathAST.getText();

        Import imp = new Import();
        imp.scope = scope();
        imp.type = Import.determineType(type);
        imp.path = target.substring(1, target.length() - 1);
        imp.ast = impAST;
        script().imports.add(imp);

        debug("Importing resource at path \"" + imp.path + "\"");

        return imp;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sa.rainbow.stitch.visitor.AbstractLiloBehavior#addImportRename(antlr
     * .collections.AST, antlr.collections.AST)
     */
    @Override
    public void addImportRename(AST origAST, AST renAST) {
        if (script().renames.containsKey(renAST)) { // uh oh, warn of duplicate
            // name
            // ALI: MODIFIED
            Tool.error("Import rename used a duplicate name '" + renAST
                    + "'!  Rename not valid.", renAST, stitchProblemHandler());
        } else {
            script().addRename(renAST, origAST);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sa.rainbow.stitch.core.AbstractLiloBehavior#doImports()
     */
    @Override
    public void doImports() {
        resolveImports();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sa.rainbow.stitch.core.AbstractLiloBehavior#createVar(antlr.collections
     * .AST, antlr.collections.AST)
     */
    @Override
    public void createVar(AST typeAST, AST nameAST) {
        Var var = new Var();
        var.scope = scope(); // var is declared in current scope
        var.setType(typeAST.getText());
        if (var.computeClass() == null) {
            var.setIsBasicType(false);
        }
        var.name = nameAST.getText();
        if (scope() instanceof Statement) {
            // store the statement of the var assignment
            var.valStmt = (Statement) scope();
        }

        if (!scope().addVar(var.name, var)) {
            // variable already defined
            // ALI: Modified
            Tool.error("Variable " + var.name + " redefined.", nameAST,
                    stitchProblemHandler());
        }

        debug("Declaring var type \"" + var.getType() + "\", " + var.name
                + " == " + var.valStmt);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sa.rainbow.stitch.core.AbstractLiloBehavior#beginVarList()
     */
    @Override
    public void beginVarList() {
        if (scope() instanceof Tactic) {
            ((Tactic) scope()).state = Tactic.ParseState.IN_VARS;
        } else if (scope() instanceof Strategy) {
            ((Strategy) scope()).state = Strategy.ParseState.IN_VARS;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sa.rainbow.stitch.core.AbstractLiloBehavior#beginParamList()
     */
    @Override
    public void beginParamList() {
        if (scope() instanceof Tactic) {
            ((Tactic) scope()).state = Tactic.ParseState.IN_PARAMS;
        } else if (scope() instanceof Strategy) {
            ((Strategy) scope()).state = Strategy.ParseState.IN_PARAMS;
        }
        debug("&> Begin param list...");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sa.rainbow.stitch.core.AbstractLiloBehavior#endParamList()
     */
    @Override
    public void endParamList() {
        debug("<& End param list");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sa.rainbow.stitch.core.AbstractLiloBehavior#beginExpression()
     */
    @Override
    public void beginExpression() {
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
        if (scope() instanceof Expression) {
            Expression expr = (Expression) scope();
            if (expr.isComplex()) {
                newExprScope = true;
            } else { // increment depth count
                expr.subLevel++;
            }
        } else {
            newExprScope = true;
        }

        if (newExprScope) {
            Expression expr = new Expression(scope(), "[expression]", m_stitch);
            debug("#> Begin expression");
            pushScope(expr);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sa.rainbow.stitch.core.AbstractLiloBehavior#endExpression()
     */
    @Override
    public void endExpression() {
        Expression expr = (Expression) scope();
        if (expr.subLevel > 0) {
            expr.subLevel--;
        } else { // we're at a top-level expression
            // AST would be stored by now
            // determine whether to add Expression to parent's list
            boolean addToParentList = false;
            if (expr.parent() instanceof Expression) {
                Expression pExpr = (Expression) expr.parent();
                if (pExpr.isComplex()) {
                    addToParentList = true;
                }
            } else {
                addToParentList = true;
            }
            if (addToParentList) {
                debug("=# add expression " + expr.getName() + " to parent "
                        + expr.parent().getName());
                expr.parent().addExpression(expr);
            }

            popScope();
            debug("<# End expression");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sa.rainbow.stitch.visitor.AbstractLiloBehavior#beginQuantifiedExpression
     * ()
     */
    @Override
    public void beginQuantifiedExpression() {
        debug("#Q> Begin quantified expression");
        // true on distinct scope to make sure vars are declared in this scope
        doBeginComplexExpr(null, Expression.Kind.QUANTIFIED, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sa.rainbow.stitch.visitor.AbstractLiloBehavior#endQuantifiedExpression
     * (antlr.collections.AST)
     */
    @Override
    public void endQuantifiedExpression(AST quanAST) {
        Expression expr = doEndComplexExpr();
        expr.setName(quanAST.getText());
        expr.setAST(quanAST);
        debug("<Q# End quantified expression");

        storeExprAST(quanAST);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sa.rainbow.stitch.visitor.AbstractLiloBehavior#beginMethodCallExpression
     * ()
     */
    @Override
    public void beginMethodCallExpression() {
        debug("=> begin method call expression");
        doBeginComplexExpr(null, Expression.Kind.LIST, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sa.rainbow.stitch.core.AbstractLiloBehavior#endMethodCallExpression
     * (antlr.collections.AST, antlr.collections.AST)
     */
    @Override
    public void endMethodCallExpression(AST mcAST, AST idAST) {
        Expression expr = doEndComplexExpr();
        expr.setName(idAST.getText());
        debug("=> end method call expression");

        storeExprAST(mcAST);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sa.rainbow.stitch.visitor.AbstractLiloBehavior#beginSetExpression()
     */
    @Override
    public void beginSetExpression() {
        debug("=> begin set expression");
        doBeginComplexExpr("set", Expression.Kind.LIST, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sa.rainbow.stitch.core.AbstractLiloBehavior#endSetExpression(antlr
     * .collections.AST)
     */
    @Override
    public void endSetExpression(AST setAST) {
        doEndComplexExpr();
        debug("=> end set expression");

        storeExprAST(setAST);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sa.rainbow.stitch.core.AbstractLiloBehavior#doExpression(antlr.
     * collections.AST)
     */
    @Override
    public void doExpression(AST exprAST) {
        debug("=> recurse expression");
        storeExprAST(exprAST);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sa.rainbow.stitch.core.AbstractLiloBehavior#doAssignExpression(antlr
     * .collections.AST, antlr.collections.AST)
     */
    @Override
    public void doAssignExpression(AST opAST, AST lValAST) {
        debug("=> assign expression");
        storeExprAST(opAST);

        ((Expression) scope()).kind = Expression.Kind.ASSIGNMENT;
        scope().setName(lValAST.getText() + opAST.getText());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sa.rainbow.stitch.core.AbstractLiloBehavior#doLogicalExpression(antlr
     * .collections.AST)
     */
    @Override
    public void doLogicalExpression(AST opAST) {
        debug("=> logical expression");
        storeExprAST(opAST);

        ((Expression) scope()).kind = Expression.Kind.LOGICAL;
        scope().setName(opAST.getText());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sa.rainbow.stitch.core.AbstractLiloBehavior#doRelationalExpression
     * (antlr.collections.AST)
     */
    @Override
    public void doRelationalExpression(AST opAST) {
        debug("=> relational expression");
        storeExprAST(opAST);

        ((Expression) scope()).kind = Expression.Kind.RELATIONAL;
        scope().setName(opAST.getText());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sa.rainbow.stitch.core.AbstractLiloBehavior#doArithmeticExpression
     * (antlr.collections.AST)
     */
    @Override
    public void doArithmeticExpression(AST opAST) {
        debug("=> arithmetic expression");
        storeExprAST(opAST);

        ((Expression) scope()).kind = Expression.Kind.ARITHMETIC;
        scope().setName(opAST.getText());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sa.rainbow.stitch.core.AbstractLiloBehavior#doUnaryExpression(antlr
     * .collections.AST)
     */
    @Override
    public void doUnaryExpression(AST opAST) {
        debug("=> unary expression");
        storeExprAST(opAST);

        ((Expression) scope()).kind = Expression.Kind.UNARY;
        scope().setName(opAST.getText());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sa.rainbow.stitch.visitor.AbstractLiloBehavior#doIdentifierExpression
     * (antlr.collections.AST, org.sa.rainbow.stitch.core.Expression.Kind)
     */
    @Override
    public void doIdentifierExpression(AST idAST, Expression.Kind kind) {
        debug("=> identifier expression");
        storeExprAST(idAST);
        // do nothing more; [2006-05-07] original use of Ref removed

        Expression expr = (Expression) scope();
        expr.kind = kind;
        expr.setName(idAST.getText());
        // if (kind == Expression.Kind.IDENTIFIER) {
        // see if identifier is a var, if yes, add to list of vars
        String id = idAST.getText();
        Object o = scope().lookup(id);
        if (o != null && o instanceof Var) {
            expr.addRefdVar((Var) o);
        }
        // }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sa.rainbow.stitch.core.AbstractLiloBehavior#beginStatement(antlr.
     * collections.AST)
     */
    @Override
    public void beginStatement(AST stmtAST) {
        String name = "[statement] " + stmtAST.getText();
        Statement stmt = new Statement(scope(), name, m_stitch);
        stmt.setAST(stmtAST); // causes Statement type to be set
        // determine what to do with different stmt types
        if (stmt.type == Statement.Kind.IF || stmt.type == Statement.Kind.FOR
                || stmt.type == Statement.Kind.WHILE
                || stmt.type == Statement.Kind.ERROR) {
            scope().setHasErrorHandler(true);
            // we don't want the child statement to establish separate scope
            stmt.suppressScope = true;
        } else if (stmt.type == Statement.Kind.DECLARATION
                || stmt.type == Statement.Kind.EXPRESSION
                || stmt.type == Statement.Kind.EMPTY) {
            // no separate scope needed
            stmt.setDistinctScope(false);
        } else if (stmt.type == Statement.Kind.COMPOUND) {
            if (scope() instanceof Statement
                    && ((Statement) scope()).suppressScope) {

                stmt.setDistinctScope(false);
            }
        } else { // may be condition, action, or effect blocks
            stmt.suppressScope = false;
            stmt.setDistinctScope(true);
        }
        if (scope().parent() instanceof Tactic) {
            // store ONLY top-level statements in tactic's scope
            scope().parent().addStatement(stmt);
        } else {
            // store statement if parent scope is distinct
            if (scope() instanceof Statement) {
                scope().addStatement(stmt);
            }
        }

        if (stmt.isDistinctScope()) {
            debug("$> Begin statement \"" + name + "\"");
        } else {
            debug("Creating statement \"" + stmtAST.toStringList() + "\"");
        }
        pushScope(stmt); // still need to move down on the scope stack...
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sa.rainbow.stitch.visitor.AbstractLiloBehavior#markForCondition()
     */
    @Override
    public void markForCondition() {
        // assume we're in the for statement scope
        // set the for condition expression index to the size of current
        // expression list, since the next expression will be the condition
        ((Statement) scope()).forCondIdx = scope().expressions().size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sa.rainbow.stitch.visitor.AbstractLiloBehavior#markForEach()
     */
    @Override
    public void markForEach() {
        // assume we're in the for statement scope
        // set the statement type to for each
        ((Statement) scope()).type = Statement.Kind.FOREACH;
        // designate the loop var by name, which should be the only var
        // currently in scope
        ((Statement) scope()).forEachVar = scope().vars().keySet().iterator()
                .next();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sa.rainbow.stitch.core.AbstractLiloBehavior#endStatement()
     */
    @Override
    public void endStatement() {
        IScope prevScope = scope();
        // had new scope, so move scope up one level
        popScope();
        // check if scope closing occurs now
        if (prevScope instanceof Statement
                && ((Statement) prevScope).isDistinctScope()) {
            debug("<$ End statement \"" + prevScope.getName() + "\"");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sa.rainbow.stitch.core.AbstractLiloBehavior#beginTactic(antlr.collections
     * .AST)
     */
    @Override
    public void beginTactic(AST nameAST) {
        String name = nameAST.getText();
        Tactic tactic = new Tactic(scope(), name, m_stitch);
        script().tactics.add(tactic);

        debug(">T> Begin tactic \"" + name + "\"");
        pushScope(tactic);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sa.rainbow.stitch.core.AbstractLiloBehavior#endTactic()
     */
    @Override
    public void endTactic() {
        if (scope() instanceof Tactic) {
            ((Tactic) scope()).state = Tactic.ParseState.PARSED;
        }
        String name = scope().getName();
        popScope();
        debug("<T< End tactic \"" + name + "\"");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sa.rainbow.stitch.core.AbstractLiloBehavior#beginConditionBlock(antlr
     * .collections.AST)
     */
    @Override
    public void beginConditionBlock(AST nameAST) {
        if (scope() instanceof Tactic) {
            ((Tactic) scope()).state = Tactic.ParseState.IN_CONDITION;
            debug("--- Begin CONDITION ---");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sa.rainbow.stitch.core.AbstractLiloBehavior#endConditionBlock()
     */
    @Override
    public void endConditionBlock() {
        if (scope() instanceof Tactic) {
            debug("--- End CONDITION ---");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sa.rainbow.stitch.core.AbstractLiloBehavior#beginActionBlock(antlr
     * .collections.AST)
     */
    @Override
    public void beginActionBlock(AST nameAST) {
        if (scope() instanceof Tactic) {
            ((Tactic) scope()).state = Tactic.ParseState.IN_ACTION;
            debug("--- Begin ACTION ---");
            beginStatement(nameAST);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sa.rainbow.stitch.core.AbstractLiloBehavior#endActionBlock()
     */
    @Override
    public void endActionBlock() {
        if (scope().parent() instanceof Tactic) {
            endStatement();
            debug("--- End ACTION ---");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sa.rainbow.stitch.core.AbstractLiloBehavior#beginEffectBlock(antlr
     * .collections.AST)
     */
    @Override
    public void beginEffectBlock(AST nameAST) {
        if (scope() instanceof Tactic) {
            ((Tactic) scope()).state = Tactic.ParseState.IN_EFFECT;
            debug("--- Begin EFFECT ---");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sa.rainbow.stitch.core.AbstractLiloBehavior#endEffectBlock()
     */
    @Override
    public void endEffectBlock() {
        if (scope() instanceof Tactic) {
            debug("--- End EFFECT ---");
        }
    }

    private Strategy curStrategy = null;
    private StrategyNode curNode = null;
    /* Designates the parent strategy node of the next branch node */
    private StrategyNode parentNode = null;

    /*
     * (non-Javadoc)
     * 
     * @see org.sa.rainbow.stitch.core.AbstractLiloBehavior#beginStrategy(antlr.
     * collections.AST)
     */
    @Override
    public void beginStrategy(AST nameAST) {
        String name = nameAST.getText();
        curStrategy = new Strategy(scope(), name, m_stitch);
        script().strategies.add(curStrategy);
        curNode = curStrategy.createRootNode();
        parentNode = curNode; // root node serves as parent of all top-level
        // strategy nodes

        debug(">S> Begin strategy \"" + name + "\"");
        pushScope(curStrategy);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sa.rainbow.stitch.core.AbstractLiloBehavior#endStrategy()
     */
    @Override
    public void endStrategy() {
        if (scope() instanceof Strategy) {
            ((Strategy) scope()).state = Strategy.ParseState.PARSED;
        }
        String name = scope().getName();
        popScope();
        debug("<S< End strategy \"" + name + "\"");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sa.rainbow.stitch.visitor.AbstractLiloBehavior#beginBranching()
     */
    @Override
    public void beginBranching() {
        parentNode = curNode;
        debug("{| Begin Strategy branch");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sa.rainbow.stitch.visitor.AbstractLiloBehavior#endBranching()
     */
    @Override
    public void endBranching() {
        if (parentNode == null) {
            Tool.error(
                    "Error! The parent node is null when completing children branch!",
                    null, stitchProblemHandler());
            return;
        }

        curNode = parentNode;
        parentNode = parentNode.getParent();
        debug("|} End Strategy branch");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sa.rainbow.stitch.visitor.AbstractLiloBehavior#beginStrategyNode(
     * antlr.collections.AST)
     */
    @Override
    public void beginStrategyNode(AST labelAST) {
        // create new Strategy tree node and add node to Strategy
        String label = labelAST.getText();
        curNode = new StrategyNode(scope().stitch(), label);
        curStrategy.addNode(curNode);

        curNode.setParent(parentNode);
        if (parentNode != null) {
            parentNode.addBranch(curNode);
        }

        debug("-> Begin strategy node \"" + label + "\"");
        pushScope(new ScopedEntity(scope(), label, m_stitch));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sa.rainbow.stitch.visitor.AbstractLiloBehavior#endStrategyNode()
     */
    @Override
    public void endStrategyNode() {
        if (curStrategy == null) {
            Tool.error("Unexpected error!  No current Strategy is set?!", null,
                    stitchProblemHandler());
            return;
        }

        curNode = null;
        String name = scope().getName();
        popScope();
        debug("<- End strategy node \"" + name + "\"");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sa.rainbow.stitch.visitor.AbstractLiloBehavior#doStrategyProbability
     * (antlr.collections.AST, antlr.collections.AST, antlr.collections.AST)
     */
    @Override
    public void doStrategyProbability (/*AST p1AST, AST p2AST, AST pLitAST*/) {
        if (curNode == null) {
            Tool.error(
                    "Expected to be processing condition part of a strategy tree node, but null curNode encountered!!",
                    null, stitchProblemHandler ());
            return;
        }
        curNode.setHasProbability (true);
        Expression expr = scope ().expressions ().get (scope ().expressions ().size () - 1);
        curNode.setProbabilityExpr (expr);
        debug ("* Probability gathered: has it? " + curNode.hasDuration () + ", expr == "
                + curNode.getProbabilityExpr ().ast ().toStringList ());
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sa.rainbow.stitch.visitor.AbstractLiloBehavior#doStrategyCondition
     * (org.sa.rainbow.stitch.core.Strategy.ConditionKind)
     */
    @Override
    public void doStrategyCondition(ConditionKind type) {
        if (curNode == null) {
            Tool.error(
                    "Expected to be processing condition part of a strategy tree node, but null curNode encountered!!",
                    null, stitchProblemHandler());
            return;
        }

        // check for condition expression
        curNode.setCondFlag(type);
        Expression expr = null;
        switch (type) {
        case APPLICABILITY: // in this case, should be the only expression in
            // strategy scope
        case EXPRESSION: // proper expression
            // retrieve the latest expression from the current transient scope
            expr = scope().expressions().get(scope().expressions().size() - 1);
            break;
        case SUCCESS:
            // set using the TRUE expression
            expr = Expression.getTrueExpression();
            // intentional fall-thru
        case FAILURE:
            if (parentNode == null || parentNode.getTactic() == null) {
                // neither top-level node nor node whose parent does not invoke
                // a tactic can just have SUCCESS as condition
                Tool.error(
                        "The "
                                + type.name()
                                + " conditon is only applicable if there's a parent node that invokes a tactic!",
                                null, stitchProblemHandler());
                return;
            } else {
                if (expr == null) {
                    // set using the FALSE expression
                    expr = Expression.getFalseExpression();
                }
            }
            break;
        case DEFAULT: // set using the TRUE expr
            expr = Expression.getTrueExpression();
            break;
        }

        curNode.setCondExpr(expr);
        debug("* Condition expr:  " + expr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sa.rainbow.stitch.visitor.AbstractLiloBehavior#doStrategyDuration()
     */
    @Override
    public void doStrategyDuration() {
        if (curNode == null) {
            Tool.error(
                    "Expected to be processing condition part of a strategy tree node, but null curNode encountered!!",
                    null, stitchProblemHandler());
            return;
        }

        // optional duration clause exists, process it
        curNode.setHasDuration(true);
        // retrieve the latest expression from the current transient scope
        Expression expr = scope().expressions().get(
                scope().expressions().size() - 1);
        curNode.setDurationExpr(expr);
        debug("* Duration gathered:  has it? " + curNode.hasDuration()
                + ", expr == " + curNode.getDurationExpr().ast().toStringList());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sa.rainbow.stitch.visitor.AbstractLiloBehavior#beginReferencedTactic
     * (antlr.collections.AST)
     */
    @Override
    public void beginReferencedTactic(AST labelAST) {
        if (curNode == null) {
            Tool.error(
                    "Expected to be processing action part of a strategy tree node, but null curNode encountered!!",
                    labelAST, stitchProblemHandler());
            return;
        }

        // action refers to a tactic, find the tactic
        String tacticName = labelAST.getText();
        Object obj = scope().lookup(tacticName);
        if (obj instanceof Tactic) { // good, found tactic, store it
            curNode.setActionFlag(Strategy.ActionKind.TACTIC);
            curNode.setTactic((Tactic) obj);
        } else { // tactic not found
            Tool.warn("Referenced tactic not yet defined? " + tacticName,
                    labelAST, stitchProblemHandler());
            // keep going though
        }
        debug("*> Begin Tactic reference:  " + tacticName);
        // start a complex expression to group argument expressions together
        doBeginComplexExpr(null, Expression.Kind.LIST, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sa.rainbow.stitch.visitor.AbstractLiloBehavior#endReferencedTactic()
     */
    @Override
    public void endReferencedTactic() {
        if (curNode == null) {
            Tool.error(
                    "Processing tactic reference in a strategy tree node, but null curNode encountered!!",
                    null, stitchProblemHandler());
            return;
        }

        // retrieve the collected expressions and store them in tactic
        Expression expr = doEndComplexExpr();
        for (Expression e : expr.expressions()) {
            debug("- argument expression: " + e.ast().toStringList());
            curNode.addTacticArgExpr(e);
        } // complex expression is disregarded after this
        debug("*> End Tactic reference");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sa.rainbow.stitch.visitor.AbstractLiloBehavior#doStrategyAction(org
     * .sa.rainbow.stitch.core.Strategy.ActionKind)
     */
    @Override
    public void doStrategyAction(ActionKind type) {
        if (curNode == null) {
            Tool.error(
                    "Expected to be processing action part of a strategy tree node, but null curNode encountered!!",
                    null, stitchProblemHandler());
            return;
        }

        // action is not a tactic, set the proper type
        curNode.setActionFlag(type);
        debug("* Set Strategy action type " + type);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sa.rainbow.stitch.visitor.AbstractLiloBehavior#doStrategyLoop(antlr
     * .collections.AST, antlr.collections.AST, antlr.collections.AST)
     */
    @Override
    public void doStrategyLoop(AST vAST, AST iAST, AST labelAST) {
        if (curNode == null) {
            Tool.error(
                    "Expected to be processing action part of a strategy tree node, but null curNode encountered!!",
                    labelAST, stitchProblemHandler());
            return;
        }

        // action is a do loop, store the parts
        curNode.setActionFlag(Strategy.ActionKind.DOLOOP);
        String doTarget = labelAST.getText();
        if (curStrategy.nodes.containsKey(doTarget)) { // check if target has
            // tactic action
            StrategyNode tgtNode = curStrategy.nodes.get(doTarget);
            if (tgtNode == null || tgtNode.getActionFlag() != ActionKind.TACTIC) {
                Tool.error(
                        "Invalid target node... target node have a Tactic action!",
                        labelAST, stitchProblemHandler());
                return;
            }
        } else {
            Tool.error("Do target does not refer to an existing label!",
                    labelAST, stitchProblemHandler());
            return;
        }
        // store target
        curNode.setDoTarget(doTarget);
        // get loop count, if any
        if (vAST == null && iAST != null) { // literal
            curNode.setNumDoTrials(Integer.parseInt(iAST.getText()));
        } else if (vAST != null && iAST == null) { // identifier, look it up
            Object vObj = scope().lookup(vAST.getText());
            // make sure we match type expectation, report error and return
            // in case of any mismatch
            if (vObj instanceof Var) { // check to make sure type is correct
                Var v = (Var) vObj;
                v.computeValue(m_stitch);
                Object val = v.getValue();
                if (val instanceof Integer) {
                    curNode.setNumDoTrials((Integer) val);
                } else {
                    Tool.error(
                            "Mismatched type in loop max count:  "
                                    + v.getType() + " instead of expected int",
                                    vAST, stitchProblemHandler());
                    return;
                }
            } else { // not expected var
                Tool.error(
                        "Identifier for loop max count should refer to a declared variable!",
                        vAST, stitchProblemHandler());
                return;
            }
        } else { // warning, neither a literal nor an identifier provided
            curNode.setNumDoTrials(StrategyNode.DEFAULT_LOOP_MAX);
            Tool.warn(
                    "No loop max count provided for DO loop, using default of "
                            + StrategyNode.DEFAULT_LOOP_MAX,
                            vAST == null ? iAST : vAST, stitchProblemHandler());
            // no returning since we're using a default value
        }
        debug("* Strategy DO loop, max count == " + curNode.getNumDoTrials());
    }

    private Expression doBeginComplexExpr(String name, Expression.Kind kind,
            boolean distinctScope) {
        Expression expr = new Expression(scope(), name, m_stitch);
        expr.kind = kind;
        expr.setDistinctScope(distinctScope);
        pushScope(expr);

        return expr;
    }

    private Expression doEndComplexExpr() {
        Expression expr = (Expression) scope();
        if (expr.parent() != null) {
            // store complex expr to the prev/parent expression's list
            expr.parent().addExpression(expr);
        } else {
            Tool.error("Parent expression of complex expression is NULL?!",
                    null, stitchProblemHandler());
        }
        popScope();

        return expr;
    }

    private void storeExprAST(AST ast) {
        Expression expr = (Expression) scope();
        if (expr.subLevel == 0 && expr.ast() == null) {
            // store if we're at the outermost expr of current subclause
            expr.setAST(ast);
            debug(" * stored [" + expr.subLevel + "]: " + ast.toStringTree());
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
    private void resolveImports() {
        for (Import imp : m_stitch.script.imports) {
            if (imp.type == Import.Kind.LIB) {
                // read in the script and add its imports, tactics
                // and strategies into this script's scope
                File f = determinePath(imp.path);
                try {
                    Stitch stitch = Ohana.instance().findStitch(
                            f.getAbsolutePath());
                    if (stitch == null) { // imported script file not previously
                        // parsed
                        stitch = Stitch.newInstance(f.getAbsolutePath(),
                                stitchProblemHandler());
                        Ohana.instance().parseFile(stitch);
                    }
                    m_stitch.script.renames.putAll(stitch.script.renames);
                    m_stitch.script.tactics.addAll(stitch.script.tactics);
                    m_stitch.script.strategies.addAll(stitch.script.strategies);
                    m_stitch.script.models.addAll(stitch.script.models);
                    m_stitch.script.ops.addAll(stitch.script.ops);
                    m_stitch.stitchProblemHandler
                    .addAll(stitch.stitchProblemHandler
                            .unreportedProblems());
                } catch (FileNotFoundException e) {
                    // ALI: Modified
                    Tool.error("Cannot find library file '" + imp.path
                            + "' to import!", e, null, stitchProblemHandler());
                    e.printStackTrace();
                }
            } else if (imp.type == Import.Kind.MODEL) {
                if (Rainbow.instance ().getRainbowMaster ().modelsManager () != null) {
                    TypedAttribute model = Util.decomposeModelReference (imp.path);
                    if (model.getType () == null) {

                        model = new TypedAttribute (imp.path.split ("\\.")[0], "Acme");
                    }
                    Object o = Rainbow.instance ().getRainbowMaster ().modelsManager ()
                            .getModelInstance (model.getType (), model.getName ());
                    if (o instanceof AcmeModelInstance) {
                        AcmeModelInstance ami = (AcmeModelInstance )o;
                        m_stitch.script.models.add (ami);
                    }
                    else {
                        Tool.warn ("Could not import unknown model " + imp.path, null, stitchProblemHandler ());
                    }
                }
                else if (Ohana.instance ().modelRepository () != null) {
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
                } else {
                    Tool.error(
                            "Model repository not set yet! Unable to load model file "
                                    + imp.path, null, stitchProblemHandler());
                }
            } else if (imp.type == Import.Kind.OP) {
                // import a file of operator, or a path to a package of classes,
                // as in Java's import statement: import java.util.*;
                // TODO: support JAR file later

                Class<?>[] classes = null;
                // see if name is a package, i.e., ends in *
                if (imp.path.endsWith(".*")) { // yes, list classes in package
                    String pkgname = imp.path.substring(0,
                            imp.path.length() - 2);
                    try {
                        classes = Util.getClasses(pkgname);
                    } catch (ClassNotFoundException e) {
                        Tool.warn("Package name in OP import appears invalid: "
                                + pkgname, e, imp.ast, stitchProblemHandler());
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
                    .setScanners (new SubTypesScanner (false), new ResourcesScanner ())
                    .setUrls (ClasspathHelper.forClassLoader (classLoaderList.toArray (new ClassLoader[0])))
                    .filterInputsBy (new FilterBuilder ().include (FilterBuilder.prefix (packageName))));
                    Set<Class<?>> foundClasses = reflections.getSubTypesOf (Object.class);
                    for (Class<?> candidate : foundClasses) {
                        String name = candidate.getName ();
                        if (name.equals (className)) {
                            classes[0] = candidate;
                            break;
                        }
                    }
                    if (classes[0] == null) {
                        Tool.warn ("Class name in OP import appears invalid: " + className, null, imp.ast,
                                stitchProblemHandler ());
                        continue;
                    }
                }
                StringBuffer mStr = new StringBuffer("[ "); // for debug
                if (classes != null) {
                    for (Class<?> clazz : classes) {
                        // store class object
                        m_stitch.script.ops.add(clazz);
                        // build list of accessible methods and method meta info
                        Method[] methods = clazz.getDeclaredMethods();
                        for (Method m : methods) {
                            mStr.append(m.toString()).append(" ");
                        }
                    }
                }
                Tool.logger().debug(
                        "Imported library has methods: " + mStr.append("]"));
            }
        }
    }

    private File determinePath(String path) {
        File f = new File(path);
        if (!f.exists()) {
            // search in path of current script
            f = new File(m_stitch.path);
            String parentDirname = f.getParent();
            String newPath = parentDirname + File.separator + path;
            f = new File(newPath);

            if (!f.exists()) { // do some more path mangling
                // look inside "model" subdir in parent dir
                String modelPath = parentDirname + File.separator + ".."
                        + File.separator + "model" + File.separator + path;
                f = new File(modelPath);
            }
        }
        return f;
    }

}
