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
import org.sa.rainbow.core.gauges.GaugeState;
import org.sa.rainbow.core.gauges.IGaugeState;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GaugeStateConverter implements TypelibJavaConversionRule {

    private final PrimitiveScope m_scope;

    public GaugeStateConverter (PrimitiveScope scope) {
        m_scope = scope;
    }

    @Override
    public boolean handles_java (Object value, DataType dst) {
        Ensure.not_null (value);
        return value instanceof IGaugeState && (dst == null || "gauge_state".equals (dst.name ()));
    }

    @Override
    public boolean handles_typelib (DataValue value, Class<?> cls) {
        Ensure.not_null (value);
        return value.type ().name ().equals ("gauge_state") && (cls == null || IGaugeState.class.isAssignableFrom (cls));
    }

    @Override
    public DataValue from_java (Object value, DataType dst, TypelibJavaConverter converter)
            throws ValueConversionException {
        if ((dst == null || dst instanceof StructureDataType) && value instanceof IGaugeState) {
            try {
                StructureDataType sdt = (StructureDataType )dst;
                if (sdt == null) {
                    sdt = (StructureDataType )m_scope.find ("operation_representation");
                }
                IGaugeState command = (IGaugeState )value;
                Map<Field, DataValue> fields = new HashMap<> ();
                Field setup = sdt.field ("setup");
                Field config = sdt.field ("config");
                Field commands = sdt.field ("commands");
                fields.put (
                        setup,
                        converter.from_java (
                                command.getSetupParams (),
                                m_scope.find ("list<typed_attribute_with_value>")));
                fields.put (
                        config,
                        converter.from_java (command.getConfigParams (),
                                m_scope.find ("list<typed_attribute_with_value>")));
                fields.put (
                        commands,
                        converter.from_java (command.getGaugeReports (),
                                m_scope.find ("list<operation_representation>")));
                return sdt.make (fields);
            } catch (UnknownFieldException | AmbiguousNameException e) {
                throw new ValueConversionException (MessageFormat.format ("Could not convert from {0} to {1}", value
                        .getClass ().toString (), (dst == null ? "gauge_state" : dst.absolute_hname ())), e);
            }
        }
        throw new ValueConversionException (MessageFormat.format ("Could not convert from {0} to {1}", value
                .getClass ().toString (), (dst == null ? "gauge_state" : dst.absolute_hname ())));

    }


    @Override
    public <T> T to_java (DataValue value, Class<T> cls, TypelibJavaConverter converter)
            throws ValueConversionException {
        if (value instanceof StructureDataValue) {
            try {
                StructureDataValue sdv = (StructureDataValue )value;
                StructureDataType sdt = (StructureDataType )sdv.type ();
                List setup = converter.to_java (sdv.value (sdt.field ("setup")), List.class);
                List config = converter.to_java (sdv.value (sdt.field ("config")), List.class);
                List commands = converter.to_java (sdv.value (sdt.field ("commands")), List.class);
                if (cls == null || cls == IGaugeState.class) {
                    cls = (Class<T> )GaugeState.class;
                }
                ValueConversionException exception = new ValueConversionException (MessageFormat.format (
                        "Could not convert from {0} to {1}. There is no appropriate constructor.", value.toString (),
                        cls.getCanonicalName ()));
                try {
                    Constructor<T> constructor = cls.getConstructor (Collection.class, Collection.class,
                            Collection.class);
                    if (constructor != null)
                        return constructor.newInstance (setup, config, commands);
                    else
                        throw exception;
                } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                        | IllegalArgumentException | InvocationTargetException e) {
                    exception.addSuppressed (e);
                    throw exception;
                }
            } catch (UnknownFieldException | AmbiguousNameException e) {
                throw new ValueConversionException (MessageFormat.format ("Could not convert from {0} to {1}",
                        value.toString (),
                        (cls == null ? "IRainbowModelCommandRepresentation" : cls.getCanonicalName ())), e);
            }
        }
        throw new ValueConversionException (MessageFormat.format ("Could not convert from {0} to {1}",
                value.toString (), (cls == null ? "IRainbowModelCommandRepresentation" : cls.getCanonicalName ())));

    }


}
