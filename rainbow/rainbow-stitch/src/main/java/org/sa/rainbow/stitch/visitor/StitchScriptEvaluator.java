package org.sa.rainbow.stitch.visitor;

import org.acmestudio.acme.ModelHelper;
import org.acmestudio.acme.core.IAcmeType;
import org.acmestudio.acme.core.exception.AcmeException;
import org.acmestudio.acme.core.type.*;
import org.acmestudio.acme.element.IAcmeDesignAnalysisDeclaration;
import org.acmestudio.acme.element.IAcmeElement;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.element.property.IAcmePropertyValue;
import org.acmestudio.acme.environment.error.AcmeError;
import org.acmestudio.acme.model.DefaultAcmeModel;
import org.acmestudio.acme.model.util.core.UMBooleanValue;
import org.acmestudio.acme.model.util.core.UMStringValue;
import org.acmestudio.acme.rule.AcmeSet;
import org.acmestudio.acme.rule.IAcmeDesignAnalysis;
import org.acmestudio.acme.rule.node.FormalParameterNode;
import org.acmestudio.acme.rule.node.IExternalAnalysisExpressionNode;
import org.acmestudio.acme.rule.node.TypeReferenceNode;
import org.acmestudio.acme.type.verification.NodeScopeLookup;
import org.acmestudio.acme.type.verification.RuleTypeChecker;
import org.acmestudio.standalone.resource.StandaloneLanguagePackHelper;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.adaptation.IAdaptationExecutor;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.stitch.Ohana;
import org.sa.rainbow.stitch.core.*;
import org.sa.rainbow.stitch.model.ModelOperator;
import org.sa.rainbow.stitch.parser.StitchParser;
import org.sa.rainbow.stitch.util.Tool;
import org.sa.rainbow.util.Util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.*;

/**
 * Created by schmerl on 10/3/2016.
 */
public class StitchScriptEvaluator extends BaseStitchBehavior {
    private static final int LOP = Expression.LOP;
    private static final int ROP = Expression.ROP;

    private StitchBeginEndVisitor m_walker;

    public StitchScriptEvaluator (Stitch/*State*/ stitchState) {
        super (stitchState);
    }

    public void setWalker (StitchBeginEndVisitor walker) {
        m_walker = walker;
    }

    @Override
    public void createVar (StitchParser.DataTypeContext type, TerminalNode id, StitchParser.ExpressionContext val,
                           boolean isFunction) {
        // see if a value was assigned at var creation
        if (scope ().expressions ().size () > 0) {
            Var v = scope ().vars ().get (id.getText ());
            if (v != null && v.valStmt != null) {
                Expression e = scope ().expressions ().get (0);
                // make sure expr assocaited with var has AST and no known
                // result
                if (e.tree () != null && (e.getResult () == null || v.isFunction ())) {
                    e.evaluate (null, m_walker);
                }
                v.setValue (e.getResult ());
            }
        }
    }

    @Override
    public void lOp () {
        if (checkSkipEval ()) return;
        expr ().curOp.push (LOP);
    }

    @Override
    public void rOp () {
        if (checkSkipEval ()) return;
        expr ().curOp.push (ROP);
    }

    @Override
    public void beginExpression () {
        if (expr () != null) {
            // don't subExpr anymore, just keep depth count
            expr ().subLevel++;
            return;
        }

        final IScope scope = scope ();
        if (scope instanceof Statement && scope.expressions ().size () > ((Statement) scope).curExprIdx) {
            setExpression (scope.expressions ().get (((Statement) scope).curExprIdx++));
            expr ().curExprIdx = 0;
        } else if (scope instanceof Expression) {
            Expression expr = (Expression) scope;
            if (expr.isComplex ()) {
                if (expr.expressions ().size () > expr.curExprIdx) {
                    setExpression (expr.expressions ().get (expr.curExprIdx++));
                    expr.subLevel = 0;
                }
            } else {
                setExpression ((Expression) scope);
                expr ().curExprIdx = 0;
            }
        }
    }

    @Override
    public void endExpression (ParserRuleContext ctx) {
        Expression expr = expr ();
        if (expr.subLevel > 0) { // decr dept count first
            expr.subLevel--;
            return;
        } else { // check quantified expression special case
            if (expr.skipQuanPredicate) {
                expr.skipQuanPredicate = false;
            }
        }

        // transfer any child result
        if (expr.getResult () == null && expr.expressions ().size () > 0) {
            // transfer children result up
            Expression sub = expr.expressions ().get (0);
            expr.setResult (sub.getResult ());
        }

        setExpression (null); // clear eval expression reference
    }

    @Override
    public void beginQuantifiedExpression () {
        if (checkSkipEval ()) return;

        doBeginComplexExpr ();
    }

    @Override
    public void doQuantifiedExpression (Strategy.ExpressionKind type, StitchParser
            .QuantifiedExpressionContext ctx) {
        if (checkSkipEval ()) return;

        Expression cExpr = (Expression) scope ();
        if (cExpr.getKind () != Expression.Kind.QUANTIFIED) {
            Tool.error ("Error! Expected quantified expression not found!!", null, stitchProblemHandler ());
            return;
        }
        // at this point, this quantified expression is ready to be evaluated
        // - there should be at least one quantifier variable
        // TODO: handle just one quantifier variable for now
        // TODO: check expr variables (references) against list of vars...
        if (cExpr.vars ().size () > 1) {
            Tool.error ("Sorry, only one quantified variable is currently supported! " + cExpr, null,
                        stitchProblemHandler ());
            return;
        }
        Var v = (Var) cExpr.vars ().values ().toArray ()[0];
        // - the set expression should have values
        Object result = cExpr.expressions ().get (0).getResult ();
        Set set = Collections.EMPTY_SET;
        if (result instanceof Set) {
            set = (Set) result;
        } else if (result instanceof IAcmeSetValue) {
            set = (Set) ModelHelper.propertyValueToJava ((IAcmeSetValue) result);
        } else if (result instanceof AcmeSet) {
            set = ((AcmeSet) result).getValues ();
        } else if (result instanceof IAcmeProperty && ((IAcmeProperty) result).getValue () instanceof IAcmeSetValue) {
            set = (Set) ModelHelper.propertyValueToJava (((IAcmeProperty) result).getValue ());
        } else {
            Tool.error ("Error! Quantifier set comes from a set I don't understand! " + cExpr.toStringTree (), null,
                        stitchProblemHandler ());
        }
        if (set == null) {
            Tool.error ("Error! Quantifier set is NULL!" + cExpr.toStringTree (), null, stitchProblemHandler ());
            set = Collections.EMPTY_SET;
        }
        // - the predicate expression should have an AST ready to be evaluated
        Expression expr = cExpr.expressions ().get (1);
        // now we retrieve the subset of elements matching the type of the var
        Set subset = new LinkedHashSet ();
        if (set.size () > 0) {
            for (Object o : set) {
                if (Tool.typeMatches (v, o) && Tool.isArchEnabled (o)) {
                    // type matches AND this object in quantified set is arch
                    // enabled
                    subset.add (o);
                }
            }
        }

        // now we can evaluate quantified expression by
        // iterating through quantified set elements and collecting results
        boolean rv = true;
        Set newSet = null;
        if (type == Strategy.ExpressionKind.EXISTS_UNIQUE || type == Strategy.ExpressionKind.EXISTS) {
            // need to start EXISTS with false
            rv = false;
        }
        for (Object elem : subset) {
            v.setValue (elem);
            expr.evaluate (null, m_walker);
            boolean b = false;
            if (expr.getResult () instanceof Boolean) {
                b = (Boolean) expr.getResult ();
            } else if (expr.getResult () instanceof IAcmeProperty) {
                IAcmeProperty prop = (IAcmeProperty) expr.getResult ();
                if (prop.getValue () instanceof IAcmeBooleanValue) {
                    b = ((IAcmeBooleanValue) prop.getValue ()).getValue ();
                }

            }

            switch (type) {
                case FORALL:
                    rv &= b;
                    break;
                case EXISTS:
                    rv |= b;
                    break;
                case EXISTS_UNIQUE:
                    rv = (rv && !b) || (!rv && b);
                    break;
                case SELECT:
                    if (newSet == null) {
                        newSet = new LinkedHashSet ();
                    }
                    if (b) { // add element to set
                        newSet.add (elem);
                    }
                    break;
                default:
                    Tool.error ("Unimplemented quantified expression type?? " + cExpr, null, stitchProblemHandler ());
                    break;
            }
        }
        // set predicate skip flag on the expression to be evaluated
        expr.skipQuanPredicate = true;
        // store result
        if (type == Strategy.ExpressionKind.SELECT) {
            if (newSet == null) { // create an empty set
                newSet = new LinkedHashSet ();
            }
            // store the chosen set
            cExpr.setResult (newSet);
        } else { // store the boolean result
            cExpr.setResult (rv);
        }
    }

