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
import org.sa.rainbow.core.gauges.OperationRepresentation;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.IRainbowOperation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandRepresentationConverter implements TypelibJavaConversionRule {

    private final PrimitiveScope m_scope;

    public CommandRepresentationConverter (PrimitiveScope scope) {
        m_scope = scope;
    }

    @Override
    public boolean handles_java (Object value, DataType dst) {
        Ensure.not_null (value);
        return value instanceof IRainbowOperation && (dst == null || "operation_representation".equals (dst.name ()));
    }

    @Override
    public boolean handles_typelib (DataValue value, Class<?> cls) {
        Ensure.not_null (value);
        return value.type ().name ().equals ("operation_representation") && (cls == null || IRainbowOperation.class.isAssignableFrom (cls));
    }

    @Override
    public DataValue from_java (Object value, DataType dst, TypelibJavaConverter converter)
            throws ValueConversionException {
        if ((dst == null || dst instanceof StructureDataType) && value instanceof IRainbowOperation) {
            try {
                StructureDataType sdt = (StructureDataType )dst;
                if (sdt == null) {
                    sdt = (StructureDataType )m_scope.find ("operation_representation");
                }
                IRainbowOperation command = (IRainbowOperation )value;
                Map<Field, DataValue> fields = new HashMap<> ();
                Field target = sdt.field ("target");
                Field modelName = sdt.field ("modelName");
                Field modelType = sdt.field ("modelType");
                Field commandName = sdt.field ("name");
                Field params = sdt.field ("params");
                fields.put (target, converter.from_java (command.getTarget (), m_scope.string ()));
                fields.put (modelName,
                        converter.from_java (command.getModelReference ().getModelName (), m_scope.string ()));
                fields.put (modelType,
                        converter.from_java (command.getModelReference ().getModelType (), m_scope.string ()));
                fields.put (commandName, converter.from_java (command.getName (), m_scope.string ()));
                fields.put (
                        params,
                        converter.from_java (Arrays.asList (command.getParameters ()),
                                m_scope.find ("list<string>")));
                return sdt.make (fields);
            } catch (UnknownFieldException | AmbiguousNameException e) {
                throw new ValueConversionException (MessageFormat.format ("Could not convert from {0} to {1}", value
                        .getClass ().toString (), (dst == null ? "operation_representation" : dst.absolute_hname ())),
                        e);
            }
        }
        throw new ValueConversionException (MessageFormat.format ("Could not convert from {0} to {1}", value
                .getClass ().toString (), (dst == null ? "operation_representation" : dst.absolute_hname ())));

    }


    @Override
    public <T> T to_java (DataValue value, Class<T> cls, TypelibJavaConverter converter)
            throws ValueConversionException {
        if (value instanceof StructureDataValue) {
            try {
                StructureDataValue sdv = (StructureDataValue )value;
                StructureDataType sdt = (StructureDataType )sdv.type ();
                String target = converter.to_java (sdv.value (sdt.field ("target")), String.class);
                String modelName = converter.to_java (sdv.value (sdt.field ("modelName")), String.class);
                String modelType = converter.to_java (sdv.value (sdt.field ("modelType")), String.class);
                String name = converter.to_java (sdv.value (sdt.field ("name")), String.class);
                List<String> parameters = converter.to_java (sdv.value (sdt.field ("params")), List.class);
                if (cls == null) {
                    OperationRepresentation crep = new OperationRepresentation (name, new ModelReference (modelName,
                            modelType), target,
                            parameters.toArray (new String[parameters.size ()]));
                    @SuppressWarnings ("unchecked")
                    T t = (T )crep;
                    return t;
                }
                else {
                    ValueConversionException exception = new ValueConversionException (MessageFormat.format (
                            "Could not convert from {0} to {1}. There is no appropriate constructor.",
                            value.toString (), cls.getCanonicalName ()));
                    try {
                        Constructor<T> constructor = cls.getConstructor (String.class, String.class, String.class,
                                String.class, String.class, String[].class);
                        if (constructor != null)
                            return constructor.newInstance (name, modelName, modelType,
                                    parameters.toArray (new String[parameters.size ()]));
                        else
                            throw exception;
                    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                            | IllegalArgumentException | InvocationTargetException e) {
                        exception.addSuppressed (e);
                        throw exception;
                    }
                }
            } catch (UnknownFieldException | AmbiguousNameException e) {
                throw new ValueConversionException (MessageFormat.format ("Could not convert from {0} to {1}",
                        value.toString (),
                        (cls == null ? "IRainbowModelOperationRepresentation" : cls.getCanonicalName ())), e);
            }
        }
        throw new ValueConversionException (MessageFormat.format ("Could not convert from {0} to {1}",
                value.toString (), (cls == null ? "IRainbowModelOperationRepresentation" : cls.getCanonicalName ())));

    }

}
