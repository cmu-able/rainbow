package org.sa.rainbow.core.ports.eseb.converters;

import incubator.pval.Ensure;

import java.text.MessageFormat;

import org.sa.rainbow.translator.effectors.IEffectorExecutionPort.Outcome;

import edu.cmu.cs.able.typelib.enumeration.EnumerationType;
import edu.cmu.cs.able.typelib.enumeration.EnumerationValue;
import edu.cmu.cs.able.typelib.jconv.TypelibJavaConversionRule;
import edu.cmu.cs.able.typelib.jconv.TypelibJavaConverter;
import edu.cmu.cs.able.typelib.jconv.ValueConversionException;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.scope.AmbiguousNameException;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataValue;

public class OutcomeConverter implements TypelibJavaConversionRule {

    private PrimitiveScope m_scope;

    public OutcomeConverter (PrimitiveScope scope) {
        m_scope = scope;
    }

    @Override
    public boolean handles_java (Object value, DataType dst) {
        Ensure.not_null (value);
        if (value instanceof Outcome) return dst == null || "outcome".equals (dst.name ());
        return false;
    }

    @Override
    public boolean handles_typelib (DataValue value, Class<?> cls) {
        Ensure.not_null (value);
        if ("outcome".equals (value.type ().name ())) return cls == null || Outcome.class.isAssignableFrom (cls);
        return false;
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
            EnumerationType et = (EnumerationType )ev.type ();
            if (cls == null || cls == Outcome.class) {
                cls = (Class<T> )Outcome.class;
            }
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