    @Override
    public void endQuantifiedExpression (Strategy.ExpressionKind quant, StitchParser.QuantifiedExpressionContext
            quantifiedExpressionContext) {
        if (checkSkipEval ()) return;

        Expression cExpr = doEndComplexExpr ();
        expr ().setResult (cExpr.getResult ());
    }

    @Override
    public void beginMethodCallExpression () {
        if (checkSkipEval ()) return;

        doBeginComplexExpr ();
    }

    @Override
    public void endMethodCallExpression (TerminalNode mc, StitchParser.MethodCallContext id) {
        if (checkSkipEval ()) return;

        Expression cExpr = doEndComplexExpr ();
        Object[] args = new Object[cExpr.expressions ().size ()];
        int i = 0;
        for (Expression e : cExpr.expressions ()) {
            if (e.tree () != null && e.getResult () == null) {
                e.evaluate (null, m_walker);
            }
            args[i++] = e.getResult ();
        }
        Object rv = executeMethod (id.IDENTIFIER ().getText (), args);
//        cExpr.setResult (rv);
        if (rv instanceof Integer) {
            MyInteger result = new MyInteger ((Integer) rv);
            cExpr.setResult(result);
			expr ().setResult (result);
        } else if (rv instanceof Long) {
            MyInteger result = new MyInteger ((Long) rv);
            cExpr.setResult(result);
			expr ().setResult (result);
        } else if (rv instanceof Float) {
            MyDouble result = new MyDouble (((Float) rv).doubleValue ());
            cExpr.setResult(result);
			expr ().setResult (result);
        } else if (rv instanceof Double) {
            MyDouble result = new MyDouble ((Double) rv);
            cExpr.setResult(result);
            expr ().setResult (result);
        } else {
        	cExpr.setResult(rv);
            expr ().setResult (rv);
        }
    }

    @Override
    public void beginSetExpression () {
        if (checkSkipEval ()) return;

        doBeginComplexExpr ();
    }

    @Override
    public void endSetExpression (StitchParser.SetExpressionContext setAST) {
        if (checkSkipEval ()) return;

        Expression cExpr = doEndComplexExpr ();
        Set set = new LinkedHashSet ();
        // iterate through elements and make sure they are the same types
        // complain if NOT, or form the proper set if yes
        Var v = new Var (); // declare a Var object to be able to use typeMatches
        for (Expression e : cExpr.expressions ()) {
            if (e.getResult () == null) {
                if (v.getValue () != null) {
                    // consider this a type mismatch?
                    Tool.error ("Unexpected null value in a SET!", setAST, stitchProblemHandler ());
                    return;
                }
            } else if (v.getValue () != null && !Tool.typeMatches (v, e.getResult ())) {
                // explicit type mismatch
                // TODO: support subtypes?
                Tool.warn ("Type mismatch between elements in set: " + e.getResult ().getClass () + " vs "
                                   + v.getValue ().getClass (), setAST, stitchProblemHandler ());
            } else {
                // store type if type not already set
                if (v.getValue () == null) {
                    v.setValue (e.getResult ());
                    v.setType (e.getResult ().getClass ().getName ());
                }
                set.add (e.getResult ());
            }
        }
        cExpr.setResult (set);
    }

    @Override
    public void doExpression (ParserRuleContext exprAST) {
        if (checkSkipEval ()) return;

        if (scope () instanceof Expression) {
            Expression expr = (Expression) scope ();
            if (expr.getResult () == null && expr.expressions ().size () > 0) {
                // transfer children result up
                Expression sub = expr.expressions ().get (0);
                expr.setResult (sub.getResult ());
            }
        }
    }

    @Override
    public void doAssignExpression (ParserRuleContext identifier, ParserRuleContext expression) {
        // Note: Assignment will only be meaningful as its own statement, so
        // should not be evaluated as expression elsewhere!

        if (checkSkipEval ()) return;
        Expression expr = expr ();

        // pop the lOp and rOp for good measures
        expr.lrOps[LOP].pop ();
        Object rVal = expr.lrOps[ROP].pop ();
        // the lVal can only be an identifier, otherwise, won't make sense!
        Object lObj = expr.lookup (identifier.getText ());
        if (lObj != null && lObj instanceof Var) {
            Var v = (Var) lObj;
            if (Tool.typeMatches (v, rVal)) {
                v.setValue (rVal);
                expr.setResult (rVal);
            } else {
                Tool.error ("Assignment expression cannot be evaluated due to mismatched value types! " + v.getType ()
                                    + " vs. " + rVal.getClass ().getName (), expression, stitchProblemHandler ());
                return;
            }
        } else {
            // can't find lvalue reference
            Tool.error ("Assignment expression cannot be evaluated because lvalue reference cannot be found! "
                                + expression.getText (), expression, stitchProblemHandler ());
            return;
        }
    }

