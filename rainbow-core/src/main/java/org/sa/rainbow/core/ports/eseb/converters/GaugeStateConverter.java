package org.sa.rainbow.core.ports.eseb.converters;

import incubator.pval.Ensure;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sa.rainbow.core.gauges.GaugeState;
import org.sa.rainbow.core.gauges.IGaugeState;

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

public class GaugeStateConverter implements TypelibJavaConversionRule {

    private PrimitiveScope m_scope;

    public GaugeStateConverter (PrimitiveScope scope) {
        m_scope = scope;
    }

    @Override
    public boolean handles_java (Object value, DataType dst) {
        Ensure.not_null (value);
        if (value instanceof IGaugeState) return dst == null || "gauge_state".equals (dst.name ());
        return false;
    }

    @Override
    public boolean handles_typelib (DataValue value, Class<?> cls) {
        Ensure.not_null (value);
        if (value.type ().name ().equals ("gauge_state")) return cls == null || IGaugeState.class.isAssignableFrom (cls);
        return false;
    }

    @Override
    public DataValue from_java (Object value, DataType dst, TypelibJavaConverter converter)
            throws ValueConversionException {
        if ((dst == null || dst instanceof StructureDataType) && value instanceof IGaugeState) {
            try {
                StructureDataType sdt = (StructureDataType )dst;
                if (sdt == null) {
                    sdt = (StructureDataType )m_scope.find ("command_representation");
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
                                m_scope.find ("list<command_representation>")));
                StructureDataValue sdv = sdt.make (fields);
                return sdv;
            }
            catch (UnknownFieldException | AmbiguousNameException e) {
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
                List setup = converter.<List> to_java (sdv.value (sdt.field ("setup")), List.class);
                List config = converter.<List> to_java (sdv.value (sdt.field ("config")), List.class);
                List commands = converter.<List> to_java (sdv.value (sdt.field ("commands")), List.class);
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
                }
                catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                        | IllegalArgumentException | InvocationTargetException e) {
                    exception.addSuppressed (e);
                    throw exception;
                }
            }
            catch (UnknownFieldException | AmbiguousNameException e) {
                throw new ValueConversionException (MessageFormat.format ("Could not convert from {0} to {1}",
                        value.toString (),
                        (cls == null ? "IRainbowModelCommandRepresentation" : cls.getCanonicalName ())), e);
            }
        }
        throw new ValueConversionException (MessageFormat.format ("Could not convert from {0} to {1}",
                value.toString (), (cls == null ? "IRainbowModelCommandRepresentation" : cls.getCanonicalName ())));

    }


}
