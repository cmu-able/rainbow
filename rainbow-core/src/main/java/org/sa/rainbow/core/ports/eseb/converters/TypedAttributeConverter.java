package org.sa.rainbow.core.ports.eseb.converters;

import incubator.pval.Ensure;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

import edu.cmu.cs.able.typelib.comp.OptionalDataType;
import edu.cmu.cs.able.typelib.comp.OptionalDataValue;
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

public class TypedAttributeConverter implements TypelibJavaConversionRule {

    private PrimitiveScope m_scope;

    public TypedAttributeConverter (PrimitiveScope scope) {
        m_scope = scope;
    }

    @Override
    public <T> T to_java (DataValue value, Class<T> cls, TypelibJavaConverter converter)
            throws ValueConversionException {
        if (value instanceof StructureDataValue) {
            try {
                Object ret;
                StructureDataValue sdv = (StructureDataValue )value;
                StructureDataType sdt = (StructureDataType )sdv.type ();
                String name = converter.<String> to_java (sdv.value (sdt.field ("name")), String.class);
                String type = converter.<String> to_java (sdv.value (sdt.field ("type")), String.class);
                DataValue v = sdv.value (sdt.field ("value"));
                if (v instanceof OptionalDataValue) {
                    OptionalDataValue odv = (OptionalDataValue )v;
                    if (odv.value () != null) {
                        ret = new TypedAttributeWithValue (name, type, converter.to_java (odv.value (), null));
                    }
                    else {
                        ret = new TypedAttribute (name, type);
                    }
                }
                else {
                    ret = new TypedAttribute (name, type);
                }
                @SuppressWarnings("unchecked")
                T t = (T )ret;
                return t;
            }
            catch (UnknownFieldException | AmbiguousNameException e) {
                throw new ValueConversionException (MessageFormat.format ("Could not convert from {0} to {1}",
                        value.toString (), cls.getCanonicalName ()), e);
            }
        }
        throw new ValueConversionException (MessageFormat.format ("Could not convert from {0} to {1}",
                value.toString (), cls.getCanonicalName ()));
    }

    @Override
    public boolean handles_typelib (DataValue value, Class<?> cls) {
        Ensure.not_null (value);
        if (value.type ().name ().equals ("typed_attribute_with_value")) {
            if (cls == null || TypedAttribute.class.isAssignableFrom (cls)) return true;
        }
        return false;
    }

    @Override
    public boolean handles_java (Object value, DataType dst) {
        if (value instanceof TypedAttributeWithValue || value instanceof TypedAttribute)
            return dst == null || dst.name ().equals ("typed_attribute_with_value");
        return false;
    }

    @Override
    public DataValue from_java (Object value, DataType dst, TypelibJavaConverter converter)
            throws ValueConversionException {
        try {
            if ((dst == null || dst instanceof StructureDataType) && value instanceof TypedAttribute) {
                TypedAttribute ta = (TypedAttribute )value;
                StructureDataType sdt = (StructureDataType )dst;
                if (sdt == null) {
                    sdt = (StructureDataType )m_scope.find ("typed_attribute_with_value");
                }
                Map<Field, DataValue> fields = new HashMap<> ();
                Field name = sdt.field ("name");
                Field type = sdt.field ("type");
                Field v = sdt.field ("value");

                fields.put (name, converter.from_java (ta.getName (), null));
                fields.put (type, converter.from_java (ta.getType (), null));
                fields.put (v, ((OptionalDataType )m_scope.find ("opt_any")).make (null));
                StructureDataValue sdv = sdt.make (fields);
                return sdv;
            }
            else if ((dst == null || dst instanceof StructureDataType) && value instanceof TypedAttributeWithValue) {
                TypedAttributeWithValue ta = (TypedAttributeWithValue )value;
                StructureDataType sdt = (StructureDataType )dst;
                if (sdt == null) {
                    sdt = (StructureDataType )m_scope.find ("typed_attribute_with_value");
                }
                Map<Field, DataValue> fields = new HashMap<> ();
                Field name = sdt.field ("name");
                Field type = sdt.field ("type");
                Field v = sdt.field ("value");

                fields.put (name, converter.from_java (ta.getName (), null));
                fields.put (type, converter.from_java (ta.getType (), null));
                fields.put (v,
                        ((OptionalDataType )m_scope.find ("any?")).make (converter.from_java (
                                ta.getValue (), null)));
                StructureDataValue sdv = sdt.make (fields);
                return sdv;
            }
        }
        catch (UnknownFieldException | AmbiguousNameException e) {
            throw new ValueConversionException (MessageFormat.format ("Could not convert from {0} to {1}", value
                    .getClass ().getCanonicalName (), (dst == null ? "typed_attribute_with_value" : dst
                            .absolute_hname ().toString ())), e);
        }
        throw new ValueConversionException (MessageFormat.format ("Could not convert from {0} to {1}", value
                .getClass ().getCanonicalName (), (dst == null ? "typed_attribute_with_value" : dst.absolute_hname ()
                        .toString ())));


    }
}