    @Override
    public void doLogicalExpression (Strategy.ExpressionKind opAST, ParserRuleContext ctx) {
        if (checkSkipEval ()) return;
        Expression expr = expr ();

        Boolean lOp = null;
        Boolean rOp = null;
        if (expr.lrOps[LOP].isEmpty () || expr.lrOps[LOP].peek () == null || expr.lrOps[ROP].isEmpty () || expr
                .lrOps[ROP].peek ()
                ==
                null) {
            // if either is NULL, result is NULL
            final String msg = "One logical operand is NULL: "
                    + (expr.lrOps[LOP] == null || expr.lrOps[LOP].isEmpty () ? "NULL" : expr.lrOps[LOP]
                    .pop ()) + ", "
                    + (expr.lrOps[ROP] == null || expr.lrOps[ROP].isEmpty () ? "NULL" : expr.lrOps[ROP]
                    .pop ());
            Tool.warn (msg, ctx,
                       stitchProblemHandler ());
//            System.out.println (msg);
            expr.setResult (null);
            return;
        }

        // deal with IAcmeProperty operands
        if (expr.lrOps[LOP].peek () instanceof IAcmeProperty) {
            IAcmeProperty aProp = (IAcmeProperty) expr.lrOps[LOP].pop ();
            if (aProp.getValue () instanceof IAcmeBooleanValue) {
                expr.lrOps[LOP].push (((IAcmeBooleanValue) aProp.getValue ()).getValue ());
            } else {
                Tool.error ("IAcmeProperty does NOT hold the expected Boolean value! " + aProp + " " + ctx.getText ()
                                    + " " + expr.lrOps[ROP].peek (), ctx, stitchProblemHandler ());
                return;
            }
        }
        if (expr.lrOps[ROP].peek () instanceof IAcmeProperty) {
            IAcmeProperty aProp = (IAcmeProperty) expr.lrOps[ROP].pop ();
            if (aProp.getValue () instanceof IAcmeBooleanValue) {
                expr.lrOps[ROP].push (((IAcmeBooleanValue) aProp.getValue ()).getValue ());
            } else {
                Tool.error ("IAcmeProperty does NOT hold the expected Boolean value! " + expr.lrOps[LOP].peek () + " "
                                    + ctx.getText () + " " + aProp, ctx, stitchProblemHandler ());
                return;
            }
        }
        if (expr.lrOps[LOP].peek () instanceof Boolean && expr.lrOps[ROP].peek () instanceof Boolean) {
            lOp = (Boolean) expr.lrOps[LOP].pop ();
            rOp = (Boolean) expr.lrOps[ROP].pop ();
        } else {
            Tool.error (
                    "Type mismatch or NOT booleans in logical expression! " + expr.lrOps[LOP].peek () + " "
                            + ctx.getText () + " " + expr.lrOps[ROP].peek (), ctx, stitchProblemHandler ());
            return;
        }

        switch (opAST) {
            case IMPLIES:
                expr.setResult (!lOp || rOp);
                break;
            case IFF:
                expr.setResult (lOp == rOp);
                break;
            case OR:
                expr.setResult (lOp || rOp);
                break;
            case AND:
                expr.setResult (lOp && rOp);
                break;
            default:
                debug ("Don't know what logical op to do... :'(");
                break;
        }
    }

    @Override
    public void doRelationalExpression (Strategy.ExpressionKind kind, ParserRuleContext opAST) {
        if (checkSkipEval ()) return;
        Expression expr = expr ();

        boolean compareAsNum = false;
        MyNumber lOp = null;
        MyNumber rOp = null;
        Object lObj = null;
        Object rObj = null;
        // deal with null operands
        if (expr.lrOps[LOP].peek () == null || expr.lrOps[ROP].peek () == null) {
            // if either is NULL, result is NULL
            final String msg = "One relational operand is NULL: " + expr.lrOps[LOP].pop () + ", " + expr.lrOps[ROP]
                    .pop ()
                    + " ... " + opAST.toStringTree ();
            Tool.warn (msg, opAST, stitchProblemHandler ());
//            System.out.println (msg);
            expr.setResult (null);
            return;
        }
        // deal with IAcmeProperty operands
        if (expr.lrOps[LOP].peek () instanceof IAcmeProperty) {

            IAcmeProperty prop = (IAcmeProperty) expr.lrOps[LOP].pop ();
            Object val = ModelHelper.propertyValueToJava (prop.getValue ());
            if (val instanceof Float) {
                expr.lrOps[LOP].push (new MyDouble ((double) ((Float) val)));
            } else if (val instanceof Double) {
                expr.lrOps[LOP].push (new MyDouble ((Double) val));
            } else if (val instanceof Integer) {
                expr.lrOps[LOP].push (new MyInteger ((Integer) val));
            } else {
                expr.lrOps[LOP].push (val);
            }
        }
        if (expr.lrOps[ROP].peek () instanceof IAcmeProperty) {

            IAcmeProperty prop = (IAcmeProperty) expr.lrOps[ROP].pop ();
            Object val = ModelHelper.propertyValueToJava (prop.getValue ());
            if (val instanceof Float) {
                expr.lrOps[ROP].push (new MyDouble ((double) ((Float) val)));
            } else if (val instanceof Double) {
                expr.lrOps[ROP].push (new MyDouble ((Double) val));

            } else if (val instanceof Integer) {
                expr.lrOps[ROP].push (new MyInteger (((Integer) val)));

            } else {
                expr.lrOps[ROP].push (val);
            }
        }
        if (expr.lrOps[LOP].peek () instanceof MyNumber && expr.lrOps[ROP].peek () instanceof MyNumber) {
            // this means all ops is checkable
            compareAsNum = true;
            lOp = (MyNumber) expr.lrOps[LOP].pop ();
            rOp = (MyNumber) expr.lrOps[ROP].pop ();
        } else if (expr.lrOps[LOP].peek ().getClass ().equals (expr.lrOps[ROP].peek ().getClass ())) {
            // only EQ and NE will be checked
            lObj = expr.lrOps[LOP].pop ();
            rObj = expr.lrOps[ROP].pop ();
        } else { // type check problem!
            Tool.error ("Type mismatch in relational expression! " + expr.lrOps[LOP].peek () + " " + opAST.getText ()
                                + " " + expr.lrOps[ROP].peek (), opAST, stitchProblemHandler ());
            return;
        }

        Boolean b = null;
        switch (kind) {
            case EQ:
                if (compareAsNum) {
                    b = lOp != null ? lOp.eq (rOp) : rOp == null;
                    expr.setResult (b);
                } else {
                    b = lObj != null ? lObj.equals (rObj) : rObj == null;
                    expr.setResult (b);
                }
                debug ("Compare:  " + lOp + " EQ " + rOp + " -> " + b);
                break;
            case NE:
                if (compareAsNum) {
                    b = lOp != null ? lOp.ne (rOp) : rOp != null;
                    expr.setResult (b);
                } else {
                    b = lObj != null ? !lObj.equals (rObj) : rObj != null;
                    expr.setResult (b);
                }
                debug ("Compare:  " + lOp + " NE " + rOp + " -> " + b);
                break;
            case LE:
                if (compareAsNum) {
                    if (lOp != null) {
                        b = lOp.le (rOp);
                        expr.setResult (b);
                    } else {
//                        System.out.println ("StitchScriptEvaluator:doRelationalExpression:670");
                        expr.setResult (null);
                    }
                    debug ("Compare:  " + lOp + " LE " + rOp + " -> " + b);
                } else {
                    Tool.error ("LE is not defined for " + lObj + " & " + rObj, opAST, stitchProblemHandler ());
                }
                break;
            case LT:
                if (compareAsNum) {
                    if (lOp != null) {
                        b = lOp.lt (rOp);
                        expr.setResult (b);
                    } else {
//                        System.out.println ("StitchScriptEvaluator:doRelationalExpression:684");
                        expr.setResult (null);
                    }
                    debug ("Compare:  " + lOp + " LT " + rOp + " -> " + b);
                } else {
                    Tool.error ("LT is not defined for " + lObj + " & " + rObj, opAST, stitchProblemHandler ());
                }
                break;
            case GT:
                if (compareAsNum) {
                    if (lOp != null) {
                        b = lOp.gt (rOp);
                        expr.setResult (b);
                    } else {
//                        System.out.println ("StitchScriptEvaluator:doRelationalExpression:698");
                        expr.setResult (null);
                    }
                    debug ("Compare:  " + lOp + " GT " + rOp + " -> " + b);
                } else {
                    Tool.error ("GT is not defined for " + lObj + " & " + rObj, opAST, stitchProblemHandler ());
                }
                break;
            case GE:
                if (compareAsNum) {
                    if (lOp != null) {
                        b = lOp.ge (rOp);
                        expr.setResult (b);
                    } else {
//                        System.out.println ("StitchScriptEvaluator:doRelationalExpression:712");
                        expr.setResult (null);
                    }
                    debug ("Compare:  " + lOp + " GE " + rOp + " -> " + b);
                } else {
                    Tool.error ("GE is not defined for " + lObj + " & " + rObj, opAST, stitchProblemHandler ());
                }
                break;
            default:
                debug ("Don't know what relational op to do... :'(");
                break;
        }
    }

