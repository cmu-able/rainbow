package org.sa.rainbow.core.util;

public class TypedAttribute {

    private String m_name;
    private String m_type;

    public TypedAttribute (String name, String type) {
        m_name = name;
        m_type = type;
    }

    public String getName () {
        return m_name;
    }

    public void setName (String name) {
        m_name = name;
    }

    public String getType () {
        return m_type;
    }

    public void setType (String type) {
        m_type = type;
    }

}
