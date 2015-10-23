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
package org.sa.rainbow.core.ports.eseb.converters;

import edu.cmu.cs.able.typelib.enumeration.EnumerationType;
import edu.cmu.cs.able.typelib.enumeration.EnumerationValue;
import edu.cmu.cs.able.typelib.jconv.TypelibJavaConversionRule;
import edu.cmu.cs.able.typelib.jconv.TypelibJavaConverter;
import edu.cmu.cs.able.typelib.jconv.ValueConversionException;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.scope.AmbiguousNameException;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataValue;
import incubator.pval.Ensure;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort.Outcome;

import java.text.MessageFormat;

public class OutcomeConverter implements TypelibJavaConversionRule {

    private final PrimitiveScope m_scope;

    public OutcomeConverter (PrimitiveScope scope) {
        m_scope = scope;
    }

    @Override
    public boolean handles_java (Object value, DataType dst) {
        Ensure.not_null (value);
        return value instanceof Outcome && (dst == null || "outcome".equals (dst.name ()));
    }

    @Override
    public boolean handles_typelib (DataValue value, Class<?> cls) {
        Ensure.not_null (value);
        return "outcome".equals (value.type ().name ()) && (cls == null || Outcome.class.isAssignableFrom (cls));
    }

    @Override
    public DataValue from_java (Object value, DataType dst, TypelibJavaConverter converter)
            throws ValueConversionException {
        if ((dst == null || dst instanceof EnumerationType) && value instanceof Outcome) {
            try {
                EnumerationType type = (EnumerationType )dst;
                if (type == null) {
                    type = (EnumerationType )m_scope.find ("outcome");
                }
                Outcome outcome = (Outcome )value;
                switch (outcome) {
                case CONFOUNDED:
                    return type.value ("confounded");
                case FAILURE:
                    return type.value ("failure");
                case SUCCESS:
                    return type.value ("success");
                case TIMEOUT:
                    return type.value ("timeout");
                default:
                    return type.value ("unknown");
                }
            }
            catch (AmbiguousNameException e) {
                throw new ValueConversionException (
                        MessageFormat.format ("Cannot convert from {0} to {1}", value.getClass ().getCanonicalName (),
                                (dst == null ? "outcome" : dst.absolute_hname ().toString ())), e);
            }
        }
        throw new ValueConversionException (MessageFormat.format ("Cannot convert from {0} to {1}", value.getClass ()
                .getCanonicalName (), (dst == null ? "outcome" : dst.absolute_hname ().toString ())));
    }


    @Override
    public <T> T to_java (DataValue value, Class<T> cls, TypelibJavaConverter converter)
            throws ValueConversionException {
        if (value instanceof EnumerationValue) {
            EnumerationValue ev = (EnumerationValue )value;
            switch (ev.name ()) {
            case "confounded":
                return (T )Outcome.CONFOUNDED;
            case "failure":
                return (T )Outcome.FAILURE;
            case "success":
                return (T )Outcome.SUCCESS;
            case "timeout":
                return (T )Outcome.TIMEOUT;
            default:
                return (T )Outcome.TIMEOUT;
            }
        }
        throw new ValueConversionException (MessageFormat.format ("Could not convert from {0} to {1}",
                value.toString (), cls == null ? "Outcome" : cls.getCanonicalName ()));
    }

}
