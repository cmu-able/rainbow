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
 * Created March 16, 2006
 */
package org.sa.rainbow.stitch.util;

import org.acmestudio.acme.ModelHelper;
import org.acmestudio.acme.core.type.*;
import org.acmestudio.acme.element.IAcmeElement;
import org.acmestudio.acme.element.IAcmeElementInstance;
import org.acmestudio.acme.element.IAcmeElementType;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.element.property.IAcmePropertyValue;
import org.acmestudio.acme.model.util.core.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.log4j.Logger;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.stitch.Ohana;
import org.sa.rainbow.stitch.core.Expression;
import org.sa.rainbow.stitch.core.MyDouble;
import org.sa.rainbow.stitch.core.MyInteger;
import org.sa.rainbow.stitch.core.Var;
import org.sa.rainbow.stitch.error.IStitchProblem;
import org.sa.rainbow.stitch.error.RecognitionException;
import org.sa.rainbow.stitch.error.StitchProblem;
import org.sa.rainbow.stitch.error.StitchProblemHandler;

import java.util.*;

/**
 * Utility tool class for various functions, like debug.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 * @author (Modified by) Ali Almossawi <aalossaw@cs.cmu.edu> (July 5, 2006)
 *         Added methods that report errors and warnings to the Problems View.  The original
 *         methods were kept in tact to avoid breaking other classes that use them.  When those
 *         classes are updated to work with StitchProblemMarker, the original methods may be
 *         commented.
 */
public abstract class Tool {

    private static Logger m_logger = null;

    static {
        m_logger = Logger.getLogger (Ohana.class);
    }

    /**
     * No construction allowed.
     */
    private Tool () {
    }

    public static Logger logger () {
        return m_logger;
    }

    //ALI: ADDED (this sets a problem for the error too)
    public static void warn (String s, ParserRuleContext ast, StitchProblemHandler stitchProblemHandler) {
        m_logger.warn (s);
        stitchProblemHandler.setProblem (new StitchProblem (new RecognitionException (s, "", ast == null ? 1 : ast
                .getStart
                        ().getLine
                        (), ast == null ? 1 : ast.getStart ().getCharPositionInLine ()), IStitchProblem.WARNING));
    }

    public static void warn (String s, Throwable t, ParserRuleContext ast, StitchProblemHandler stitchProblemHandler) {
        m_logger.warn (s, t);
        stitchProblemHandler.setProblem (new StitchProblem (new RecognitionException (s, "", ast == null ? 1 : ast
                .getStart ()
                .getLine (),
                                                                                      ast == null ? 1 : ast.getStart
                                                                                              ()
                                                                                              .getCharPositionInLine
                                                                                                      ()),
                                                            IStitchProblem.WARNING));
    }

    //ALI: ADDED (this sets a problem for the error too)
    public static void error (String s, ParserRuleContext ast, StitchProblemHandler stitchProblemHandler) {
        m_logger.error (s);
        stitchProblemHandler.setProblem (new StitchProblem (new RecognitionException (s, "", ast == null ? 1 : ast
                .getStart ().getLine (), ast == null ? 1 : ast.getStart ().getCharPositionInLine ()), IStitchProblem
                .ERROR));
    }

    public static void error (String s, Throwable t, ParserRuleContext ast, StitchProblemHandler stitchProblemHandler) {
        m_logger.error (s, t);
        stitchProblemHandler.setProblem (new StitchProblem (new RecognitionException (s, "", ast == null ? 1 : ast
                .getStart ().getLine (), ast == null ? 1 : ast.getStart ().getCharPositionInLine ()), IStitchProblem
                .ERROR));
    }

    /**
     * Checks whether the type of a variable and supplied Class matches.
     * If the object supplied is an IAcmeElementInstance, then an automatic
     * filter is performed on the isArchEnabled status.  Only arch-enabled
     * element instances are considered.
     *
     * @param v Variable to check type match
     * @param o Object against whose class to match type
     * @return boolean <code>true</code> if the type matches, <code>false</code> otherwise
     */
    @SuppressWarnings("unchecked")
    public static boolean typeMatches (Var v, Object o) {
        // check string matching of these kinds:
        // - left type object having same class as right
        //   (incl. string & java.lang.String, int & int, float & float)
        // - int and (Java long or MyInteger)
        // - float and (Java double or MyDouble)
        // - right type ending with left type
        boolean rv = false;
        if (v.typeObj != null && v.typeObj instanceof IAcmeElementType && o instanceof IAcmeElement) {
            IAcmeElementInstance<?, ?> inst = (IAcmeElementInstance<?, ?>) o;
            rv = ModelHelper.declaresType (inst, v.typeObj);
        } else {
            Class c = o.getClass ();
            rv = v.computeClass ().equals (c)
                    || v.getType ().equals ("int") && (c.equals (long.class) || c.equals (MyInteger.class))
                    || v.getType ().equals ("float") && (c.equals (double.class) || c.equals (MyDouble.class))
                    || c.getName ().endsWith (v.getType ());
            if (!rv) {  // check for assignable, meaning if v is super of o's class
                // TODO: we want to support widening cast later for set member purposes
                rv = v.computeClass ().isAssignableFrom (c);
            }
        }
        return rv;
    }