    @Override
    public void doArithmeticExpression (Strategy.ExpressionKind kind, ParserRuleContext opAST) {
        if (checkSkipEval ()) return;
        Expression expr = expr ();

        // support String concatenation
        if (expr.lrOps[LOP].peek () instanceof String && kind == Strategy.ExpressionKind.PLUS) {
            String s1 = (String) expr.lrOps[LOP].pop ();
            Object s2 = expr.lrOps[ROP].pop ();
            expr.setResult (s1 + s2.toString ());
            return;
        }

        MyNumber lOp = null;
        MyNumber rOp = null;
        if (expr.lrOps[LOP].peek () == null || expr.lrOps[ROP].peek () == null) {
            // if either is NULL, result is NULL
            final String msg = "One arithmetic operand is NULL: " + expr.lrOps[LOP].pop () + ", " + expr.lrOps[ROP]
                    .pop ()
                    + " ... " + opAST.toStringTree ();
            Tool.warn (msg, opAST, stitchProblemHandler ());
//            System.out.println (msg);
            expr.setResult (null);
            return;
        }

        // deal with IAcmeProperty operands
        if (expr.lrOps[LOP].peek () instanceof IAcmeProperty) {
            expr.lrOps[LOP].push (MyNumber.newNumber ((IAcmeProperty) expr.lrOps[LOP].pop ()));
        }
        if (expr.lrOps[ROP].peek () instanceof IAcmeProperty) {
            expr.lrOps[ROP].push (MyNumber.newNumber ((IAcmeProperty) expr.lrOps[ROP].pop ()));
        }
        if (expr.lrOps[LOP].peek () instanceof MyNumber && expr.lrOps[ROP].peek () instanceof MyNumber) {
            lOp = (MyNumber) expr.lrOps[LOP].pop ();
            rOp = (MyNumber) expr.lrOps[ROP].pop ();
        } else { // type check problem!
            Tool.error (
                    "Types not Numbers in arithmetic expression! " + expr.lrOps[LOP].peek () + " " + opAST.getText ()
                            + " " + expr.lrOps[ROP].peek (), opAST, stitchProblemHandler ());
            return;
        }

        MyNumber mynum = null;
        switch (kind) {
            case PLUS:
                mynum = lOp.plus (rOp);
                expr.setResult (mynum);
                debug ("Add:  " + lOp + " + " + rOp + " -> " + mynum.toJavaNumber ());
                break;
            case MINUS:
                mynum = lOp.minus (rOp);
                expr.setResult (mynum);
                debug ("Subtract:  " + lOp + " - " + rOp + " -> " + mynum.toJavaNumber ());
                break;
            case MULTIPLY:
                mynum = lOp.times (rOp);
                expr.setResult (mynum);
                debug ("Multiply:  " + lOp + " * " + rOp + " -> " + mynum.toJavaNumber ());
                break;
            case DIVIDE:
                mynum = lOp.dividedBy (rOp);
                expr.setResult (mynum);
                debug ("Divide:  " + lOp + " / " + rOp + " -> " + mynum.toJavaNumber ());
                break;
            case MOD:
                mynum = lOp.modulus (rOp);
                expr.setResult (mynum);
                debug ("Mod:  " + lOp + " % " + rOp + " -> " + mynum.toJavaNumber ());
                break;
            default:
                debug ("Don't know what arithmetic op to do... :'(");
                break;
        }
    }

    @Override
    public void doUnaryExpression (Strategy.ExpressionKind kind, StitchParser.UnaryExpressionContext opAST) {
        if (checkSkipEval ()) return;
        Expression expr = expr ();

        // assume operand is number!
        Boolean bOp = null;
        MyNumber op = null;
        if (expr.lrOps[LOP].peek () == null) {
            // if operand NULL, result NULL
            Tool.warn ("Unary operand is NULL: " + expr.lrOps[LOP].pop (), opAST, stitchProblemHandler ());
            expr.setResult (null);
            return;
        }
        // deal with IAcmeProperty operand
        if (expr.lrOps[LOP].peek () instanceof IAcmeProperty) {

            IAcmeProperty pop = (IAcmeProperty) expr.lrOps[LOP].peek ();
            if (pop.getValue () instanceof IAcmeIntValue || pop.getValue () instanceof IAcmeFloatingPointValue) {
                MyNumber newNum = MyNumber.newNumber (pop);
                if (newNum != null) {
                    expr.lrOps[LOP].pop ();
                    expr.lrOps[LOP].push (newNum);
                }
            }
        }
        if (expr.lrOps[LOP].peek () instanceof MyNumber) {
            op = (MyNumber) expr.lrOps[LOP].pop ();
        } else if (kind == Strategy.ExpressionKind.NOT) {
            if (expr.lrOps[LOP].peek () instanceof Boolean) {
                bOp = (Boolean) expr.lrOps[LOP].pop ();
            } else if (expr.lrOps[LOP].peek () instanceof IAcmeProperty
                    && ((IAcmeProperty) expr.lrOps[LOP].peek ()).getValue () instanceof IAcmeBooleanValue) {
                bOp = ((IAcmeBooleanValue) ((IAcmeProperty) expr.lrOps[LOP].pop ()).getValue ()).getValue ();
            } else {
                Tool.error ("Type of logical not operand NOT Boolean! " + expr.lrOps[LOP].peek (), opAST,
                            stitchProblemHandler ());
                return;
            }
        } else { // type check problem!
            Tool.error ("Type of unary operand NOT Number! " + opAST.getText () + " " + expr.lrOps[LOP].peek (), opAST,
                        stitchProblemHandler ());
            return;
        }

        switch (kind) {
            case INCR:
                expr.setResult (op != null ? op.incr () : null);
                break;
            case DECR:
                expr.setResult (op != null ? op.decr () : null);
                break;
            case NOT:
                expr.setResult (bOp != null ? !bOp : null);
                break;
            case UNARY_MINUS:
                expr.setResult (op != null ? op.negate () : null);
                break;
            case UNARY_PLUS:
                expr.setResult (op);
                break;
            default:
                debug ("Don't know what unary op to do... :'(");
                break;
        }
    }

    @Override
    public void doPostIdentifierExpression (StitchParser.PostIdExpressionContext identifier) {
        if (checkSkipEval ()) return;
        String iden = "__post__" + identifier.IDENTIFIER ().getText ();
        Expression expr = expr ();
        Object o = scope ().lookup (iden);
        if (o instanceof PostVar) {
            PostVar v = (PostVar) o;
//            if (v.getValue () == null) {
            // Value of the postVar should always be recomputed because
            // the effect (value) may take time to achieve. So storing it
            // might store an old value
            v.computeAndSetValue ();
//            }
            expr.setResult (((Var) o).getValue ());
        } else {
            expr.setResult (null);
            stitchProblemHandler ().setProblem (generateErrorFromToken (identifier.IDENTIFIER ().getSymbol (),
                                                                        "Variable " + identifier.getText () +
                                                                                " not defined."));
        }
    }

