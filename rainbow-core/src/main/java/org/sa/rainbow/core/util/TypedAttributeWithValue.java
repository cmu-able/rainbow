package org.sa.rainbow.core.util;

public class TypedAttributeWithValue extends Pair<TypedAttribute, Object> {

    private static final long serialVersionUID = 7005191042752015697L;

    public TypedAttributeWithValue (String name, String type, Object value) {
        super (new TypedAttribute (name, type), value);
    }

    public TypedAttributeWithValue (TypedAttribute attr, Object value) {
        super (attr, value);
    }

    public Object getValue () {
        return secondValue ();
    }

    public void setValue (Object value) {
        setSecondValue (value);
    }

    public String getName () {
        return firstValue ().getName ();
    }

    public void setName (String name) {
        firstValue ().setName (name);
    }

    public String getType () {
        return firstValue ().getType ();
    }

    public void setType (String type) {
        firstValue ().setType (type);
    }

    @Override
    public Object clone () {
        return super.clone ();
    }
}
