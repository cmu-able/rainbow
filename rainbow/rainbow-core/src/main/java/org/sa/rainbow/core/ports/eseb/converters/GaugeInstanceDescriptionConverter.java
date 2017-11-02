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
import org.sa.rainbow.core.gauges.GaugeInstanceDescription;
import org.sa.rainbow.core.gauges.OperationRepresentation;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class GaugeInstanceDescriptionConverter implements TypelibJavaConversionRule {
    private final PrimitiveScope m_scope;

    //struct gauge_instance {string name; string comment; string type; string type_comment; list<typed_attribute_with_value> setup_params; map<string,list<operation_representation> commands; }

    public GaugeInstanceDescriptionConverter (PrimitiveScope scope) {
        m_scope = scope;
    }

    @Override
    public boolean handles_java (Object value, DataType dst) {
        return value instanceof GaugeInstanceDescription && (dst == null || dst.name ().equals ("gauge_instance"));
    }

    @Override
    public boolean handles_typelib (DataValue value, Class<?> cls) {
        Ensure.not_null (value);
        return "gauge_instance".equals (value.type ().name ()) && (cls == null || GaugeInstanceDescription.class.isAssignableFrom (cls));
    }

    @Override
    public DataValue from_java (Object value, DataType dst, TypelibJavaConverter converter)
            throws ValueConversionException {
        try {
//            System.out.println ();
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
                return sdt.make (fields);

            }
        } catch (UnknownFieldException | AmbiguousNameException e) {
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
                String name = converter.to_java (sdv.value (sdt.field ("name")), String.class);
                String type = converter.to_java (sdv.value (sdt.field ("type")), String.class);
                String comment = converter.to_java (sdv.value (sdt.field ("comment")), String.class);
                String typeComment = converter.to_java (sdv.value (sdt.field ("type_comment")), String.class);
                String modelType = converter.to_java (sdv.value (sdt.field ("model_type")), String.class);
                String modelName = converter.to_java (sdv.value (sdt.field ("model_name")), String.class);
                List<TypedAttributeWithValue> setupParams = converter.to_java (
                        sdv.value (sdt.field ("setup_params")), List.class);
                Map<String, OperationRepresentation> commands = converter.to_java (
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
        } catch (UnknownFieldException | AmbiguousNameException e) {
            throw new ValueConversionException (MessageFormat.format ("Could not convert from {0} to {1}",
                    value.toString (), (cls == null ? "GaugeInstanceDescription" : cls.getCanonicalName ())), e);
        }
        throw new ValueConversionException (MessageFormat.format ("Could not convert from {0} to {1}",
                value.toString (), (cls == null ? "GaugeInstanceDescription" : cls.getCanonicalName ())));
    }

}
