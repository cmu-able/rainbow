/**
 * Created March 16, 2006
 */
package org.sa.rainbow.stitch.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.acmestudio.acme.ModelHelper;
import org.acmestudio.acme.core.type.IAcmeBooleanValue;
import org.acmestudio.acme.core.type.IAcmeEnumValue;
import org.acmestudio.acme.core.type.IAcmeFloatValue;
import org.acmestudio.acme.core.type.IAcmeIntValue;
import org.acmestudio.acme.core.type.IAcmeRecordField;
import org.acmestudio.acme.core.type.IAcmeRecordValue;
import org.acmestudio.acme.core.type.IAcmeSequenceValue;
import org.acmestudio.acme.core.type.IAcmeSetValue;
import org.acmestudio.acme.core.type.IAcmeStringValue;
import org.acmestudio.acme.element.IAcmeElement;
import org.acmestudio.acme.element.IAcmeElementInstance;
import org.acmestudio.acme.element.IAcmeElementType;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.element.property.IAcmePropertyValue;
import org.acmestudio.acme.model.util.core.UMBooleanValue;
import org.acmestudio.acme.model.util.core.UMFloatValue;
import org.acmestudio.acme.model.util.core.UMIntValue;
import org.acmestudio.acme.model.util.core.UMRecordField;
import org.acmestudio.acme.model.util.core.UMRecordValue;
import org.acmestudio.acme.model.util.core.UMSequenceValue;
import org.acmestudio.acme.model.util.core.UMSetValue;
import org.acmestudio.acme.model.util.core.UMStringValue;
import org.apache.log4j.Logger;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.stitch.Ohana;
import org.sa.rainbow.stitch.core.Expression;
import org.sa.rainbow.stitch.core.MyDouble;
import org.sa.rainbow.stitch.core.MyInteger;
import org.sa.rainbow.stitch.core.Var;
import org.sa.rainbow.stitch.error.StitchProblem;
import org.sa.rainbow.stitch.error.StitchProblemHandler;

import antlr.RecognitionException;
import antlr.collections.AST;

/**
 * Utility tool class for various functions, like debug.
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 * @author (Modified by) Ali Almossawi <aalossaw@cs.cmu.edu> (July 5, 2006)
 * Added methods that report errors and warnings to the Problems View.  The original
 * methods were kept in tact to avoid breaking other classes that use them.  When those
 * classes are updated to work with StitchProblemMarker, the original methods may be 
 * commented.
 */
public abstract class Tool {

    private static Logger m_logger = null;

    static {
        m_logger = Logger.getLogger (Ohana.class);
    }

    /**
     * No construction allowed.
     */
    private Tool() {
    }

    public static Logger logger () {
        return m_logger;
    }

    //ALI: ADDED (this sets a problem for the error too)
    public static void warn (String s, AST ast, StitchProblemHandler stitchProblemHandler) {
        m_logger.warn(s);
        stitchProblemHandler.setProblem(new StitchProblem(new RecognitionException(s,"",ast==null?1:ast.getLine (),ast==null?1:ast.getColumn ()), StitchProblem.WARNING));
    }
    public static void warn (String s, Throwable t, AST ast, StitchProblemHandler stitchProblemHandler) {
        m_logger.warn(s, t);
        stitchProblemHandler.setProblem(new StitchProblem(new RecognitionException(s,"",ast==null?1:ast.getLine (),ast==null?1:ast.getColumn ()), StitchProblem.WARNING));
    }

    //ALI: ADDED (this sets a problem for the error too)
    public static void error (String s, AST ast, StitchProblemHandler stitchProblemHandler) {
        m_logger.error(s);
        stitchProblemHandler.setProblem(new StitchProblem(new RecognitionException(s,"",ast==null?1:ast.getLine (),ast==null?1:ast.getColumn ()), StitchProblem.ERROR));	
    }
    public static void error (String s, Throwable t, AST ast, StitchProblemHandler stitchProblemHandler) {
        m_logger.error(s, t);
        stitchProblemHandler.setProblem(new StitchProblem(new RecognitionException(s,"",ast==null?1:ast.getLine (),ast==null?1:ast.getColumn ()), StitchProblem.ERROR));	
    }

