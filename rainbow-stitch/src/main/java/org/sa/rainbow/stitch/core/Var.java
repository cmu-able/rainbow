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
/**
 * Created March 15, 2006, separated from class Stitch April 5, 2006.
 */
package org.sa.rainbow.stitch.core;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.acmestudio.acme.element.IAcmeElementType;

/**
 * Represents a variable declared in script.
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class Var {
    public IScope scope = null;
    public String name = null;
    public IAcmeElementType typeObj = null;  // optionally used to hold AcmeModel type
    public Statement valStmt = null;

    /** Indicates whether this variable has a basic, internally-supported type.  True by default. */
    protected boolean m_isBasicType = true;
    protected String m_type = null;
    protected Object m_value = null;

    @Override
    public String toString () {
        return "var(type \"" + m_type + "\", " + name + " == " + getValue() + ")";
    }

    public Var clone () {
        Var c = new Var ();
        c.scope = scope;
        c.name = name;
        c.typeObj = typeObj;
        c.valStmt = valStmt;
        c.m_isBasicType = m_isBasicType;
        c.m_type = m_type;
        c.m_value = m_value;
        return c;
    }

    public Class computeClass () {
        Class clazz = null;
        if (m_type.toLowerCase().equals("int") || m_type.toLowerCase().equals("long")) {
            clazz = Long.class;
        } else if (m_type.toLowerCase().equals("float") || m_type.toLowerCase().equals("double")) {
            clazz = Double.class;
        } else if (m_type.toLowerCase().equals("boolean")) {
            clazz = Boolean.class;
        } else if (m_type.toLowerCase().equals("char")) {
            clazz = Character.class;
        } else if (m_type.toLowerCase().equals("string")) {
            clazz = String.class;
        } else if (m_type.toLowerCase().equals("set")) {
            clazz = Set.class;
        } else if (m_type.toLowerCase().equals("sequence")) {
            clazz = List.class;
        } else if (m_type.toLowerCase().equals("record")) {
            clazz = Map.class;
        } else if (m_type.toLowerCase().equals("enum")) {
            clazz = Enum.class;
        } else {  // assume some object
            // look for type name in model, if scope is non-null
            if (scope != null) {
                if (typeObj == null) {
                    Object t = scope.lookup(m_type);
                    if (t != null && t instanceof IAcmeElementType) {
                        typeObj = (IAcmeElementType )t;
                        clazz = typeObj.getClass();
                    }
                } else {
                    clazz = typeObj.getClass();
                }
            } else if (getValue() != null) {
                clazz = getValue().getClass();
            }
        }
        return clazz;
    }

    public void computeValue () {
        if (valStmt != null) {
            valStmt.clearState();
            valStmt.evaluate(null);
        }
    }

    public void setIsBasicType (boolean b) {
        m_isBasicType = b;
    }

    public boolean isBasicType () {
        return m_isBasicType;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        m_type = type;
    }

    /**
     * @return the type
     */
    public String getType() {
        return m_type;
    }

    /**
     * @param value the value to set
     */
    public void setValue(Object value) {
        if (value instanceof Integer) {
            m_value = new MyInteger((Integer )value);
        } else if (value instanceof Long) {
            m_value = new MyInteger((Long )value);
        } else if (value instanceof Float) {
            m_value = new MyDouble(((Float )value).doubleValue());
        } else if (value instanceof Double) {
            m_value = new MyDouble((Double )value);
        } else {
            m_value = value;
        }
    }

    /**
     * @return the value
     */
    public Object getValue() {
        return m_value;
    }

    /**
     * Clear the variable value to ensure fresh evaluation.
     */
    public void clearValue () {
        if (valStmt != null) {
            m_value = null;
        }
    }

    /**
     * Clear the variable state to ensure recursively fresh evaluation.
     */
    public void clearState () {
        if (valStmt != null) {
            m_value = null;
            valStmt.clearState();
        }
    }

}
