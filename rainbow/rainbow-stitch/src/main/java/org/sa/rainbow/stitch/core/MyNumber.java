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
 * Created April 24, 2006.
 */
package org.sa.rainbow.stitch.core;

import org.acmestudio.acme.core.type.IAcmeFloatingPointValue;
import org.acmestudio.acme.core.type.IAcmeIntValue;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.sa.rainbow.stitch.util.Tool;

import java.text.MessageFormat;

/**
 * Custom implementation of Number to support polymorphic arithmetic operation.
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public abstract class MyNumber extends Number {

    private Number m_number = null;
    protected double m_double = 0.0;  // a hack, store the most precise possible
    protected long m_long = 0L;  // a hack, store the most precise possible

    /**
     * Support IAcmeProperty value for Float and Int
     */
    public static MyNumber newNumber (IAcmeProperty prop) {
        MyNumber num = null;
        if (prop.getValue() instanceof IAcmeIntValue) {
            num = new MyInteger(((IAcmeIntValue )prop.getValue()).getValue());
        } else if (prop.getValue () instanceof IAcmeFloatingPointValue) {
            num = new MyDouble (((IAcmeFloatingPointValue) prop.getValue ()).getDoubleValue ());
        } else {  // unhandled type
            Tool.logger ().error (MessageFormat.format ("Unsupported IAcmeProperty value type! {0}", prop.getValue ()));
        }

        return num;
    }

    /**
     * Default constructor
     */
    public MyNumber (Number n) {
        m_number = n;
        if (n instanceof Integer || n instanceof Long) {
            m_long = n.longValue();
        } else if (n instanceof Float || n instanceof Double) {
            m_double = n.doubleValue();
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        String s = "";
        if (m_number instanceof Integer || m_number instanceof Long) {
            s = String.valueOf(m_long);
        } else if (m_number instanceof Float || m_number instanceof Double) {
            s = String.valueOf(m_double);
        }
        return s;
    }

    /* (non-Javadoc)
     * @see java.lang.Number#intValue()
     */
    @Override
    public int intValue () {
        return (int )m_long;
    }

    /* (non-Javadoc)
     * @see java.lang.Number#longValue()
     */
    @Override
    public long longValue () {
        return m_long;
    }

    /* (non-Javadoc)
     * @see java.lang.Number#floatValue()
     */
    @Override
    public float floatValue () {
        return (float )m_double;
    }

    /* (non-Javadoc)
     * @see java.lang.Number#doubleValue()
     */
    @Override
    public double doubleValue () {
        return m_double;
    }

    public abstract Number toJavaNumber ();

    public abstract MyNumber plus (MyNumber addend);
    public abstract MyNumber minus (MyNumber subtrahend);
    public abstract MyNumber times (MyNumber multipler);
    public abstract MyNumber dividedBy (MyNumber divisor);
    public abstract MyNumber modulus (MyNumber divisor);

    public MyNumber incr () {
        if (this instanceof MyDouble) {
            ++m_double;
        } else {
            ++m_long;
        }
        return this;
    }

    public MyNumber decr () {
        if (this instanceof MyDouble) {
            --m_double;
        } else {
            --m_long;
        }
        return this;
    }

    public MyNumber negate () {
        if (this instanceof MyDouble) {
            m_double = -m_double;
        } else {
            m_long = -m_long;
        }
        return this;
    }

    public abstract Boolean eq (MyNumber operand);
    public abstract Boolean lt (MyNumber operand);

    public Boolean ne (MyNumber operand) {
        return ! eq(operand);
    }

    public Boolean le (MyNumber operand) {
        return lt(operand) || eq(operand);
    }

    public Boolean gt (MyNumber operand) {
        return ! le(operand);
    }

    public Boolean ge (MyNumber operand) {
        return gt(operand) || eq(operand);
    }

}