    @Override
    public void doIdentifierExpression (ParserRuleContext idAST, Strategy.ExpressionKind kind) {
        if (checkSkipEval ()) return;
        Expression expr = expr ();

        if (kind == Strategy.ExpressionKind.IDENTIFIER) {
            // find identifier, lookup entire string first
            String iden = idAST.getText ();
            Object o = null;
            if (scope ().lookup ("__path_variable") != null) {
                Object pv = scope ().lookup ("__path_variable");
                if (pv instanceof Var) {
                    pv = ((Var) pv).getValue ();
                }
                if (pv instanceof IAcmeElement) {
                    o = ((IAcmeElement) pv).lookupName (iden);
                } else if (pv instanceof IAcmeRecordValue) {
                    o = ((IAcmeRecordValue) pv).getField (iden);
                    if (o instanceof IAcmePropertyValue) {
                        o = ModelHelper.propertyValueToJava ((IAcmePropertyValue) o);
                    }
                }
            } else if (scope ().lookup ("__path_filter_type") != null) {
                Var pv = (Var) scope ().lookup ("__path_filter_type");
                if (pv.typeObj != null) {
                    Object o1 = pv.typeObj.lookupName (iden);
                    if (o1 == null) {
                        Tool.error ("Unresolved reference '" + iden + "'! Perhaps model not accessible?", idAST,
                                    stitchProblemHandler ());
                    }
                    return;
                }

            }
            if (o == null)
                o = scope ().lookup (iden);
            if (o == null) { // break up dot notation
                int dotIdx = iden.indexOf (".");
                if (dotIdx > -1) { // looking for v.something
                    o = scope ().lookup (iden.substring (0, dotIdx));
                    if (o != null && o instanceof Var) {
                        Var v = (Var) o;
                        // find idx sub within object's scope
                        o = v.scope.lookup (iden.substring (dotIdx + 1));
                        if (o == null) {
                            // treat var as model element and access rest as its
                            // attribute
                            String dotVal = iden.substring (dotIdx);
                            Object val = v.getValue ();
                            if (val instanceof IAcmeElement) {
                                IAcmeElement elem = (IAcmeElement) val;
                                o = scope ().lookup (elem.getQualifiedName () + dotVal);
                                if (o == null) {
                                    // this may mean an invalid reference to
                                    // element attribute
                                    Tool.error ("Invalid reference '" + iden + "' encountered!", idAST,
                                                stitchProblemHandler ());
                                }
                            } else {
                                o = scope ().lookup (v.name + dotVal);
                            }
                            // if (o != null && o instanceof IAcmeProperty) {
                            // get the Acme Property value
                            // o = Tool.deriveValue(((IAcmeProperty
                            // )o).getValue());
                            // }
                        }
                    }
                }
            }
            if (o == null) {
                // lookup of various combo failed, could indicate invalid
                // reference
                Tool.error ("Unresolved reference '" + iden + "'! Perhaps model not accessible?", idAST,
                            stitchProblemHandler ());
            } else {
                if (o instanceof Var) {
                    Var v = (Var) o;
                    if (v.getValue () == null || v.isFunction ()) {
                        v.computeValue ();
                    }
                    expr.setResult (((Var) o).getValue ());
                } else if (o instanceof IAcmeProperty) {
                    Object rv = ModelHelper.propertyValueToJava (((IAcmeProperty) o).getValue ());
                    if (rv instanceof Integer) {
                        expr.setResult (new MyInteger ((Integer) rv));
                    } else if (rv instanceof Long) {
                        expr.setResult (new MyInteger ((Long) rv));
                    } else if (rv instanceof Float) {
                        expr.setResult (new MyDouble (((Float) rv).doubleValue ()));
                    } else if (rv instanceof Double) {
                        expr.setResult (new MyDouble ((Double) rv));
                    } else {
                        expr.setResult (rv);
                    }
                } else { // store the object directly
                    expr.setResult (o);
                }
            }
        } else if (kind == Strategy.ExpressionKind.INTEGER) {
            expr.setResult (new MyInteger (Integer.parseInt (idAST.getText ())));
        } else if (kind == Strategy.ExpressionKind.FLOAT) {
            expr.setResult (new MyDouble (Double.parseDouble (idAST.getText ())));
        } else if (kind == Strategy.ExpressionKind.STRING) {
            // strip the double quotes of string literal
            String s = idAST.getText ();
            expr.setResult (s.substring (1, s.length () - 1));
        } else if (kind == Strategy.ExpressionKind.CHAR) {
            // strip the single quotes, so char is at index 1
            expr.setResult (new Character (idAST.getText ().charAt (1)));
        } else if (kind == Strategy.ExpressionKind.BOOLEAN) {
//            System.out.println ("Evaluating boolean for " + idAST.getText ());
            expr.setResult (new Boolean (Boolean.parseBoolean (idAST.getText ())));
        } else if (kind == Strategy.ExpressionKind.NULL) {
//            System.out.println ("StitchScriptEvaluator:doArithmeticExpression setting expr result to null");
            expr.setResult (null);
        }
    }

    protected boolean checkSkipEval () {
        boolean rv = false;
        if (expr () != null) {
            rv = expr ().skipQuanPredicate;
        }

        return rv;
    }

    private void doBeginComplexExpr () {
        // get the next expression in list, checking to see if it's complex.
        // if it is, we're in a straight complex expression;
        // if not, check current expr to see if it is complex, in which case,
        // we're within a complex expression already...
        Expression expr = expr ();
        if (expr == null && scope () instanceof Expression)
            expr = (Expression) scope ();
        Expression cExpr = null;
        if (expr.expressions ().size () > 0) {
            Expression nextExpr = expr.expressions ().get (expr.curExprIdx++);
            if (nextExpr.isComplex ()) {
                cExpr = nextExpr;
            } else if (expr.isComplex ()) {
                cExpr = expr;
            }
        } else {
            if (expr.isComplex ()) {
                // this SHOULD be the case, by nature of tree walk
                cExpr = expr;
            }
        }

        if (cExpr != null) {
            // reset expr count for descending into element exprs
            cExpr.curExprIdx = 0;
            setExpression (null);
            pushScope (cExpr);
        }
    }

    private Expression doEndComplexExpr () {
        Expression cExpr = (Expression) scope ();
        setExpression ((Expression) cExpr.parent ());
        popScope ();
        return cExpr;
    }