    /**
     * If the object supplied is an IAcmeElementInstance, then checks whether
     * the element has the isArchEnabled status enabled.
     *
     * @param o Object to check isArchEnabled status
     * @return boolean <code>true</code> if arch status is enabled, <code>false</code> otherwise
     */
    public static boolean isArchEnabled (Object o) {
        boolean rv = true;  // assume enabled, in case no property exists
        if (o instanceof IAcmeElement) {
            // check that the arch instance is an enabled arch element
            IAcmeElementInstance<?, ?> inst = (IAcmeElementInstance<?, ?>) o;
            IAcmeProperty prop = inst.getProperty (AcmeModelInstance.PROPKEY_ARCH_ENABLED);
            if (prop != null && prop.getValue () instanceof IAcmeBooleanValue) {
                if (!((IAcmeBooleanValue) prop.getValue ()).getValue ()) {
                    // nope, arch NOT enabled
                    rv = false;
                }
            }
        }
        return rv;
    }

    public static Object deriveValue (IAcmePropertyValue pVal) {
        Object val = null;
        if (pVal instanceof IAcmeIntValue) {
            val = new MyInteger (((IAcmeIntValue) pVal).getValue ());
        } else if (pVal instanceof IAcmeFloatingPointValue) {
            val = new MyDouble (((IAcmeFloatingPointValue) pVal).getDoubleValue ());
        } else if (pVal instanceof IAcmeBooleanValue) {
            val = ((IAcmeBooleanValue) pVal).getValue ();
        } else if (pVal instanceof IAcmeStringValue) {
            val = ((IAcmeStringValue) pVal).getValue ();
        } else if (pVal instanceof IAcmeEnumValue) {
            val = ((IAcmeEnumValue) pVal).getValue ();
        } else if (pVal instanceof IAcmeSetValue) {  // iterate over the set
            IAcmeSetValue valAcmeSet = (IAcmeSetValue) pVal;
            Set<Object> valSet = new HashSet<Object> ();
            for (IAcmePropertyValue setVal : valAcmeSet.getValues ()) {
                valSet.add (deriveValue (setVal));
            }
            val = valSet;
        } else if (pVal instanceof IAcmeSequenceValue) {
            IAcmeSequenceValue valAcmeSeq = (IAcmeSequenceValue) pVal;
            List<Object> valList = new ArrayList<Object> ();
            for (IAcmePropertyValue seqVal : valAcmeSeq.getValues ()) {
                valList.add (deriveValue (seqVal));
            }
            val = valList;
        } else if (pVal instanceof IAcmeRecordValue) {
            IAcmeRecordValue valRec = (IAcmeRecordValue) pVal;
            Map<String, Object> valMap = new LinkedHashMap<String, Object> ();
            for (IAcmeRecordField field : valRec.getFields ()) {
                valMap.put (field.getName (), deriveValue (field.getValue ()));
            }
            val = valMap;
        }
        return val;
    }

    public static IAcmePropertyValue convertValue (Object val) {
        IAcmePropertyValue pVal = null;
        if (val instanceof MyInteger) {
            pVal = new UMIntValue (((MyInteger) val).intValue ());
        } else if (val instanceof MyDouble) {
            pVal = new UMFloatingPointValue (((MyDouble) val).floatValue ());
        } else if (val instanceof Boolean) {
            pVal = new UMBooleanValue ((Boolean) val);
        } else if (val instanceof String) {
            pVal = new UMStringValue ((String) val);
        } else if (val instanceof Set<?>) {
            Set<?> vSet = (Set<?>) val;
            Set<IAcmePropertyValue> pSet = new HashSet<IAcmePropertyValue> (vSet.size ());
            for (Object o : vSet) {
                pSet.add (convertValue (o));
            }
            pVal = new UMSetValue (pSet);
        } else if (val instanceof List<?>) {
            List<?> vList = (List<?>) val;
            List<IAcmePropertyValue> pList = new ArrayList<IAcmePropertyValue> (vList.size ());
            for (Object o : vList) {
                pList.add (convertValue (o));
            }
            pVal = new UMSequenceValue (pList);
        } else if (val instanceof Map<?, ?>) {
            Map<?, ?> vMap = (Map<?, ?>) val;
            Map<String, IAcmeRecordField> pMap = new LinkedHashMap<String, IAcmeRecordField> (vMap.size ());
            for (Map.Entry<?, ?> entry : vMap.entrySet ()) {
                String recKey = entry.getKey ().toString ();
                IAcmePropertyValue recVal = convertValue (entry.getValue ());
                UMRecordField f = new UMRecordField (recKey, recVal.getType (), recVal);
                pMap.put (recKey, f);
            }
            pVal = new UMRecordValue (pMap);
        }
        return pVal;
    }

    /**
     * Evaluates and returns the list of supplied tactic arguments.
     *
     * @param argList the supplied list of arguments for some tactic
     * @return the array of values corresponding to the supplied argument list
     */
    public static Object[] evaluateArgs (List<Expression> argList) {
        Object[] args = new Object[argList.size ()];
        int i = 0;
        for (Expression e : argList) {
            args[i++] = e.evaluate (null);
        }
        return args;
    }

}
