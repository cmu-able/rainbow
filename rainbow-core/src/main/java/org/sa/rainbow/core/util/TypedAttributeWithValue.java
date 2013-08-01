package org.sa.rainbow.core.util;

public class TypedAttributeWithValue extends TypedAttribute {
    private Object m_value;

    public TypedAttributeWithValue (String name, String type, Object value) {
        super (name, type);
        m_value = value;
    }

    public Object getValue () {
        return m_value;
    }

    public void setValue (Object value) {
        m_value = value;
    }

}