    /**
     * Executes a method assuming Java... can be refactored and extended later to support methods in other languages.
     *
     * @param name the Method name to evaluate
     * @param args the arguments to the method
     */
    @SuppressWarnings("unchecked")
    // suppress error on Class.isAssignableFrom()
    private Object executeMethod (String name, Object[] args) {
        String origName = name;
        Object rv = null;
        Method method = null;
        int dotIdx = name.indexOf (".");
        Object nameObj = null;
        if (dotIdx > -1) {
            Object n = scope ().lookup (name.substring (0, dotIdx));
            if (n instanceof IAcmeElement) {
                // This branch should no longer happen
                nameObj = ((IAcmeElement) n).lookupName (name.substring (dotIdx + 1), true);
            } else if (n instanceof AcmeModelInstance) {

                AcmeModelInstance ami = (AcmeModelInstance) n;
                IAdaptationExecutor<Object> executor = Rainbow.instance ().getRainbowMaster ()
                        .strategyExecutor (Util.genModelRef (ami.getModelName (), ami.getModelType ()));
                nameObj = ami.getModelInstance ().lookupName (name.substring (dotIdx + 1), true);
                if (nameObj == null) {
                    // Look for the <name>Cmd method in the Command Factory
                    Class<?> commandFactoryClass = ami.getCommandFactory ().getClass ();
                    Method[] methods = commandFactoryClass.getMethods ();
                    String m = name.substring (dotIdx + 1);
                    if (!m.endsWith ("Cmd")) {
                        m += "Cmd";
                    }
                    for (int i = 0; i < methods.length && nameObj == null; i++) {
                        if (methods[i].getName ().equals (m)) {
                            nameObj = methods[i];
                        }
                    }
                    if (nameObj != null) {
                        Method mtd = (Method) nameObj;
                        Class[] params = constructFormalParams (args);
                        if (checkMethodParams (mtd, params, args)) {
                            method = mtd;
                        }
                        if (method != null) {
                            try {
                                rv = method.invoke (ami.getCommandFactory (), args);
                            } catch (Throwable e) {
                                scope ().setError (true);
                                Tool.error ("Method invocation failed! " + method.toString (), e, null,
                                            stitchProblemHandler ());
                                e.printStackTrace ();
                                IModelDSBusPublisherPort.OperationResult or = new IModelDSBusPublisherPort
                                        .OperationResult ();
                                or.result = IModelDSBusPublisherPort.Result.FAILURE;
                                or.reply = "Method invocation failed! + " + method.toString () + ". Reason: "
                                        + e.getMessage ();
                                rv = or;

                            }
                            if (rv instanceof IRainbowOperation) {
                                // Submit this to the effector manager to call the right effectors
                                IRainbowOperation op = (IRainbowOperation) rv;

                                IModelDSBusPublisherPort port = executor.getOperationPublishingPort ();
                                ((AbstractRainbowRunnable) executor).reportingPort ().info (RainbowComponentT.EXECUTOR,
                                                                                            "SSE: Attempting " +
                                                                                                    "operation: "
                                                                                                    + op.toString ());
                                IModelDSBusPublisherPort.OperationResult result = port
                                        .publishOperation (op);
                                ((AbstractRainbowRunnable) executor).reportingPort ().info (RainbowComponentT
                                                                                                    .EXECUTOR,
                                                                                            "SSE: Finished operation " +
                                                                                                    op.toString () +
                                                                                                    " = " + result
                                                                                                    .result + "(" +
                                                                                                    result.reply + ")");
                                rv = result;
                            }
                        }
                        // check if OperatorResult, and if result is a failure
                        if (rv != null) {
                            if (rv instanceof String) {
                                ModelOperator.OperatorResult opResult = ModelOperator.OperatorResult
                                        .parseEffectorResult ((String) rv);
                                switch (opResult) {
                                    case UNKNOWN:
                                        ((AbstractRainbowRunnable) executor).reportingPort ().info (
                                                RainbowComponentT.EXECUTOR,
                                                "No effector found corresponding to method '" + name + "'!");
                                        break;
                                    case FAILURE:
                                        // bad, set state to indicate failure occurred
                                        scope ().setError (true);
                                        ((AbstractRainbowRunnable) executor).reportingPort ().info (
                                                RainbowComponentT.EXECUTOR, "Method invocation did not succeeed! " +
                                                        name);
                                        break;
                                }
                            } else if (rv instanceof IModelDSBusPublisherPort.OperationResult) {
                                IModelDSBusPublisherPort.OperationResult or = (IModelDSBusPublisherPort
                                        .OperationResult) rv;
                                switch (or.result) {
                                    case UNKNOWN:
                                        ((AbstractRainbowRunnable) executor).reportingPort ().info (
                                                RainbowComponentT.EXECUTOR, "No effector found: " + or.reply);
                                        break;
                                    case FAILURE:
                                        scope ().setError (true);
                                        ((AbstractRainbowRunnable) executor).reportingPort ().info (
                                                RainbowComponentT.EXECUTOR, "Failed to execute operation " + name + "" +
                                                        ". Reason: " + or.reply);
                                        break;
                                }

                            }
                        }
                    }
                }
            }
        } else {
            nameObj = scope ().lookup (name);
        }
        if (nameObj instanceof IAcmeDesignAnalysisDeclaration) {
            nameObj = ((IAcmeDesignAnalysisDeclaration) nameObj).getDesignAnalysis ();
        }
        if (nameObj instanceof IAcmeDesignAnalysis) {
            IAcmeDesignAnalysis da = (IAcmeDesignAnalysis) nameObj;
            rv = executeDesignAnalysis (name, args, da);

        } else if (rv == null) {
            // break method call name into class and method parts
            dotIdx = name.lastIndexOf (".");
            String methodClass = null;
            if (dotIdx > -1) {
                methodClass = name.substring (0, dotIdx);
                // mangle any method class renaming
                if (script ().renames.containsKey (methodClass)) { // replace
                    methodClass = script ().renames.get (methodClass);
                }
                name = name.substring (dotIdx + 1);
            }
            int i;
            Class[] params = constructFormalParams (args);

            // construct list of classes in which to search for method name,
            // look in imports
            List<Class> classesToSearch = new ArrayList<Class> ();
            for (Class opClass : m_stitch./*stitch ().*/script.ops) {
                // first, see if method class matches the imported method's
                // class
                if (methodClass != null) {
                    if (!opClass.getName ().endsWith (methodClass)) {
                        // not a match, don't waste time searching its methods
                        continue;
                    }
                }
                // add to list to search
                classesToSearch.add (opClass);
            }
            if (classesToSearch.size () == 0 && methodClass != null) {
                // attempt to load the method class and search it
                try {
                    classesToSearch.add (Class.forName (methodClass));
                } catch (ClassNotFoundException e) {
                    if (Tool.logger ().isInfoEnabled ()) {
                        Tool.logger ()
                                .info ("Attempt to load class " + methodClass + " failed while executing method "
                                               + name + "!", e);
                    }
                }
            }

            // find this name reference in reduced list of classes
            OUTER:
            for (Class opClass : classesToSearch) {
                // iterate thru list of declared methods for whose name match,
                // and look to see if supplied param is a proper subtype
                for (Method m : opClass.getDeclaredMethods ()) {
                    if (m.getName ().equals (name)) { // method name matches,
                        // check params
                        boolean allParamClassOk = checkMethodParams (m, params, args);
                        if (allParamClassOk) { // found a method!!
                            if (Modifier.isStatic (m.getModifiers ())) {
                                method = m;
                                break OUTER;
                            } else {
                                Tool.error ("Applicable method for " + name + " is NOT STATIC; invocation will fail!",
                                            null, stitchProblemHandler ());
                            }
                        }
                    }
                }
            }
            // check if we should continue to actually invoke method
            if (!m_stitch./*stitch ().*/isCanceled ()) {
                if (method == null) { // cannot execute anything
                    // try method in ModelOperator using name and first argument
                    if (args.length > 0) {
                        rv = Ohana.instance ().modelOperator ().invoke (name, args);
                    }
                    if (rv == null) {
                        Tool.error ("No applicable method found for " + name, null, stitchProblemHandler ());
                    }
                } else {
                    try {
                        rv = method.invoke (null, args);
                    } catch (Throwable e) {
                        scope ().setError (true);
                        Tool.error ("Method invocation failed! " + method.toString (), e, null, stitchProblemHandler
                                ());
                        e.printStackTrace ();
                        IModelDSBusPublisherPort.OperationResult or = new IModelDSBusPublisherPort.OperationResult ();
                        or.result = IModelDSBusPublisherPort.Result.FAILURE;
                        or.reply = "Method invocation failed! + " + method.toString () + ". Reason: " + e.getMessage ();
                        rv = or;
                    }
                }
                // check if OperatorResult, and if result is a failure
                if (rv != null && rv instanceof String) {
                    ModelOperator.OperatorResult opResult = ModelOperator.OperatorResult
                            .parseEffectorResult ((String) rv);
                    switch (opResult) {
                        case UNKNOWN:
                            Tool.error ("No effector found corresponding to method '" + name + "'!", null,
                                        stitchProblemHandler ());
                            break;
                        case FAILURE:
                            // bad, set state to indicate failure occurred
                            scope ().setError (true);
                            Tool.error ("Method invocation did not succeeed! " + name,
                                        null,
                                        stitchProblemHandler
                                                ());
                            break;
                    }
                }
            }
        }
        return rv;
    }

