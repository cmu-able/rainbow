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
package org.sa.rainbow.stitch.visitor;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.acmestudio.acme.element.IAcmeElement;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.stitch.Ohana;
import org.sa.rainbow.stitch.core.Expression;
import org.sa.rainbow.stitch.core.Var;
import org.sa.rainbow.stitch.util.Tool;

import antlr.collections.AST;

public class StitchTypechecker extends LiloScopeEstablisher implements ILiloBehavior {

    public Set<String> m_modelOperationsReferenced = new HashSet<> ();

    public StitchTypechecker (Stitch stitch) {
        super (stitch);
    }

    @Override
    public void doIdentifierExpression (AST idAST, Expression.Kind kind) {
        super.doIdentifierExpression (idAST, kind);
        Expression expr = expr ();
        if (kind == Expression.Kind.IDENTIFIER) {
            String iden = idAST.getText ();
            Object o = scope ().lookup (iden);
            if (o == null) {
                int dotIdx = iden.indexOf (".");
                if (dotIdx > -1) {
                    o = scope ().lookup (iden.substring (0, dotIdx));
                    if (o != null && o instanceof Var) {
                        Var v = (Var )o;
                        o = v.scope.lookup (iden.substring (dotIdx + 1));
                        if (o == null) {
                            String dotVal = iden.substring (dotIdx);
                            Object val = v.getValue ();
                            if (val instanceof IAcmeElement) {
                                IAcmeElement elem = (IAcmeElement )val;
                                o = scope ().lookup (elem.getQualifiedName () + dotVal);

                            }
                            else if (v.typeObj != null) {
                                o = v.typeObj.lookupName (iden.substring (dotIdx + 1), false);
                            }
                            else {
                                o = scope ().lookup (v.name + dotVal);
                            }
                        }
                    }
                    else if (o instanceof IAcmeElement) {
                        IAcmeElement elem = (IAcmeElement )o;
                        o = elem.lookupName (iden.substring (dotIdx + 1), false);

                    }
                }
            }
            if (o == null) {
                Tool.error (new File (m_stitch.path).getName () + ": " + idAST.getText () + " is undefined", idAST,
                        stitchProblemHandler ());

            }
        }
    }

    @Override
    public void endMethodCallExpression (AST mcAST, AST idAST) {
        super.endMethodCallExpression (mcAST, idAST);
//		Expression cExpr = (Expression )scope ();
//				
//		Object [] args = new Object [((Expression )cExpr.getChildren().iterator ().next ()).expressions().size ()];
//		int i = 0;
//		for (Expression e : cExpr.expressions ()) {
//			e.evaluate (null);
//		}

        String name = idAST.getText ();
        int dotIdx = name.indexOf (".");
        Object nameObj = null;
        if (dotIdx > -1) {
            Object n = scope ().lookup (name.substring (0, dotIdx));
            if (n instanceof IAcmeElement) {
                nameObj = ((IAcmeElement )n).lookupName (name.substring (dotIdx + 1), true);
            }
            else if (n instanceof AcmeModelInstance) {
                Class<?> commandFactoryClass = ((AcmeModelInstance )n).getCommandFactory ().getClass ();
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
                    nameObj = ((AcmeModelInstance )n).getModelInstance ()
                            .lookupName (name.substring (dotIdx + 1), true);
                }
            }
        }
        else {
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
            for (Class opClass : m_stitch.script.ops) {
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
                }
                catch (ClassNotFoundException e) {
                    if (Tool.logger ().isInfoEnabled ()) {
                        Tool.logger ()
                        .info ("Attempt to load class " + methodClass + " failed while executing method "
                                + name + "!", e);
                    }
                }
            }
            Method method = null;
            // find this name reference in reduced list of classes
            OUTER: for (Class opClass : classesToSearch) {
                // iterate thru list of declared methods for whose name match,
                // and look to see if supplied param is a proper subtype
                for (Method m : opClass.getDeclaredMethods ()) {
                    if (m.getName ().equals (name)) { // method name matches, check params
                        if (Modifier.isStatic (m.getModifiers ())) {
                            method = m;
                            break OUTER;
                        }
                        else {
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
                if (Ohana.instance ().modelOperator ().lookupOperator (name) == null) {
                    Tool.warn (
                            MessageFormat
                            .format (
                                    "{0} : Could not find method {1} in any scopes, and could not verify that it is an operator.",
                                    new File (m_stitch.path).getName (), idAST.getText ()), idAST,
                                    stitchProblemHandler ());
                }
            }
        }

    }

    @Override
    public void endSetExpression (AST setAST) {
        super.endSetExpression (setAST);
//		Expression cExpr =  (Expression )scope ();
//		setExpression ((Expression )cExpr.parent ());
//		popScope ();
//		
//		Var v = new Var ();
//		for (Expression e : cExpr.expressions()) {
//			
//		}
    }

}