    /**
     * Checks whether the type of a variable and supplied Class matches.
     * If the object supplied is an IAcmeElementInstance, then an automatic
     * filter is performed on the isArchEnabled status.  Only arch-enabled
     * element instances are considered.
     * @param v  Variable to check type match
     * @param o  Object against whose class to match type
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
            IAcmeElementInstance<?,?> inst = (IAcmeElementInstance<?,?> )o;
            rv = ModelHelper.declaresType(inst, v.typeObj);
        } else {
            Class c = o.getClass();
            rv = v.computeClass().equals(c)
                    || v.getType().equals("int") && (c.equals(long.class) || c.equals(MyInteger.class))
                    || v.getType().equals("float") && (c.equals(double.class) || c.equals(MyDouble.class))
                    || c.getName().endsWith(v.getType());
            if (! rv) {  // check for assignable, meaning if v is super of o's class
                // TODO: we want to support widening cast later for set member purposes
                rv = v.computeClass().isAssignableFrom(c);
            }
        }
        return rv;
    }

    /**
     * If the object supplied is an IAcmeElementInstance, then checks whether
     * the element has the isArchEnabled status enabled.
     * @param o  Object to check isArchEnabled status
     * @return boolean <code>true</code> if arch status is enabled, <code>false</code> otherwise
     */
    public static boolean isArchEnabled (Object o) {
        boolean rv = true;  // assume enabled, in case no property exists
        if (o instanceof IAcmeElement) {
            // check that the arch instance is an enabled arch element
            IAcmeElementInstance<?,?> inst = (IAcmeElementInstance<?,?> )o;
            IAcmeProperty prop = inst.getProperty (AcmeModelInstance.PROPKEY_ARCH_ENABLED);
            if (prop != null && prop.getValue() instanceof IAcmeBooleanValue) {
                if (! ((IAcmeBooleanValue )prop.getValue()).getValue()) {
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
            val = new MyInteger(((IAcmeIntValue )pVal).getValue());
        } else if (pVal instanceof IAcmeFloatValue) {
            val = new MyDouble(((Float )((IAcmeFloatValue )pVal).getValue()).doubleValue());
        } else if (pVal instanceof IAcmeBooleanValue) {
            val = ((IAcmeBooleanValue )pVal).getValue();
        } else if (pVal instanceof IAcmeStringValue) {
            val = ((IAcmeStringValue )pVal).getValue();
        } else if (pVal instanceof IAcmeEnumValue) {
            val = ((IAcmeEnumValue )pVal).getValue();
        } else if (pVal instanceof IAcmeSetValue) {  // iterate over the set
            IAcmeSetValue valAcmeSet = (IAcmeSetValue )pVal;
            Set<Object> valSet = new HashSet<Object>();
            for (IAcmePropertyValue setVal : valAcmeSet.getValues()) {
                valSet.add(deriveValue(setVal));
            }
            val = valSet;
        } else if (pVal instanceof IAcmeSequenceValue) {
            IAcmeSequenceValue valAcmeSeq = (IAcmeSequenceValue )pVal;
            List<Object> valList = new ArrayList<Object>();
            for (IAcmePropertyValue seqVal : valAcmeSeq.getValues()) {
                valList.add(deriveValue(seqVal));
            }
            val = valList;
        } else if (pVal instanceof IAcmeRecordValue) {
            IAcmeRecordValue valRec = (IAcmeRecordValue )pVal;
            Map<String,Object> valMap = new LinkedHashMap<String,Object>(); 
            for (IAcmeRecordField field : valRec.getFields()) {
                valMap.put(field.getName(), deriveValue(field.getValue()));
            }
            val = valMap;
        }
        return val;
    }

    public static IAcmePropertyValue convertValue (Object val) {
        IAcmePropertyValue pVal = null;
        if (val instanceof MyInteger) {
            pVal = new UMIntValue(((MyInteger )val).intValue());
        } else if (val instanceof MyDouble) {
            pVal = new UMFloatValue(((MyDouble )val).floatValue());
        } else if (val instanceof Boolean) {
            pVal = new UMBooleanValue((Boolean )val);
        } else if (val instanceof String) {
            pVal = new UMStringValue((String )val);
        } else if (val instanceof Set<?>) {
            Set<?> vSet = (Set<?> )val;
            Set<IAcmePropertyValue> pSet = new HashSet<IAcmePropertyValue>(vSet.size());
            for (Object o : vSet) {
                pSet.add(convertValue(o));
            }
            pVal = new UMSetValue(pSet);
        } else if (val instanceof List<?>) {
            List<?> vList = (List<?> )val;
            List<IAcmePropertyValue> pList = new ArrayList<IAcmePropertyValue>(vList.size());
            for (Object o : vList) {
                pList.add(convertValue(o));
            }
            pVal = new UMSequenceValue(pList);
        } else if (val instanceof Map<?,?>) {
            Map<?,?> vMap = (Map<?,?> )val;
            Map<String,IAcmeRecordField> pMap = new LinkedHashMap<String,IAcmeRecordField>(vMap.size());
            for (Map.Entry<?,?> entry : vMap.entrySet()) {
                String recKey = entry.getKey().toString();
                IAcmePropertyValue recVal = convertValue(entry.getValue());
                UMRecordField f = new UMRecordField(recKey, recVal.getType(), recVal);
                pMap.put(recKey, f);
            }
            pVal = new UMRecordValue(pMap);
        }
        return pVal;
    }

    /**
     * Evaluates and returns the list of supplied tactic arguments.
     * @param argList  the supplied list of arguments for some tactic
     * @return  the array of values corresponding to the supplied argument list
     */
    public static Object[] evaluateArgs (List<Expression> argList) {
        Object[] args = new Object[argList.size()];
        int i = 0;
        for (Expression e : argList) {
            args[i++] = e.evaluate(null);
        }
        return args;
    }

}
