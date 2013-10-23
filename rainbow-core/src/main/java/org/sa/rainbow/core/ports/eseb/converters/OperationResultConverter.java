package org.sa.rainbow.core.ports.eseb.converters;

import incubator.pval.Ensure;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.sa.rainbow.core.ports.IModelDSBusPublisherPort.OperationResult;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort.Result;

import edu.cmu.cs.able.typelib.jconv.TypelibJavaConversionRule;
import edu.cmu.cs.able.typelib.jconv.TypelibJavaConverter;
import edu.cmu.cs.able.typelib.jconv.ValueConversionException;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.scope.AmbiguousNameException;
import edu.cmu.cs.able.typelib.struct.Field;
import edu.cmu.cs.able.typelib.struct.StructureDataType;
import edu.cmu.cs.able.typelib.struct.StructureDataValue;
import edu.cmu.cs.able.typelib.struct.UnknownFieldException;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataValue;

public class OperationResultConverter implements TypelibJavaConversionRule {

    private PrimitiveScope m_scope;

    public OperationResultConverter (PrimitiveScope scope) {
        m_scope = scope;
    }

    @Override
    public boolean handles_java (Object value, DataType dst) {
        Ensure.not_null (value);
        if (value instanceof OperationResult) return dst == null || "operation_result".equals (dst.name ());
        return false;
    }

    @Override
    public boolean handles_typelib (DataValue value, Class<?> cls) {
        Ensure.not_null (value);
        if ("operation_result".equals (value.type ().name ()))
            return cls == null || OperationResult.class.isAssignableFrom (cls);
        return false;
    }

    @Override
    public DataValue from_java (Object value, DataType dst, TypelibJavaConverter converter)
            throws ValueConversionException {
        if ((dst == null || dst instanceof StructureDataType) && value instanceof OperationResult) {
            try {
                OperationResult or = (OperationResult )value;
                StructureDataType sdt = (StructureDataType )dst;
                if (sdt == null) {
                    sdt = (StructureDataType )m_scope.find ("operation_result");
                }
                Field result = sdt.field ("result");
                Field reply = sdt.field ("reply");
                Map<Field, DataValue> fields = new HashMap<> ();
                fields.put (result, m_scope.string ().make (or.result.name ()));
                fields.put (reply, m_scope.string ().make (or.reply == null ? "" : or.reply));
                StructureDataValue sdv = sdt.make (fields);
                return sdv;
            }
            catch (UnknownFieldException | AmbiguousNameException e) {
                throw new ValueConversionException (MessageFormat.format ("Could not convert from {0} to {1}", value
                        .getClass ().toString (), (dst == null ? "operation_result" : dst.absolute_hname ())), e);
            }

        }
        throw new ValueConversionException (MessageFormat.format ("Could not convert from {0} to {1}", value
                .getClass ().toString (), (dst == null ? "operation_result" : dst.absolute_hname ())));
    }

    @Override
    public <T> T to_java (DataValue value, Class<T> cls, TypelibJavaConverter converter)
            throws ValueConversionException {
        if (value instanceof StructureDataValue) {
            try {
                StructureDataValue sdv = (StructureDataValue )value;
                StructureDataType sdt = (StructureDataType )value.type ();
                String reply = converter.<String> to_java (sdv.value (sdt.field ("reply")), String.class);
                String result = converter.<String> to_java (sdv.value (sdt.field ("result")), String.class);
                OperationResult r = new OperationResult ();
                r.reply = reply;
                r.result = Result.valueOf (result);
                T o = (T )r;
                return o;
            }
            catch (Exception e) {
                throw new ValueConversionException (MessageFormat.format ("Could not convert from {0} to {1}",
                        value.toString (), (cls == null ? "OperationResult" : cls.getCanonicalName ())), e);
            }
        }
        throw new ValueConversionException (MessageFormat.format ("Could not convert from {0} to {1}",
                value.toString (), (cls == null ? "OperationResult" : cls.getCanonicalName ())));
    }

}
