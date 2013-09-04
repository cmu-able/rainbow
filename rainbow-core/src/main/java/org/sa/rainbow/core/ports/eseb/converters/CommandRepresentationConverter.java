package org.sa.rainbow.core.ports.eseb.converters;

import incubator.pval.Ensure;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sa.rainbow.core.gauges.CommandRepresentation;
import org.sa.rainbow.core.models.commands.IRainbowModelCommandRepresentation;

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

public class CommandRepresentationConverter implements TypelibJavaConversionRule {

    private PrimitiveScope m_scope;

    public CommandRepresentationConverter (PrimitiveScope scope) {
        m_scope = scope;
    }

    @Override
    public boolean handles_java (Object value, DataType dst) {
        Ensure.not_null (value);
        if (value instanceof IRainbowModelCommandRepresentation) return dst == null || "command_representation".equals (dst.name ());
        return false;
    }

    @Override
    public boolean handles_typelib (DataValue value, Class<?> cls) {
        Ensure.not_null (value);
        if (value.type ().name ().equals ("command_representation")) return cls == null || IRainbowModelCommandRepresentation.class.isAssignableFrom (cls);
        return false;
    }

    @Override
    public DataValue from_java (Object value, DataType dst, TypelibJavaConverter converter)
            throws ValueConversionException {
        if ((dst == null || dst instanceof StructureDataType) && value instanceof IRainbowModelCommandRepresentation) {
            try {
                StructureDataType sdt = (StructureDataType )dst;
                if (sdt == null) {
                    sdt = (StructureDataType )m_scope.find ("command_representation");
                }
                IRainbowModelCommandRepresentation command = (IRainbowModelCommandRepresentation )value;
                Map<Field, DataValue> fields = new HashMap<> ();
                Field label = sdt.field ("label");
                Field target = sdt.field ("target");
                Field modelName = sdt.field ("modelName");
                Field modelType = sdt.field ("modelType");
                Field commandName = sdt.field ("name");
                Field params = sdt.field ("params");
                fields.put (label, converter.from_java (command.getLabel (), m_scope.string ()));
                fields.put (target, converter.from_java (command.getTarget (), m_scope.string ()));
                fields.put (modelName, converter.from_java (command.getModelName (), m_scope.string ()));
                fields.put (modelType, converter.from_java (command.getModelType (), m_scope.string ()));
                fields.put (commandName, converter.from_java (command.getCommandName (), m_scope.string ()));
                fields.put (
                        params,
                        converter.from_java (Arrays.asList (command.getParameters ()),
 m_scope.find ("list<string>")));
                StructureDataValue sdv = sdt.make (fields);
                return sdv;
            }
            catch (UnknownFieldException | AmbiguousNameException e) {
                throw new ValueConversionException (MessageFormat.format ("Could not convert from {0} to {1}", value
                        .getClass ().toString (), (dst == null ? "command_representation" : dst.absolute_hname ())), e);
            }
        }
        throw new ValueConversionException (MessageFormat.format ("Could not convert from {0} to {1}", value
                .getClass ().toString (), (dst == null ? "command_representation" : dst.absolute_hname ())));

    }

    @Override
    public <T> T to_java (DataValue value, Class<T> cls, TypelibJavaConverter converter)
            throws ValueConversionException {
        if (value instanceof StructureDataValue) {
            try {
                StructureDataValue sdv = (StructureDataValue )value;
                StructureDataType sdt = (StructureDataType )sdv.type ();
                String label = converter.<String> to_java (sdv.value (sdt.field ("label")), String.class);
                String target = converter.<String> to_java (sdv.value (sdt.field ("target")), String.class);
                String modelName = converter.<String> to_java (sdv.value (sdt.field ("modelName")), String.class);
                String modelType = converter.<String> to_java (sdv.value (sdt.field ("modelType")), String.class);
                String name = converter.<String> to_java (sdv.value (sdt.field ("name")), String.class);
                List<String> parameters = converter.<List> to_java (sdv.value (sdt.field ("params")), List.class);
                if (cls == null) {
                    CommandRepresentation crep = new CommandRepresentation (label, name, modelName, modelType, target,
                            parameters.toArray (new String[0]));
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
                            return constructor.newInstance (label, name, modelName, modelType,
                                    parameters.toArray (new String[0]));
                        else
                            throw exception;
                    }
                    catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                            | IllegalArgumentException | InvocationTargetException e) {
                        exception.addSuppressed (e);
                        throw exception;
                    }
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
