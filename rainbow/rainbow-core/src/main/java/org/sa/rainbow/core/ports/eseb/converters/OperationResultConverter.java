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
import incubator.pval.Ensure;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort.OperationResult;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort.Result;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class OperationResultConverter implements TypelibJavaConversionRule {

    private final PrimitiveScope m_scope;

    public OperationResultConverter (PrimitiveScope scope) {
        m_scope = scope;
    }

    @Override
    public boolean handles_java (Object value, DataType dst) {
        Ensure.not_null (value);
        return value instanceof OperationResult && (dst == null || "operation_result".equals (dst.name ()));
    }

    @Override
    public boolean handles_typelib (DataValue value, Class<?> cls) {
        Ensure.not_null (value);
        return "operation_result".equals (value.type ().name ()) && (cls == null || OperationResult.class.isAssignableFrom (cls));
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
                return sdt.make (fields);
            } catch (UnknownFieldException | AmbiguousNameException e) {
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
                String reply = converter.to_java (sdv.value (sdt.field ("reply")), String.class);
                String result = converter.to_java (sdv.value (sdt.field ("result")), String.class);
                OperationResult r = new OperationResult ();
                r.reply = reply;
                r.result = Result.valueOf (result);
                return (T) r;
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