    boolean checkMethodParams (Method m, Class[] params, Object[] args) {
        int i;
        boolean allParamClassOk = true;
        i = 0; // track param index for positional comparison
        for (Class c : m.getParameterTypes ()) {
            boolean matchPrimitive = true;
            if (c.isAssignableFrom (params[i])) { // good!
            } else if (c.isPrimitive ()) {
                matchPrimitive = c.equals (int.class) && Integer.class.equals (params[i]) || c.equals (short.class)
                        && Short.class.equals (params[i]) || c.equals (long.class) && Long.class.equals (params[i])
                        || c.equals (float.class) && Float.class.equals (params[i]) || c.equals (double.class)
                        && Double.class.equals (params[i]) || c.equals (boolean.class)
                        && Boolean.class.equals (params[i]) || c.equals (byte.class) && Byte.class.equals (params[i])
                        || c.equals (char.class) && Character.class.equals (params[i]);
            }
            if (!matchPrimitive && args[i] != null && args[i] instanceof IAcmeProperty) {
                // check if class would match if we extract the
                // IAcmeProperty
                Object vObj = Tool.deriveValue (((IAcmeProperty) args[i]).getValue ());
                if (vObj != null) {
                    Class vClass = vObj.getClass ();
                    if (c.isInstance (vObj)) {
                        matchPrimitive = true; // good!
                        // change param class and arg
                        args[i] = vObj;
                        params[i] = vObj.getClass ();
                    } else if ((c.equals (int.class) || c.equals (long.class)) && vClass.equals (MyInteger.class)) {
                        matchPrimitive = true; // good!
                        args[i] = ((MyInteger) vObj).intValue ();
                        params[i] = int.class;
                    } else if ((c.equals (float.class) || c.equals (double.class)) && vClass.equals (MyDouble.class)) {
                        matchPrimitive = true; // good!
                        args[i] = ((MyDouble) vObj).doubleValue ();
                        params[i] = double.class;
                    }
                }
            }
            allParamClassOk &= matchPrimitive;
            ++i;
        }

        return allParamClassOk;
    }

    Class[] constructFormalParams (Object[] args) {
        // construct the method's formal parameter list
        Class[] params = new Class[args.length];
        // convert all MyNumber to java Number object first
        for (int i = 0; i < args.length; ++i) {
            if (args[i] != null && args[i] instanceof MyNumber) {
                args[i] = ((MyNumber) args[i]).toJavaNumber ();
            } else if (args[i] != null && args[i] instanceof IAcmeProperty) {
                args[i] = ModelHelper.propertyValueToJava (((IAcmeProperty) args[i]).getValue ());
            }
        }
        // populate formal parameter list based on args list
        int i = 0;
        for (Object o : args) {
            if (o != null) {
                params[i++] = o.getClass ();
            }
        }
        return params;
    }

    private Object executeDesignAnalysis (String name, Object[] args, IAcmeDesignAnalysis da) {
        Object rv = null;
        int dotIdx = name.indexOf (".");
        IAcmeElement context = null;
        if (dotIdx > -1) {
            Object o = scope ().lookup (name.substring (0, dotIdx));
            if (o instanceof IAcmeElement) {
                context = (IAcmeElement) o;
            } else if (o instanceof AcmeModelInstance) {
                context = ((AcmeModelInstance) o).getModelInstance ();
            }
        }

        List<FormalParameterNode> formalParams = da.getFormalParameters ();
        if (args.length != formalParams.size ()) {
            Tool.error ("Call to Acme design analysis does not have the right number of parameters", null,
                        stitchProblemHandler ());
            // TODO: BRS this should really throw some sort of evaluation
            // exception.
            return rv;
        }
        NodeScopeLookup lookup = new NodeScopeLookup ();
        List<Object> argList = new LinkedList<Object> ();
        for (int i = 0; i < formalParams.size (); i++) {
            Object arg = args[i];
            String formalParamName = formalParams.get (i).getParameterName ();
            // IAcmeType formalParamType = resolveType(context,
            // formalParams.get(i).getTypeReference());
            if (arg == null) {
                lookup.put (formalParamName, null);
                argList.add (null);
            } else if (arg instanceof MyNumber) {
                MyNumber num = (MyNumber) arg;
                String val = num.toString ();
                try {
                    IAcmePropertyValue propVal = StandaloneLanguagePackHelper.defaultLanguageHelper ()
                            .propertyValueFromString (val, null);
                    lookup.put (formalParamName, propVal);
                    argList.add (propVal);

                } catch (Exception e) {
                    Tool.error ("Could not evaluate '" + num.toString () + "' as an Acme Property to pass to " + name,
                                null, stitchProblemHandler ());
                    lookup.put (formalParamName, null);
                    argList.add (null);
                }
            } else if (arg instanceof String) {
                UMStringValue string = new UMStringValue ((String) arg);
                lookup.put (formalParamName, string);
                argList.add (string);
            } else if (arg instanceof Boolean) {
                UMBooleanValue bool = new UMBooleanValue ((Boolean) arg);
                lookup.put (formalParamName, bool);
                argList.add (bool);
            }
            // else if (arg instanceof Set) {
            // lookup.put(formalParamName, new UMSetV)
            // }
            else if (arg instanceof IAcmeProperty) {
                IAcmePropertyValue val = ((IAcmeProperty) arg).getValue ();
                if (val instanceof IAcmeSetValue) {
                    AcmeSet s = new AcmeSet ();
                    s.setValues (((IAcmeSetValue) val).getValues ());
                    lookup.put (formalParamName, s);
                    argList.add (s);
                } else {
                    lookup.put (formalParamName, val);
                    argList.add (val);
                }
            } else if (arg instanceof IAcmePropertyValue) {
                IAcmePropertyValue val = (IAcmePropertyValue) arg;
                lookup.put (formalParamName, val);
                argList.add (val);
            } else if (arg instanceof IAcmeElement) {
                lookup.put (formalParamName, arg);
                argList.add (arg);
            } else if (arg instanceof IModelInstance) {
                IModelInstance inst = (IModelInstance) arg;
                lookup.put (formalParamName, inst.getModelInstance ());
                argList.add (inst.getModelInstance ());

            } else {
                argList.add (arg);
            }
        }

        Stack<AcmeError> errors = new Stack<AcmeError> ();
        try {
            Object result = null;
            if (da.getAnalysisType () == IAcmeDesignAnalysis.DesignAnalysisType.EXTERNAL) {
                if (context == null) {
                    Tool.error (
                            MessageFormat.format ("Could not find the object ''{0}'' to do the call",
                                                  name.substring (0, dotIdx)), null, stitchProblemHandler ());
                    return rv;
                }
                IExternalAnalysisExpressionNode node = context.getContext ().getEnvironment ()
                        .getExternalAnalysis (da.getExternalAnalysisKey ());
                result = node.evaluate (da.getResultTypeReference ().getType (), argList, errors);
            } else {
                IAcmeType returnType = resolveType (context, da.getResultTypeReference ());
                result = RuleTypeChecker
                        .evaluateAsType (context, returnType, null, da.getExpression (), errors, lookup);
            }
            if (result instanceof IAcmePropertyValue) {
                rv = ModelHelper.propertyValueToJava ((IAcmePropertyValue) result);
            } else {
                rv = result;
            }

        } catch (AcmeException e) {
            Tool.error (name + " failed to evaluate due to the following exception: " + e.getMessage (), null,
                        stitchProblemHandler ());
        }
        return rv;
    }

    private IAcmeType resolveType (IAcmeElement context, TypeReferenceNode tr) {
        IAcmeType returnType = DefaultAcmeModel.defaultAnyType ();
        if (tr != null) {
            if (tr.getType () != null) {
                returnType = tr.getType ();
            } else if (context != null) {
                returnType = ((IAcmeType) context.lookupName (tr.getReference ().asQualifiedReference (), true));
            }

        }
        return returnType;
    }

