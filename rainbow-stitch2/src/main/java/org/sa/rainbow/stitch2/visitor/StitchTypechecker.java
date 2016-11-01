package org.sa.rainbow.stitch2.visitor;

import org.acmestudio.acme.element.IAcmeElement;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.stitch2.Ohana2;
import org.sa.rainbow.stitch2.core.Expression;
import org.sa.rainbow.stitch2.core.Strategy;
import org.sa.rainbow.stitch2.core.Var;
import org.sa.rainbow.stitch2.parser.StitchParser;
import org.sa.rainbow.stitch2.util.Tool;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by schmerl on 10/3/2016.
 */
public class StitchTypechecker extends BaseStitchBehavior {

    public Set<String> m_modelOperationsReferenced = new HashSet<> ();
    StitchBeginEndVisitor m_walker;

    protected StitchTypechecker (Stitch/*State*/ stitch) {
        super (stitch);
    }

    @Override
    public void setWalker (StitchBeginEndVisitor walker) {
        m_walker = walker;
    }

    @Override
    public void doIdentifierExpression (ParserRuleContext idAST, Strategy.ExpressionKind kind) {
        super.doIdentifierExpression (idAST, kind);
        Expression expr = expr ();
        if (kind == Strategy.ExpressionKind.IDENTIFIER) {
            String iden = idAST.getText ();
            Object o = scope ().lookup (iden);
            if (o == null) {
                int dotIdx = iden.indexOf (".");
                if (dotIdx > -1) {
                    o = scope ().lookup (iden.substring (0, dotIdx));
                    if (o != null && o instanceof Var) {
                        Var v = (Var) o;
                        o = v.scope.lookup (iden.substring (dotIdx + 1));
                        if (o == null) {
                            String dotVal = iden.substring (dotIdx);
                            Object val = v.getValue ();
                            if (val instanceof IAcmeElement) {
                                IAcmeElement elem = (IAcmeElement) val;
                                o = scope ().lookup (elem.getQualifiedName () + dotVal);

                            } else if (v.typeObj != null) {
                                o = v.typeObj.lookupName (iden.substring (dotIdx + 1), false);
                            } else {
                                o = scope ().lookup (v.name + dotVal);
                            }
                        }
                    } else if (o instanceof IAcmeElement) {
                        IAcmeElement elem = (IAcmeElement) o;
                        o = elem.lookupName (iden.substring (dotIdx + 1), false);

                    }
                }
            }
            if (o == null) {
                Tool.error (new File (m_stitch/*.stitch ()*/.path).getName () + ": " + idAST.getText () + " is " +
                                    "undefined",
                            idAST,
                            stitchProblemHandler ());

            }
        }
    }

    @Override
    public void doPostIdentifierExpression (StitchParser.PostIdExpressionContext identifier) {
        if (scope () != null) {
            if (scope ().parent () != null) {
                if (scope ().parent ().vars ().containsKey (identifier.IDENTIFIER ().getText ())) {
                    return;
                }
            }
        }
        Tool.error (identifier.IDENTIFIER ().getText () + " is not defined in the tactic scope, and so " + identifier
                .getText () +
                            " cannot be used in the effect.", identifier, stitchProblemHandler ());
    }

    @Override
    public void endMethodCallExpression (TerminalNode mc, StitchParser.MethodCallContext id) {
//		Expression cExpr = (Expression )scope ();
//
//		Object [] args = new Object [((Expression )cExpr.getChildren().iterator ().next ()).expressions().size ()];
//		int i = 0;
//		for (Expression e : cExpr.expressions ()) {
//			e.evaluate (null);
//		}

        String name = id.getText ();
        int dotIdx = name.indexOf (".");
        Object nameObj = null;
        if (dotIdx > -1) {
            Object n = scope ().lookup (name.substring (0, dotIdx));
            if (n instanceof IAcmeElement) {
                nameObj = ((IAcmeElement) n).lookupName (name.substring (dotIdx + 1), true);
            } else if (n instanceof AcmeModelInstance) {
                Class<?> commandFactoryClass = ((AcmeModelInstance) n).getCommandFactory ().getClass ();
                Method[] methods = commandFactoryClass.getMethods ();
                String m = name.substring (dotIdx + 1);
                if (!m.endsWith ("Cmd")) {
                    m += "Cmd";
                }
                for (int i = 0; i < methods.length && nameObj == null; i++) {
                    if (methods[i].getName ().equals (m)) {
                        nameObj = methods[i];
                        m_modelOperationsReferenced.add (name.substring (dotIdx + 1));
                        break;
                    }
                }

                if (nameObj == null) {
                    nameObj = ((AcmeModelInstance) n).getModelInstance ()
                            .lookupName (name.substring (dotIdx + 1), true);
                }
            }
        } else {
            nameObj = scope ().lookup (name);
        }
        if (nameObj == null) {
            dotIdx = name.lastIndexOf (".");
            String methodClass = null;
            if (dotIdx > -1) {
                methodClass = name.substring (0, dotIdx);
                if (script ().renames.containsKey (methodClass)) {
                    methodClass = script ().renames.get (methodClass);
                }
                name = name.substring (dotIdx + 1);
            }
            List<Class> classesToSearch = new ArrayList<Class> ();
            for (Class opClass : m_stitch/*.stitch()*/.script.ops) {
                // first, see if method class matches the imported method's class
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
            Method method = null;
            // find this name reference in reduced list of classes
            OUTER:
            for (Class opClass : classesToSearch) {
                // iterate thru list of declared methods for whose name match,
                // and look to see if supplied param is a proper subtype
                for (Method m : opClass.getDeclaredMethods ()) {
                    if (m.getName ().equals (name)) { // method name matches, check params
                        if (Modifier.isStatic (m.getModifiers ())) {
                            method = m;
                            break OUTER;
                        } else {
                            Tool.error (MessageFormat.format (
                                    "Applicable method for {0} is NOT STATIC; invocation will fail", name), null,
                                        stitchProblemHandler ());
                            return;
                        }
                    }
                }
            }
            if (method == null) {
                // Need to check if there is an effector
                // Ohana.instance().modelOperator().invoke (name, null);
                if (Ohana2.instance ().modelOperator ().lookupOperator (name) == null) {
                    Tool.warn (
                            MessageFormat
                                    .format (
                                            "{0} : Could not find method {1} in any scopes, and could not verify that" +
                                                    " it is an operator.",
                                            new File (m_stitch/*.stitch ()*/.path).getName (), id.getText ()), id,
                            stitchProblemHandler ());
                }
            }
        }
    }

    @Override
    public void endSetExpression (StitchParser.SetExpressionContext setAST) {
        super.endSetExpression (setAST);
    }
}
