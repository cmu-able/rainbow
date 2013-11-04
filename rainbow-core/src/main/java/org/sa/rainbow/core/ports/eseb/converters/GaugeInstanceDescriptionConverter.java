package org.sa.rainbow.core.ports.eseb.converters;

import incubator.pval.Ensure;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.sa.rainbow.core.gauges.GaugeInstanceDescription;
import org.sa.rainbow.core.gauges.OperationRepresentation;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

import edu.cmu.cs.able.typelib.comp.MapDataType;
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

public class GaugeInstanceDescriptionConverter implements TypelibJavaConversionRule {
    private PrimitiveScope m_scope;
    private MapDataType    m_commandMapType;
    private DataType       m_commandRepresentationType;

    //struct gauge_instance {string name; string comment; string type; string type_comment; list<typed_attribute_with_value> setup_params; map<string,list<operation_representation> commands; }

    public GaugeInstanceDescriptionConverter (PrimitiveScope scope) {
        m_scope = scope;
        try {
            m_commandRepresentationType = m_scope.find ("operation_representation");
            m_commandMapType = MapDataType.map_of (m_scope.string (), m_commandRepresentationType, m_scope);
        }
        catch (AmbiguousNameException e) {
            // TODO Auto-generated catch block
            e.printStackTrace ();
        }
    }

    @Override
    public boolean handles_java (Object value, DataType dst) {
        if (value instanceof GaugeInstanceDescription) return dst == null || dst.name ().equals ("gauge_instance");
        return false;
    }

    @Override
    public boolean handles_typelib (DataValue value, Class<?> cls) {
        Ensure.not_null (value);
        if ("gauge_instance".equals (value.type ().name ()))
            return cls == null || GaugeInstanceDescription.class.isAssignableFrom (cls);
        return false;
    }

    @Override
    public DataValue from_java (Object value, DataType dst, TypelibJavaConverter converter)
            throws ValueConversionException {
        try {
            System.out.println ();
            if ((dst == null || dst instanceof StructureDataType) && value instanceof GaugeInstanceDescription) {
                GaugeInstanceDescription gid = (GaugeInstanceDescription )value;
                StructureDataType sdt = (StructureDataType )dst;
                if (sdt == null) {
                    sdt = (StructureDataType )m_scope.find ("gauge_instance");
                }
                Map<Field, DataValue> fields = new HashMap<> ();
                Field name = sdt.field ("name");
                Field type = sdt.field ("type");
                Field comment = sdt.field ("comment");
                Field typeComment = sdt.field ("type_comment");
                Field setupParams = sdt.field ("setup_params");
                Field modelName = sdt.field ("model_name");
                Field modelType = sdt.field ("model_type");
                Field commands = sdt.field ("commands");

                fields.put (name, converter.from_java (gid.gaugeName (), null));
                fields.put (type, converter.from_java (gid.gaugeType (), null));
                fields.put (comment, converter.from_java (gid.instanceComment (), null));
                fields.put (typeComment, converter.from_java (gid.typeComment (), null));
                fields.put (modelName, converter.from_java (gid.modelDesc ().getName (), null));
                fields.put (modelType, converter.from_java (gid.modelDesc ().getType (), null));
                fields.put (setupParams,
                        converter.from_java (gid.setupParams (), m_scope.find ("list<typed_attribute_with_value>")));
                fields.put (commands,
                        converter.from_java (gid.mappings (), m_scope.find ("map<string,operation_representation>")));
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

    @Override
    public <T> T to_java (DataValue value, Class<T> cls, TypelibJavaConverter converter)
            throws ValueConversionException {
        try {
            if (value instanceof StructureDataValue) {
                StructureDataValue sdv = (StructureDataValue )value;
                StructureDataType sdt = (StructureDataType )sdv.type ();
                String name = converter.<String>to_java (sdv.value (sdt.field ("name")), String.class);
                String type = converter.<String>to_java (sdv.value (sdt.field ("type")), String.class);
                String comment = converter.<String>to_java (sdv.value (sdt.field ("comment")), String.class);
                String typeComment = converter.<String> to_java (sdv.value (sdt.field ("type_comment")), String.class);
                String modelType = converter.<String> to_java (sdv.value (sdt.field ("model_type")), String.class);
                String modelName = converter.<String> to_java (sdv.value (sdt.field ("model_name")), String.class);
                List<TypedAttributeWithValue> setupParams = converter.<List> to_java (
                        sdv.value (sdt.field ("setup_params")), List.class);
                Map<String, OperationRepresentation> commands = converter.<Map> to_java (
                        sdv.value (sdt.field ("commands")),
 Map.class);
                GaugeInstanceDescription gid = new GaugeInstanceDescription (type, name, typeComment, comment);
                gid.setModelDesc (new TypedAttribute (modelName, modelType));
                for (TypedAttributeWithValue tav : setupParams) {
                    gid.addSetupParam (tav);
                }
                for (Entry<String, OperationRepresentation> e : commands.entrySet ()) {
                    gid.addCommand (e.getKey (), e.getValue ());
                }
                T t = (T )gid;
                return t;
            }
        }
        catch (UnknownFieldException | AmbiguousNameException e) {
            throw new ValueConversionException (MessageFormat.format ("Could not convert from {0} to {1}",
                    value.toString (), (cls == null ? "GaugeInstanceDescription" : cls.getCanonicalName ())), e);
        }
        throw new ValueConversionException (MessageFormat.format ("Could not convert from {0} to {1}",
                value.toString (), (cls == null ? "GaugeInstanceDescription" : cls.getCanonicalName ())));
    }

}