    @Override
    public void beginPathExpression () {
        if (checkSkipEval ()) return;
        doBeginComplexExpr ();
    }

    final ThreadLocal<Var>     pathVariable = new ThreadLocal<Var> () {
        @Override
        protected Var initialValue () {
            return null;
        }
    };
    final ThreadLocal<Integer> exprIndex    = new ThreadLocal<Integer> () {
        @Override
        protected Integer initialValue () {
            return null;
        }
    };

    @Override
    public void setupPathFilter (TerminalNode identifier) {
        if (identifier != null) {
            Var v = new Var ();
            v.name = "__path_filter_type";
            v.setType (identifier.toString ());
            scope ().addVar (v.name, v);
            v.scope = scope ();
            v.computeClass ();
        }
    }

    @Override
    public void pathExpressionFilter (TypeFilterT filter, TerminalNode identifier, StitchParser.ExpressionContext
            expression) {
        if (checkSkipEval ()) return;

        Expression cExpr = (Expression) scope ();
        if (cExpr.getKind () != Expression.Kind.PATH) {
            Tool.error ("Error! Expected path expression not found!", null, stitchProblemHandler ());
            return;
        }

        Object result = cExpr.expressions ().get (0).getResult ();
        Set set = Collections.EMPTY_SET;

        if (result instanceof Set) {
            set = new HashSet ((Set) result);
        } else if (result instanceof IAcmeSetValue) {
            set = (Set) ModelHelper.propertyValueToJava ((IAcmeSetValue) result);
        } else if (result instanceof AcmeSet) {
            set = new HashSet (((AcmeSet) result).getValues ());
        } else if (result instanceof IAcmeProperty && ((IAcmeProperty) result).getValue () instanceof IAcmeSetValue) {
            set = (Set) ModelHelper.propertyValueToJava (((IAcmeProperty) result).getValue ());
        } else {
            Tool.error ("Error! Path set is NULL!" + cExpr.toStringTree (), null, stitchProblemHandler ());
            set = Collections.EMPTY_SET;
        }


        pathVariable.set (new Var ());
        pathVariable.get ().name = "___Path";
        pathVariable.get ().scope = scope ();
        if (identifier != null) {
            pathVariable.get ().setType (identifier.getText ());
            pathVariable.get ().computeClass ();
            Set subset = new LinkedHashSet ();
            if (set.size () > 0) {
                for (Object o : set) {
                    if (Tool.typeMatches (pathVariable.get (), o) && Tool.isArchEnabled (o)) {
                        // type matches AND this object in quantified set is arch
                        // enabled
                        subset.add (o);
                    }
                }
            }
            pathVariable.get ().setValue (subset);


        } else {
            pathVariable.get ().setType ("set");
            pathVariable.get ().computeClass ();
            pathVariable.get ().setValue (set);
        }

        cExpr.setResult (pathVariable.get ().getValue ());


        if (expression != null) {
            exprIndex.set (1);
            Expression expr = cExpr.expressions ().get (exprIndex.get ());
            Iterator setIterator = ((Set) pathVariable.get ().getValue ()).iterator ();
            Var __path_element = new Var ();
            __path_element.name = "__path_variable";
            scope ().addVar (__path_element.name, __path_element);
            while (setIterator.hasNext ()) {
                Object o = setIterator.next ();
                __path_element.setValue (o);
                expr.evaluate (null, m_walker);
                boolean b = true;
                if (expr.getResult () instanceof Boolean) {
                    b = (Boolean) expr.getResult ();
                } else if (expr.getResult () instanceof IAcmeProperty) {
                    IAcmeProperty prop = (IAcmeProperty) expr.getResult
                            ();
                    if (prop.getValue () instanceof IAcmeBooleanValue) {
                        b = ((IAcmeBooleanValue) prop.getValue ()).getValue ();
                    }
                }
                if (!b) {
                    setIterator.remove ();
                }
            }
            scope ().vars ().remove (__path_element.name);
        }
    }

    @Override
    public void continueExpressionFilter (TypeFilterT filter, TerminalNode setIdentifier, TerminalNode
            typeIdentifier, StitchParser.ExpressionContext
                                                  expression) {
        if (pathVariable.get () == null) {
            Tool.error ("Path is not definde!", null, stitchProblemHandler ());
            return;
        }
        Expression cExpr = (Expression) scope ();

        Set set = (Set) pathVariable.get ().getValue ();
        Set resultSet = new HashSet ();
        for (Object s : set) {
            if (s instanceof IAcmeElement) {
                final Object result = ((IAcmeElement) s).lookupName (setIdentifier.getText ());
                Set tSet = null;
                if (result instanceof Set) {
                    tSet = new HashSet ((Set) result);
                } else if (result instanceof IAcmeSetValue) {
                    tSet = (Set) ModelHelper.propertyValueToJava ((IAcmeSetValue) result);
                } else if (result instanceof AcmeSet) {
                    tSet = new HashSet (((AcmeSet) result).getValues ());
                } else if (result instanceof IAcmeProperty && ((IAcmeProperty) result).getValue () instanceof
                        IAcmeSetValue) {
                    tSet = (Set) ModelHelper.propertyValueToJava (((IAcmeProperty) result).getValue ());
                } else {
                    Tool.error ("Error! Path set is NULL!" + setIdentifier.getText (), null, stitchProblemHandler ());
                    tSet = Collections.EMPTY_SET;
                }
                resultSet.addAll (tSet);
            }
        }
        if (typeIdentifier != null) {
            pathVariable.get ().setType (typeIdentifier.toString ());
            pathVariable.get ().typeObj = null;
            pathVariable.get ().computeClass ();
            Iterator i = resultSet.iterator ();
            while (i.hasNext ()) {
                Object o = i.next ();
                if (!Tool.typeMatches (pathVariable.get (), o) || !Tool.isArchEnabled (o)) {
                    // type matches AND this object in quantified set is arch
                    // enabled
                    i.remove ();
                }
            }
        }
        if (expression != null) {
            pathVariable.get ().setValue (resultSet);
            Iterator setIterator = resultSet.iterator ();
            Var __path_element = new Var ();
            __path_element.name = "__path_variable";
            scope ().addVar (__path_element.name, __path_element);
            exprIndex.set (exprIndex.get () + 1);
            Expression expr = cExpr.expressions ().get (exprIndex.get ());

            while (setIterator.hasNext ()) {
                Object o = setIterator.next ();
                __path_element.setValue (o);
                expr.evaluate (null, m_walker);
                boolean b = true;
                if (expr.getResult () instanceof Boolean) {
                    b = (Boolean) expr.getResult ();
                } else if (expr.getResult () instanceof IAcmeProperty) {
                    IAcmeProperty prop = (IAcmeProperty) expr.getResult
                            ();
                    if (prop.getValue () instanceof IAcmeBooleanValue) {
                        b = ((IAcmeBooleanValue) prop.getValue ()).getValue ();
                    }
                }
                if (!b) {
                    setIterator.remove ();
                }
            }
            scope ().vars ().remove (__path_element.name);
        }
        pathVariable.get ().setValue (resultSet);
        cExpr.setResult (resultSet);
    }

    @Override
    public void endPathExpression (StitchParser.PathExpressionContext ctx) {
        if (checkSkipEval ()) return;
        pathVariable.set (null);
        Expression cExpr = doEndComplexExpr ();
        expr ().setResult (cExpr.getResult ());
        scope ().vars ().remove ("__path_filter_type");
    }
}
